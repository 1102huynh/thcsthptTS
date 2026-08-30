package com.schoolmanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Schema(description = "One recorded sensitive operation.")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long actorId;
    private String actorName;
    private String action;
    private String entityType;
    private Long entityId;
    private LocalDateTime occurredAt;

    @Schema(description = "Free-form JSON context (e.g. old/new values) - null if none was recorded for this event.")
    private String detailJson;
}
