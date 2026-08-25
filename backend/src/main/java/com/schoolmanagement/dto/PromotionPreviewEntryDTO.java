package com.schoolmanagement.dto;

import com.schoolmanagement.entity.ConductRating;
import com.schoolmanagement.entity.PromotionDecision;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Schema(description = "One student's xét lên lớp preview row — computed live, not yet saved. Nothing here is persisted until POST /v1/promotions/confirm.")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionPreviewEntryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long studentId;
    private String studentName;
    private String rollNumber;

    @Schema(description = "The lowest of the student's per-subject điểm TB năm — null if they have no grade records at all this year")
    private Double lowestSubjectAverage;

    @Schema(description = "The student's HK2 hạnh kiểm rating — null if not yet evaluated")
    private ConductRating conduct;

    @Schema(description = "% of recorded school days present this year — null if no attendance was recorded")
    private Double attendanceRate;

    @Schema(description = "null if no PromotionThresholdConfig applies to this academic year yet")
    private Boolean meetsThresholds;

    @Schema(description = "A suggestion only (LEN_LOP/O_LAI/TOT_NGHIEP) — null if no threshold config applies yet. RA_TRUONG is never suggested; the final decision is always chosen by a human at confirm time.")
    private PromotionDecision suggestedDecision;

    @Schema(description = "Human-readable notes — why thresholds aren't met, or why no threshold config applies yet. Empty when the student meets every threshold.")
    private List<String> reasons;
}
