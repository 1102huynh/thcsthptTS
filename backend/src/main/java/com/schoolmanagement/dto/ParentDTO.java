package com.schoolmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParentDTO {
    private Long id;
    private Long userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String relationToStudent;
    private String occupation;
    private String officeAddress;
    private String annualIncome;
    private List<StudentSummaryDTO> children;
    private Boolean notificationEmailEnabled;
    private Boolean notificationSmsEnabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StudentSummaryDTO {
        private Long id;
        private String rollNumber;
        private String admissionNumber;
        private String firstName;
        private String lastName;
        private String className;
        private String gradeLevel;
    }
}

