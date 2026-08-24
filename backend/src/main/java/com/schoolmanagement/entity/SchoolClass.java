package com.schoolmanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "classes", uniqueConstraints = @UniqueConstraint(
        name = "uk_classes_name_section_year",
        columnNames = {"class_name", "section", "academic_year"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchoolClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "class_name", nullable = false)
    private String className;

    @NotBlank
    @Column(name = "section", nullable = false)
    private String section;

    @Positive
    @Column(name = "capacity")
    private Integer capacity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_teacher_id")
    private Staff classTeacher;

    /**
     * @deprecated free-text academic year label, e.g. "2024-2025". Superseded by
     * {@link #academicYearRef}, kept (and still required) so Phase 1-2 code and
     * existing rows keep working during the transition — see Phase 3.1 migration
     * (V3__academic_structure.sql), which backfills {@link #academicYearRef} from
     * this column for every existing class.
     */
    @Deprecated
    @NotBlank
    @Column(name = "academic_year", nullable = false)
    private String academicYear;

    /** The real academic year reference — set this on new/updated classes going forward. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id")
    private AcademicYear academicYearRef;

    /** Khối 6-12 (THCS: 6-9, THPT: 10-12). Optional during the transition — see academicYearRef. */
    @Min(6)
    @Max(12)
    @Column(name = "grade_level")
    private Integer gradeLevel;

    @Column(name = "room_number")
    private String roomNumber;

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

