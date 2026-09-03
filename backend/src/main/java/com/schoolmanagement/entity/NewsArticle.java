package com.schoolmanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * A public news / thông báo article.
 *
 * <p>{@code content} is rich-text HTML entered in the CMS - it is run
 * through {@link com.schoolmanagement.service.HtmlSanitizerService} on every
 * write so only an allow-listed tag/attribute set is ever stored, and the
 * public endpoints can serve it directly. {@code slug} is unique and
 * derived from the title (de-duplicated with a numeric suffix).
 *
 * <p>Visible on the public portal only when {@code status == PUBLISHED} and
 * {@code publishedAt <= now}.
 */
@Entity
@Table(name = "news_articles", indexes = {
        @Index(name = "idx_news_articles_status_published_at", columnList = "status, published_at"),
        @Index(name = "idx_news_articles_slug", columnList = "slug"),
        @Index(name = "idx_news_articles_category", columnList = "category_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 255)
    private String title;

    @NotBlank
    @Column(unique = true, nullable = false, length = 280)
    private String slug;

    /** Short plain-text blurb for cards + the SEO/OG description. */
    @Column(length = 500)
    private String summary;

    /** Sanitized rich-text HTML body. */
    @Column(columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private NewsCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ContentStatus status = ContentStatus.DRAFT;

    /** Null until first published; the public gate also checks {@code <= now}. */
    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "is_featured", nullable = false)
    @Builder.Default
    private Boolean isFeatured = false;

    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private Long viewCount = 0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

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
