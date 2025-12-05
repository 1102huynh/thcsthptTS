package com.schoolmanagement.repository;

import com.schoolmanagement.entity.Admission;
import com.schoolmanagement.entity.AdmissionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdmissionRepository extends JpaRepository<Admission, Long> {

    // Find open admissions ordered by display order
    Page<Admission> findByStatusOrderByDisplayOrderAsc(AdmissionStatus status, Pageable pageable);

    // Find admissions by academic year
    Page<Admission> findByAcademicYearOrderByDisplayOrderAsc(String academicYear, Pageable pageable);

    // Find all admissions ordered by display order
    Page<Admission> findAllByOrderByDisplayOrderAsc(Pageable pageable);

    // Find admissions by status
    List<Admission> findByStatusOrderByDisplayOrderAsc(AdmissionStatus status);
}

