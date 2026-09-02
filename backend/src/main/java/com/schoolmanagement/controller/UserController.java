package com.schoolmanagement.controller;

import com.schoolmanagement.dto.AuthResponse;
import com.schoolmanagement.dto.CreateUserRequest;
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

@RestController
@RequestMapping("/v1/users")
@AllArgsConstructor
@Tag(name = "User Management", description = "Admin-managed user account creation")
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
}
