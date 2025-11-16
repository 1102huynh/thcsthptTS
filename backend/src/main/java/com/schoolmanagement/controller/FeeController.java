package com.schoolmanagement.controller;

import com.schoolmanagement.entity.Fee;
import com.schoolmanagement.entity.FeeStatus;
import com.schoolmanagement.service.FeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    public ResponseEntity<Fee> createFee(@RequestBody Fee fee) {
        Fee createdFee = feeService.createFee(fee);
        return new ResponseEntity<>(createdFee, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ACCOUNTANT')")
    @Operation(summary = "Update fee record")
    public ResponseEntity<Fee> updateFee(@PathVariable Long id, @RequestBody Fee feeDetails) {
        Fee updatedFee = feeService.updateFee(id, feeDetails);
        return new ResponseEntity<>(updatedFee, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT', 'STUDENT')")
    @Operation(summary = "Get fee record by ID")
    public ResponseEntity<Fee> getFeeById(@PathVariable Long id) {
        Fee fee = feeService.getFeeById(id);
        return new ResponseEntity<>(fee, HttpStatus.OK);
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT', 'STUDENT')")
    @Operation(summary = "Get all fees for a student")
    public ResponseEntity<List<Fee>> getStudentFees(@PathVariable Long studentId) {
        List<Fee> fees = feeService.getStudentFees(studentId);
        return new ResponseEntity<>(fees, HttpStatus.OK);
    }

    @GetMapping("/student/{studentId}/year/{academicYear}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT', 'STUDENT')")
    @Operation(summary = "Get student fees by academic year")
    public ResponseEntity<List<Fee>> getStudentFeesByYear(
            @PathVariable Long studentId,
            @PathVariable String academicYear) {
        List<Fee> fees = feeService.getStudentFeesByYear(studentId, academicYear);
        return new ResponseEntity<>(fees, HttpStatus.OK);
    }

    @GetMapping("/student/{studentId}/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT', 'STUDENT')")
    @Operation(summary = "Get pending fees for a student")
    public ResponseEntity<List<Fee>> getStudentPendingFees(@PathVariable Long studentId) {
        List<Fee> fees = feeService.getStudentPendingFees(studentId);
        return new ResponseEntity<>(fees, HttpStatus.OK);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ACCOUNTANT')")
    @Operation(summary = "Get fees by status")
    public ResponseEntity<List<Fee>> getFeesByStatus(@PathVariable FeeStatus status) {
        List<Fee> fees = feeService.getFeesByStatus(status);
        return new ResponseEntity<>(fees, HttpStatus.OK);
    }

    @GetMapping("/year/{academicYear}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ACCOUNTANT')")
    @Operation(summary = "Get all fees by academic year")
    public ResponseEntity<List<Fee>> getFeesByAcademicYear(@PathVariable String academicYear) {
        List<Fee> fees = feeService.getFeesByAcademicYear(academicYear);
        return new ResponseEntity<>(fees, HttpStatus.OK);
    }

    @PostMapping("/{feeId}/payment")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT', 'STUDENT')")
    @Operation(summary = "Process fee payment")
    public ResponseEntity<Fee> processPayment(
            @PathVariable Long feeId,
            @RequestParam Double amount,
            @RequestParam(defaultValue = "ONLINE") String paymentMethod) {
        Fee updatedFee = feeService.processPayment(feeId, amount, paymentMethod);
        return new ResponseEntity<>(updatedFee, HttpStatus.OK);
    }

    @GetMapping("/student/{studentId}/total-dues")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT', 'STUDENT')")
    @Operation(summary = "Get total dues for a student")
    public ResponseEntity<Double> getStudentTotalDues(@PathVariable Long studentId) {
        Double totalDues = feeService.getStudentTotalDues(studentId);
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

