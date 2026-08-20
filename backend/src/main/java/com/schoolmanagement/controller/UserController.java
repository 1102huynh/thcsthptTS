package com.schoolmanagement.controller;

import com.schoolmanagement.dto.AuthResponse;
import com.schoolmanagement.dto.CreateUserRequest;
import com.schoolmanagement.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/users")
@AllArgsConstructor
@Tag(name = "User Management", description = "Admin-managed user account creation")
public class UserController {

    private AuthenticationService authenticationService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a user account with an explicit role (ADMIN only)")
    public ResponseEntity<AuthResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        AuthResponse response = authenticationService.createUserByAdmin(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
