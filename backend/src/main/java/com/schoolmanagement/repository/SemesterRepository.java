package com.schoolmanagement.repository;

import com.schoolmanagement.entity.AcademicYear;
import com.schoolmanagement.entity.Semester;
import com.schoolmanagement.entity.SemesterName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SemesterRepository extends JpaRepository<Semester, Long> {
    List<Semester> findByAcademicYear(AcademicYear academicYear);
    Optional<Semester> findByAcademicYearAndName(AcademicYear academicYear, SemesterName name);

    /**
     * Every semester whose [startDate, endDate] range covers a given date -
     * call with the same date for both parameters. A {@code List}, not an
     * {@code Optional}: nothing in the schema guarantees semester date
     * ranges never overlap (different academic years' semesters, or plain
     * data-entry overlap), so callers must be prepared for more than one
     * match.
     */
    List<Semester> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDate startDate, LocalDate endDate);
}
