package com.schoolmanagement.repository;

import com.schoolmanagement.entity.SchoolClass;
import com.schoolmanagement.entity.Semester;
import com.schoolmanagement.entity.Staff;
import com.schoolmanagement.entity.Subject;
import com.schoolmanagement.entity.TeachingAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeachingAssignmentRepository extends JpaRepository<TeachingAssignment, Long> {
    List<TeachingAssignment> findBySchoolClass(SchoolClass schoolClass);
    List<TeachingAssignment> findByTeacher(Staff teacher);
    List<TeachingAssignment> findBySemester(Semester semester);
    Optional<TeachingAssignment> findBySchoolClassAndSubjectAndSemester(
            SchoolClass schoolClass, Subject subject, Semester semester);
}
