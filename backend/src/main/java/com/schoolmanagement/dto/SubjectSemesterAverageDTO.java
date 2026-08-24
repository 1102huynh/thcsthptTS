package com.schoolmanagement.dto;

import com.schoolmanagement.entity.GradeClassification;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Schema(description = "One subject's điểm trung bình môn học kỳ = Σ(score × weight) / Σ(weight).")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubjectSemesterAverageDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long subjectId;
    private String subjectName;
    private Long semesterId;
    private String semesterLabel;

    @Schema(description = "Σ(score × weight) / Σ(weight), null if the student has no grade records for this subject/semester")
    private Double average;

    @Schema(description = "Not yet computed — TT22/58 classification thresholds need confirmation from someone with education-domain expertise before this is implemented. See IMPLEMENTATION_PLAN.md 3.3.")
    private GradeClassification classification;
}
