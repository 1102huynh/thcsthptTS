package com.schoolmanagement.controller;

import com.schoolmanagement.dto.TeachingAssignmentDTO;
import com.schoolmanagement.entity.TeachingAssignment;
import com.schoolmanagement.service.TeachingAssignmentService;
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
@RequestMapping("/v1/teaching-assignments")
@AllArgsConstructor
@Tag(name = "Teaching Assignments", description = "Phân công giảng dạy — which teacher teaches which subject to which class")
public class TeachingAssignmentController {

    private TeachingAssignmentService teachingAssignmentService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRINCIPAL')")
    @Operation(summary = "Create teaching assignment")
    public ResponseEntity<TeachingAssignmentDTO> createTeachingAssignment(@Valid @RequestBody TeachingAssignment request) {
        return new ResponseEntity<>(teachingAssignmentService.createTeachingAssignment(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRINCIPAL')")
    @Operation(summary = "Update teaching assignment")
    public ResponseEntity<TeachingAssignmentDTO> updateTeachingAssignment(
            @PathVariable Long id, @Valid @RequestBody TeachingAssignment request) {
        return new ResponseEntity<>(teachingAssignmentService.updateTeachingAssignment(id, request), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER')")
    @Operation(summary = "Get teaching assignment by ID")
    public ResponseEntity<TeachingAssignmentDTO> getTeachingAssignmentById(@PathVariable Long id) {
        return new ResponseEntity<>(teachingAssignmentService.getTeachingAssignmentById(id), HttpStatus.OK);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER')")
    @Operation(summary = "Get all teaching assignments")
    public ResponseEntity<List<TeachingAssignmentDTO>> getAllTeachingAssignments() {
        return new ResponseEntity<>(teachingAssignmentService.getAllTeachingAssignments(), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRINCIPAL')")
    @Operation(summary = "Delete teaching assignment", description = "Refused with 409 if it still has timetable slots scheduled.")
    public ResponseEntity<Void> deleteTeachingAssignment(@PathVariable Long id) {
        teachingAssignmentService.deleteTeachingAssignment(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
