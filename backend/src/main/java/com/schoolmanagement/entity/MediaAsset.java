package com.schoolmanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * An image uploaded in the CMS for a news article / event cover or inline
 * use. Bytes live on the filesystem via {@link com.schoolmanagement.service.FileStorageService}
 * (same store as {@link DocumentAttachment}); this row is the metadata and
 * the id in the public URL {@code /v1/public/media/{id}}.
 *
 * <p>Kept separate from {@code DocumentAttachment} on purpose - that entity's
 * {@code owner_type} is a closed enum (STUDENT/STAFF/ADMISSION_APPLICATION)
 * and its endpoints enforce per-owner access checks, neither of which fits a
 * freely-served public image.
 */
@Entity
@Table(name = "media_assets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Client-supplied original filename, metadata only. */
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    /** Server-generated UUID name on disk (see FileStorageService). */
    @Column(name = "stored_file_name", unique = true, nullable = false, length = 255)
    private String storedFileName;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_id")
    private User uploadedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
