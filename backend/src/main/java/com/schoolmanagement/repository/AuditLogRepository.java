package com.schoolmanagement.repository;

import com.schoolmanagement.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // JOIN FETCH actor to avoid one lazy-load query per row across a page of
    // results; :entityType/:actorId are optional (null = don't filter on it).
    // Sorting comes from the caller's Pageable (see AuditLogController), not
    // a static ORDER BY here - mixing both risks Spring Data emitting two
    // conflicting ORDER BY clauses.
    @Query("SELECT a FROM AuditLog a LEFT JOIN FETCH a.actor "
            + "WHERE (:entityType IS NULL OR a.entityType = :entityType) "
            + "AND (:actorId IS NULL OR a.actor.id = :actorId)")
    Page<AuditLog> search(@Param("entityType") String entityType, @Param("actorId") Long actorId, Pageable pageable);
}
