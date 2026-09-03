package com.schoolmanagement.controller;

import com.schoolmanagement.dto.MediaAssetDTO;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.service.MediaAssetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * CMS image upload for news/events. Returns {@code id} + public {@code url}
 * ({@code /v1/public/media/{id}}). ADMIN/PRINCIPAL.
 */
@RestController
@RequestMapping("/v1/media")
@AllArgsConstructor
@Tag(name = "News (CMS)", description = "Upload ảnh cho tin/sự kiện — ADMIN/PRINCIPAL")
public class MediaAdminController {

    private MediaAssetService mediaAssetService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL')")
    @Operation(summary = "Upload ảnh (JPEG/PNG/WebP/GIF, ≤10MB)")
    public ResponseEntity<MediaAssetDTO> upload(@RequestParam("file") MultipartFile file,
                                                Authentication authentication) {
        User uploadedBy = (User) authentication.getPrincipal();
        return new ResponseEntity<>(mediaAssetService.upload(file, uploadedBy), HttpStatus.CREATED);
    }
}
