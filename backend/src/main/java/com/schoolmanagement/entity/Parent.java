package com.schoolmanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "parents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Parent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "relation_to_student")
    private String relationToStudent;

    @Column(name = "occupation")
    private String occupation;

    @Column(name = "office_address")
    private String officeAddress;

    @Column(name = "annual_income")
    private String annualIncome;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "parent_student",
        joinColumns = @JoinColumn(name = "parent_id"),
        inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    @Builder.Default
    private List<Student> children = new ArrayList<>();

    @Column(name = "notification_email_enabled")
    @Builder.Default
    private Boolean notificationEmailEnabled = true;

    @Column(name = "notification_sms_enabled")
    @Builder.Default
    private Boolean notificationSmsEnabled = false;

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

