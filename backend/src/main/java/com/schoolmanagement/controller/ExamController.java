package com.schoolmanagement.controller;

import com.schoolmanagement.entity.Exam;
import com.schoolmanagement.service.ExamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/exams")
@AllArgsConstructor
@Tag(name = "Exam Management", description = "Exam scheduling and management")
public class ExamController {

    private ExamService examService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get all exams")
    public ResponseEntity<List<Exam>> getAllExams() {
        return ResponseEntity.ok(examService.getAllExams());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get exam by ID")
    public ResponseEntity<Exam> getExamById(@PathVariable Long id) {
        return ResponseEntity.ok(examService.getExamById(id));
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get exams by date range")
    public ResponseEntity<List<Exam>> getExamsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(examService.getExamsByDateRange(startDate, endDate));
    }

    @GetMapping("/grade-level/{gradeLevelId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get exams by grade level")
    public ResponseEntity<List<Exam>> getExamsByGradeLevel(@PathVariable Long gradeLevelId) {
        return ResponseEntity.ok(examService.getExamsByGradeLevel(gradeLevelId));
    }

    @GetMapping("/subject/{subjectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get exams by subject")
    public ResponseEntity<List<Exam>> getExamsBySubject(@PathVariable Long subjectId) {
        return ResponseEntity.ok(examService.getExamsBySubject(subjectId));
    }

    @GetMapping("/academic-year/{academicYearId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER')")
    @Operation(summary = "Get exams by academic year")
    public ResponseEntity<List<Exam>> getExamsByAcademicYear(@PathVariable Long academicYearId) {
        return ResponseEntity.ok(examService.getExamsByAcademicYear(academicYearId));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER')")
    @Operation(summary = "Get exams by status")
    public ResponseEntity<List<Exam>> getExamsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(examService.getExamsByStatus(status));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRINCIPAL')")
    @Operation(summary = "Create new exam")
    public ResponseEntity<Exam> createExam(@RequestBody Exam exam) {
        return new ResponseEntity<>(examService.createExam(exam), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRINCIPAL')")
    @Operation(summary = "Update exam")
    public ResponseEntity<Exam> updateExam(@PathVariable Long id, @RequestBody Exam examDetails) {
        return ResponseEntity.ok(examService.updateExam(id, examDetails));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRINCIPAL')")
    @Operation(summary = "Update exam status")
    public ResponseEntity<Exam> updateExamStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(examService.updateExamStatus(id, status));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRINCIPAL')")
    @Operation(summary = "Delete exam")
    public ResponseEntity<Void> deleteExam(@PathVariable Long id) {
        examService.deleteExam(id);
        return ResponseEntity.noContent().build();
    }
}
