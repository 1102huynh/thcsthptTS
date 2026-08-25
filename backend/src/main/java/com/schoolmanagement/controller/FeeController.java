package com.schoolmanagement.controller;

import com.schoolmanagement.dto.FeeDTO;
import com.schoolmanagement.entity.Fee;
import com.schoolmanagement.entity.FeeStatus;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.service.FeeService;
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
@RequestMapping("/v1/fees")
@AllArgsConstructor
@Tag(name = "Fee Management", description = "Fee management endpoints")
public class FeeController {

    private FeeService feeService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('ACCOUNTANT')")
    @Operation(summary = "Create fee record")
    public ResponseEntity<Fee> createFee(@Valid @RequestBody Fee fee) {
        Fee createdFee = feeService.createFee(fee);
        return new ResponseEntity<>(createdFee, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ACCOUNTANT')")
    @Operation(summary = "Update fee record")
    public ResponseEntity<FeeDTO> updateFee(@PathVariable Long id, @Valid @RequestBody Fee feeDetails) {
        FeeDTO updatedFee = feeService.updateFee(id, feeDetails);
        return new ResponseEntity<>(updatedFee, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT', 'STUDENT', 'PARENT')")
    @Operation(summary = "Get fee record by ID",
            description = "A STUDENT may only fetch their own fees; a PARENT only their own child's (403 otherwise).")
    public ResponseEntity<FeeDTO> getFeeById(@PathVariable Long id, Authentication authentication) {
        FeeDTO fee = feeService.getFeeById(id, (User) authentication.getPrincipal());
        return new ResponseEntity<>(fee, HttpStatus.OK);
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT', 'STUDENT', 'PARENT')")
    @Operation(summary = "Get all fees for a student",
            description = "A STUDENT may only fetch their own fees; a PARENT only their own child's (403 otherwise).")
    public ResponseEntity<List<FeeDTO>> getStudentFees(@PathVariable Long studentId, Authentication authentication) {
        List<FeeDTO> fees = feeService.getStudentFees(studentId, (User) authentication.getPrincipal());
        return new ResponseEntity<>(fees, HttpStatus.OK);
    }

    @GetMapping("/student/{studentId}/year/{academicYear}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT', 'STUDENT', 'PARENT')")
    @Operation(summary = "Get student fees by academic year",
            description = "A STUDENT may only fetch their own fees; a PARENT only their own child's (403 otherwise).")
    public ResponseEntity<List<FeeDTO>> getStudentFeesByYear(
            @PathVariable Long studentId,
            @PathVariable String academicYear,
            Authentication authentication) {
        List<FeeDTO> fees = feeService.getStudentFeesByYear(studentId, academicYear, (User) authentication.getPrincipal());
        return new ResponseEntity<>(fees, HttpStatus.OK);
    }

    @GetMapping("/student/{studentId}/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT', 'STUDENT', 'PARENT')")
    @Operation(summary = "Get pending fees for a student",
            description = "A STUDENT may only fetch their own fees; a PARENT only their own child's (403 otherwise).")
    public ResponseEntity<List<FeeDTO>> getStudentPendingFees(@PathVariable Long studentId, Authentication authentication) {
        List<FeeDTO> fees = feeService.getStudentPendingFees(studentId, (User) authentication.getPrincipal());
        return new ResponseEntity<>(fees, HttpStatus.OK);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ACCOUNTANT')")
    @Operation(summary = "Get fees by status")
    public ResponseEntity<List<FeeDTO>> getFeesByStatus(@PathVariable FeeStatus status) {
        List<FeeDTO> fees = feeService.getFeesByStatus(status);
        return new ResponseEntity<>(fees, HttpStatus.OK);
    }

    @GetMapping("/year/{academicYear}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ACCOUNTANT')")
    @Operation(summary = "Get all fees by academic year",
            description = "Optional page/size query params paginate the result (0-indexed page); omit both to get the full list. Total count is returned in the X-Total-Count header when paginated.")
    public ResponseEntity<List<FeeDTO>> getFeesByAcademicYear(
            @PathVariable String academicYear,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        Pageable pageable = PaginationUtil.toPageable(page, size);
        if (pageable == null) {
            return new ResponseEntity<>(feeService.getFeesByAcademicYear(academicYear), HttpStatus.OK);
        }
        Page<FeeDTO> result = feeService.getFeesByAcademicYear(academicYear, pageable);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(result.getTotalElements()))
                .body(result.getContent());
    }

    @PostMapping("/{feeId}/payment")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT', 'STUDENT', 'PARENT')")
    @Operation(summary = "Process fee payment",
            description = "A STUDENT may only pay their own fees; a PARENT only their own child's (403 otherwise).")
    public ResponseEntity<FeeDTO> processPayment(
            @PathVariable Long feeId,
            @RequestParam Double amount,
            @RequestParam(defaultValue = "ONLINE") String paymentMethod,
            Authentication authentication) {
        FeeDTO updatedFee = feeService.processPayment(feeId, amount, paymentMethod, (User) authentication.getPrincipal());
        return new ResponseEntity<>(updatedFee, HttpStatus.OK);
    }

    @GetMapping("/student/{studentId}/total-dues")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT', 'STUDENT', 'PARENT')")
    @Operation(summary = "Get total dues for a student",
            description = "A STUDENT may only fetch their own total dues; a PARENT only their own child's (403 otherwise).")
    public ResponseEntity<Double> getStudentTotalDues(@PathVariable Long studentId, Authentication authentication) {
        Double totalDues = feeService.getStudentTotalDues(studentId, (User) authentication.getPrincipal());
        return new ResponseEntity<>(totalDues, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ACCOUNTANT')")
    @Operation(summary = "Delete fee record")
    public ResponseEntity<Void> deleteFee(@PathVariable Long id) {
        feeService.deleteFee(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

