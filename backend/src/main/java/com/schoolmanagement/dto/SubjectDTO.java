package com.schoolmanagement.dto;

import com.schoolmanagement.entity.SubjectCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Schema(description = "Môn học — a subject taught at one or more khối (grade levels).")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubjectDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    @Schema(example = "TOAN")
    private String code;

    @Schema(example = "Toán học")
    private String name;

    @Schema(description = "CSV of applicable khối 6-12", example = "6,7,8,9,10,11,12")
    private String gradeLevels;
    private SubjectCategory category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
