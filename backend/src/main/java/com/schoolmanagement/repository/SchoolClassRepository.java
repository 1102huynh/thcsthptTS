package com.schoolmanagement.repository;

import com.schoolmanagement.entity.SchoolClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SchoolClassRepository extends JpaRepository<SchoolClass, Long> {
    
    // Find by class name and academic year
    Optional<SchoolClass> findByClassNameAndAcademicYear(String className, String academicYear);
    
    // Find all by academic year
    List<SchoolClass> findByAcademicYear(String academicYear);
    
    // Find by grade level
    List<SchoolClass> findByGradeLevelId(Long gradeLevelId);
    
    // Find by grade level and academic year
    List<SchoolClass> findByGradeLevelIdAndAcademicYear(Long gradeLevelId, String academicYear);
    
    // Find by homeroom teacher
    List<SchoolClass> findByHomeroomTeacherId(Long teacherId);
    
    // Find active classes
    List<SchoolClass> findByStatus(String status);
    
    // Find classes with available slots
    @Query("SELECT c FROM SchoolClass c WHERE c.currentStudents < c.maxStudents AND c.status = 'ACTIVE'")
    List<SchoolClass> findClassesWithAvailableSlots();
    
    // Find classes by grade level name pattern
    @Query("SELECT c FROM SchoolClass c JOIN c.gradeLevel gl WHERE gl.levelName LIKE %:levelName%")
    List<SchoolClass> findByGradeLevelNameContaining(@Param("levelName") String levelName);
    
    // Count students in class
    @Query("SELECT c.currentStudents FROM SchoolClass c WHERE c.id = :classId")
    Integer countStudentsInClass(@Param("classId") Long classId);
    
    // Check if class exists
    boolean existsByClassNameAndAcademicYear(String className, String academicYear);
}
