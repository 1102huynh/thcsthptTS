package com.schoolmanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * GradeLevel Entity - Represents a grade level (Khối lớp) in Vietnamese education system
 * Examples: Khối 6, Khối 7, Khối 10, Khối 11, etc.
 */
@Entity
@Table(name = "grade_levels", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"level_number", "academic_year"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GradeLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "level_number", nullable = false)
    private Integer levelNumber;  // 6, 7, 8, 9, 10, 11, 12

    @Column(name = "level_name", nullable = false, length = 50)
    private String levelName;  // "Khối 6", "Khối 7", etc.

    @Column(name = "school_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private SchoolType schoolType;  // THCS or THPT

    @Column(name = "academic_year", nullable = false, length = 20)
    private String academicYear;  // "2024-2025"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "head_teacher_id")
    private Staff headTeacher;  // Tổ trưởng khối (optional)

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 20)
    private String status = "ACTIVE";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "gradeLevel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SchoolClass> classes;

    @OneToMany(mappedBy = "gradeLevel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Student> students;

    // Utility methods
    public String getFullName() {
        return levelName + " (" + schoolType + ")";
    }

    public boolean isMiddleSchool() {
        return schoolType == SchoolType.THCS;
    }

    public boolean isHighSchool() {
        return schoolType == SchoolType.THPT;
    }

    public enum SchoolType {
        THCS("Trung học cơ sở"),
        THPT("Trung học phổ thông");

        private final String vietnameseName;

        SchoolType(String vietnameseName) {
            this.vietnameseName = vietnameseName;
        }

        public String getVietnameseName() {
            return vietnameseName;
        }
    }
}
