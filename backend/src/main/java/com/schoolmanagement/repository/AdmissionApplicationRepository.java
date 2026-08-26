package com.schoolmanagement.repository;

import com.schoolmanagement.entity.AdmissionApplication;
import com.schoolmanagement.entity.AdmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdmissionApplicationRepository extends JpaRepository<AdmissionApplication, Long> {
    /** Fetch-joins reviewedBy so listing many applications doesn't do one lazy lookup per distinct reviewer. */
    @Query("SELECT a FROM AdmissionApplication a LEFT JOIN FETCH a.reviewedBy ORDER BY a.submittedAt DESC")
    List<AdmissionApplication> findAllWithReviewer();

    @Query("SELECT a FROM AdmissionApplication a LEFT JOIN FETCH a.reviewedBy WHERE a.status = :status ORDER BY a.submittedAt DESC")
    List<AdmissionApplication> findByStatusWithReviewer(@Param("status") AdmissionStatus status);
}
