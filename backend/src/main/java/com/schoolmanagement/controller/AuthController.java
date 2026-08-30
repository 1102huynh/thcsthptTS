package com.schoolmanagement.controller;

import com.schoolmanagement.dto.AuthRequest;
import com.schoolmanagement.dto.AuthResponse;
import com.schoolmanagement.dto.ForgotPasswordRequest;
import com.schoolmanagement.dto.RegisterRequest;
import com.schoolmanagement.dto.ResetPasswordRequest;
import com.schoolmanagement.service.AuthenticationService;
import com.schoolmanagement.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/auth")
@AllArgsConstructor
@Tag(name = "Authentication", description = "Authentication and authorization endpoints")
public class AuthController {

    private AuthenticationService authenticationService;
    private PasswordResetService passwordResetService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user (always created as STUDENT — an ADMIN must use POST /v1/users to grant any other role)")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authenticationService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate with username/password and receive an access token + refresh token.")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest) {
        AuthResponse response = authenticationService.login(authRequest);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh access token", description = "Exchange a still-valid refresh token for a new access + refresh token pair. Send it as `Authorization: Bearer <refreshToken>`.")
    public ResponseEntity<AuthResponse> refreshToken(@RequestHeader("Authorization") String refreshToken) {
        String token = refreshToken.replace("Bearer ", "");
        AuthResponse response = authenticationService.refreshToken(token);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request a password reset email",
            description = "Public — no login required, rate-limited per IP (see ForgotPasswordRateLimitFilter). "
                    + "Always returns the same generic response whether or not the email matches an account, "
                    + "so this can't be used to enumerate registered emails.")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.forgotPassword(request.getEmail());
        return new ResponseEntity<>(
                Map.of("message", "Nếu email này đã đăng ký, một liên kết đặt lại mật khẩu đã được gửi tới đó."),
                HttpStatus.OK);
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset a password using a forgot-password token",
            description = "Public — no login required. The token is single-use and expires 15 minutes after being issued.")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return new ResponseEntity<>(Map.of("message", "Mật khẩu đã được đặt lại thành công."), HttpStatus.OK);
    }
}

