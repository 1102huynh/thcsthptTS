package com.schoolmanagement.service;

import com.schoolmanagement.dto.TeachingAssignmentDTO;
import com.schoolmanagement.entity.SchoolClass;
import com.schoolmanagement.entity.Semester;
import com.schoolmanagement.entity.Staff;
import com.schoolmanagement.entity.Subject;
import com.schoolmanagement.entity.TeachingAssignment;
import com.schoolmanagement.exception.DuplicateResourceException;
import com.schoolmanagement.exception.ResourceInUseException;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.repository.SchoolClassRepository;
import com.schoolmanagement.repository.SemesterRepository;
import com.schoolmanagement.repository.StaffRepository;
import com.schoolmanagement.repository.SubjectRepository;
import com.schoolmanagement.repository.TeachingAssignmentRepository;
import com.schoolmanagement.repository.TimetableSlotRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class TeachingAssignmentService {

    private TeachingAssignmentRepository teachingAssignmentRepository;
    private TimetableSlotRepository timetableSlotRepository;
    private SchoolClassRepository schoolClassRepository;
    private SubjectRepository subjectRepository;
    private StaffRepository staffRepository;
    private SemesterRepository semesterRepository;

    public TeachingAssignmentDTO createTeachingAssignment(TeachingAssignment request) {
        SchoolClass schoolClass = resolveSchoolClass(request.getSchoolClass());
        Subject subject = resolveSubject(request.getSubject());
        Staff teacher = resolveTeacher(request.getTeacher());
        Semester semester = resolveSemester(request.getSemester());

        assertNoDuplicate(schoolClass, subject, semester, null);

        TeachingAssignment assignment = TeachingAssignment.builder()
                .schoolClass(schoolClass)
                .subject(subject)
                .teacher(teacher)
                .semester(semester)
                .build();

        return mapToDTO(teachingAssignmentRepository.save(assignment));
    }

    public TeachingAssignmentDTO updateTeachingAssignment(Long id, TeachingAssignment request) {
        TeachingAssignment assignment = teachingAssignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teaching assignment not found with id: " + id));

        SchoolClass schoolClass = resolveSchoolClass(request.getSchoolClass());
        Subject subject = resolveSubject(request.getSubject());
        Staff teacher = resolveTeacher(request.getTeacher());
        Semester semester = resolveSemester(request.getSemester());

        assertNoDuplicate(schoolClass, subject, semester, id);

        assignment.setSchoolClass(schoolClass);
        assignment.setSubject(subject);
        assignment.setTeacher(teacher);
        assignment.setSemester(semester);

        return mapToDTO(teachingAssignmentRepository.save(assignment));
    }

    public TeachingAssignmentDTO getTeachingAssignmentById(Long id) {
        return mapToDTO(teachingAssignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teaching assignment not found with id: " + id)));
    }

    public List<TeachingAssignmentDTO> getAllTeachingAssignments() {
        return teachingAssignmentRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public void deleteTeachingAssignment(Long id) {
        TeachingAssignment assignment = teachingAssignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teaching assignment not found with id: " + id));

        if (!timetableSlotRepository.findByTeachingAssignment(assignment).isEmpty()) {
            throw new ResourceInUseException(
                    "Cannot delete teaching assignment: it still has timetable slots scheduled");
        }

        teachingAssignmentRepository.delete(assignment);
    }

    private void assertNoDuplicate(SchoolClass schoolClass, Subject subject, Semester semester, Long excludeId) {
        teachingAssignmentRepository.findBySchoolClassAndSubjectAndSemester(schoolClass, subject, semester)
                .filter(existing -> !existing.getId().equals(excludeId))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException(
                            "A teaching assignment for this class/subject/semester already exists");
                });
    }

    private SchoolClass resolveSchoolClass(SchoolClass reference) {
        if (reference == null || reference.getId() == null) {
            throw new ResourceNotFoundException("A school class id is required");
        }
        return schoolClassRepository.findById(reference.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + reference.getId()));
    }

    private Subject resolveSubject(Subject reference) {
        if (reference == null || reference.getId() == null) {
            throw new ResourceNotFoundException("A subject id is required");
        }
        return subjectRepository.findById(reference.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + reference.getId()));
    }

    private Staff resolveTeacher(Staff reference) {
        if (reference == null || reference.getId() == null) {
            throw new ResourceNotFoundException("A teacher (staff) id is required");
        }
        return staffRepository.findById(reference.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + reference.getId()));
    }

    private Semester resolveSemester(Semester reference) {
        if (reference == null || reference.getId() == null) {
            throw new ResourceNotFoundException("A semester id is required");
        }
        return semesterRepository.findById(reference.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Semester not found with id: " + reference.getId()));
    }

    private TeachingAssignmentDTO mapToDTO(TeachingAssignment assignment) {
        SchoolClass schoolClass = assignment.getSchoolClass();
        Subject subject = assignment.getSubject();
        Staff teacher = assignment.getTeacher();
        Semester semester = assignment.getSemester();

        return TeachingAssignmentDTO.builder()
                .id(assignment.getId())
                .schoolClassId(schoolClass.getId())
                .schoolClassLabel(schoolClass.getClassName() + "-" + schoolClass.getSection())
                .subjectId(subject.getId())
                .subjectCode(subject.getCode())
                .subjectName(subject.getName())
                .teacherId(teacher.getId())
                .teacherName(teacher.getUser() != null
                        ? teacher.getUser().getFirstName() + " " + teacher.getUser().getLastName()
                        : null)
                .semesterId(semester.getId())
                .semesterLabel(semester.getAcademicYear().getName() + " - " + semester.getName())
                .createdAt(assignment.getCreatedAt())
                .updatedAt(assignment.getUpdatedAt())
                .build();
    }
}
