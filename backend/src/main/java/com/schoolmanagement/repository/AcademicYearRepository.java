package com.schoolmanagement.repository;

import com.schoolmanagement.entity.AcademicYear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AcademicYearRepository extends JpaRepository<AcademicYear, Long> {
    Optional<AcademicYear> findByYearName(String yearName);
    Optional<AcademicYear> findByIsCurrent(Boolean isCurrent);
    Optional<AcademicYear> findByIsActive(Boolean isActive);
}
