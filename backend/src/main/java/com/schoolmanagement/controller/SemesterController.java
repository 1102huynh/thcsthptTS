package com.schoolmanagement.controller;

import com.schoolmanagement.dto.SemesterDTO;
import com.schoolmanagement.entity.Semester;
import com.schoolmanagement.service.SemesterService;
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
@RequestMapping("/v1/semesters")
@AllArgsConstructor
@Tag(name = "Semesters", description = "Học kỳ management endpoints")
public class SemesterController {

    private SemesterService semesterService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRINCIPAL')")
    @Operation(summary = "Create semester")
    public ResponseEntity<SemesterDTO> createSemester(@Valid @RequestBody Semester semester) {
        return new ResponseEntity<>(semesterService.createSemester(semester), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRINCIPAL')")
    @Operation(summary = "Update semester")
    public ResponseEntity<SemesterDTO> updateSemester(@PathVariable Long id, @Valid @RequestBody Semester details) {
        return new ResponseEntity<>(semesterService.updateSemester(id, details), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER')")
    @Operation(summary = "Get semester by ID")
    public ResponseEntity<SemesterDTO> getSemesterById(@PathVariable Long id) {
        return new ResponseEntity<>(semesterService.getSemesterById(id), HttpStatus.OK);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER')")
    @Operation(summary = "Get all semesters")
    public ResponseEntity<List<SemesterDTO>> getAllSemesters() {
        return new ResponseEntity<>(semesterService.getAllSemesters(), HttpStatus.OK);
    }

    @GetMapping("/academic-year/{academicYearId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER')")
    @Operation(summary = "Get semesters for an academic year")
    public ResponseEntity<List<SemesterDTO>> getSemestersByAcademicYear(@PathVariable Long academicYearId) {
        return new ResponseEntity<>(semesterService.getSemestersByAcademicYear(academicYearId), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRINCIPAL')")
    @Operation(summary = "Delete semester")
    public ResponseEntity<Void> deleteSemester(@PathVariable Long id) {
        semesterService.deleteSemester(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
