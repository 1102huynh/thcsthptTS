package com.schoolmanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Self-service password change for a logged-in user (POST
 * /v1/users/me/change-password) — distinct from the forgot/reset-password
 * flow (PasswordResetService), which is for someone who can't log in at all.
 * This one requires proving the current password first.
 */
@Schema(description = "Logged-in self-service password change — requires the current password.")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangePasswordRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank
    private String currentPassword;

    @Schema(example = "N3wStr0ngPassw0rd!", minLength = 8)
    @NotBlank
    @Size(min = 8)
    private String newPassword;
}
