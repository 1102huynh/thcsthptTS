package com.schoolmanagement.controller;

import com.schoolmanagement.dto.NewsCategoryDTO;
import com.schoolmanagement.dto.PublicHomeDTO;
import com.schoolmanagement.dto.PublicNewsDTO;
import com.schoolmanagement.dto.SchoolEventDTO;
import com.schoolmanagement.service.NewsService;
import com.schoolmanagement.service.PublicPortalService;
import com.schoolmanagement.service.SchoolEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.time.Duration;
import java.util.List;

/**
 * Public, unauthenticated read API for the school portal
 * ({@code /v1/public/**}, permitAll in SecurityConfig). Every endpoint
 * returns only PUBLISHED content whose publish time has passed - DRAFT and
 * ARCHIVED never leave the CMS, even by guessing a slug.
 */
@RestController
@RequestMapping("/v1/public")
@AllArgsConstructor
@Tag(name = "Public portal", description = "Tin tức / sự kiện công khai — không cần đăng nhập")
public class PublicPortalController {

    private static final int MAX_PAGE_SIZE = 50;

    private NewsService newsService;
    private SchoolEventService eventService;
    private PublicPortalService publicPortalService;

    private static Pageable page(Integer page, Integer size, int defaultSize) {
        int p = page != null && page >= 0 ? page : 0;
        int s = size != null && size > 0 ? Math.min(size, MAX_PAGE_SIZE) : defaultSize;
        return PageRequest.of(p, s);
    }

    // Bare array + X-Total-Count header, matching StaffController /
    // StudentController's paginated-list convention (not a Spring Page JSON
    // envelope), plus a short public cache.
    private static <T extends Serializable> ResponseEntity<List<T>> cachedPage(Page<T> body) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(2)).cachePublic())
                .header("X-Total-Count", String.valueOf(body.getTotalElements()))
                .body(body.getContent());
    }

    @GetMapping("/home")
    @Operation(summary = "Dữ liệu trang chủ (tin nổi bật + tin mới + sự kiện sắp tới) trong 1 request")
    public ResponseEntity<PublicHomeDTO> home() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(2)).cachePublic())
                .body(publicPortalService.home());
    }

    @GetMapping("/news")
    @Operation(summary = "Danh sách tin đã đăng (featured trước, rồi mới nhất). Lọc ?category=slug. Tổng số ở header X-Total-Count")
    public ResponseEntity<List<PublicNewsDTO>> news(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return cachedPage(newsService.listPublished(category, page(page, size, 12)));
    }

    @GetMapping("/news/categories")
    @Operation(summary = "Danh sách chuyên mục tin")
    public ResponseEntity<List<NewsCategoryDTO>> newsCategories() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(10)).cachePublic())
                .body(newsService.listCategories());
    }

    @GetMapping("/news/{slug}")
    @Operation(summary = "Chi tiết 1 tin theo slug (tăng lượt xem)")
    public ResponseEntity<PublicNewsDTO> newsDetail(@PathVariable String slug) {
        return new ResponseEntity<>(newsService.getPublishedBySlug(slug), HttpStatus.OK);
    }

    @GetMapping("/events")
    @Operation(summary = "Danh sách sự kiện đã đăng. Lọc ?when=upcoming|past. Tổng số ở header X-Total-Count")
    public ResponseEntity<List<SchoolEventDTO>> events(
            @RequestParam(required = false) String when,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return cachedPage(eventService.listPublished(when, page(page, size, 12)));
    }

    @GetMapping("/events/{slug}")
    @Operation(summary = "Chi tiết sự kiện theo slug")
    public ResponseEntity<SchoolEventDTO> eventDetail(@PathVariable String slug) {
        return new ResponseEntity<>(eventService.getPublishedBySlug(slug), HttpStatus.OK);
    }

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @Operation(summary = "Sitemap động: route tĩnh + mỗi tin/sự kiện đã đăng một URL (P4)")
    public ResponseEntity<String> sitemap() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePublic())
                .body(publicPortalService.sitemapXml());
    }
}
