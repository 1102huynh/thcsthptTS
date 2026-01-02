package com.schoolmanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * TeacherSpecialization Entity - Represents a teacher's subject specialization
 * Example: Thầy Minh chuyên môn Toán (bộ môn chính, trình độ Giỏi, 10 năm kinh nghiệm)
 */
@Entity
@Table(name = "teacher_specializations",
       uniqueConstraints = @UniqueConstraint(columnNames = {"teacher_id", "subject_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherSpecialization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Staff teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(name = "is_primary")
    private Boolean isPrimary = false;  // Bộ môn chính

    @Column(name = "certification_level", length = 50)
    private String certificationLevel;  // "Giỏi", "Khá", "Trung bình"

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Enums for certification level
    public enum CertificationLevel {
        GIOI("Giỏi", "Excellent"),
        KHA("Khá", "Good"),
        TRUNG_BINH("Trung bình", "Average");

        private final String vietnamese;
        private final String english;

        CertificationLevel(String vietnamese, String english) {
            this.vietnamese = vietnamese;
            this.english = english;
        }

        public String getVietnamese() {
            return vietnamese;
        }

        public String getEnglish() {
            return english;
        }

        public static CertificationLevel fromString(String text) {
            for (CertificationLevel level : CertificationLevel.values()) {
                if (level.vietnamese.equalsIgnoreCase(text)) {
                    return level;
                }
            }
            return TRUNG_BINH;
        }
    }

    // Utility methods
    public String getSpecializationSummary() {
        return String.format("%s - %s (%s, %d years)",
            teacher != null ? teacher.getUser().getFirstName() : "Unknown",
            subject != null ? subject.getSubjectName() : "Unknown",
            certificationLevel != null ? certificationLevel : "N/A",
            yearsOfExperience != null ? yearsOfExperience : 0
        );
    }

    public boolean isPrimarySpecialization() {
        return isPrimary != null && isPrimary;
    }

    public boolean isExperienced() {
        return yearsOfExperience != null && yearsOfExperience >= 5;
    }

    public String getExperienceLevel() {
        if (yearsOfExperience == null || yearsOfExperience == 0) {
            return "Mới vào nghề";
        } else if (yearsOfExperience < 3) {
            return "Còn trẻ";
        } else if (yearsOfExperience < 10) {
            return "Có kinh nghiệm";
        } else {
            return "Dày dặn kinh nghiệm";
        }
    }
}
