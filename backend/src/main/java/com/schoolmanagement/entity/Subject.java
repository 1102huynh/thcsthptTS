package com.schoolmanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "subjects")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Short subject code, e.g. "TOAN", "VAN", "ANH". */
    @NotBlank
    @Column(unique = true, nullable = false)
    private String code;

    /** Full Vietnamese name, e.g. "Toán học". */
    @NotBlank
    @Column(nullable = false)
    private String name;

    /**
     * CSV of applicable khối (grade levels 6-12), e.g. "6,7,8,9". Kept as a
     * simple CSV column rather than a child table for now — normalize into a
     * SubjectGradeLevel table later if per-grade-level queries are needed.
     */
    @Column(name = "grade_levels")
    private String gradeLevels;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubjectCategory category;

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
