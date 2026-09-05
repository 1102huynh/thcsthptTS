package com.schoolmanagement.repository;

import com.schoolmanagement.entity.ContentStatus;
import com.schoolmanagement.entity.NewsArticle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface NewsArticleRepository extends JpaRepository<NewsArticle, Long> {

    boolean existsBySlug(String slug);

    Optional<NewsArticle> findBySlug(String slug);

    // ---- public (published only) --------------------------------------
    // Featured first, then newest published. `now` is passed in so "not
    // scheduled yet" (publishedAt in the future) stays hidden.
    @Query(value = """
            SELECT a FROM NewsArticle a LEFT JOIN FETCH a.category
            WHERE a.status = com.schoolmanagement.entity.ContentStatus.PUBLISHED
              AND a.publishedAt <= :now
              AND (:categorySlug IS NULL OR a.category.slug = :categorySlug)
            ORDER BY a.isFeatured DESC, a.publishedAt DESC
            """,
            countQuery = """
            SELECT count(a) FROM NewsArticle a
            WHERE a.status = com.schoolmanagement.entity.ContentStatus.PUBLISHED
              AND a.publishedAt <= :now
              AND (:categorySlug IS NULL OR a.category.slug = :categorySlug)
            """)
    Page<NewsArticle> findPublished(@Param("now") LocalDateTime now,
                                    @Param("categorySlug") String categorySlug,
                                    Pageable pageable);

    @Query("""
            SELECT a FROM NewsArticle a LEFT JOIN FETCH a.category
            WHERE a.slug = :slug
              AND a.status = com.schoolmanagement.entity.ContentStatus.PUBLISHED
              AND a.publishedAt <= :now
            """)
    Optional<NewsArticle> findPublishedBySlug(@Param("slug") String slug, @Param("now") LocalDateTime now);

    @Query("""
            SELECT a FROM NewsArticle a LEFT JOIN FETCH a.category
            WHERE a.status = com.schoolmanagement.entity.ContentStatus.PUBLISHED
              AND a.publishedAt <= :now
              AND a.isFeatured = true
            ORDER BY a.publishedAt DESC
            """)
    java.util.List<NewsArticle> findFeatured(@Param("now") LocalDateTime now, Pageable pageable);

    // ---- CMS (all statuses) ------------------------------------------
    @Query(value = "SELECT a FROM NewsArticle a LEFT JOIN FETCH a.category LEFT JOIN FETCH a.author",
            countQuery = "SELECT count(a) FROM NewsArticle a")
    Page<NewsArticle> findAllForCms(Pageable pageable);

    // Batched by NewsViewCountAggregator - one UPDATE per article per flush
    // instead of one per page view (P4, KE_HOACH_TRANG_TIN_TUC_CONG_KHAI.md §8).
    // clearAutomatically: a bulk update bypasses the persistence context, so
    // without this an article already loaded earlier in the same
    // transaction (e.g. the detail view that triggered this flush) would
    // keep showing its stale pre-flush viewCount from Hibernate's
    // first-level cache instead of the fresh DB value.
    @Modifying(clearAutomatically = true)
    @Query("UPDATE NewsArticle a SET a.viewCount = a.viewCount + :delta WHERE a.id = :id")
    void incrementViewCountBy(@Param("id") Long id, @Param("delta") long delta);
}
