package com.schoolmanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Links a PARENT-role {@link User} to a {@link Student} they're the parent/
 * guardian of — per IMPLEMENTATION_PLAN.md 3.6. Drives both the sổ liên lạc
 * điện tử recipient resolution (Notification, targetType=STUDENT/CLASS) and
 * the "a parent may only see their own child's records" access check
 * (see StudentAccessGuard) on the existing grade/attendance/fee endpoints.
 */
@Entity
@Table(name = "parent_student_relations", uniqueConstraints = @UniqueConstraint(
        name = "uk_parent_student", columnNames = {"parent_id", "student_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParentStudentRelation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Must be a User with role=PARENT — enforced in ParentService, not at the DB/entity level. */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", nullable = false)
    private User parent;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParentRelationship relationship;

    @Column(name = "is_primary_contact", nullable = false)
    @Builder.Default
    private Boolean isPrimaryContact = false;

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
