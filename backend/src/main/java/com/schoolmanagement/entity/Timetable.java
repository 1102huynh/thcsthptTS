package com.schoolmanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.DayOfWeek;

@Entity
@Table(name = "timetables", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"class_id", "day_of_week", "time_slot", "session_type"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Timetable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private SchoolClass schoolClass;

    @Column(name = "day_of_week", nullable = false)
    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;

    @Column(name = "session_type", nullable = false)
    private String sessionType; // "MORNING" or "AFTERNOON"

    @Column(name = "time_slot", nullable = false)
    private Integer timeSlot; // 1-5 for lesson order

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "classroom", nullable = false)
    private String classroom; // Usually "A" for single classroom

    @Column(name = "academic_year", nullable = false)
    private String academicYear;

    // Subject Teacher - the teacher who teaches this subject
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_teacher_id")
    private Staff subjectTeacher;

    @Column(name = "status")
    private String status; // "ACTIVE", "CANCELLED", "RESCHEDULED"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}

