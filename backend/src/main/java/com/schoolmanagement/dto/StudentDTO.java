package com.schoolmanagement.dto;

import com.schoolmanagement.entity.StudentStatus;
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
public class StudentDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String rollNumber;
    private String admissionNumber;
    private UserDTO user;
    private LocalDate dateOfBirth;
    private String gender;
    private String bloodGroup;
    private String className;
    private String section;
    private LocalDate dateOfAdmission;
    private StudentStatus status;
    private String fatherName;
    private String fatherPhone;
    private String fatherOccupation;
    private String motherName;
    private String motherPhone;
    private String motherOccupation;
    private String address;
    private String city;
    private String state;
    private String postalCode;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String emergencyContactRelation;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

