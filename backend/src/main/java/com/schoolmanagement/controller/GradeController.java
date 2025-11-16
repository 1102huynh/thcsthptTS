package com.schoolmanagement.controller;

import com.schoolmanagement.entity.Grade;
import com.schoolmanagement.service.GradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/grades")
@AllArgsConstructor
@Tag(name = "Grade Management", description = "Grade management endpoints")
public class GradeController {

    private GradeService gradeService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    @Operation(summary = "Create grade record")
    public ResponseEntity<Grade> createGrade(@RequestBody Grade grade) {
        Grade createdGrade = gradeService.createGrade(grade);
        return new ResponseEntity<>(createdGrade, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    @Operation(summary = "Update grade record")
    public ResponseEntity<Grade> updateGrade(@PathVariable Long id, @RequestBody Grade gradeDetails) {
        Grade updatedGrade = gradeService.updateGrade(id, gradeDetails);
        return new ResponseEntity<>(updatedGrade, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get grade record by ID")
    public ResponseEntity<Grade> getGradeById(@PathVariable Long id) {
        Grade grade = gradeService.getGradeById(id);
        return new ResponseEntity<>(grade, HttpStatus.OK);
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get all grades for a student")
    public ResponseEntity<List<Grade>> getStudentGrades(@PathVariable Long studentId) {
        List<Grade> grades = gradeService.getStudentGrades(studentId);
        return new ResponseEntity<>(grades, HttpStatus.OK);
    }

    @GetMapping("/student/{studentId}/year/{academicYear}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get grades for student by academic year")
    public ResponseEntity<List<Grade>> getStudentGradesByYear(
            @PathVariable Long studentId,
            @PathVariable String academicYear) {
        List<Grade> grades = gradeService.getStudentGradesByAcademicYear(studentId, academicYear);
        return new ResponseEntity<>(grades, HttpStatus.OK);
    }

    @GetMapping("/student/{studentId}/subject/{subject}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get grades for student by subject")
    public ResponseEntity<List<Grade>> getStudentGradesBySubject(
            @PathVariable Long studentId,
            @PathVariable String subject) {
        List<Grade> grades = gradeService.getStudentGradesBySubject(studentId, subject);
        return new ResponseEntity<>(grades, HttpStatus.OK);
    }

    @GetMapping("/year/{academicYear}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Get all grades by academic year")
    public ResponseEntity<List<Grade>> getGradesByAcademicYear(@PathVariable String academicYear) {
        List<Grade> grades = gradeService.getGradesByAcademicYear(academicYear);
        return new ResponseEntity<>(grades, HttpStatus.OK);
    }

    @GetMapping("/student/{studentId}/average")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get average percentage for student")
    public ResponseEntity<Double> getStudentAveragePercentage(@PathVariable Long studentId) {
        Double average = gradeService.getStudentAveragePercentage(studentId);
        return new ResponseEntity<>(average, HttpStatus.OK);
    }

    @GetMapping("/student/{studentId}/average/year/{academicYear}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get average percentage for student by academic year")
    public ResponseEntity<Double> getStudentAveragePercentageByYear(
            @PathVariable Long studentId,
            @PathVariable String academicYear) {
        Double average = gradeService.getStudentAveragePercentageByYear(studentId, academicYear);
        return new ResponseEntity<>(average, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    @Operation(summary = "Delete grade record")
    public ResponseEntity<Void> deleteGrade(@PathVariable Long id) {
        gradeService.deleteGrade(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

