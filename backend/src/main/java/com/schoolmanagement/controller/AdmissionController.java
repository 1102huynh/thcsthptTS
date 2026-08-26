package com.schoolmanagement.controller;

import com.schoolmanagement.dto.AdmissionApplicationDTO;
import com.schoolmanagement.dto.AdmissionApprovalResultDTO;
import com.schoolmanagement.dto.ApproveAndCreateRequest;
import com.schoolmanagement.dto.SubmitAdmissionRequest;
import com.schoolmanagement.dto.UpdateAdmissionStatusRequest;
import com.schoolmanagement.entity.AdmissionStatus;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.service.AdmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/admissions")
@AllArgsConstructor
@Tag(name = "Admissions (Tuyển sinh)", description = "Public submission (rate-limited, see AdmissionRateLimitFilter); everything else ADMIN-only.")
public class AdmissionController {

    private AdmissionService admissionService;

    @PostMapping
    @Operation(summary = "Submit an admission application",
            description = "Public — no login required. Rate-limited per IP (see IMPLEMENTATION_PLAN.md 3.7). status/submittedAt/reviewedBy are always server-controlled regardless of what's sent.")
    public ResponseEntity<AdmissionApplicationDTO> submit(@Valid @RequestBody SubmitAdmissionRequest request) {
        return new ResponseEntity<>(admissionService.submit(request), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List admission applications", description = "Optional ?status= filter.")
    public ResponseEntity<List<AdmissionApplicationDTO>> getAllApplications(
            @RequestParam(required = false) AdmissionStatus status) {
        List<AdmissionApplicationDTO> applications = status != null
                ? admissionService.getApplicationsByStatus(status)
                : admissionService.getAllApplications();
        return new ResponseEntity<>(applications, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get an admission application by ID")
    public ResponseEntity<AdmissionApplicationDTO> getApplicationById(@PathVariable Long id) {
        return new ResponseEntity<>(admissionService.getApplicationById(id), HttpStatus.OK);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Move an application to a new status (PENDING/REVIEWING/APPROVED/REJECTED)")
    public ResponseEntity<AdmissionApplicationDTO> updateStatus(
            @PathVariable Long id, @Valid @RequestBody UpdateAdmissionStatusRequest request, Authentication authentication) {
        User reviewer = (User) authentication.getPrincipal();
        return new ResponseEntity<>(admissionService.updateStatus(id, request, reviewer), HttpStatus.OK);
    }

    @PostMapping("/{id}/approve-and-create")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a User+Student account from an APPROVED application",
            description = "Fails if the application isn't APPROVED yet, or if an account was already created from it.")
    public ResponseEntity<AdmissionApprovalResultDTO> approveAndCreate(
            @PathVariable Long id, @Valid @RequestBody ApproveAndCreateRequest request) {
        return new ResponseEntity<>(admissionService.approveAndCreate(id, request), HttpStatus.CREATED);
    }
}
