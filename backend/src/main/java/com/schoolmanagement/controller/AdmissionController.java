package com.schoolmanagement.controller;

import com.schoolmanagement.dto.AdmissionDTO;
import com.schoolmanagement.entity.AdmissionStatus;
import com.schoolmanagement.service.AdmissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admissions")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AdmissionController {

    private final AdmissionService admissionService;

    /**
     * Get open admissions (Public access)
     * GET /api/admissions?page=0&size=10
     */
    @GetMapping
    public ResponseEntity<Page<AdmissionDTO>> getOpenAdmissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/admissions - page: {}, size: {}", page, size);
        Page<AdmissionDTO> admissions = admissionService.getOpenAdmissions(page, size);
        return ResponseEntity.ok(admissions);
    }

    /**
     * Get admission by ID (Public access)
     * GET /api/admissions/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<AdmissionDTO> getAdmissionById(@PathVariable Long id) {
        log.info("GET /api/admissions/{}", id);
        AdmissionDTO admission = admissionService.getAdmissionById(id);
        return ResponseEntity.ok(admission);
    }

    /**
     * Get all admissions (Admin only)
     * GET /api/admissions/admin/all?page=0&size=10
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL')")
    public ResponseEntity<Page<AdmissionDTO>> getAllAdmissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/admissions/admin/all - page: {}, size: {}", page, size);
        Page<AdmissionDTO> admissions = admissionService.getAllAdmissions(page, size);
        return ResponseEntity.ok(admissions);
    }

    /**
     * Create new admission (Admin only)
     * POST /api/admissions
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL')")
    public ResponseEntity<AdmissionDTO> createAdmission(@RequestBody AdmissionDTO admissionDTO) {
        log.info("POST /api/admissions - Creating admission: {}", admissionDTO.getTitle());
        AdmissionDTO created = admissionService.createAdmission(admissionDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Update admission (Admin only)
     * PUT /api/admissions/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL')")
    public ResponseEntity<AdmissionDTO> updateAdmission(
            @PathVariable Long id,
            @RequestBody AdmissionDTO admissionDTO) {
        log.info("PUT /api/admissions/{} - Updating admission", id);
        AdmissionDTO updated = admissionService.updateAdmission(id, admissionDTO);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete admission (Admin only)
     * DELETE /api/admissions/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL')")
    public ResponseEntity<Void> deleteAdmission(@PathVariable Long id) {
        log.info("DELETE /api/admissions/{}", id);
        admissionService.deleteAdmission(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Update admission status (Admin only)
     * PUT /api/admissions/{id}/status
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL')")
    public ResponseEntity<AdmissionDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam AdmissionStatus status) {
        log.info("PUT /api/admissions/{}/status - Updating status to {}", id, status);
        AdmissionDTO updated = admissionService.updateStatus(id, status);
        return ResponseEntity.ok(updated);
    }
}

