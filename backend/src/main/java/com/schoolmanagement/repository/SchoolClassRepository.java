package com.schoolmanagement.repository;

import com.schoolmanagement.entity.AcademicYear;
import com.schoolmanagement.entity.SchoolClass;
import com.schoolmanagement.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SchoolClassRepository extends JpaRepository<SchoolClass, Long> {
    Optional<SchoolClass> findByClassNameAndSection(String className, String section);
    Optional<SchoolClass> findByClassNameAndSectionAndAcademicYear(String className, String section, String academicYear);
    List<SchoolClass> findByAcademicYear(String academicYear);
    List<SchoolClass> findByClassName(String className);
    List<SchoolClass> findByClassTeacher(Staff classTeacher);
    List<SchoolClass> findByAcademicYearRef(AcademicYear academicYearRef);
}

