package com.schoolmanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Schema(description = "The account created from an approved AdmissionApplication.")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdmissionApprovalResultDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long applicationId;
    private Long userId;
    private String username;
    private Long studentId;
    private String rollNumber;
    private String admissionNumber;
}
