package com.schoolmanagement.dto;

import com.schoolmanagement.entity.ConductRating;
import com.schoolmanagement.entity.PromotionDecision;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "A confirmed xét lên lớp/ở lại/tốt nghiệp decision for one student, one academic year.")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionRecordDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long studentId;
    private String studentName;
    private Long academicYearId;
    private String academicYearName;
    private Double lowestSubjectAverageSnapshot;
    private ConductRating conductSnapshot;
    private Double attendanceRateSnapshot;
    private PromotionDecision decision;
    private LocalDate decisionDate;
    private Long decidedById;
    private String decidedByName;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
