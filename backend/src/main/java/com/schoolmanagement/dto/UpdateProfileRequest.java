package com.schoolmanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Self-service profile update payload for PUT /v1/users/me — deliberately
 * has no `username`/`role`/`enabled` field, mirroring RegisterRequest: a
 * caller can edit their own contact details but never their own login name
 * or role through this endpoint.
 */
@Schema(description = "Self-service profile update — the caller's own username/role can't be changed here.")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "Given name", example = "Nguyen")
    @NotBlank
    private String firstName;

    @Schema(description = "Family name", example = "Van A")
    @NotBlank
    private String lastName;

    @Schema(description = "Unique email address", example = "student1@school.com")
    @NotBlank
    @Email
    private String email;

    @Schema(description = "Optional contact phone number", example = "0912345678")
    private String phoneNumber;
}
