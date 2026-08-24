package com.schoolmanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Schema(description = "One weekly-recurring period on the thời khoá biểu (timetable).")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimetableSlotDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long teachingAssignmentId;
    private Long schoolClassId;

    @Schema(example = "10-A")
    private String schoolClassLabel;

    private Long subjectId;
    private String subjectName;
    private Long teacherId;
    private String teacherName;

    @Schema(description = "Thứ Hai (Monday) = 2 ... Thứ Bảy (Saturday) = 7", example = "2")
    private Integer dayOfWeek;

    @Schema(description = "Tiết học trong ngày, 1-10", example = "1")
    private Integer period;

    @Schema(example = "P.101")
    private String room;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
