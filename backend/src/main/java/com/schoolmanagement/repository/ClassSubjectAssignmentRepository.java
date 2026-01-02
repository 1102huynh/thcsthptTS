package com.schoolmanagement.repository;

import com.schoolmanagement.entity.ClassSubjectAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassSubjectAssignmentRepository extends JpaRepository<ClassSubjectAssignment, Long> {
    
    // Find by class
    List<ClassSubjectAssignment> findBySchoolClassId(Long classId);
    
    // Find by teacher
    List<ClassSubjectAssignment> findByTeacherId(Long teacherId);
    
    // Find by subject
    List<ClassSubjectAssignment> findBySubjectId(Long subjectId);
    
    // Find by academic year and semester
    List<ClassSubjectAssignment> findByAcademicYearAndSemester(String academicYear, Integer semester);
    
    // Find by class, subject, and semester
    Optional<ClassSubjectAssignment> findBySchoolClassIdAndSubjectIdAndSemesterAndAcademicYear(
        Long classId, Long subjectId, Integer semester, String academicYear
    );
    
    // Find teacher's assignments for academic year
    List<ClassSubjectAssignment> findByTeacherIdAndAcademicYear(Long teacherId, String academicYear);
    
    // Find class assignments for semester
    List<ClassSubjectAssignment> findBySchoolClassIdAndSemester(Long classId, Integer semester);
    
    // Find active assignments
    List<ClassSubjectAssignment> findByStatus(String status);
    
    // Get teacher workload (total periods per week)
    @Query("SELECT SUM(csa.periodsPerWeek) FROM ClassSubjectAssignment csa " +
           "WHERE csa.teacher.id = :teacherId AND csa.academicYear = :academicYear AND csa.semester = :semester")
    Integer calculateTeacherWorkload(
        @Param("teacherId") Long teacherId,
        @Param("academicYear") String academicYear,
        @Param("semester") Integer semester
    );
    
    // Check if assignment exists
    boolean existsBySchoolClassIdAndSubjectIdAndSemesterAndAcademicYear(
        Long classId, Long subjectId, Integer semester, String academicYear
    );
}
