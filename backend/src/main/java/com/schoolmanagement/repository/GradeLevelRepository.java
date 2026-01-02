package com.schoolmanagement.repository;

import com.schoolmanagement.entity.GradeLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GradeLevelRepository extends JpaRepository<GradeLevel, Long> {
    
    // Find by level number and academic year
    Optional<GradeLevel> findByLevelNumberAndAcademicYear(Integer levelNumber, String academicYear);
    
    // Find all by academic year
    List<GradeLevel> findByAcademicYear(String academicYear);
    
    // Find all by school type
    List<GradeLevel> findBySchoolType(GradeLevel.SchoolType schoolType);
    
    // Find by academic year and school type
    List<GradeLevel> findByAcademicYearAndSchoolType(String academicYear, GradeLevel.SchoolType schoolType);
    
    // Find active grade levels
    List<GradeLevel> findByStatus(String status);
    
    // Find by level number
    List<GradeLevel> findByLevelNumber(Integer levelNumber);
    
    // Check if exists
    boolean existsByLevelNumberAndAcademicYear(Integer levelNumber, String academicYear);
    
    // Get current academic year grade levels (assuming 1 active year)
    @Query("SELECT g FROM GradeLevel g WHERE g.status = 'ACTIVE' AND g.academicYear = " +
           "(SELECT MAX(gl.academicYear) FROM GradeLevel gl WHERE gl.status = 'ACTIVE')")
    List<GradeLevel> findCurrentAcademicYearGradeLevels();
}
