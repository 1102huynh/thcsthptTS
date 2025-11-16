package com.schoolmanagement.repository;

import com.schoolmanagement.entity.Fee;
import com.schoolmanagement.entity.Student;
import com.schoolmanagement.entity.FeeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeeRepository extends JpaRepository<Fee, Long> {
    List<Fee> findByStudent(Student student);
    List<Fee> findByStudentAndAcademicYear(Student student, String academicYear);
    List<Fee> findByStatus(FeeStatus status);
    List<Fee> findByAcademicYear(String academicYear);
    List<Fee> findByStudentAndStatus(Student student, FeeStatus status);
}

