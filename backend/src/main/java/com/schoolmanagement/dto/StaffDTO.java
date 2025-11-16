package com.schoolmanagement.dto;

import com.schoolmanagement.entity.EmploymentStatus;
import com.schoolmanagement.entity.StaffPosition;
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
public class StaffDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String employeeId;
    private UserDTO user;
    private StaffPosition position;
    private String department;
    private LocalDate dateOfBirth;
    private LocalDate dateOfJoining;
    private String qualification;
    private String subjectSpecialization;
    private Double salary;
    private EmploymentStatus status;
    private String address;
    private String city;
    private String state;
    private String postalCode;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

