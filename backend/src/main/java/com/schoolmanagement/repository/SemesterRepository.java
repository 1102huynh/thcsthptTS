package com.schoolmanagement.repository;

import com.schoolmanagement.entity.AcademicYear;
import com.schoolmanagement.entity.Semester;
import com.schoolmanagement.entity.SemesterName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SemesterRepository extends JpaRepository<Semester, Long> {
    List<Semester> findByAcademicYear(AcademicYear academicYear);
    Optional<Semester> findByAcademicYearAndName(AcademicYear academicYear, SemesterName name);
}
