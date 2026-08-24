package com.schoolmanagement.controller;

import com.schoolmanagement.dto.GradeRecordDTO;
import com.schoolmanagement.dto.SubjectSemesterAverageDTO;
import com.schoolmanagement.dto.SubjectYearAverageDTO;
import com.schoolmanagement.entity.GradeRecord;
import com.schoolmanagement.service.GradeRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/grade-records")
@AllArgsConstructor
@Tag(name = "Grade Records", description = "Điểm số theo Thông tư 22/2021 (tương thích TT58) — thang điểm 10, theo loại điểm miệng/15 phút/1 tiết/giữa kỳ/cuối kỳ. Superseded /v1/grades' percentage-based model.")
public class GradeRecordController {

    private GradeRecordService gradeRecordService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    @Operation(summary = "Create a grade record")
    public ResponseEntity<GradeRecordDTO> createGradeRecord(@Valid @RequestBody GradeRecord request) {
        return new ResponseEntity<>(gradeRecordService.createGradeRecord(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    @Operation(summary = "Update a grade record")
    public ResponseEntity<GradeRecordDTO> updateGradeRecord(@PathVariable Long id, @Valid @RequestBody GradeRecord request) {
        return new ResponseEntity<>(gradeRecordService.updateGradeRecord(id, request), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get a grade record by ID")
    public ResponseEntity<GradeRecordDTO> getGradeRecordById(@PathVariable Long id) {
        return new ResponseEntity<>(gradeRecordService.getGradeRecordById(id), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    @Operation(summary = "Delete a grade record")
    public ResponseEntity<Void> deleteGradeRecord(@PathVariable Long id) {
        gradeRecordService.deleteGradeRecord(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/student/{studentId}/semester/{semesterId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get every grade record for a student in one semester (all subjects, all component types)")
    public ResponseEntity<List<GradeRecordDTO>> getStudentSemesterGrades(
            @PathVariable Long studentId, @PathVariable Long semesterId) {
        return new ResponseEntity<>(gradeRecordService.getStudentSemesterGrades(studentId, semesterId), HttpStatus.OK);
    }

    @GetMapping("/student/{studentId}/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Điểm TB môn học kỳ, per subject",
            description = "Σ(score × weight) / Σ(weight) for every subject the student has a grade record in for that semester. classification is not computed yet — see field description.")
    public ResponseEntity<List<SubjectSemesterAverageDTO>> getStudentSemesterSummary(
            @PathVariable Long studentId, @RequestParam Long semesterId) {
        return new ResponseEntity<>(gradeRecordService.getStudentSemesterSummary(studentId, semesterId), HttpStatus.OK);
    }

    @GetMapping("/student/{studentId}/year-summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Điểm TB môn cả năm, per subject",
            description = "(ĐTB HK1 + ĐTB HK2 × 2) / 3 for every subject the student has a grade record in for that academic year. classification is not computed yet — see field description.")
    public ResponseEntity<List<SubjectYearAverageDTO>> getStudentYearSummary(
            @PathVariable Long studentId, @RequestParam Long academicYearId) {
        return new ResponseEntity<>(gradeRecordService.getStudentYearSummary(studentId, academicYearId), HttpStatus.OK);
    }
}
