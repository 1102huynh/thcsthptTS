package com.schoolmanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * One weekly-recurring period on the thời khoá biểu (timetable) for a
 * {@link TeachingAssignment}. dayOfWeek follows the Vietnamese calendar
 * convention: Thứ Hai (Monday) = 2 ... Thứ Bảy (Saturday) = 7 (no Sunday).
 */
@Entity
@Table(name = "timetable_slots")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimetableSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teaching_assignment_id", nullable = false)
    private TeachingAssignment teachingAssignment;

    /** Thứ Hai (Monday) = 2 ... Thứ Bảy (Saturday) = 7. */
    @NotNull
    @Min(2)
    @Max(7)
    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek;

    /** Tiết học trong ngày, 1-10 (buổi sáng + buổi chiều). */
    @NotNull
    @Min(1)
    @Max(10)
    @Column(nullable = false)
    private Integer period;

    @NotBlank
    @Column(nullable = false)
    private String room;

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
