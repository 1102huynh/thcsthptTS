package com.schoolmanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * One record of a sensitive operation, per IMPLEMENTATION_PLAN.md 3.9 —
 * written manually (via AuditLogService.log) at the specific call sites that
 * actually need a trail (sửa/xóa điểm, xóa học sinh, duyệt tuyển sinh, cấp
 * quyền, đặt lại mật khẩu), not via a blanket AOP interceptor around every
 * create/update/delete in the app. See AuditLogService's Javadoc for the
 * full list of instrumented call sites and the reasoning for this scope.
 */
@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = false)
    private User actor;

    @NotBlank
    @Column(nullable = false)
    private String action;

    @NotBlank
    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "occurred_at", nullable = false)
    @Builder.Default
    private LocalDateTime occurredAt = LocalDateTime.now();

    @Column(name = "detail_json", columnDefinition = "TEXT")
    private String detailJson;
}
