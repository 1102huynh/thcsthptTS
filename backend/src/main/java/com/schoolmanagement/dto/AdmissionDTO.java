package com.schoolmanagement.dto;

import com.schoolmanagement.entity.AdmissionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdmissionDTO {
    private Long id;
    private String title;
    private String grade;
    private String description;
    private List<String> requirements;
    private String eligibility;
    private LocalDate examDate;
    private LocalDate deadline;
    private AdmissionStatus status;
    private String seats;
    private String classStructure;
    private String studentsPerClass;
    private String contact;
    private String academicYear;
    private Integer displayOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

