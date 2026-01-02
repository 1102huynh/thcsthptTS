package com.schoolmanagement.repository;

import com.schoolmanagement.entity.Announcement;
import com.schoolmanagement.entity.Announcement.AnnouncementTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    @Query("SELECT a FROM Announcement a WHERE a.published = true AND " +
           "(a.expiresAt IS NULL OR a.expiresAt > :now) " +
           "ORDER BY a.publishedAt DESC")
    List<Announcement> findActiveAnnouncements(@Param("now") LocalDateTime now);

    @Query("SELECT a FROM Announcement a WHERE a.published = true AND " +
           "(a.targetAudience = :target OR a.targetAudience = 'ALL') AND " +
           "(a.expiresAt IS NULL OR a.expiresAt > :now) " +
           "ORDER BY a.priority DESC, a.publishedAt DESC")
    List<Announcement> findActiveAnnouncementsByTarget(
        @Param("target") AnnouncementTarget target,
        @Param("now") LocalDateTime now
    );

    List<Announcement> findByPublishedOrderByCreatedAtDesc(Boolean published);
}

