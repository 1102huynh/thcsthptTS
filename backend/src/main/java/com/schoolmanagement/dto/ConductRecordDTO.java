package com.schoolmanagement.dto;

import com.schoolmanagement.entity.ConductRating;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Schema(description = "Đánh giá hạnh kiểm/rèn luyện của một học sinh trong một học kỳ.")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConductRecordDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long studentId;
    private String studentName;
    private Long semesterId;
    private String semesterLabel;
    private ConductRating rating;
    private String remarks;
    private Long evaluatedById;
    private String evaluatedByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
