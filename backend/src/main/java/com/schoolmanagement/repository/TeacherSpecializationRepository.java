package com.schoolmanagement.repository;

import com.schoolmanagement.entity.TeacherSpecialization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherSpecializationRepository extends JpaRepository<TeacherSpecialization, Long> {
    
    // Find by teacher
    List<TeacherSpecialization> findByTeacherId(Long teacherId);
    
    // Find by subject
    List<TeacherSpecialization> findBySubjectId(Long subjectId);
    
    // Find by teacher and subject
    Optional<TeacherSpecialization> findByTeacherIdAndSubjectId(Long teacherId, Long subjectId);
    
    // Find primary specializations
    List<TeacherSpecialization> findByIsPrimary(Boolean isPrimary);
    
    // Find teacher's primary specialization
    Optional<TeacherSpecialization> findByTeacherIdAndIsPrimary(Long teacherId, Boolean isPrimary);
    
    // Find by certification level
    List<TeacherSpecialization> findByCertificationLevel(String certificationLevel);
    
    // Find experienced teachers (>= years)
    @Query("SELECT ts FROM TeacherSpecialization ts WHERE ts.yearsOfExperience >= :minYears")
    List<TeacherSpecialization> findExperiencedTeachers(@Param("minYears") Integer minYears);
    
    // Find teachers specialized in subject with minimum experience
    @Query("SELECT ts FROM TeacherSpecialization ts WHERE ts.subject.id = :subjectId AND ts.yearsOfExperience >= :minYears")
    List<TeacherSpecialization> findQualifiedTeachersForSubject(@Param("subjectId") Long subjectId, @Param("minYears") Integer minYears);
    
    // Check if exists
    boolean existsByTeacherIdAndSubjectId(Long teacherId, Long subjectId);
}
