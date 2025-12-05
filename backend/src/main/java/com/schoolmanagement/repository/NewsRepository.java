package com.schoolmanagement.repository;

import com.schoolmanagement.entity.News;
import com.schoolmanagement.entity.NewsStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {

    // Find all published news ordered by published date
    Page<News> findByStatusOrderByPublishedDateDesc(NewsStatus status, Pageable pageable);

    // Find published news by category
    Page<News> findByStatusAndCategoryOrderByPublishedDateDesc(NewsStatus status, String category, Pageable pageable);

    // Find featured published news
    Page<News> findByStatusAndFeaturedOrderByPublishedDateDesc(NewsStatus status, Boolean featured, Pageable pageable);

    // Find all news (for admin)
    Page<News> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // Search news by title or content
    @Query("SELECT n FROM News n WHERE n.status = :status AND (LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(n.content) LIKE LOWER(CONCAT('%', :keyword, '%'))) ORDER BY n.publishedDate DESC")
    Page<News> searchPublishedNews(@Param("status") NewsStatus status, @Param("keyword") String keyword, Pageable pageable);

    // Find recent news (limit)
    List<News> findTop5ByStatusOrderByPublishedDateDesc(NewsStatus status);

    // Count news by status
    Long countByStatus(NewsStatus status);

    // Find news by date range
    @Query("SELECT n FROM News n WHERE n.status = :status AND n.publishedDate BETWEEN :startDate AND :endDate ORDER BY n.publishedDate DESC")
    Page<News> findByStatusAndDateRange(@Param("status") NewsStatus status, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);
}

