package com.schoolmanagement.dto;

import com.schoolmanagement.entity.GradeComponentType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Schema(description = "Hệ số (weight) áp dụng cho một loại điểm thành phần, có hiệu lực từ một năm học trở đi — ADMIN cấu hình qua /v1/grade-config.")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeComponentConfigDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private GradeComponentType componentType;

    @Schema(example = "3", description = "Hệ số nhân với điểm khi tính điểm trung bình môn")
    private Integer weight;

    @Schema(example = "2024-2025", description = "Năm học bắt đầu áp dụng hệ số này — dùng hệ số có applies_from mới nhất <= năm học đang tính")
    private String appliesFrom;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
