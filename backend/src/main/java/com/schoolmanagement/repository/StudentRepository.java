package com.schoolmanagement.repository;

import com.schoolmanagement.entity.Student;
import com.schoolmanagement.entity.StudentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    // JOIN FETCH s.user variant of the above, for callers that need every
    // roster student's display name (Student.user is @ManyToOne(LAZY)) -
    // avoids one lazy-load query per student on top of the roster query
    // itself. See ReportService.generateClassAttendanceExcel.
    @Query("SELECT s FROM Student s LEFT JOIN FETCH s.user WHERE s.className = :className AND s.section = :section")
    List<Student> findByClassNameAndSectionWithUser(@Param("className") String className, @Param("section") String section);

    long countByClassNameAndSection(String className, String section);
    List<Student> findByStatus(StudentStatus status);
    long countByStatus(StudentStatus status);
    boolean existsByRollNumber(String rollNumber);
    boolean existsByAdmissionNumber(String admissionNumber);
}

