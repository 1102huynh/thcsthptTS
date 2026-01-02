package com.schoolmanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "staff")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String employeeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StaffPosition position;

    @Column(name = "department")
    private String department;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "date_of_joining")
    private LocalDate dateOfJoining;

    @Column(name = "qualification")
    private String qualification;

    @Column(name = "subject_specialization")
    private String subjectSpecialization;

    @Column(name = "salary")
    private Double salary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EmploymentStatus status = EmploymentStatus.ACTIVE;

    @Column(name = "address")
    private String address;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "emergency_contact_name")
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone")
    private String emergencyContactPhone;

    // Vietnamese Education System - NEW Relationships
    @OneToMany(mappedBy = "homeroomTeacher", fetch = FetchType.LAZY)
    private java.util.List<SchoolClass> homeroomClasses;  // Lớp chủ nhiệm

    @OneToMany(mappedBy = "teacher", fetch = FetchType.LAZY)
    private java.util.List<ClassSubjectAssignment> subjectAssignments;  // Phân công dạy

    @OneToMany(mappedBy = "teacher", fetch = FetchType.LAZY)
    private java.util.List<TeacherSpecialization> specializations;  // Chuyên môn

    @OneToMany(mappedBy = "headTeacher", fetch = FetchType.LAZY)
    private java.util.List<GradeLevel> headedGradeLevels;  // Tổ trưởng khối

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

