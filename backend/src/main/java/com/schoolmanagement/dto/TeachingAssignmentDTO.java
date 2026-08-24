package com.schoolmanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Schema(description = "Phân công giảng dạy — which teacher teaches which subject to which class, for one semester.")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeachingAssignmentDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long schoolClassId;

    @Schema(example = "10-A")
    private String schoolClassLabel;
    private Long subjectId;
    private String subjectCode;
    private String subjectName;
    private Long teacherId;
    private String teacherName;
    private Long semesterId;
    private String semesterLabel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
