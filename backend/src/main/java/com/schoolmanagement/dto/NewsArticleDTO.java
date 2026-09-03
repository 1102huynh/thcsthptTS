package com.schoolmanagement.dto;

import com.schoolmanagement.entity.ContentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * CMS-facing view of a news article (all statuses, all fields). The public
 * portal uses {@link PublicNewsDTO} instead, which omits internal fields.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsArticleDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String title;
    private String slug;
    private String summary;
    private String content;
    private String coverImageUrl;
    private Long categoryId;
    private String categoryName;
    private ContentStatus status;
    private LocalDateTime publishedAt;
    private Boolean isFeatured;
    private Long viewCount;
    private String authorName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
