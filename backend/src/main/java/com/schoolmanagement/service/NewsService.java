package com.schoolmanagement.service;

import com.schoolmanagement.dto.NewsArticleDTO;
import com.schoolmanagement.dto.NewsArticleRequest;
import com.schoolmanagement.dto.NewsCategoryDTO;
import com.schoolmanagement.dto.NewsCategoryRequest;
import com.schoolmanagement.dto.PublicNewsDTO;
import com.schoolmanagement.entity.ContentStatus;
import com.schoolmanagement.entity.NewsArticle;
import com.schoolmanagement.entity.NewsCategory;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.repository.NewsArticleRepository;
import com.schoolmanagement.repository.NewsCategoryRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
@Transactional
public class NewsService {

    private NewsArticleRepository articleRepository;
    private NewsCategoryRepository categoryRepository;
    private SlugService slugService;
    private HtmlSanitizerService htmlSanitizer;

    // ================= categories =================

    @Transactional(readOnly = true)
    public List<NewsCategoryDTO> listCategories() {
        return categoryRepository.findAllByOrderByDisplayOrderAscNameAsc()
                .stream().map(this::toCategoryDTO).toList();
    }

    public NewsCategoryDTO createCategory(NewsCategoryRequest request) {
        NewsCategory category = NewsCategory.builder()
                .name(request.getName().trim())
                .slug(slugService.uniqueSlug(request.getName(), categoryRepository::existsBySlug))
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .build();
        return toCategoryDTO(categoryRepository.save(category));
    }

