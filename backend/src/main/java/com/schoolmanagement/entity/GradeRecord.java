package com.schoolmanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * One điểm số (miệng/15 phút/1 tiết/giữa kỳ/cuối kỳ) on the thang điểm 10,
 * per Thông tư 22/2021 (and TT58-compatible). Supersedes the old
 * {@code Grade} entity's percentage-based model (kept, untouched, at
 * /v1/grades, for Phase 1-2 compatibility) — that model didn't match how
 * Vietnamese schools actually score students; see the cross-cutting
 * principle in IMPLEMENTATION_PLAN.md.
 */
@Entity
@Table(name = "grade_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "component_type", nullable = false)
    private GradeComponentType componentType;

    /** Thang điểm 10 — 0.0 to 10.0. */
    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("10.0")
    @Column(nullable = false)
    private Double score;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Staff teacher;

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
