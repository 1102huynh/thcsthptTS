package com.schoolmanagement.dto;

import com.schoolmanagement.entity.ContentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Serves both the CMS (all fields) and the public portal (the public
 * endpoints only ever return PUBLISHED rows, and {@code status}/
 * {@code publishedAt} are harmless to expose for those).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchoolEventDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String title;
    private String slug;
    /** Sanitized HTML. */
    private String description;
    private String coverImageUrl;
    private String location;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private ContentStatus status;
    private LocalDateTime publishedAt;
    private Boolean isFeatured;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
