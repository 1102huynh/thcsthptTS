package com.schoolmanagement.dto;

import com.schoolmanagement.entity.GradeComponentType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Schema(description = "Một điểm thành phần (miệng/15 phút/1 tiết/giữa kỳ/cuối kỳ) của một học sinh, một môn, một học kỳ — theo Thông tư 22/2021 (thang điểm 10).")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeRecordDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long studentId;
    private String studentName;
    private Long subjectId;
    private String subjectName;
    private Long semesterId;
    private String semesterLabel;
    private GradeComponentType componentType;

    @Schema(example = "8.5", minimum = "0", maximum = "10")
    private Double score;
    private Long teacherId;
    private String teacherName;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
