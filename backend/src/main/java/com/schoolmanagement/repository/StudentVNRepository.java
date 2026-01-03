package com.schoolmanagement.repository;

import com.schoolmanagement.entity.StudentVN;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentVNRepository extends JpaRepository<StudentVN, Long> {
    
    // Find by student code
    Optional<StudentVN> findByStudentCode(String studentCode);
    
    // Find by student ID (legacy)
    Optional<StudentVN> findByStudentId(String studentId);
    
    // Find by ID number (CMND/CCCD)
    Optional<StudentVN> findByIdNumber(String idNumber);
    
    // Find by user ID
    Optional<StudentVN> findByUser_Id(Long userId);
    
    // Find by class
    List<StudentVN> findBySchoolClass_Id(Long classId);
    
    // Find by grade level
    List<StudentVN> findByGradeLevel_Id(Long gradeLevelId);
    
    // Find by status
    List<StudentVN> findByStatus(String status);
    
    // Find by academic year
    List<StudentVN> findByAcademicYear(String academicYear);
    
    // Find by ethnicity
    List<StudentVN> findByEthnicity(String ethnicity);
    
    // Find by province
    List<StudentVN> findByProvince(String province);
    
    // Search by name (first name or last name contains)
    @Query("SELECT s FROM StudentVN s WHERE LOWER(s.firstName) LIKE LOWER(CONCAT('%', :name, '%')) " +
           "OR LOWER(s.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<StudentVN> searchByName(@Param("name") String name);
    
    // Advanced search
    @Query("SELECT s FROM StudentVN s WHERE " +
           "(:studentCode IS NULL OR s.studentCode = :studentCode) AND " +
           "(:classId IS NULL OR s.schoolClass.id = :classId) AND " +
           "(:gradeLevelId IS NULL OR s.gradeLevel.id = :gradeLevelId) AND " +
           "(:status IS NULL OR s.status = :status) AND " +
           "(:name IS NULL OR LOWER(s.firstName) LIKE LOWER(CONCAT('%', :name, '%')) " +
           "OR LOWER(s.lastName) LIKE LOWER(CONCAT('%', :name, '%')))")
    List<StudentVN> advancedSearch(
        @Param("studentCode") String studentCode,
        @Param("classId") Long classId,
        @Param("gradeLevelId") Long gradeLevelId,
        @Param("status") String status,
        @Param("name") String name
    );
    
    // Count by class
    Long countBySchoolClass_Id(Long classId);
    
    // Count by grade level
    Long countByGradeLevel_Id(Long gradeLevelId);
    
    // Count by status
    Long countByStatus(String status);
    
    // Find latest student by admission year for code generation
    @Query("SELECT s FROM StudentVN s WHERE s.admissionYear = :year " +
           "ORDER BY s.studentCode DESC")
    List<StudentVN> findLatestByAdmissionYear(@Param("year") Integer year);
    
    // Check if student code exists
    boolean existsByStudentCode(String studentCode);
    
    // Check if ID number exists
    boolean existsByIdNumber(String idNumber);
}
