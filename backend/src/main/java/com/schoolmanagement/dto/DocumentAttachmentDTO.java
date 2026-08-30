package com.schoolmanagement.dto;

import com.schoolmanagement.entity.DocumentOwnerType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Schema(description = "Metadata for one uploaded file. The actual bytes are fetched separately via downloadUrl.")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentAttachmentDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private DocumentOwnerType ownerType;
    private Long ownerId;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private Long uploadedById;
    private String uploadedByName;
    private LocalDateTime uploadedAt;

    @Schema(description = "GET this URL (with auth) to download the file bytes.")
    private String downloadUrl;
}
