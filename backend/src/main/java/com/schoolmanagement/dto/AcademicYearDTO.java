package com.schoolmanagement.dto;

import com.schoolmanagement.entity.AcademicYearStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Năm học — spans roughly September to May of the following calendar year.")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicYearDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    @Schema(example = "2025-2026")
    private String name;

    @Schema(example = "2025-09-01")
    private LocalDate startDate;

    @Schema(example = "2026-05-31")
    private LocalDate endDate;

    private AcademicYearStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
