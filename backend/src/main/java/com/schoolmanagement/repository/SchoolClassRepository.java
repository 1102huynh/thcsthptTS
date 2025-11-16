package com.schoolmanagement.repository;

import com.schoolmanagement.entity.SchoolClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SchoolClassRepository extends JpaRepository<SchoolClass, Long> {
    Optional<SchoolClass> findByClassNameAndSection(String className, String section);
    List<SchoolClass> findByAcademicYear(String academicYear);
    List<SchoolClass> findByClassName(String className);
}

