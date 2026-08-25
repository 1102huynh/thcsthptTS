package com.schoolmanagement.repository;

import com.schoolmanagement.entity.AcademicYear;
import com.schoolmanagement.entity.PromotionRecord;
import com.schoolmanagement.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRecordRepository extends JpaRepository<PromotionRecord, Long> {
    Optional<PromotionRecord> findByStudentAndAcademicYear(Student student, AcademicYear academicYear);
    List<PromotionRecord> findByStudent(Student student);
}
