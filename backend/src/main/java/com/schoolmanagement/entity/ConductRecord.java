package com.schoolmanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Đánh giá hạnh kiểm/rèn luyện của một học sinh trong một học kỳ — per
 * IMPLEMENTATION_PLAN.md 3.4. One record per (student, semester): a
 * student's conduct is re-evaluated (not accumulated) each semester.
 */
@Entity
@Table(name = "conduct_records", uniqueConstraints = @UniqueConstraint(
        name = "uk_conduct_student_semester", columnNames = {"student_id", "semester_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConductRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConductRating rating;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    /** The GVCN (or ADMIN) who made this evaluation. */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluated_by_id", nullable = false)
    private Staff evaluatedBy;

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
