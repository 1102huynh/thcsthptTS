package com.schoolmanagement.controller;

import com.schoolmanagement.dto.NewsDTO;
import com.schoolmanagement.service.NewsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class NewsController {

    private final NewsService newsService;

    /**
     * Get all published news (Public access)
     * GET /api/news?page=0&size=10
     */
    @GetMapping
    public ResponseEntity<Page<NewsDTO>> getPublishedNews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/news - page: {}, size: {}", page, size);
        Page<NewsDTO> news = newsService.getPublishedNews(page, size);
        return ResponseEntity.ok(news);
    }

    /**
     * Get news by category (Public access)
     * GET /api/news/category/{category}?page=0&size=10
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<Page<NewsDTO>> getNewsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/news/category/{} - page: {}, size: {}", category, page, size);
        Page<NewsDTO> news = newsService.getNewsByCategory(category, page, size);
        return ResponseEntity.ok(news);
    }

    /**
     * Get featured news (Public access)
     * GET /api/news/featured?page=0&size=5
     */
    @GetMapping("/featured")
    public ResponseEntity<Page<NewsDTO>> getFeaturedNews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        log.info("GET /api/news/featured - page: {}, size: {}", page, size);
        Page<NewsDTO> news = newsService.getFeaturedNews(page, size);
        return ResponseEntity.ok(news);
    }

    /**
     * Get recent news (Public access)
     * GET /api/news/recent
     */
    @GetMapping("/recent")
    public ResponseEntity<List<NewsDTO>> getRecentNews() {
        log.info("GET /api/news/recent");
        List<NewsDTO> news = newsService.getRecentNews();
        return ResponseEntity.ok(news);
    }

    /**
     * Search news (Public access)
     * GET /api/news/search?keyword=admission&page=0&size=10
     */
    @GetMapping("/search")
    public ResponseEntity<Page<NewsDTO>> searchNews(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/news/search?keyword={} - page: {}, size: {}", keyword, page, size);
        Page<NewsDTO> news = newsService.searchNews(keyword, page, size);
        return ResponseEntity.ok(news);
    }

    /**
     * Get news by ID (Public access)
     * GET /api/news/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<NewsDTO> getNewsById(@PathVariable Long id) {
        log.info("GET /api/news/{}", id);
        NewsDTO news = newsService.getNewsById(id);
        return ResponseEntity.ok(news);
    }

    /**
     * Get all news including drafts (Admin only)
     * GET /api/news/admin/all?page=0&size=10
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL')")
    public ResponseEntity<Page<NewsDTO>> getAllNewsForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/news/admin/all - page: {}, size: {}", page, size);
        Page<NewsDTO> news = newsService.getAllNewsForAdmin(page, size);
        return ResponseEntity.ok(news);
    }

    /**
     * Create news (Admin only)
     * POST /api/news
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL')")
    public ResponseEntity<NewsDTO> createNews(
            @RequestBody NewsDTO newsDTO,
            Authentication authentication) {
        log.info("POST /api/news - Creating news: {}", newsDTO.getTitle());
        String username = authentication.getName();
        NewsDTO createdNews = newsService.createNews(newsDTO, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdNews);
    }

    /**
     * Update news (Admin only)
     * PUT /api/news/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL')")
    public ResponseEntity<NewsDTO> updateNews(
            @PathVariable Long id,
            @RequestBody NewsDTO newsDTO,
            Authentication authentication) {
        log.info("PUT /api/news/{} - Updating news", id);
        String username = authentication.getName();
        NewsDTO updatedNews = newsService.updateNews(id, newsDTO, username);
        return ResponseEntity.ok(updatedNews);
    }

    /**
     * Delete news (Admin only)
     * DELETE /api/news/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL')")
    public ResponseEntity<Void> deleteNews(
            @PathVariable Long id,
            Authentication authentication) {
        log.info("DELETE /api/news/{} - Deleting news", id);
        String username = authentication.getName();
        newsService.deleteNews(id, username);
        return ResponseEntity.noContent().build();
    }

    /**
     * Publish news (Admin only)
     * PUT /api/news/{id}/publish
     */
    @PutMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL')")
    public ResponseEntity<NewsDTO> publishNews(
            @PathVariable Long id,
            Authentication authentication) {
        log.info("PUT /api/news/{}/publish - Publishing news", id);
        String username = authentication.getName();
        NewsDTO publishedNews = newsService.publishNews(id, username);
        return ResponseEntity.ok(publishedNews);
    }

    /**
     * Archive news (Admin only)
     * PUT /api/news/{id}/archive
     */
    @PutMapping("/{id}/archive")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL')")
    public ResponseEntity<NewsDTO> archiveNews(
            @PathVariable Long id,
            Authentication authentication) {
        log.info("PUT /api/news/{}/archive - Archiving news", id);
        String username = authentication.getName();
        NewsDTO archivedNews = newsService.archiveNews(id, username);
        return ResponseEntity.ok(archivedNews);
    }
}

