package com.schoolmanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ClassSubjectAssignment Entity - Represents teacher assignment to teach a subject in a class
 * Example: Thầy Minh dạy Toán cho lớp 6A (HK1, 5 tiết/tuần)
 */
@Entity
@Table(name = "class_subject_assignments",
       uniqueConstraints = @UniqueConstraint(
           columnNames = {"class_id", "subject_id", "semester", "academic_year"}
       ))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassSubjectAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private SchoolClass schoolClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Staff teacher;

    @Column(name = "academic_year", nullable = false, length = 20)
    private String academicYear;  // "2024-2025"

    @Column(nullable = false)
    private Integer semester;  // 1 or 2 (HK1 or HK2)

    @Column(name = "periods_per_week")
    private Integer periodsPerWeek;  // Số tiết/tuần

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(length = 20)
    private String status = "ACTIVE";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Utility methods
    public String getAssignmentSummary() {
        return String.format("%s teaches %s in %s (Semester %d, %d periods/week)",
            teacher != null ? teacher.getUser().getFirstName() : "Unknown",
            subject != null ? subject.getSubjectName() : "Unknown",
            schoolClass != null ? schoolClass.getClassName() : "Unknown",
            semester,
            periodsPerWeek != null ? periodsPerWeek : 0
        );
    }

    public boolean isForSemester(int sem) {
        return this.semester != null && this.semester == sem;
    }

    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    public String getSemesterDisplay() {
        return semester != null ? "HK" + semester : "Unknown";
    }

    public Integer getTotalPeriodsInSemester() {
        // Assuming ~18 weeks per semester
        if (periodsPerWeek == null) return 0;
        return periodsPerWeek * 18;
    }
}
