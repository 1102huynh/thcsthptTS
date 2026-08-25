package com.schoolmanagement.dto;

import com.schoolmanagement.entity.ConductRating;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Schema(description = "Ngưỡng xét lên lớp áp dụng từ một năm học trở đi — ADMIN/PRINCIPAL cấu hình qua /v1/promotion-thresholds.")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionThresholdConfigDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    @Schema(example = "2025-2026")
    private String appliesFrom;

    @Schema(description = "No subject's điểm TB năm may fall below this", example = "5.0")
    private Double minSubjectAverage;

    private ConductRating minConduct;

    @Schema(description = "Max % of recorded school days absent still allowed", example = "20.0")
    private Double maxAbsenceRate;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
