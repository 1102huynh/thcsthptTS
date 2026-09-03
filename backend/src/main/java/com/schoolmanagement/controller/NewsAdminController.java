package com.schoolmanagement.controller;

import com.schoolmanagement.dto.NewsArticleDTO;
import com.schoolmanagement.dto.NewsArticleRequest;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.service.NewsService;
import com.schoolmanagement.util.PaginationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CMS for news articles - ADMIN/PRINCIPAL. Returns all statuses (incl.
 * DRAFT), unlike the public {@code /v1/public/news}. Content is sanitized
 * server-side on every write.
 */
@RestController
@RequestMapping("/v1/news")
@AllArgsConstructor
@Tag(name = "News (CMS)", description = "Quản trị bài tin công khai — ADMIN/PRINCIPAL")
public class NewsAdminController {

    private NewsService newsService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL')")
    @Operation(summary = "Danh sách bài tin (gồm cả DRAFT). page/size để phân trang; bỏ trống = trang đầu 20 bài")
    public ResponseEntity<List<NewsArticleDTO>> list(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        Pageable pageable = PaginationUtil.toPageable(page, size);
        if (pageable == null) {
            pageable = PageRequest.of(0, 20);
        }
        Page<NewsArticleDTO> result = newsService.listForCms(pageable);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(result.getTotalElements()))
                .body(result.getContent());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL')")
    @Operation(summary = "Chi tiết 1 bài tin theo id (gồm cả DRAFT)")
    public ResponseEntity<NewsArticleDTO> get(@PathVariable Long id) {
        return new ResponseEntity<>(newsService.getForCms(id), HttpStatus.OK);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL')")
    @Operation(summary = "Tạo bài tin mới (trạng thái DRAFT)")
    public ResponseEntity<NewsArticleDTO> create(@Valid @RequestBody NewsArticleRequest request,
                                                 Authentication authentication) {
        User author = (User) authentication.getPrincipal();
        return new ResponseEntity<>(newsService.create(request, author), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL')")
    @Operation(summary = "Sửa bài tin (slug giữ nguyên)")
    public ResponseEntity<NewsArticleDTO> update(@PathVariable Long id,
                                                 @Valid @RequestBody NewsArticleRequest request) {
        return new ResponseEntity<>(newsService.update(id, request), HttpStatus.OK);
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL')")
    @Operation(summary = "Đăng bài (status = PUBLISHED, đặt publishedAt lần đầu)")
    public ResponseEntity<NewsArticleDTO> publish(@PathVariable Long id) {
        return new ResponseEntity<>(newsService.publish(id), HttpStatus.OK);
    }

    @PutMapping("/{id}/unpublish")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL')")
    @Operation(summary = "Gỡ bài (status = ARCHIVED)")
    public ResponseEntity<NewsArticleDTO> unpublish(@PathVariable Long id) {
        return new ResponseEntity<>(newsService.unpublish(id), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL')")
    @Operation(summary = "Xoá bài tin")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        newsService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
