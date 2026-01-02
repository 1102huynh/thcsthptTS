package com.schoolmanagement.repository;

import com.schoolmanagement.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {
    List<Exam> findByExamDateBetween(LocalDate startDate, LocalDate endDate);
    List<Exam> findByGradeLevel_Id(Long gradeLevelId);
    List<Exam> findBySubject_Id(Long subjectId);
    List<Exam> findByAcademicYear_Id(Long academicYearId);
    List<Exam> findByStatus(String status);
    List<Exam> findByExamTypeAndSemester(String examType, String semester);
}
