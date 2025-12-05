package com.schoolmanagement.service;

import com.schoolmanagement.dto.NewsDTO;
import com.schoolmanagement.entity.News;
import com.schoolmanagement.entity.NewsStatus;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.exception.UnauthorizedException;
import com.schoolmanagement.repository.NewsRepository;
import com.schoolmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsService {

    private final NewsRepository newsRepository;
    private final UserRepository userRepository;

    /**
     * Get all published news with pagination
     */
    @Transactional(readOnly = true)
    public Page<NewsDTO> getPublishedNews(int page, int size) {
        log.info("Fetching published news - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<News> newsPage = newsRepository.findByStatusOrderByPublishedDateDesc(NewsStatus.PUBLISHED, pageable);
        return newsPage.map(this::convertToDTO);
    }

    /**
     * Get published news by category
     */
    @Transactional(readOnly = true)
    public Page<NewsDTO> getNewsByCategory(String category, int page, int size) {
        log.info("Fetching news by category: {} - page: {}, size: {}", category, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<News> newsPage = newsRepository.findByStatusAndCategoryOrderByPublishedDateDesc(
                NewsStatus.PUBLISHED, category, pageable);
        return newsPage.map(this::convertToDTO);
    }

    /**
     * Get featured news
     */
    @Transactional(readOnly = true)
    public Page<NewsDTO> getFeaturedNews(int page, int size) {
        log.info("Fetching featured news - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<News> newsPage = newsRepository.findByStatusAndFeaturedOrderByPublishedDateDesc(
                NewsStatus.PUBLISHED, true, pageable);
        return newsPage.map(this::convertToDTO);
    }

    /**
     * Get recent news (top 5)
     */
    @Transactional(readOnly = true)
    public List<NewsDTO> getRecentNews() {
        log.info("Fetching recent news (top 5)");
        List<News> newsList = newsRepository.findTop5ByStatusOrderByPublishedDateDesc(NewsStatus.PUBLISHED);
        return newsList.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * Search published news
     */
    @Transactional(readOnly = true)
    public Page<NewsDTO> searchNews(String keyword, int page, int size) {
        log.info("Searching news with keyword: {} - page: {}, size: {}", keyword, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<News> newsPage = newsRepository.searchPublishedNews(NewsStatus.PUBLISHED, keyword, pageable);
        return newsPage.map(this::convertToDTO);
    }

    /**
     * Get news by ID (public)
     */
    @Transactional
    public NewsDTO getNewsById(Long id) {
        log.info("Fetching news by id: {}", id);
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("News not found with id: " + id));

        // Increment view count
        news.setViewCount(news.getViewCount() + 1);
        newsRepository.save(news);

        return convertToDTO(news);
    }

    /**
     * Get all news for admin (including drafts and archived)
     */
    @Transactional(readOnly = true)
    public Page<NewsDTO> getAllNewsForAdmin(int page, int size) {
        log.info("Fetching all news for admin - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<News> newsPage = newsRepository.findAllByOrderByCreatedAtDesc(pageable);
        return newsPage.map(this::convertToDTO);
    }

    /**
     * Create news (Admin only)
     */
    @Transactional
    public NewsDTO createNews(NewsDTO newsDTO, String username) {
        log.info("Creating news: {} by user: {}", newsDTO.getTitle(), username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        News news = News.builder()
                .title(newsDTO.getTitle())
                .content(newsDTO.getContent())
                .publishedDate(newsDTO.getPublishedDate() != null ? newsDTO.getPublishedDate() : LocalDateTime.now())
                .category(newsDTO.getCategory())
                .image(newsDTO.getImage())
                .status(newsDTO.getStatus() != null ? newsDTO.getStatus() : NewsStatus.DRAFT)
                .featured(newsDTO.getFeatured() != null ? newsDTO.getFeatured() : false)
                .authorName(newsDTO.getAuthorName() != null ? newsDTO.getAuthorName() : user.getFirstName() + " " + user.getLastName())
                .createdBy(user)
                .viewCount(0)
                .build();

        News savedNews = newsRepository.save(news);
        log.info("News created successfully with id: {}", savedNews.getId());
        return convertToDTO(savedNews);
    }

    /**
     * Update news (Admin only)
     */
    @Transactional
    public NewsDTO updateNews(Long id, NewsDTO newsDTO, String username) {
        log.info("Updating news id: {} by user: {}", id, username);

        News news = newsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("News not found with id: " + id));

        news.setTitle(newsDTO.getTitle());
        news.setContent(newsDTO.getContent());
        news.setPublishedDate(newsDTO.getPublishedDate());
        news.setCategory(newsDTO.getCategory());
        news.setImage(newsDTO.getImage());
        news.setStatus(newsDTO.getStatus());
        news.setFeatured(newsDTO.getFeatured());
        if (newsDTO.getAuthorName() != null) {
            news.setAuthorName(newsDTO.getAuthorName());
        }

        News updatedNews = newsRepository.save(news);
        log.info("News updated successfully with id: {}", updatedNews.getId());
        return convertToDTO(updatedNews);
    }

    /**
     * Delete news (Admin only)
     */
    @Transactional
    public void deleteNews(Long id, String username) {
        log.info("Deleting news id: {} by user: {}", id, username);

        News news = newsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("News not found with id: " + id));

        newsRepository.delete(news);
        log.info("News deleted successfully with id: {}", id);
    }

    /**
     * Publish news (Admin only)
     */
    @Transactional
    public NewsDTO publishNews(Long id, String username) {
        log.info("Publishing news id: {} by user: {}", id, username);

        News news = newsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("News not found with id: " + id));

        news.setStatus(NewsStatus.PUBLISHED);
        news.setPublishedDate(LocalDateTime.now());

        News publishedNews = newsRepository.save(news);
        log.info("News published successfully with id: {}", publishedNews.getId());
        return convertToDTO(publishedNews);
    }

    /**
     * Archive news (Admin only)
     */
    @Transactional
    public NewsDTO archiveNews(Long id, String username) {
        log.info("Archiving news id: {} by user: {}", id, username);

        News news = newsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("News not found with id: " + id));

        news.setStatus(NewsStatus.ARCHIVED);

        News archivedNews = newsRepository.save(news);
        log.info("News archived successfully with id: {}", archivedNews.getId());
        return convertToDTO(archivedNews);
    }

    /**
     * Convert News entity to DTO
     */
    private NewsDTO convertToDTO(News news) {
        return NewsDTO.builder()
                .id(news.getId())
                .title(news.getTitle())
                .content(news.getContent())
                .publishedDate(news.getPublishedDate())
                .category(news.getCategory())
                .image(news.getImage())
                .status(news.getStatus())
                .featured(news.getFeatured())
                .authorName(news.getAuthorName())
                .createdById(news.getCreatedBy().getId())
                .createdByUsername(news.getCreatedBy().getUsername())
                .viewCount(news.getViewCount())
                .createdAt(news.getCreatedAt())
                .updatedAt(news.getUpdatedAt())
                .build();
    }
}

