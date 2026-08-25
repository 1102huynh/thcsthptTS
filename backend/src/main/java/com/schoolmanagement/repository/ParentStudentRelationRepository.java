package com.schoolmanagement.repository;

import com.schoolmanagement.entity.ParentStudentRelation;
import com.schoolmanagement.entity.Student;
import com.schoolmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParentStudentRelationRepository extends JpaRepository<ParentStudentRelation, Long> {
    Optional<ParentStudentRelation> findByParentAndStudent(User parent, Student student);
    List<ParentStudentRelation> findByParent(User parent);
    List<ParentStudentRelation> findByStudent(Student student);
    List<ParentStudentRelation> findByStudentIn(List<Student> students);
    boolean existsByParentAndStudent(User parent, Student student);
}
