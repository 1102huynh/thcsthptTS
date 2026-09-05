package com.schoolmanagement.service;

import com.schoolmanagement.dto.ConductRecordDTO;
import com.schoolmanagement.dto.ConductRosterEntryDTO;
import com.schoolmanagement.entity.ConductRecord;
import com.schoolmanagement.entity.Role;
import com.schoolmanagement.entity.SchoolClass;
import com.schoolmanagement.entity.Semester;
import com.schoolmanagement.entity.Staff;
import com.schoolmanagement.entity.Student;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.exception.DuplicateResourceException;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.repository.ConductRecordRepository;
import com.schoolmanagement.repository.SchoolClassRepository;
import com.schoolmanagement.repository.SemesterRepository;
import com.schoolmanagement.repository.StaffRepository;
import com.schoolmanagement.repository.StudentRepository;
import com.schoolmanagement.security.StudentAccessGuard;
import com.schoolmanagement.security.TeacherHomeroomGuard;
import com.schoolmanagement.util.EntityResolver;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Hạnh kiểm/rèn luyện per IMPLEMENTATION_PLAN.md 3.4 — one ConductRecord per
 * (student, semester). A TEACHER may only create/update conduct records for
 * students in the class they are GVCN (homeroom teacher) of; ADMIN is
 * unrestricted. Reads are open to ADMIN/TEACHER; a STUDENT caller may only
 * read their own records (same object-level pattern as GradeRecordService).
 */
@Service
@AllArgsConstructor
@Transactional
public class ConductRecordService {

    private ConductRecordRepository conductRecordRepository;
    private StudentRepository studentRepository;
    private SemesterRepository semesterRepository;
    private StaffRepository staffRepository;
    private SchoolClassRepository schoolClassRepository;
    private StudentAccessGuard studentAccessGuard;
    private TeacherHomeroomGuard teacherHomeroomGuard;

    public ConductRecordDTO createConductRecord(ConductRecord request, User requester) {
        Student student = resolveStudent(request.getStudent());
        Semester semester = resolveSemester(request.getSemester());
        Staff evaluatedBy = resolveStaff(request.getEvaluatedBy());

        enforceHomeroomWriteAccess(student, evaluatedBy, requester);

        if (conductRecordRepository.existsByStudentAndSemester(student, semester)) {
            throw new DuplicateResourceException(
                    "A conduct record already exists for this student in this semester — use PUT to update it");
        }

        ConductRecord record = ConductRecord.builder()
                .student(student)
                .semester(semester)
                .rating(request.getRating())
                .remarks(request.getRemarks())
                .evaluatedBy(evaluatedBy)
                .build();

        try {
            return mapToDTO(conductRecordRepository.save(record));
        } catch (DataIntegrityViolationException ex) {
            // Two concurrent requests can both pass the exists() check above before
            // either commits; surface that race as 409, not a masked 500.
            throw new DuplicateResourceException(
                    "A conduct record already exists for this student in this semester — use PUT to update it");
        }
    }

    public ConductRecordDTO updateConductRecord(Long id, ConductRecord request, User requester) {
        ConductRecord record = conductRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conduct record not found with id: " + id));

        // Authorize against the record as it stands today FIRST - a TEACHER must
        // already be the GVCN of whichever class this record currently belongs
        // to before touching it at all, otherwise they could "reassign" someone
        // else's conduct record to their own student and pass the check below.
        enforceHomeroomWriteAccess(record.getStudent(), record.getEvaluatedBy(), requester);

        Student student = resolveStudent(request.getStudent());
        Semester semester = resolveSemester(request.getSemester());
        Staff evaluatedBy = resolveStaff(request.getEvaluatedBy());

        // And also authorize against the (possibly different) target student/evaluator.
        enforceHomeroomWriteAccess(student, evaluatedBy, requester);

        boolean changingKey = !student.getId().equals(record.getStudent().getId())
                || !semester.getId().equals(record.getSemester().getId());
        if (changingKey && conductRecordRepository.existsByStudentAndSemester(student, semester)) {
            throw new DuplicateResourceException(
                    "A conduct record already exists for this student in this semester");
        }

        record.setStudent(student);
        record.setSemester(semester);
        record.setRating(request.getRating());
        record.setRemarks(request.getRemarks());
        record.setEvaluatedBy(evaluatedBy);

        try {
            return mapToDTO(conductRecordRepository.save(record));
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateResourceException(
                    "A conduct record already exists for this student in this semester");
        }
    }

