package com.schoolmanagement.repository;

import com.schoolmanagement.entity.Grade;
import com.schoolmanagement.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {
    List<Grade> findByStudent(Student student);
    List<Grade> findByStudentAndAcademicYear(Student student, String academicYear);
    List<Grade> findByStudentAndSubject(Student student, String subject);
    List<Grade> findByAcademicYear(String academicYear);
    Page<Grade> findByAcademicYear(String academicYear, Pageable pageable);
}

