package com.schoolmanagement.service;

import com.schoolmanagement.dto.MediaAssetDTO;
import com.schoolmanagement.entity.MediaAsset;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.repository.MediaAssetRepository;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

/**
 * CMS image uploads for the public portal. Stores bytes via
 * {@link FileStorageService} and keeps a {@link MediaAsset} row whose id is
 * the public URL ({@code /v1/public/media/{id}}).
 */
@Service
@AllArgsConstructor
@Transactional
public class MediaAssetService {

    private static final long MAX_BYTES = 10L * 1024 * 1024; // 10MB, matches DocumentController
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif");

    private MediaAssetRepository mediaAssetRepository;
    private FileStorageService fileStorageService;

    public MediaAssetDTO upload(MultipartFile file, User uploadedBy) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Tệp trống");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new IllegalArgumentException("Ảnh vượt quá 10MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Chỉ chấp nhận ảnh JPEG/PNG/WebP/GIF");
        }

        String storedName = fileStorageService.store(file);
        MediaAsset asset = mediaAssetRepository.save(MediaAsset.builder()
                .fileName(file.getOriginalFilename() != null ? file.getOriginalFilename() : storedName)
                .storedFileName(storedName)
                .contentType(contentType)
                .sizeBytes(file.getSize())
                .uploadedBy(uploadedBy)
                .build());
        return toDTO(asset);
    }

    @Transactional(readOnly = true)
    public MediaAsset getEntity(Long id) {
        return mediaAssetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Media asset not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Resource loadBytes(MediaAsset asset) {
        return fileStorageService.load(asset.getStoredFileName());
    }

    private MediaAssetDTO toDTO(MediaAsset a) {
        return MediaAssetDTO.builder()
                .id(a.getId())
                .url("/v1/public/media/" + a.getId())
                .fileName(a.getFileName())
                .contentType(a.getContentType())
                .sizeBytes(a.getSizeBytes())
                .build();
    }
}
