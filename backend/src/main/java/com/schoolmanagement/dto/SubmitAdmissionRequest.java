package com.schoolmanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Public submission payload for POST /v1/admissions — deliberately has no
 * id/status/submittedAt/reviewedBy/createdStudent field, unlike the
 * AdmissionApplication entity itself, so an anonymous caller has no channel
 * to set any of those (mirrors RegisterRequest's same reasoning for the
 * other public, unauthenticated endpoint in this API).
 */
@Schema(description = "Public tuyển sinh đầu cấp submission — no login required.")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitAdmissionRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(example = "Nguyễn Văn An")
    @NotBlank
    @Size(max = 255)
    private String applicantName;

    @Schema(example = "2014-05-20")
    @NotNull
    @Past
    private LocalDate dateOfBirth;

    @Schema(example = "0912345678")
    @NotBlank
    @Pattern(regexp = "^(\\+84|0)[0-9]{9,10}$", message = "contactPhone must be a valid Vietnamese phone number, e.g. 0912345678")
    private String contactPhone;

    @Schema(description = "Khối 6-12 (THCS: 6-9, THPT: 10-12)", example = "10")
    @NotNull
    @Min(6)
    @Max(12)
    private Integer desiredGradeLevel;

    @Schema(example = "THCS Nguyễn Du")
    @Size(max = 255)
    private String priorSchool;
}
