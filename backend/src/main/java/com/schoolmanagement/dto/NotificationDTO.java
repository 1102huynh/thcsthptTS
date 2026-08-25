package com.schoolmanagement.dto;

import com.schoolmanagement.entity.NotificationChannel;
import com.schoolmanagement.entity.NotificationStatus;
import com.schoolmanagement.entity.NotificationTargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Schema(description = "A sổ liên lạc điện tử message — created and sent in the same request.")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String title;
    private String content;
    private NotificationTargetType targetType;
    private Long targetId;
    private NotificationChannel channel;
    private Long createdById;
    private String createdByName;
    private LocalDateTime sentAt;
    private NotificationStatus status;

    @Schema(description = "How many recipients were resolved for this notification")
    private Integer recipientCount;

    @Schema(description = "Of recipientCount, how many the sender reported as delivered successfully")
    private Integer deliveredCount;

    private LocalDateTime createdAt;
}
