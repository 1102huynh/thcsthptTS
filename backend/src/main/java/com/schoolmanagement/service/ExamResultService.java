package com.schoolmanagement.service;

import com.schoolmanagement.entity.ExamResult;
import com.schoolmanagement.repository.ExamResultRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class ExamResultService {

    private ExamResultRepository examResultRepository;

    public List<ExamResult> getAllResults() {
        return examResultRepository.findAll();
    }

    public ExamResult getResultById(Long id) {
        return examResultRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exam result not found with id: " + id));
    }

    public List<ExamResult> getResultsByExam(Long examId) {
        return examResultRepository.findByExam_Id(examId);
    }

    public List<ExamResult> getResultsByStudent(Long studentId) {
        return examResultRepository.findByStudent_Id(studentId);
    }

    public ExamResult getResultByExamAndStudent(Long examId, Long studentId) {
        return examResultRepository.findByExam_IdAndStudent_Id(examId, studentId)
                .orElse(null);
    }

    @Transactional
    public ExamResult createResult(ExamResult result) {
        calculateGradeAndStatus(result);
        return examResultRepository.save(result);
    }

    @Transactional
    public ExamResult updateResult(Long id, ExamResult resultDetails) {
        ExamResult result = getResultById(id);
        
        result.setMarksObtained(resultDetails.getMarksObtained());
        result.setRemarks(resultDetails.getRemarks());
        result.setGradedById(resultDetails.getGradedById());
        result.setGradedAt(LocalDateTime.now());
        
        calculateGradeAndStatus(result);
        
        return examResultRepository.save(result);
    }

    @Transactional
    public void deleteResult(Long id) {
        ExamResult result = getResultById(id);
        examResultRepository.delete(result);
    }

    private void calculateGradeAndStatus(ExamResult result) {
        if (result.getMarksObtained() == null || result.getExam() == null) {
            return;
        }

        Double marks = result.getMarksObtained();
        Integer totalMarks = result.getExam().getTotalMarks();
        Integer passingMarks = result.getExam().getPassingMarks();

        if (totalMarks != null && totalMarks > 0) {
            double percentage = (marks / totalMarks) * 100;
            result.setPercentage(percentage);

            // Calculate grade
            String grade;
            if (percentage >= 90) grade = "A+";
            else if (percentage >= 80) grade = "A";
            else if (percentage >= 70) grade = "B+";
            else if (percentage >= 60) grade = "B";
            else if (percentage >= 50) grade = "C";
            else if (percentage >= 40) grade = "D";
            else grade = "F";
            
            result.setGrade(grade);

            // Calculate status
            if (passingMarks != null) {
                result.setStatus(marks >= passingMarks ? "PASS" : "FAIL");
            } else {
                result.setStatus(percentage >= 40 ? "PASS" : "FAIL");
            }
        }
    }

    public Double calculateAverageScore(Long examId) {
        List<ExamResult> results = getResultsByExam(examId);
        if (results.isEmpty()) return 0.0;
        
        double sum = results.stream()
                .filter(r -> r.getMarksObtained() != null)
                .mapToDouble(ExamResult::getMarksObtained)
                .sum();
        
        return sum / results.size();
    }

    public Long countPassedStudents(Long examId) {
        return examResultRepository.findByExam_Id(examId).stream()
                .filter(r -> "PASS".equals(r.getStatus()))
                .count();
    }
}
