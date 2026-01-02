package com.schoolmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParentMeetingDTO {
    private Long id;
    private Long parentId;
    private String parentName;
    private Long teacherId;
    private String teacherName;
    private Long studentId;
    private String studentName;
    private LocalDateTime meetingDate;
    private String purpose;
    private String location;
    private String status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

