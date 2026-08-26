package com.schoolmanagement.dto;

import com.schoolmanagement.entity.AdmissionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "A tuyển sinh đầu cấp application.")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdmissionApplicationDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String applicantName;
    private LocalDate dateOfBirth;
    private String contactPhone;
    private Integer desiredGradeLevel;
    private String priorSchool;
    private AdmissionStatus status;
    private LocalDateTime submittedAt;
    private Long reviewedById;
    private String reviewedByName;
    private String note;

    @Schema(description = "The Student id created from this application via POST /{id}/approve-and-create — null until that's happened")
    private Long createdStudentId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
