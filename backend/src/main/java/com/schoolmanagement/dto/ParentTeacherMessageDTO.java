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
public class ParentTeacherMessageDTO {
    private Long id;
    private Long parentId;
    private String parentName;
    private Long teacherId;
    private String teacherName;
    private Long studentId;
    private String studentName;
    private String subject;
    private String message;
    private Long senderId;
    private String senderName;
    private Boolean isRead;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
}

