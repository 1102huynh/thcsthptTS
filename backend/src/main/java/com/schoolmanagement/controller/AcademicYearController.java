package com.schoolmanagement.controller;

import com.schoolmanagement.dto.AcademicYearDTO;
import com.schoolmanagement.entity.AcademicYear;
import com.schoolmanagement.service.AcademicYearService;
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
@RequestMapping("/v1/academic-years")
@AllArgsConstructor
@Tag(name = "Academic Years", description = "Năm học management endpoints")
public class AcademicYearController {

    private AcademicYearService academicYearService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRINCIPAL')")
    @Operation(summary = "Create academic year")
    public ResponseEntity<AcademicYearDTO> createAcademicYear(@Valid @RequestBody AcademicYear academicYear) {
        return new ResponseEntity<>(academicYearService.createAcademicYear(academicYear), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRINCIPAL')")
    @Operation(summary = "Update academic year")
    public ResponseEntity<AcademicYearDTO> updateAcademicYear(@PathVariable Long id, @Valid @RequestBody AcademicYear details) {
        return new ResponseEntity<>(academicYearService.updateAcademicYear(id, details), HttpStatus.OK);
    }

    // STUDENT/PARENT read-only: the self-service portal (C3) needs the year
    // list to drive its "học kỳ / năm học" picker for grades. This is
    // low-sensitivity reference data (year name + date range + ACTIVE flag),
    // no write access.
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER', 'STUDENT', 'PARENT')")
    @Operation(summary = "Get academic year by ID")
    public ResponseEntity<AcademicYearDTO> getAcademicYearById(@PathVariable Long id) {
        return new ResponseEntity<>(academicYearService.getAcademicYearById(id), HttpStatus.OK);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER', 'STUDENT', 'PARENT')")
    @Operation(summary = "Get all academic years")
    public ResponseEntity<List<AcademicYearDTO>> getAllAcademicYears() {
        return new ResponseEntity<>(academicYearService.getAllAcademicYears(), HttpStatus.OK);
    }

    @PutMapping("/{id}/close")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRINCIPAL')")
    @Operation(summary = "Close an academic year", description = "Marks it CLOSED; does not touch its classes/semesters/grades.")
    public ResponseEntity<AcademicYearDTO> closeAcademicYear(@PathVariable Long id) {
        return new ResponseEntity<>(academicYearService.closeAcademicYear(id), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRINCIPAL')")
    @Operation(summary = "Delete academic year", description = "Refused with 409 if it still has semesters or classes.")
    public ResponseEntity<Void> deleteAcademicYear(@PathVariable Long id) {
        academicYearService.deleteAcademicYear(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
