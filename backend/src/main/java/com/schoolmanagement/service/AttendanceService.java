package com.schoolmanagement.service;

import com.schoolmanagement.dto.AttendanceDTO;
import com.schoolmanagement.entity.Attendance;
import com.schoolmanagement.entity.AttendanceStatus;
import com.schoolmanagement.entity.Role;
import com.schoolmanagement.entity.Student;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.repository.AttendanceRepository;
import com.schoolmanagement.repository.SemesterRepository;
import com.schoolmanagement.repository.StudentRepository;
import com.schoolmanagement.security.StudentAccessGuard;
import com.schoolmanagement.security.TeacherAssignmentGuard;
import com.schoolmanagement.security.TeacherHomeroomGuard;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
@Transactional
public class AttendanceService {

    private AttendanceRepository attendanceRepository;
    private StudentRepository studentRepository;
    private SemesterRepository semesterRepository;
    private StudentAccessGuard studentAccessGuard;
    private TeacherHomeroomGuard teacherHomeroomGuard;
    private TeacherAssignmentGuard teacherAssignmentGuard;

    /**
     * H.3.1, mở rộng theo cách ghi "sổ đầu bài" thực tế: a TEACHER may mark
     * attendance for a class if they are GVCN (homeroom teacher) of it, OR
     * hold a TeachingAssignment for it in the semester covering {@code date}
     * (GVBM - real sổ đầu bài attendance is recorded per period by whichever
     * teacher is teaching, not only GVCN). ADMIN is unrestricted.
     */
    private void enforceCanTouchAttendance(String className, String section, LocalDate date, User requester) {
        if (requester == null || requester.getRole() != Role.TEACHER) {
            return;
        }
        if (teacherHomeroomGuard.isHomeroomClassNameSection(className, section, requester)) {
            return;
        }
        // Nothing guarantees semester date ranges never overlap (different
        // academic years, or plain data overlap), so a date can match more
        // than one semester - any one of them having the assignment is enough.
        boolean hasAssignment = semesterRepository
                .findByStartDateLessThanEqualAndEndDateGreaterThanEqual(date, date)
                .stream()
                .anyMatch(semester -> teacherAssignmentGuard.hasAssignmentForClass(className, section, semester, requester));
        if (!hasAssignment) {
            throw new AccessDeniedException(
                    "Only the class's GVCN or a teacher with a TeachingAssignment for this class may mark attendance");
        }
    }

    public Attendance markAttendance(Attendance attendance, User requester) {
        Student student = studentRepository.findById(attendance.getStudent().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        enforceCanTouchAttendance(student.getClassName(), student.getSection(), attendance.getAttendanceDate(), requester);

        return attendanceRepository.save(attendance);
    }

    public void markAttendanceForClass(String className, String section, LocalDate date, List<Long> presentStudentIds,
                                        AttendanceStatus status, User marker) {
        enforceCanTouchAttendance(className, section, date, marker);
        List<Student> students = studentRepository.findByClassNameAndSection(className, section);

        // Re-marking the same class+date used to just insert a second batch
        // of rows on top of the first (no unique constraint on
        // student+date), silently duplicating records and skewing every %
        // calculation that counts rows. Deleting this class's existing rows
        // for this exact date first makes a re-submit a real correction
        // instead - matches the frontend's "đã điểm danh, sửa lại?" flow
        // (AttendanceManagement, Tuần 4 Ngày 2).
        List<Attendance> existing = attendanceRepository.findByStudentInAndAttendanceDate(students, date);
        if (!existing.isEmpty()) {
            attendanceRepository.deleteAll(existing);
        }

        for (Student student : students) {
            Attendance attendance = Attendance.builder()
                    .student(student)
                    .attendanceDate(date)
                    .status(presentStudentIds.contains(student.getId()) ? AttendanceStatus.PRESENT : status)
                    .markedBy(marker)
                    .build();
            attendanceRepository.save(attendance);
        }
    }

    public AttendanceDTO updateAttendance(Long id, Attendance attendanceDetails, User requester) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found"));
        Student student = attendance.getStudent();
        enforceCanTouchAttendance(student.getClassName(), student.getSection(), attendance.getAttendanceDate(), requester);

        attendance.setStatus(attendanceDetails.getStatus());
        attendance.setRemarks(attendanceDetails.getRemarks());
        attendance.setMarkedBy(attendanceDetails.getMarkedBy());

        return mapToDTO(attendanceRepository.save(attendance));
    }

