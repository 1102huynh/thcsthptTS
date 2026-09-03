package com.schoolmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Public view of a news article. Used for both the list ({@code content}
 * null) and the detail page ({@code content} set). No internal fields
 * (status, author id, updatedAt...).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicNewsDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String slug;
    private String title;
    private String summary;
    /** Sanitized HTML - only populated on the detail endpoint. */
    private String content;
    private String coverImageUrl;
    private String categoryName;
    private String categorySlug;
    private Boolean isFeatured;
    private Long viewCount;
    private LocalDateTime publishedAt;
}
