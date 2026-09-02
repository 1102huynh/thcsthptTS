package com.schoolmanagement.controller;

import com.schoolmanagement.dto.StaffDTO;
import com.schoolmanagement.entity.Staff;
import com.schoolmanagement.entity.EmploymentStatus;
import com.schoolmanagement.entity.StaffPosition;
import com.schoolmanagement.service.StaffService;
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
    public ResponseEntity<StaffDTO> createStaff(@Valid @RequestBody Staff staff) {
        StaffDTO createdStaff = staffService.createStaff(staff);
        return new ResponseEntity<>(createdStaff, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRINCIPAL')")
    @Operation(summary = "Update staff member")
    public ResponseEntity<StaffDTO> updateStaff(@PathVariable Long id, @Valid @RequestBody Staff staffDetails) {
        StaffDTO updatedStaff = staffService.updateStaff(id, staffDetails);
        return new ResponseEntity<>(updatedStaff, HttpStatus.OK);
    }

    // STUDENT deliberately excluded from all 3 GET endpoints below (was
    // previously included) - StaffDTO carries salary and home
    // address/emergency-contact PII with no per-field redaction, so any
    // logged-in student could browse the full staff directory including
    // every teacher's salary. Caught in a security review (see
    // KE_HOACH_NANG_CAP_V4.md, Phần G.4 mục 3), not by a bug report.
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER')")
    @Operation(summary = "Get staff member by ID")
    public ResponseEntity<StaffDTO> getStaffById(@PathVariable Long id) {
        StaffDTO staff = staffService.getStaffById(id);
        return new ResponseEntity<>(staff, HttpStatus.OK);
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER')")
    @Operation(summary = "Get staff member by employee ID")
    public ResponseEntity<StaffDTO> getStaffByEmployeeId(@PathVariable String employeeId) {
        StaffDTO staff = staffService.getStaffByEmployeeId(employeeId);
        return new ResponseEntity<>(staff, HttpStatus.OK);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER')")
    @Operation(summary = "Get all staff members",
            description = "Optional page/size query params paginate the result (0-indexed page); omit both to get the full list. Total count is returned in the X-Total-Count header when paginated.")
    public ResponseEntity<List<StaffDTO>> getAllStaff(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        Pageable pageable = PaginationUtil.toPageable(page, size);
        if (pageable == null) {
            return new ResponseEntity<>(staffService.getAllStaff(), HttpStatus.OK);
        }
        Page<StaffDTO> result = staffService.getAllStaff(pageable);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(result.getTotalElements()))
                .body(result.getContent());
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

