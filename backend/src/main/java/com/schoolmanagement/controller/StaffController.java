package com.schoolmanagement.controller;

import com.schoolmanagement.dto.StaffDTO;
import com.schoolmanagement.entity.Staff;
import com.schoolmanagement.entity.EmploymentStatus;
import com.schoolmanagement.entity.StaffPosition;
import com.schoolmanagement.service.StaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/staff")
@AllArgsConstructor
@Tag(name = "Staff Management", description = "Staff management endpoints")
public class StaffController {

    private StaffService staffService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRINCIPAL')")
    @Operation(summary = "Create new staff member")
    public ResponseEntity<StaffDTO> createStaff(@RequestBody Staff staff) {
        StaffDTO createdStaff = staffService.createStaff(staff);
        return new ResponseEntity<>(createdStaff, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRINCIPAL')")
    @Operation(summary = "Update staff member")
    public ResponseEntity<StaffDTO> updateStaff(@PathVariable Long id, @RequestBody Staff staffDetails) {
        StaffDTO updatedStaff = staffService.updateStaff(id, staffDetails);
        return new ResponseEntity<>(updatedStaff, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get staff member by ID")
    public ResponseEntity<StaffDTO> getStaffById(@PathVariable Long id) {
        StaffDTO staff = staffService.getStaffById(id);
        return new ResponseEntity<>(staff, HttpStatus.OK);
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get staff member by employee ID")
    public ResponseEntity<StaffDTO> getStaffByEmployeeId(@PathVariable String employeeId) {
        StaffDTO staff = staffService.getStaffByEmployeeId(employeeId);
        return new ResponseEntity<>(staff, HttpStatus.OK);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get all staff members")
    public ResponseEntity<List<StaffDTO>> getAllStaff() {
        List<StaffDTO> staffList = staffService.getAllStaff();
        return new ResponseEntity<>(staffList, HttpStatus.OK);
    }

    @GetMapping("/position/{position}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL')")
    @Operation(summary = "Get staff by position")
    public ResponseEntity<List<StaffDTO>> getStaffByPosition(@PathVariable StaffPosition position) {
        List<StaffDTO> staffList = staffService.getStaffByPosition(position);
        return new ResponseEntity<>(staffList, HttpStatus.OK);
    }

    @GetMapping("/department/{department}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL')")
    @Operation(summary = "Get staff by department")
    public ResponseEntity<List<StaffDTO>> getStaffByDepartment(@PathVariable String department) {
        List<StaffDTO> staffList = staffService.getStaffByDepartment(department);
        return new ResponseEntity<>(staffList, HttpStatus.OK);
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL')")
    @Operation(summary = "Get all active staff members")
    public ResponseEntity<List<StaffDTO>> getActiveStaff() {
        List<StaffDTO> staffList = staffService.getActiveStaff();
        return new ResponseEntity<>(staffList, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRINCIPAL')")
    @Operation(summary = "Delete staff member")
    public ResponseEntity<Void> deleteStaff(@PathVariable Long id) {
        staffService.deleteStaff(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

