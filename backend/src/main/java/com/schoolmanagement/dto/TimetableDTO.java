package com.schoolmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimetableDTO {

    private Long id;
    private Long classId;
    private String className;
    private String dayOfWeek;
    private String sessionType; // "MORNING" or "AFTERNOON"
    private Integer timeSlot; // 1-5 for lesson order
    private LocalTime startTime;
    private LocalTime endTime;
    private String subject;
    private String classroom;
    private String academicYear;

    // Subject Teacher Information
    private Long subjectTeacherId;
    private String subjectTeacherName; // First Name + Last Name
    private String subjectTeacherEmail;
    private String subjectTeacherPhone;

    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

