package com.schoolmanagement.repository;

import com.schoolmanagement.entity.SchoolEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SchoolEventRepository extends JpaRepository<SchoolEvent, Long> {

    boolean existsBySlug(String slug);

    Optional<SchoolEvent> findBySlug(String slug);

    @Query(value = """
            SELECT e FROM SchoolEvent e
            WHERE e.status = com.schoolmanagement.entity.ContentStatus.PUBLISHED
              AND e.publishedAt <= :now
              AND (:upcomingOnly = false OR e.startAt >= :now)
              AND (:pastOnly = false OR e.startAt < :now)
            ORDER BY CASE WHEN e.startAt >= :now THEN 0 ELSE 1 END, e.startAt ASC
            """,
            countQuery = """
            SELECT count(e) FROM SchoolEvent e
            WHERE e.status = com.schoolmanagement.entity.ContentStatus.PUBLISHED
              AND e.publishedAt <= :now
              AND (:upcomingOnly = false OR e.startAt >= :now)
              AND (:pastOnly = false OR e.startAt < :now)
            """)
    Page<SchoolEvent> findPublished(@Param("now") LocalDateTime now,
                                    @Param("upcomingOnly") boolean upcomingOnly,
                                    @Param("pastOnly") boolean pastOnly,
                                    Pageable pageable);

    @Query("""
            SELECT e FROM SchoolEvent e
            WHERE e.slug = :slug
              AND e.status = com.schoolmanagement.entity.ContentStatus.PUBLISHED
              AND e.publishedAt <= :now
            """)
    Optional<SchoolEvent> findPublishedBySlug(@Param("slug") String slug, @Param("now") LocalDateTime now);

    @Query("""
            SELECT e FROM SchoolEvent e
            WHERE e.status = com.schoolmanagement.entity.ContentStatus.PUBLISHED
              AND e.publishedAt <= :now
              AND e.startAt >= :now
            ORDER BY e.startAt ASC
            """)
    List<SchoolEvent> findUpcoming(@Param("now") LocalDateTime now, Pageable pageable);

    Page<SchoolEvent> findAllByOrderByStartAtDesc(Pageable pageable);
}