    public NewsCategoryDTO updateCategory(Long id, NewsCategoryRequest request) {
        NewsCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("News category not found with id: " + id));
        category.setName(request.getName().trim());
        if (request.getDisplayOrder() != null) {
            category.setDisplayOrder(request.getDisplayOrder());
        }
        return toCategoryDTO(categoryRepository.save(category));
    }

    public void deleteCategory(Long id) {
        NewsCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("News category not found with id: " + id));
        // articles keep pointing at null (FK ON DELETE SET NULL) - a category
        // going away shouldn't delete its news.
        categoryRepository.delete(category);
    }

    // ================= CMS articles =================

    @Transactional(readOnly = true)
    public Page<NewsArticleDTO> listForCms(Pageable pageable) {
        return articleRepository.findAllForCms(pageable).map(this::toArticleDTO);
    }

    @Transactional(readOnly = true)
    public NewsArticleDTO getForCms(Long id) {
        return toArticleDTO(loadArticle(id));
    }

    public NewsArticleDTO create(NewsArticleRequest request, User author) {
        NewsArticle article = NewsArticle.builder()
                .title(request.getTitle().trim())
                .slug(slugService.uniqueSlug(request.getTitle(), articleRepository::existsBySlug))
                .summary(trimToNull(request.getSummary()))
                .content(htmlSanitizer.sanitize(request.getContent()))
                .coverImageUrl(trimToNull(request.getCoverImageUrl()))
                .category(resolveCategory(request.getCategoryId()))
                .status(ContentStatus.DRAFT)
                .isFeatured(Boolean.TRUE.equals(request.getIsFeatured()))
                .author(author)
                .build();
        return toArticleDTO(articleRepository.save(article));
    }

    public NewsArticleDTO update(Long id, NewsArticleRequest request) {
        NewsArticle article = loadArticle(id);
        // slug is intentionally NOT regenerated on title change - stable URLs.
        article.setTitle(request.getTitle().trim());
        article.setSummary(trimToNull(request.getSummary()));
        article.setContent(htmlSanitizer.sanitize(request.getContent()));
        article.setCoverImageUrl(trimToNull(request.getCoverImageUrl()));
        article.setCategory(resolveCategory(request.getCategoryId()));
        if (request.getIsFeatured() != null) {
            article.setIsFeatured(request.getIsFeatured());
        }
        return toArticleDTO(articleRepository.save(article));
    }

    public NewsArticleDTO publish(Long id) {
        NewsArticle article = loadArticle(id);
        article.setStatus(ContentStatus.PUBLISHED);
        if (article.getPublishedAt() == null) {
            article.setPublishedAt(LocalDateTime.now());
        }
        return toArticleDTO(articleRepository.save(article));
    }

    public NewsArticleDTO unpublish(Long id) {
        NewsArticle article = loadArticle(id);
        article.setStatus(ContentStatus.ARCHIVED);
        return toArticleDTO(articleRepository.save(article));
    }

    public void delete(Long id) {
        articleRepository.delete(loadArticle(id));
    }

    // ================= public =================

    @Transactional(readOnly = true)
    public Page<PublicNewsDTO> listPublished(String categorySlug, Pageable pageable) {
        return articleRepository.findPublished(LocalDateTime.now(), trimToNull(categorySlug), pageable)
                .map(a -> toPublicDTO(a, false));
    }

    public PublicNewsDTO getPublishedBySlug(String slug) {
        NewsArticle article = articleRepository.findPublishedBySlug(slug, LocalDateTime.now())
                .orElseThrow(() -> new ResourceNotFoundException("Bài viết không tồn tại hoặc chưa được đăng"));
        articleRepository.incrementViewCount(article.getId());
        return toPublicDTO(article, true);
    }

    @Transactional(readOnly = true)
    public List<PublicNewsDTO> featured(int limit) {
        return articleRepository.findFeatured(LocalDateTime.now(), PageRequest.of(0, limit))
                .stream().map(a -> toPublicDTO(a, false)).toList();
    }

    @Transactional(readOnly = true)
    public List<PublicNewsDTO> latest(int limit) {
        return articleRepository.findPublished(LocalDateTime.now(), null, PageRequest.of(0, limit))
                .stream().map(a -> toPublicDTO(a, false)).toList();
    }

    // ================= helpers =================

    private NewsArticle loadArticle(Long id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("News article not found with id: " + id));
    }

    private NewsCategory resolveCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("News category not found with id: " + categoryId));
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private NewsCategoryDTO toCategoryDTO(NewsCategory c) {
        return NewsCategoryDTO.builder()
                .id(c.getId()).name(c.getName()).slug(c.getSlug()).displayOrder(c.getDisplayOrder())
                .build();
    }

    private NewsArticleDTO toArticleDTO(NewsArticle a) {
        return NewsArticleDTO.builder()
                .id(a.getId()).title(a.getTitle()).slug(a.getSlug())
                .summary(a.getSummary()).content(a.getContent()).coverImageUrl(a.getCoverImageUrl())
                .categoryId(a.getCategory() != null ? a.getCategory().getId() : null)
                .categoryName(a.getCategory() != null ? a.getCategory().getName() : null)
                .status(a.getStatus()).publishedAt(a.getPublishedAt())
                .isFeatured(a.getIsFeatured()).viewCount(a.getViewCount())
                .authorName(authorName(a.getAuthor()))
                .createdAt(a.getCreatedAt()).updatedAt(a.getUpdatedAt())
                .build();
    }

    private PublicNewsDTO toPublicDTO(NewsArticle a, boolean withContent) {
        return PublicNewsDTO.builder()
                .slug(a.getSlug()).title(a.getTitle()).summary(a.getSummary())
                .content(withContent ? a.getContent() : null)
                .coverImageUrl(a.getCoverImageUrl())
                .categoryName(a.getCategory() != null ? a.getCategory().getName() : null)
                .categorySlug(a.getCategory() != null ? a.getCategory().getSlug() : null)
                .isFeatured(a.getIsFeatured())
                .viewCount(a.getViewCount())
                .publishedAt(a.getPublishedAt())
                .build();
    }

    private static String authorName(User u) {
        if (u == null) {
            return null;
        }
        String name = ((u.getFirstName() == null ? "" : u.getFirstName()) + " "
                + (u.getLastName() == null ? "" : u.getLastName())).trim();
        return name.isEmpty() ? u.getUsername() : name;
    }
}
