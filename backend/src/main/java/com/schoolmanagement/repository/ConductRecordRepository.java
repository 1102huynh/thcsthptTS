package com.schoolmanagement.repository;

import com.schoolmanagement.entity.ConductRecord;
import com.schoolmanagement.entity.Semester;
import com.schoolmanagement.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConductRecordRepository extends JpaRepository<ConductRecord, Long> {
    Optional<ConductRecord> findByStudentAndSemester(Student student, Semester semester);
    List<ConductRecord> findByStudent(Student student);
    List<ConductRecord> findBySemesterAndStudentIn(Semester semester, List<Student> students);
    boolean existsByStudentAndSemester(Student student, Semester semester);
}
