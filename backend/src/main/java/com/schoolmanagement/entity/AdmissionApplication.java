package com.schoolmanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * A tuyển sinh đầu cấp application — per IMPLEMENTATION_PLAN.md 3.7.
 * Submitted publicly (no login, POST /v1/admissions, rate-limited — see
 * AdmissionRateLimitFilter), reviewed by ADMIN (PUT /v1/admissions/{id}/status),
 * then — once APPROVED — turned into a real User+Student account via
 * POST /v1/admissions/{id}/approve-and-create, without retyping the name/
 * DOB/phone/priorSchool already captured here.
 */
@Entity
@Table(name = "admission_applications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdmissionApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Optimistic lock — guards approve-and-create against a double-click/
     * concurrent-request race: two requests reading the same not-yet-processed
     * application would otherwise both pass the "no student created yet"
     * check and each create their own Student before either commits. A second
     * concurrent save() now fails fast with ObjectOptimisticLockingFailureException
     * (mapped to 409) instead of silently creating two accounts.
     */
    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;

    @NotBlank
    @Size(max = 255)
    @Column(name = "applicant_name", nullable = false)
    private String applicantName;

    @NotNull
    @Past
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    /** Vietnamese phone number — mobile (0/+84 + 9 digits) or landline. */
    @NotBlank
    @Pattern(regexp = "^(\\+84|0)[0-9]{9,10}$", message = "contactPhone must be a valid Vietnamese phone number, e.g. 0912345678")
    @Column(name = "contact_phone", nullable = false)
    private String contactPhone;

    /** Khối 6-12 (THCS: 6-9, THPT: 10-12) — same range constraint as SchoolClass.gradeLevel. */
    @NotNull
    @Min(6)
    @Max(12)
    @Column(name = "desired_grade_level", nullable = false)
    private Integer desiredGradeLevel;

    @Size(max = 255)
    @Column(name = "prior_school")
    private String priorSchool;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AdmissionStatus status = AdmissionStatus.PENDING;

    /** Always set server-side at submission time — never trust a client-supplied value. */
    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    /** Set when an ADMIN changes the status away from PENDING; null until then. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_id")
    private User reviewedBy;

    /** Reviewer's note (reason for rejection, follow-up needed, ...). */
    @Column(columnDefinition = "TEXT")
    private String note;

    /**
     * Set once POST /v1/admissions/{id}/approve-and-create has run for this
     * application, so it can't be processed into a second Student by mistake.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_student_id")
    private Student createdStudent;

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
