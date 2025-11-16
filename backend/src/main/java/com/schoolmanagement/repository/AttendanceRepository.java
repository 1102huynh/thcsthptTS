package com.schoolmanagement.repository;

import com.schoolmanagement.entity.Attendance;
import com.schoolmanagement.entity.Student;
import com.schoolmanagement.entity.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByStudent(Student student);
    List<Attendance> findByAttendanceDateBetween(LocalDate startDate, LocalDate endDate);
    List<Attendance> findByStudentAndAttendanceDateBetween(Student student, LocalDate startDate, LocalDate endDate);
    List<Attendance> findByStatus(AttendanceStatus status);
}

