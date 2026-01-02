package com.schoolmanagement.controller;

import com.schoolmanagement.entity.ExamResult;
import com.schoolmanagement.service.ExamResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exam-results")
@AllArgsConstructor
@Tag(name = "Exam Results", description = "Exam results and grade management")
public class ExamResultController {

    private ExamResultService examResultService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER')")
    @Operation(summary = "Get all exam results")
    public ResponseEntity<List<ExamResult>> getAllResults() {
        return ResponseEntity.ok(examResultService.getAllResults());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get result by ID")
    public ResponseEntity<ExamResult> getResultById(@PathVariable Long id) {
        return ResponseEntity.ok(examResultService.getResultById(id));
    }

    @GetMapping("/exam/{examId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER')")
    @Operation(summary = "Get results by exam")
    public ResponseEntity<List<ExamResult>> getResultsByExam(@PathVariable Long examId) {
        return ResponseEntity.ok(examResultService.getResultsByExam(examId));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get results by student")
    public ResponseEntity<List<ExamResult>> getResultsByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(examResultService.getResultsByStudent(studentId));
    }

    @GetMapping("/exam/{examId}/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get result by exam and student")
    public ResponseEntity<ExamResult> getResultByExamAndStudent(
            @PathVariable Long examId,
            @PathVariable Long studentId) {
        ExamResult result = examResultService.getResultByExamAndStudent(examId, studentId);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/exam/{examId}/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER')")
    @Operation(summary = "Get exam statistics")
    public ResponseEntity<Map<String, Object>> getExamStatistics(@PathVariable Long examId) {
        Double averageScore = examResultService.calculateAverageScore(examId);
        Long passedCount = examResultService.countPassedStudents(examId);
        Long totalCount = (long) examResultService.getResultsByExam(examId).size();
        
        Map<String, Object> statistics = Map.of(
            "averageScore", averageScore,
            "passedCount", passedCount,
            "failedCount", totalCount - passedCount,
            "totalCount", totalCount,
            "passPercentage", totalCount > 0 ? (passedCount * 100.0 / totalCount) : 0
        );
        
        return ResponseEntity.ok(statistics);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRINCIPAL') or hasRole('TEACHER')")
    @Operation(summary = "Create exam result")
    public ResponseEntity<ExamResult> createResult(@RequestBody ExamResult result) {
        return new ResponseEntity<>(examResultService.createResult(result), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRINCIPAL') or hasRole('TEACHER')")
    @Operation(summary = "Update exam result")
    public ResponseEntity<ExamResult> updateResult(@PathVariable Long id, @RequestBody ExamResult resultDetails) {
        return ResponseEntity.ok(examResultService.updateResult(id, resultDetails));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRINCIPAL')")
    @Operation(summary = "Delete exam result")
    public ResponseEntity<Void> deleteResult(@PathVariable Long id) {
        examResultService.deleteResult(id);
        return ResponseEntity.noContent().build();
    }
}
