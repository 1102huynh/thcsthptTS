package com.schoolmanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * A file uploaded against some other entity (student hồ sơ, staff document,
 * admission application attachment...) — per IMPLEMENTATION_PLAN.md 3.9.
 * ownerType+ownerId is the same polymorphic-owner pattern Notification
 * already uses (targetType+targetId).
 *
 * <p>The file itself lives on local disk (see FileStorageService /
 * app.uploads.dir), named by {@link #storedFileName} — a generated,
 * collision-proof, path-traversal-proof name, deliberately never derived
 * from the client-supplied {@link #fileName}. fileName is metadata only
 * (what to show the user, what to name the file on download) and is never
 * used to build a filesystem path.
 */
@Entity
@Table(name = "document_attachments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", nullable = false)
    private DocumentOwnerType ownerType;

    @NotNull
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @NotBlank
    @Column(name = "file_name", nullable = false)
    private String fileName;

    @NotBlank
    @Column(name = "stored_file_name", nullable = false, unique = true)
    private String storedFileName;

    @NotBlank
    @Column(name = "file_type", nullable = false)
    private String fileType;

    @NotNull
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_id", nullable = false)
    private User uploadedBy;

    @Column(name = "uploaded_at", nullable = false)
    @Builder.Default
    private LocalDateTime uploadedAt = LocalDateTime.now();
}
