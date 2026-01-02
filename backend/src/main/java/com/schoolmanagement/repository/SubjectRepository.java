package com.schoolmanagement.repository;

import com.schoolmanagement.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {
    
    // Find by subject code
    Optional<Subject> findBySubjectCode(String subjectCode);
    
    // Find by school type
    List<Subject> findBySchoolType(Subject.SchoolType schoolType);
    
    // Find required subjects
    List<Subject> findByIsRequired(Boolean isRequired);
    
    // Find by category
    List<Subject> findByCategory(String category);
    
    // Find active subjects
    List<Subject> findByStatus(String status);
    
    // Find by school type and status
    List<Subject> findBySchoolTypeAndStatus(Subject.SchoolType schoolType, String status);
    
    // Search by name
    List<Subject> findBySubjectNameContainingIgnoreCase(String name);
    
    // Check if exists
    boolean existsBySubjectCode(String subjectCode);
    
    // Get all subjects for middle school (THCS)
    @Query("SELECT s FROM Subject s WHERE (s.schoolType = 'THCS' OR s.schoolType = 'BOTH') AND s.status = 'ACTIVE'")
    List<Subject> findMiddleSchoolSubjects();
    
    // Get all subjects for high school (THPT)
    @Query("SELECT s FROM Subject s WHERE (s.schoolType = 'THPT' OR s.schoolType = 'BOTH') AND s.status = 'ACTIVE'")
    List<Subject> findHighSchoolSubjects();
}
