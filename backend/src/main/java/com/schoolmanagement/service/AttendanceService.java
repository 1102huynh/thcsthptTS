package com.schoolmanagement.service;

import com.schoolmanagement.entity.Attendance;
import com.schoolmanagement.entity.AttendanceStatus;
import com.schoolmanagement.entity.Student;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.repository.AttendanceRepository;
import com.schoolmanagement.repository.StudentRepository;
import lombok.AllArgsConstructor;
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

    public Attendance markAttendance(Attendance attendance) {
        Student student = studentRepository.findById(attendance.getStudent().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        return attendanceRepository.save(attendance);
    }

    public void markAttendanceForClass(String className, String section, LocalDate date, List<Long> presentStudentIds, AttendanceStatus status) {
        List<Student> students = studentRepository.findByClassNameAndSection(className, section);

        for (Student student : students) {
            Attendance attendance = Attendance.builder()
                    .student(student)
                    .attendanceDate(date)
                    .status(presentStudentIds.contains(student.getId()) ? AttendanceStatus.PRESENT : status)
                    .build();
            attendanceRepository.save(attendance);
        }
    }

    public Attendance updateAttendance(Long id, Attendance attendanceDetails) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found"));

        attendance.setStatus(attendanceDetails.getStatus());
        attendance.setRemarks(attendanceDetails.getRemarks());
        attendance.setMarkedBy(attendanceDetails.getMarkedBy());

        return attendanceRepository.save(attendance);
    }

    public Attendance getAttendanceById(Long id) {
        return attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found"));
    }

    public List<Attendance> getStudentAttendance(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        return attendanceRepository.findByStudent(student);
    }

    public List<Attendance> getStudentAttendanceBetweenDates(Long studentId, LocalDate startDate, LocalDate endDate) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        return attendanceRepository.findByStudentAndAttendanceDateBetween(student, startDate, endDate);
    }

    public List<Attendance> getAttendanceByDate(LocalDate date) {
        return attendanceRepository.findByAttendanceDateBetween(date, date);
    }

    public List<Attendance> getAttendanceBetweenDates(LocalDate startDate, LocalDate endDate) {
        return attendanceRepository.findByAttendanceDateBetween(startDate, endDate);
    }

    public long getPresenceDays(Long studentId, LocalDate startDate, LocalDate endDate) {
        List<Attendance> attendances = getStudentAttendanceBetweenDates(studentId, startDate, endDate);
        return attendances.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.PRESENT)
                .count();
    }

    public long getAbsenceDays(Long studentId, LocalDate startDate, LocalDate endDate) {
        List<Attendance> attendances = getStudentAttendanceBetweenDates(studentId, startDate, endDate);
        return attendances.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.ABSENT)
                .count();
    }

    public double getAttendancePercentage(Long studentId, LocalDate startDate, LocalDate endDate) {
        List<Attendance> attendances = getStudentAttendanceBetweenDates(studentId, startDate, endDate);
        if (attendances.isEmpty()) {
            return 0;
        }

        long presentDays = attendances.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.PRESENT)
                .count();

        return (double) presentDays / attendances.size() * 100;
    }

    public void deleteAttendance(Long id) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found"));
        attendanceRepository.delete(attendance);
    }
}

