package com.schoolmanagement.controller;

import com.schoolmanagement.dto.GradeComponentConfigDTO;
import com.schoolmanagement.entity.GradeComponentConfig;
import com.schoolmanagement.service.GradeComponentConfigService;
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
@RequestMapping("/v1/grade-config")
@AllArgsConstructor
@Tag(name = "Grade Config", description = "ADMIN-configurable weight (hệ số) per grade component type, scoped by the academic year it starts applying from")
public class GradeConfigController {

    private GradeComponentConfigService gradeComponentConfigService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a grade component weight config")
    public ResponseEntity<GradeComponentConfigDTO> createConfig(@Valid @RequestBody GradeComponentConfig config) {
        return new ResponseEntity<>(gradeComponentConfigService.createConfig(config), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a grade component weight config")
    public ResponseEntity<GradeComponentConfigDTO> updateConfig(@PathVariable Long id, @Valid @RequestBody GradeComponentConfig details) {
        return new ResponseEntity<>(gradeComponentConfigService.updateConfig(id, details), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Get a grade component weight config by ID")
    public ResponseEntity<GradeComponentConfigDTO> getConfigById(@PathVariable Long id) {
        return new ResponseEntity<>(gradeComponentConfigService.getConfigById(id), HttpStatus.OK);
    }

    // TEACHER (unlike the write endpoints below) can read these - a teacher
    // entering grades needs the weights to make sense of/preview "TB môn"
    // averages client-side (GradeManagement.jsx), the same reasoning
    // TimetableController's read side is broader than its write side.
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Get all grade component weight configs")
    public ResponseEntity<List<GradeComponentConfigDTO>> getAllConfigs() {
        return new ResponseEntity<>(gradeComponentConfigService.getAllConfigs(), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a grade component weight config")
    public ResponseEntity<Void> deleteConfig(@PathVariable Long id) {
        gradeComponentConfigService.deleteConfig(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
