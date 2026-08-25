package com.schoolmanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Hệ số tính điểm trung bình cho một {@link GradeComponentType} (thường 1,
 * 2, hoặc 3 theo TT22/58), scoped by the academic year it starts applying
 * from — so a future regulation change doesn't require a code change, just
 * a new config row.
 */
@Entity
@Table(name = "grade_component_configs", uniqueConstraints = @UniqueConstraint(
        name = "uk_grade_component_config_type_applies_from",
        columnNames = {"component_type", "applies_from"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeComponentConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "component_type", nullable = false)
    private GradeComponentType componentType;

    @Positive
    @Column(nullable = false)
    private Integer weight;

    /**
     * Academic year name (e.g. "2025-2026") this weight starts applying from.
     * Must be "YYYY-YYYY" — {@link com.schoolmanagement.service.GradeRecordService}
     * parses the leading year out of this same format on both this field and
     * {@code Semester.academicYear.name} to pick the weight in effect for a given
     * semester, so a malformed value here would only surface later as a confusing
     * error on an unrelated student/teacher request.
     */
    @NotBlank
    @Pattern(regexp = "^\\d{4}-\\d{4}$", message = "appliesFrom must look like a school year, e.g. 2025-2026")
    @Column(name = "applies_from", nullable = false)
    private String appliesFrom;

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
