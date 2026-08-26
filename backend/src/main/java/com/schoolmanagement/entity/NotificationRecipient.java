package com.schoolmanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * One resolved recipient of a {@link Notification} — drives GET
 * /v1/notifications/my (readAt tracks whether that recipient opened it) and
 * per-recipient delivery outcome (deliveredAt/failureReason).
 */
@Entity
@Table(name = "notification_recipients")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRecipient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    /** null until delivery is attempted; set on success, left null (with failureReason set) on failure. */
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    /** null on successful delivery — the sender's error message otherwise (e.g. SMTP connection refused). */
    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
