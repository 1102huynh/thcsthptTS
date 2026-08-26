package com.schoolmanagement.dto;

import com.schoolmanagement.entity.AdmissionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Schema(description = "ADMIN request to move an application to a new status, optionally with a reviewer note.")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateAdmissionStatusRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(example = "REVIEWING")
    @NotNull
    private AdmissionStatus status;

    @Schema(description = "Optional — e.g. the reason for a REJECTED decision", example = "Thiếu giấy khai sinh")
    private String note;
}
