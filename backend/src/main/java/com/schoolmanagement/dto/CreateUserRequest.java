package com.schoolmanagement.dto;

import com.schoolmanagement.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Admin-only account creation payload — the only place a caller is allowed
 * to choose a role, and only reachable via POST /v1/users, which requires
 * ROLE_ADMIN (see SecurityConfig / UserController).
 */
@Schema(description = "ADMIN-only account creation request — the only payload that can set a role directly. Requires ROLE_ADMIN.")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "Unique login username", example = "teacher2", minLength = 4, maxLength = 50)
    @NotBlank
    @Size(min = 4, max = 50)
    private String username;

    @Schema(description = "Unique email address", example = "teacher2@school.com")
    @NotBlank
    @Email
    private String email;

    @Schema(description = "Plain-text password (hashed with BCrypt before storage)", example = "Str0ngPassw0rd!", minLength = 8)
    @NotBlank
    @Size(min = 8)
    private String password;

    @Schema(description = "Given name", example = "Tran")
    @NotBlank
    private String firstName;

    @Schema(description = "Family name", example = "Thi B")
    @NotBlank
    private String lastName;

    @Schema(description = "Optional contact phone number", example = "0912345678")
    private String phoneNumber;

    @Schema(description = "Role to grant the new account", example = "TEACHER")
    @NotNull
    private Role role;
}
