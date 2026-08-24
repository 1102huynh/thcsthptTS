package com.schoolmanagement.dto;

import com.schoolmanagement.entity.SemesterName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Học kỳ 1 (HK1) or Học kỳ 2 (HK2) within an academic year.")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SemesterDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long academicYearId;
    private String academicYearName;
    private SemesterName name;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
