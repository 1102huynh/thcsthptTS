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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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
    // address/emergency-contact PII, so any logged-in student could browse
    // the full staff directory including every teacher's salary. Caught in a
    // security review (see KE_HOACH_NANG_CAP_V4.md, Phần G.4 mục 3), not by a
    // bug report.
    //
    // TEACHER is allowed to read the directory (colleagues' names, positions,
    // subjects, contact for coordination) but must NOT see salary or home
    // address / emergency contact - those fields are redacted by
    // redactSensitiveFields() for any caller that isn't ADMIN/PRINCIPAL
    // (KE_HOACH_NANG_CAP_V4.md, Phần H.1 #2).
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER')")
    @Operation(summary = "Get staff member by ID",
            description = "salary, address and emergency-contact fields are returned only to ADMIN/PRINCIPAL; other roles get them as null.")
    public ResponseEntity<StaffDTO> getStaffById(@PathVariable Long id, Authentication authentication) {
        StaffDTO staff = redactSensitiveFields(staffService.getStaffById(id), authentication);
        return new ResponseEntity<>(staff, HttpStatus.OK);
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER')")
    @Operation(summary = "Get staff member by employee ID",
            description = "salary, address and emergency-contact fields are returned only to ADMIN/PRINCIPAL; other roles get them as null.")
    public ResponseEntity<StaffDTO> getStaffByEmployeeId(@PathVariable String employeeId, Authentication authentication) {
        StaffDTO staff = redactSensitiveFields(staffService.getStaffByEmployeeId(employeeId), authentication);
        return new ResponseEntity<>(staff, HttpStatus.OK);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER')")
    @Operation(summary = "Get all staff members",
            description = "Optional page/size query params paginate the result (0-indexed page); omit both to get the full list. Total count is returned in the X-Total-Count header when paginated. "
                    + "salary, address and emergency-contact fields are returned only to ADMIN/PRINCIPAL; other roles get them as null.")
    public ResponseEntity<List<StaffDTO>> getAllStaff(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            Authentication authentication) {
        Pageable pageable = PaginationUtil.toPageable(page, size);
        if (pageable == null) {
            return new ResponseEntity<>(redactSensitiveFields(staffService.getAllStaff(), authentication), HttpStatus.OK);
        }
        Page<StaffDTO> result = staffService.getAllStaff(pageable);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(result.getTotalElements()))
                .body(redactSensitiveFields(result.getContent(), authentication));
    }

    // Per-field redaction the DTO can't do on its own (H.1 #2). ADMIN/PRINCIPAL
    // see the full record; every other role that reaches these endpoints
    // (currently only TEACHER) gets salary + home address + emergency contact
    // blanked out. Mutates the DTO in place - each is a fresh instance built
    // per request by StaffService.mapToDTO, never a shared/cached object.
    private boolean isPrivileged(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String role = authority.getAuthority();
            if ("ROLE_ADMIN".equals(role) || "ROLE_PRINCIPAL".equals(role)) {
                return true;
            }
        }
        return false;
    }

    private StaffDTO redactSensitiveFields(StaffDTO staff, Authentication authentication) {
        if (staff == null || isPrivileged(authentication)) {
            return staff;
        }
        staff.setSalary(null);
        staff.setAddress(null);
        staff.setCity(null);
        staff.setState(null);
        staff.setPostalCode(null);
        staff.setEmergencyContactName(null);
        staff.setEmergencyContactPhone(null);
        return staff;
    }

    private List<StaffDTO> redactSensitiveFields(List<StaffDTO> staff, Authentication authentication) {
        if (isPrivileged(authentication)) {
            return staff;
        }
        staff.forEach(dto -> redactSensitiveFields(dto, authentication));
        return staff;
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

