package com.schoolmanagement.controller;

import com.schoolmanagement.dto.AuthResponse;
import com.schoolmanagement.dto.ChangePasswordRequest;
import com.schoolmanagement.dto.CreateUserRequest;
import com.schoolmanagement.dto.UpdateProfileRequest;
import com.schoolmanagement.dto.UserDTO;
import com.schoolmanagement.entity.Role;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.service.AuthenticationService;
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
import java.util.Map;

@RestController
@RequestMapping("/v1/users")
@AllArgsConstructor
@Tag(name = "User Management", description = "Admin-managed user account creation + self-service profile")
public class UserController {

    private AuthenticationService authenticationService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a user account with an explicit role (ADMIN only)")
    public ResponseEntity<AuthResponse> createUser(@Valid @RequestBody CreateUserRequest request, Authentication authentication) {
        User actor = (User) authentication.getPrincipal();
        AuthResponse response = authenticationService.createUserByAdmin(request, actor);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List user accounts by role",
            description = "ADMIN only - added for ParentManagement.jsx to list existing PARENT accounts when linking a child, without needing to create a brand-new parent account per link.")
    public ResponseEntity<List<UserDTO>> getUsersByRole(@RequestParam Role role) {
        return new ResponseEntity<>(authenticationService.getUsersByRole(role), HttpStatus.OK);
    }

    // ---- Self-service ("my profile" / "account settings") — any authenticated
    // role, always acting on the caller's own account (never an id in the URL,
    // so there's no IDOR surface here). Not under /v1/auth/** on purpose: that
    // prefix is permitAll in SecurityConfig, and these must stay behind the
    // default "anyRequest().authenticated()" rule instead.

    @GetMapping("/me")
    @Operation(summary = "Get the logged-in user's own profile")
    public ResponseEntity<UserDTO> getMe(Authentication authentication) {
        User principal = (User) authentication.getPrincipal();
        return new ResponseEntity<>(authenticationService.getCurrentUserProfile(principal), HttpStatus.OK);
    }

    @PutMapping("/me")
    @Operation(summary = "Update the logged-in user's own profile",
            description = "firstName/lastName/email/phoneNumber only — username and role can't be changed here.")
    public ResponseEntity<UserDTO> updateMe(@Valid @RequestBody UpdateProfileRequest request, Authentication authentication) {
        User principal = (User) authentication.getPrincipal();
        return new ResponseEntity<>(authenticationService.updateProfile(principal, request), HttpStatus.OK);
    }

    @PostMapping("/me/change-password")
    @Operation(summary = "Change the logged-in user's own password",
            description = "Requires the current password. For someone who can't log in at all, see POST /v1/auth/forgot-password instead.")
    public ResponseEntity<Map<String, String>> changePassword(@Valid @RequestBody ChangePasswordRequest request, Authentication authentication) {
        User principal = (User) authentication.getPrincipal();
        authenticationService.changePassword(principal, request);
        return new ResponseEntity<>(Map.of("message", "Mật khẩu đã được đổi thành công."), HttpStatus.OK);
    }
}
