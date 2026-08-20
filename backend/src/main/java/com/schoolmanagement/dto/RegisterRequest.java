package com.schoolmanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Self-service registration payload. Deliberately has no `role`/`enabled`/`id`
 * field — a client can never request a privileged role this way; the service
 * always creates these accounts as STUDENT. ADMIN must use POST /v1/users to
 * grant any other role.
 */
@Schema(description = "Self-service registration request. Always creates a STUDENT account — there is no `role` field to request otherwise.")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "Unique login username", example = "student1", minLength = 4, maxLength = 50)
    @NotBlank
    @Size(min = 4, max = 50)
    private String username;

    @Schema(description = "Unique email address", example = "student1@school.com")
    @NotBlank
    @Email
    private String email;

    @Schema(description = "Plain-text password (hashed with BCrypt before storage)", example = "Str0ngPassw0rd!", minLength = 8)
    @NotBlank
    @Size(min = 8)
    private String password;

    @Schema(description = "Given name", example = "Nguyen")
    @NotBlank
    private String firstName;

    @Schema(description = "Family name", example = "Van A")
    @NotBlank
    private String lastName;

    @Schema(description = "Optional contact phone number", example = "0912345678")
    private String phoneNumber;
}
