package com.schoolmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * CMS create/update payload for a news article. {@code slug} is generated
 * from the title on create and left unchanged on update (stable URLs);
 * {@code status}/{@code publishedAt} are controlled only by the
 * publish/unpublish endpoints; {@code content} is sanitized server-side.
 */
@Data
public class NewsArticleRequest {

    @NotBlank
    @Size(max = 255)
    private String title;

    @Size(max = 500)
    private String summary;

    /** Rich-text HTML; sanitized (allow-list) before it is stored. */
    private String content;

    @Size(max = 500)
    private String coverImageUrl;

    private Long categoryId;

    private Boolean isFeatured;
}
