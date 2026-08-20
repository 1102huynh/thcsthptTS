package com.schoolmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long studentId;
    private String studentName;
    private String subject;
    private String examType;
    private Double marksObtained;
    private Double totalMarks;
    private Double percentage;
    private String grade;
    private Long teacherId;
    private String teacherName;
    private String academicYear;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
