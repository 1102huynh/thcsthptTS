package com.schoolmanagement.entity;

/**
 * Publish state shared by {@link NewsArticle} and {@link SchoolEvent}.
 * The public portal ({@code /v1/public/**}) only ever returns
 * {@code PUBLISHED} rows whose {@code publishedAt <= now}; DRAFT and
 * ARCHIVED never leave the CMS.
 */
public enum ContentStatus {
    DRAFT,
    PUBLISHED,
    ARCHIVED
}
