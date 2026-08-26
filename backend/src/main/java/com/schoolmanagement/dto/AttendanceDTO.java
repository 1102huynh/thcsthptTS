package com.schoolmanagement.dto;

import com.schoolmanagement.entity.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long studentId;
    private String studentName;
    private LocalDate attendanceDate;
    private AttendanceStatus status;
    private String remarks;
    private Long markedById;
    private String markedByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
