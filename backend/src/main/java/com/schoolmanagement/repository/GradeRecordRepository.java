package com.schoolmanagement.repository;

import com.schoolmanagement.entity.GradeRecord;
import com.schoolmanagement.entity.Semester;
import com.schoolmanagement.entity.Student;
import com.schoolmanagement.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GradeRecordRepository extends JpaRepository<GradeRecord, Long> {
    List<GradeRecord> findByStudentAndSemester(Student student, Semester semester);
    List<GradeRecord> findByStudentAndSubjectAndSemester(Student student, Subject subject, Semester semester);
}
