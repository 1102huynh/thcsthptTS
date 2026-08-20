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
public class SchoolClassDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String className;
    private String section;
    private Integer capacity;
    private Long classTeacherId;
    private String classTeacherName;
    private String academicYear;
    private String roomNumber;
    private Integer studentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
