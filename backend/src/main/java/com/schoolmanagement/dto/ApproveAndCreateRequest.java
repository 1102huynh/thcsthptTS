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
 * The account/student identifiers an approved AdmissionApplication can't
 * supply itself (no login was ever collected from the applicant, and roll
 * number / admission number follow the school's own numbering scheme, not
 * something to invent here) — everything else (name, DOB, phone, priorSchool)
 * is pulled from the application automatically.
 */
@Schema(description = "ADMIN-supplied identifiers to turn an APPROVED application into a real STUDENT account. Name/DOB/phone are taken from the application itself, not repeated here.")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApproveAndCreateRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "Unique login username for the new account", example = "nguyenvana2026")
    @NotBlank
    @Size(min = 4, max = 50)
    private String username;

    @Schema(description = "Unique email address for the new account", example = "nguyenvana2026@school.com")
    @NotBlank
    @Email
    private String email;

    @Schema(description = "Plain-text initial password (hashed with BCrypt before storage) — communicate it to the family out of band", example = "Str0ngPassw0rd!", minLength = 8)
    @NotBlank
    @Size(min = 8)
    private String password;

    @Schema(example = "10A015")
    @NotBlank
    private String rollNumber;

    @Schema(example = "ADM2026015")
    @NotBlank
    private String admissionNumber;
}
