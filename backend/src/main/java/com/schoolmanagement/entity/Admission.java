package com.schoolmanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "admissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Admission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 100)
    private String grade;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String requirements; // JSON array stored as text

    @Column(nullable = false, length = 500)
    private String eligibility;

    @Column(name = "exam_date", nullable = false)
    private LocalDate examDate;

    @Column(nullable = false)
    private LocalDate deadline;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private AdmissionStatus status = AdmissionStatus.OPEN;

    @Column(nullable = false, length = 100)
    private String seats;

    @Column(name = "class_structure", nullable = false, length = 200)
    private String classStructure;

    @Column(name = "students_per_class", nullable = false, length = 50)
    private String studentsPerClass;

    @Column(nullable = false, length = 100)
    private String contact;

    @Column(name = "academic_year", nullable = false, length = 20)
    private String academicYear;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

