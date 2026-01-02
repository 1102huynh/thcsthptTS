package com.schoolmanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Subject Entity - Represents a subject (Môn học) taught in school
 * Examples: Toán học (Mathematics), Ngữ văn (Literature), Tiếng Anh (English)
 */
@Entity
@Table(name = "subjects")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "subject_code", nullable = false, unique = true, length = 20)
    private String subjectCode;  // "TOAN", "VAN", "ANH", etc.

    @Column(name = "subject_name", nullable = false, length = 100)
    private String subjectName;  // "Toán học", "Ngữ văn", etc.

    @Column(name = "subject_name_en", length = 100)
    private String subjectNameEn;  // "Mathematics", "Literature"

    @Column(name = "school_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private SchoolType schoolType;  // THCS, THPT, or BOTH

    @Column(length = 50)
    private String category;  // "Khoa học tự nhiên", "Xã hội", etc.

    @Column(name = "total_periods_per_week")
    private Integer totalPeriodsPerWeek;  // Default periods per week

    @Column(precision = 3, scale = 1)
    private BigDecimal coefficient = BigDecimal.ONE;  // Hệ số môn học (1.0, 2.0)

    @Column(name = "is_required")
    private Boolean isRequired = true;  // Môn bắt buộc

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
    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ClassSubjectAssignment> classAssignments;

    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TeacherSpecialization> teacherSpecializations;

    // Enums
    public enum SchoolType {
        THCS("Cấp 2 - THCS"),
        THPT("Cấp 3 - THPT"),
        BOTH("Cả 2 cấp");

        private final String description;

        SchoolType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum SubjectCategory {
        KHOA_HOC_TU_NHIEN("Khoa học tự nhiên"),
        XA_HOI("Xã hội"),
        NGOAI_NGU("Ngoại ngữ"),
        NGHE_THUAT("Nghệ thuật"),
        CONG_NGHE("Công nghệ"),
        KHAC("Khác");

        private final String vietnameseName;

        SubjectCategory(String vietnameseName) {
            this.vietnameseName = vietnameseName;
        }

        public String getVietnameseName() {
            return vietnameseName;
        }
    }

    // Utility methods
    public String getDisplayName() {
        return subjectName + " (" + subjectCode + ")";
    }

    public boolean isForMiddleSchool() {
        return schoolType == SchoolType.THCS || schoolType == SchoolType.BOTH;
    }

    public boolean isForHighSchool() {
        return schoolType == SchoolType.THPT || schoolType == SchoolType.BOTH;
    }

    public boolean isOptional() {
        return !isRequired;
    }

    public double getCoefficientValue() {
        return coefficient != null ? coefficient.doubleValue() : 1.0;
    }
}
