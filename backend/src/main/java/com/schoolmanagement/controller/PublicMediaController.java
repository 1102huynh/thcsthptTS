package com.schoolmanagement.controller;

import com.schoolmanagement.entity.MediaAsset;
import com.schoolmanagement.service.MediaAssetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

/**
 * Serves CMS-uploaded images to the public with the right Content-Type and
 * a long cache (read-mostly). Public, no auth.
 */
@RestController
@RequestMapping("/v1/public/media")
@AllArgsConstructor
@Tag(name = "Public portal", description = "Ảnh cho tin/sự kiện — không cần đăng nhập")
public class PublicMediaController {

    private MediaAssetService mediaAssetService;

    @GetMapping("/{id}")
    @Operation(summary = "Tải ảnh theo id")
    public ResponseEntity<Resource> media(@PathVariable Long id) {
        MediaAsset asset = mediaAssetService.getEntity(id);
        Resource body = mediaAssetService.loadBytes(asset);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(asset.getContentType()))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic())
                .body(body);
    }
}
