package com.schoolmanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * SchoolClass Entity - Represents a class (Lớp học) in school
 * Examples: 6A, 6B, 10A1, 10A2, etc.
 */
@Entity
@Table(name = "classes",
       uniqueConstraints = @UniqueConstraint(columnNames = {"class_name", "academic_year"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "students", "subjectAssignments"})
public class SchoolClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)  // Changed to EAGER - we always need grade level info
    @JoinColumn(name = "grade_level_id", nullable = false)
    private GradeLevel gradeLevel;

    @Column(name = "class_name", nullable = false, length = 50)
    private String className;  // "6A", "6B", "10A1", etc.

    @Column(name = "full_name", length = 100)
    private String fullName;  // "Lớp 6A", "Lớp 10A1"

    @ManyToOne(fetch = FetchType.EAGER)  // Changed to EAGER - we need teacher info
    @JoinColumn(name = "homeroom_teacher_id")
    private Staff homeroomTeacher;  // GVCN (Giáo viên chủ nhiệm)

    @Column(name = "academic_year", nullable = false, length = 20)
    private String academicYear;  // "2024-2025"

    @Column(name = "max_students")
    private Integer maxStudents = 40;

    @Column(name = "current_students")
    private Integer currentStudents = 0;

    @Column(name = "room_number", length = 20)
    private String roomNumber;  // "A101", "B205"

    @Column(length = 20)
    private String status = "ACTIVE";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "schoolClass", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Student> students;

    @OneToMany(mappedBy = "schoolClass", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ClassSubjectAssignment> subjectAssignments;

    // Utility methods
    public String getDisplayName() {
        return fullName != null ? fullName : ("Lớp " + className);
    }

    public boolean isFull() {
        return currentStudents != null && maxStudents != null && currentStudents >= maxStudents;
    }

    public boolean canAcceptMoreStudents() {
        return !isFull();
    }

    public int getAvailableSlots() {
        if (maxStudents == null || currentStudents == null) {
            return 0;
        }
        return Math.max(0, maxStudents - currentStudents);
    }

    public double getOccupancyRate() {
        if (maxStudents == null || maxStudents == 0) {
            return 0.0;
        }
        return (currentStudents != null ? currentStudents : 0) * 100.0 / maxStudents;
    }

    // Pre-persist: Auto-set fullName
    @PrePersist
    @PreUpdate
    public void prePersist() {
        if (this.fullName == null || this.fullName.isEmpty()) {
            this.fullName = "Lớp " + this.className;
        }
    }
}
