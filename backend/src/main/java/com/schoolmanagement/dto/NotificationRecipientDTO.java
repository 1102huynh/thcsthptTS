package com.schoolmanagement.dto;

import com.schoolmanagement.entity.NotificationChannel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Schema(description = "One notification as it appears to the recipient — GET /v1/notifications/my.")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRecipientDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** The NotificationRecipient row id — pass this to PUT /v1/notifications/{id}/read. */
    private Long id;

    private Long notificationId;
    private String title;
    private String content;
    private NotificationChannel channel;
    private String createdByName;
    private LocalDateTime sentAt;
    private LocalDateTime readAt;
    private LocalDateTime deliveredAt;
    private String failureReason;
}
