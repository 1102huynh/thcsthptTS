package com.schoolmanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Ngưỡng xét lên lớp, scoped by the academic year it starts applying from —
 * ADMIN/PRINCIPAL-configurable (same "no defaults seeded" pattern as
 * GradeComponentConfig in 3.3, for the same reason: the exact cutoffs are a
 * regulation/school-policy detail this codebase must not invent).
 *
 * Compared against per-subject year averages (the "no subject below X"
 * floor), not a single invented "overall average across subjects" — TT22/58
 * define xếp loại per-subject-plus-conditions, not one blended number, and
 * xếp loại học lực itself is deliberately not computed yet (see
 * GradeClassification's Javadoc, 3.3). This threshold is therefore a
 * configurable approximation of the promotion criteria, not the official
 * xếp loại calculation.
 */
@Entity
@Table(name = "promotion_threshold_configs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionThresholdConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Academic year name (e.g. "2025-2026") this threshold starts applying from. */
    @NotBlank
    @Pattern(regexp = "^\\d{4}-\\d{4}$", message = "appliesFrom must look like a school year, e.g. 2025-2026")
    @Column(name = "applies_from", nullable = false, unique = true)
    private String appliesFrom;

    /** No subject's điểm TB năm may fall below this for the student to be considered đạt yêu cầu. */
    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("10.0")
    @Column(name = "min_subject_average", nullable = false)
    private Double minSubjectAverage;

    /** Minimum hạnh kiểm rating required (compared by enum declaration order: TOT best, YEU worst). */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "min_conduct", nullable = false)
    private ConductRating minConduct;

    /** Maximum % of recorded school days absent, for the year, still allowed. */
    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    @Column(name = "max_absence_rate", nullable = false)
    private Double maxAbsenceRate;

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
