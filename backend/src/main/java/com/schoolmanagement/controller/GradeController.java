package com.schoolmanagement.controller;

import com.schoolmanagement.dto.GradeDTO;
import com.schoolmanagement.entity.Grade;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.service.GradeService;
import com.schoolmanagement.util.PaginationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
    public ResponseEntity<Grade> createGrade(@Valid @RequestBody Grade grade) {
        Grade createdGrade = gradeService.createGrade(grade);
        return new ResponseEntity<>(createdGrade, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    @Operation(summary = "Update grade record")
    public ResponseEntity<GradeDTO> updateGrade(@PathVariable Long id, @Valid @RequestBody Grade gradeDetails) {
        GradeDTO updatedGrade = gradeService.updateGrade(id, gradeDetails);
        return new ResponseEntity<>(updatedGrade, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    @Operation(summary = "Get grade record by ID",
            description = "A STUDENT may only fetch their own grades; a PARENT only their own child's (403 otherwise).")
    public ResponseEntity<GradeDTO> getGradeById(@PathVariable Long id, Authentication authentication) {
        GradeDTO grade = gradeService.getGradeById(id, (User) authentication.getPrincipal());
        return new ResponseEntity<>(grade, HttpStatus.OK);
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    @Operation(summary = "Get all grades for a student",
            description = "A STUDENT may only fetch their own grades; a PARENT only their own child's (403 otherwise).")
    public ResponseEntity<List<GradeDTO>> getStudentGrades(@PathVariable Long studentId, Authentication authentication) {
        List<GradeDTO> grades = gradeService.getStudentGrades(studentId, (User) authentication.getPrincipal());
        return new ResponseEntity<>(grades, HttpStatus.OK);
    }

    @GetMapping("/student/{studentId}/year/{academicYear}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    @Operation(summary = "Get grades for student by academic year",
            description = "A STUDENT may only fetch their own grades; a PARENT only their own child's (403 otherwise).")
    public ResponseEntity<List<GradeDTO>> getStudentGradesByYear(
            @PathVariable Long studentId,
            @PathVariable String academicYear,
            Authentication authentication) {
        List<GradeDTO> grades = gradeService.getStudentGradesByAcademicYear(
                studentId, academicYear, (User) authentication.getPrincipal());
        return new ResponseEntity<>(grades, HttpStatus.OK);
    }

    @GetMapping("/student/{studentId}/subject/{subject}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    @Operation(summary = "Get grades for student by subject",
            description = "A STUDENT may only fetch their own grades; a PARENT only their own child's (403 otherwise).")
    public ResponseEntity<List<GradeDTO>> getStudentGradesBySubject(
            @PathVariable Long studentId,
            @PathVariable String subject,
            Authentication authentication) {
        List<GradeDTO> grades = gradeService.getStudentGradesBySubject(
                studentId, subject, (User) authentication.getPrincipal());
        return new ResponseEntity<>(grades, HttpStatus.OK);
    }

    @GetMapping("/year/{academicYear}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Get all grades by academic year",
            description = "Optional page/size query params paginate the result (0-indexed page); omit both to get the full list. Total count is returned in the X-Total-Count header when paginated.")
    public ResponseEntity<List<GradeDTO>> getGradesByAcademicYear(
            @PathVariable String academicYear,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        Pageable pageable = PaginationUtil.toPageable(page, size);
        if (pageable == null) {
            return new ResponseEntity<>(gradeService.getGradesByAcademicYear(academicYear), HttpStatus.OK);
        }
        Page<GradeDTO> result = gradeService.getGradesByAcademicYear(academicYear, pageable);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(result.getTotalElements()))
                .body(result.getContent());
    }

    @GetMapping("/student/{studentId}/average")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    @Operation(summary = "Get average percentage for student",
            description = "A STUDENT may only fetch their own average; a PARENT only their own child's (403 otherwise).")
    public ResponseEntity<Double> getStudentAveragePercentage(@PathVariable Long studentId, Authentication authentication) {
        Double average = gradeService.getStudentAveragePercentage(studentId, (User) authentication.getPrincipal());
        return new ResponseEntity<>(average, HttpStatus.OK);
    }

    @GetMapping("/student/{studentId}/average/year/{academicYear}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    @Operation(summary = "Get average percentage for student by academic year",
            description = "A STUDENT may only fetch their own average; a PARENT only their own child's (403 otherwise).")
    public ResponseEntity<Double> getStudentAveragePercentageByYear(
            @PathVariable Long studentId,
            @PathVariable String academicYear,
            Authentication authentication) {
        Double average = gradeService.getStudentAveragePercentageByYear(
                studentId, academicYear, (User) authentication.getPrincipal());
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

