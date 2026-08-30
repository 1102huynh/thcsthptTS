package com.schoolmanagement.repository;

import com.schoolmanagement.entity.DocumentAttachment;
import com.schoolmanagement.entity.DocumentOwnerType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentAttachmentRepository extends JpaRepository<DocumentAttachment, Long> {

    // JOIN FETCH uploadedBy to avoid one lazy-load query per row when listing
    // a whole owner's documents (mapToDTO reads uploadedBy for uploadedByName).
    @Query("SELECT d FROM DocumentAttachment d LEFT JOIN FETCH d.uploadedBy "
            + "WHERE d.ownerType = :ownerType AND d.ownerId = :ownerId ORDER BY d.uploadedAt DESC")
    List<DocumentAttachment> findByOwnerTypeAndOwnerIdOrderByUploadedAtDesc(
            @Param("ownerType") DocumentOwnerType ownerType, @Param("ownerId") Long ownerId);

    @Query("SELECT d FROM DocumentAttachment d LEFT JOIN FETCH d.uploadedBy WHERE d.id = :id")
    Optional<DocumentAttachment> findByIdWithUploadedBy(@Param("id") Long id);
}
