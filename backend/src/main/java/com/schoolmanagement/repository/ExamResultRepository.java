package com.schoolmanagement.repository;

import com.schoolmanagement.entity.ExamResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamResultRepository extends JpaRepository<ExamResult, Long> {
    List<ExamResult> findByExam_Id(Long examId);
    List<ExamResult> findByStudent_Id(Long studentId);
    List<ExamResult> findByStudentIdOrderByExam_ExamDateDesc(Long studentId);
    Optional<ExamResult> findByExam_IdAndStudent_Id(Long examId, Long studentId);
    List<ExamResult> findByStatus(String status);
    List<ExamResult> findByGrade(String grade);
}