    public AttendanceDTO getAttendanceById(Long id, User requester) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found"));
        studentAccessGuard.enforceCanAccessStudent(attendance.getStudent().getId(), requester);
        return mapToDTO(attendance);
    }

    public List<AttendanceDTO> getStudentAttendance(Long studentId, User requester) {
        studentAccessGuard.enforceCanAccessStudent(studentId, requester);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        return attendanceRepository.findByStudent(student).stream().map(this::mapToDTO).toList();
    }

    public List<AttendanceDTO> getStudentAttendanceBetweenDates(Long studentId, LocalDate startDate, LocalDate endDate, User requester) {
        studentAccessGuard.enforceCanAccessStudent(studentId, requester);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        return attendanceRepository.findByStudentAndAttendanceDateBetween(student, startDate, endDate)
                .stream().map(this::mapToDTO).toList();
    }

    /**
     * Every read path here returns AttendanceDTO, never the raw entity — its
     * lazy `student`/`markedBy` associations are not resolved by the time
     * Jackson serializes the response (open-in-view is off, so the
     * persistence context is already closed), which throws
     * LazyInitializationException. (Found live while retrofitting PARENT
     * access in 3.6 — pre-existing, affected every role, not just the new
     * one; fixed for all of these methods at once rather than only the ones
     * PARENT needed.)
     */
    public List<AttendanceDTO> getAttendanceByDate(LocalDate date) {
        return attendanceRepository.findByAttendanceDateBetween(date, date).stream().map(this::mapToDTO).toList();
    }

    public List<AttendanceDTO> getAttendanceBetweenDates(LocalDate startDate, LocalDate endDate) {
        return attendanceRepository.findByAttendanceDateBetween(startDate, endDate).stream().map(this::mapToDTO).toList();
    }

    public long getPresenceDays(Long studentId, LocalDate startDate, LocalDate endDate, User requester) {
        List<AttendanceDTO> attendances = getStudentAttendanceBetweenDates(studentId, startDate, endDate, requester);
        return attendances.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.PRESENT)
                .count();
    }

    public long getAbsenceDays(Long studentId, LocalDate startDate, LocalDate endDate, User requester) {
        List<AttendanceDTO> attendances = getStudentAttendanceBetweenDates(studentId, startDate, endDate, requester);
        return attendances.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.ABSENT)
                .count();
    }

    public double getAttendancePercentage(Long studentId, LocalDate startDate, LocalDate endDate, User requester) {
        List<AttendanceDTO> attendances = getStudentAttendanceBetweenDates(studentId, startDate, endDate, requester);
        if (attendances.isEmpty()) {
            return 0;
        }

        long presentDays = attendances.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.PRESENT)
                .count();

        return (double) presentDays / attendances.size() * 100;
    }

    public void deleteAttendance(Long id, User requester) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found"));
        Student student = attendance.getStudent();
        enforceCanTouchAttendance(student.getClassName(), student.getSection(), attendance.getAttendanceDate(), requester);
        attendanceRepository.delete(attendance);
    }

    private AttendanceDTO mapToDTO(Attendance attendance) {
        Student student = attendance.getStudent();
        User markedBy = attendance.getMarkedBy();

        return AttendanceDTO.builder()
                .id(attendance.getId())
                .studentId(student != null ? student.getId() : null)
                .studentName(student != null && student.getUser() != null
                        ? student.getUser().getFirstName() + " " + student.getUser().getLastName()
                        : null)
                .attendanceDate(attendance.getAttendanceDate())
                .status(attendance.getStatus())
                .remarks(attendance.getRemarks())
                .markedById(markedBy != null ? markedBy.getId() : null)
                .markedByName(markedBy != null ? markedBy.getFirstName() + " " + markedBy.getLastName() : null)
                .createdAt(attendance.getCreatedAt())
                .updatedAt(attendance.getUpdatedAt())
                .build();
    }
}