    public List<ConductRecordDTO> getStudentConductRecords(Long studentId, User requester) {
        studentAccessGuard.enforceCanAccessStudent(studentId, requester);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        return conductRecordRepository.findByStudent(student)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Bảng đánh giá hàng loạt cho GVCN — one row per student currently in the
     * class. Roster membership is matched on the same (deprecated)
     * className/section pair {@link com.schoolmanagement.service.SchoolClassService#getStudentsInClass}
     * already uses codebase-wide — not scoped by academic year, so a
     * className/section reused across years would over-match. Pre-existing
     * limitation of that roster convention, not new to this endpoint.
     */
    public List<ConductRosterEntryDTO> getClassSemesterRoster(Long classId, Long semesterId) {
        SchoolClass schoolClass = schoolClassRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + classId));
        Semester semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new ResourceNotFoundException("Semester not found with id: " + semesterId));

        List<Student> roster = studentRepository.findByClassNameAndSection(
                schoolClass.getClassName(), schoolClass.getSection());

        Map<Long, ConductRecord> byStudentId = conductRecordRepository
                .findBySemesterAndStudentIn(semester, roster)
                .stream()
                .collect(Collectors.toMap(r -> r.getStudent().getId(), Function.identity()));

        return roster.stream()
                .map(student -> {
                    ConductRecord record = byStudentId.get(student.getId());
                    ConductRosterEntryDTO.ConductRosterEntryDTOBuilder entry = ConductRosterEntryDTO.builder()
                            .studentId(student.getId())
                            .studentName(studentName(student))
                            .rollNumber(student.getRollNumber());

                    if (record != null) {
                        entry.conductRecordId(record.getId())
                                .rating(record.getRating())
                                .remarks(record.getRemarks())
                                .evaluatedById(record.getEvaluatedBy().getId())
                                .evaluatedByName(staffName(record.getEvaluatedBy()));
                    }

                    return entry.build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Only a TEACHER is restricted, and only to a class they are GVCN of;
     * ADMIN is unrestricted. The homeroom check itself now lives in
     * {@link TeacherHomeroomGuard#enforceHomeroomClassNameSection} (shared
     * with Student/Attendance/Promotion) — this method keeps only what's
     * specific to conduct: a TEACHER may only submit evaluations under their
     * own staff profile.
     */
    private void enforceHomeroomWriteAccess(Student student, Staff evaluatedBy, User requester) {
        if (requester == null || requester.getRole() != Role.TEACHER) {
            return;
        }

        if (student.getClassName() == null || student.getSection() == null) {
            throw new ResourceNotFoundException("Student " + student.getId() + " has no class assigned");
        }
        teacherHomeroomGuard.enforceHomeroomClassNameSection(student.getClassName(), student.getSection(), requester);

        Staff teacherStaff = teacherHomeroomGuard.resolveOwnStaff(requester);
        if (evaluatedBy != null && !teacherStaff.getId().equals(evaluatedBy.getId())) {
            throw new AccessDeniedException(
                    "A TEACHER may only submit conduct evaluations under their own staff profile");
        }
    }

    private Student resolveStudent(Student reference) {
        return EntityResolver.resolveOrThrow(studentRepository, reference != null ? reference.getId() : null, "Student");
    }

    private Semester resolveSemester(Semester reference) {
        return EntityResolver.resolveOrThrow(semesterRepository, reference != null ? reference.getId() : null, "Semester");
    }

    private Staff resolveStaff(Staff reference) {
        return EntityResolver.resolveOrThrow(staffRepository, reference != null ? reference.getId() : null, "Staff (evaluatedBy)");
    }

    private String studentName(Student student) {
        return student.getUser() != null ? student.getUser().getFirstName() + " " + student.getUser().getLastName() : null;
    }

    private String staffName(Staff staff) {
        return staff.getUser() != null ? staff.getUser().getFirstName() + " " + staff.getUser().getLastName() : null;
    }

    private ConductRecordDTO mapToDTO(ConductRecord record) {
        Student student = record.getStudent();
        Semester semester = record.getSemester();
        Staff evaluatedBy = record.getEvaluatedBy();

        return ConductRecordDTO.builder()
                .id(record.getId())
                .studentId(student.getId())
                .studentName(studentName(student))
                .semesterId(semester.getId())
                .semesterLabel(semester.getAcademicYear().getName() + " - " + semester.getName())
                .rating(record.getRating())
                .remarks(record.getRemarks())
                .evaluatedById(evaluatedBy.getId())
                .evaluatedByName(staffName(evaluatedBy))
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }
}
