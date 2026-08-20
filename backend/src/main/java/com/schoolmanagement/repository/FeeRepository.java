package com.schoolmanagement.repository;

import com.schoolmanagement.entity.Fee;
import com.schoolmanagement.entity.Student;
import com.schoolmanagement.entity.FeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeeRepository extends JpaRepository<Fee, Long> {
    List<Fee> findByStudent(Student student);
    List<Fee> findByStudentAndAcademicYear(Student student, String academicYear);
    List<Fee> findByStatus(FeeStatus status);
    List<Fee> findByAcademicYear(String academicYear);
    Page<Fee> findByAcademicYear(String academicYear, Pageable pageable);
    List<Fee> findByStudentAndStatus(Student student, FeeStatus status);

    @Query("SELECT COALESCE(SUM(f.remainingAmount), 0) FROM Fee f WHERE f.status NOT IN :excludedStatuses")
    Double sumRemainingAmountByStatusNotIn(@Param("excludedStatuses") List<FeeStatus> excludedStatuses);
}

