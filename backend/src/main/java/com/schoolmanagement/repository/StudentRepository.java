package com.schoolmanagement.repository;

import com.schoolmanagement.entity.Student;
import com.schoolmanagement.entity.StudentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByRollNumber(String rollNumber);
    Optional<Student> findByAdmissionNumber(String admissionNumber);
    Optional<Student> findByUserId(Long userId);
    List<Student> findByClassName(String className);
    List<Student> findByClassNameAndSection(String className, String section);
    List<Student> findByStatus(StudentStatus status);
    boolean existsByRollNumber(String rollNumber);
    boolean existsByAdmissionNumber(String admissionNumber);
}

