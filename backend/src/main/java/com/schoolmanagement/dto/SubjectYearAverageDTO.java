package com.schoolmanagement.dto;

import com.schoolmanagement.entity.GradeClassification;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Schema(description = "One subject's điểm trung bình cả năm = (ĐTB HK1 + ĐTB HK2 × 2) / 3.")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubjectYearAverageDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long subjectId;
    private String subjectName;
    private Long academicYearId;
    private String academicYearName;

    @Schema(description = "null if the student has no HK1 grade records for this subject")
    private Double semester1Average;

    @Schema(description = "null if the student has no HK2 grade records for this subject")
    private Double semester2Average;

    @Schema(description = "(HK1 + HK2 × 2) / 3 — null if either semester average is null")
    private Double yearAverage;

    @Schema(description = "Not yet computed — TT22/58 classification thresholds need confirmation from someone with education-domain expertise before this is implemented. See IMPLEMENTATION_PLAN.md 3.3.")
    private GradeClassification classification;
}
