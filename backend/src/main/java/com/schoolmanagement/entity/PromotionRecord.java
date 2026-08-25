package com.schoolmanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * A recorded xét lên lớp/ở lại/tốt nghiệp decision for one student, one
 * academic year — per IMPLEMENTATION_PLAN.md 3.5. One per (student,
 * academicYear); confirming again overwrites the previous decision
 * ("hỗ trợ ghi đè hàng loạt" per the plan).
 *
 * The *Snapshot fields freeze the data the decision was based on at
 * confirm time — later grade/conduct/attendance corrections must not
 * silently change a decision already made.
 */
@Entity
@Table(name = "promotion_records", uniqueConstraints = @UniqueConstraint(
        name = "uk_promotion_student_year", columnNames = {"student_id", "academic_year_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;

    /** null if the student had no grade records at all for the year. */
    @Column(name = "lowest_subject_average_snapshot")
    private Double lowestSubjectAverageSnapshot;

    /** null if the student had no HK2 conduct record. */
    @Enumerated(EnumType.STRING)
    @Column(name = "conduct_snapshot")
    private ConductRating conductSnapshot;

    /** null if the student had no attendance records for the year. Percentage present, 0-100. */
    @Column(name = "attendance_rate_snapshot")
    private Double attendanceRateSnapshot;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PromotionDecision decision;

    /**
     * Always set server-side to today's date at confirm time (see
     * PromotionService) — never trust a client-supplied decision date, so no
     * {@code @NotNull} here: the confirm request doesn't need to (and can't
     * usefully) provide one.
     */
    @Column(name = "decision_date", nullable = false)
    private LocalDate decisionDate;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decided_by_id", nullable = false)
    private Staff decidedBy;

    @Column(columnDefinition = "TEXT")
    private String remarks;

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
