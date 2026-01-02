package com.schoolmanagement.repository;

import com.schoolmanagement.entity.ParentTeacherMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParentTeacherMessageRepository extends JpaRepository<ParentTeacherMessage, Long> {

    @Query("SELECT m FROM ParentTeacherMessage m WHERE m.parent.id = :parentId ORDER BY m.createdAt DESC")
    List<ParentTeacherMessage> findByParentIdOrderByCreatedAtDesc(@Param("parentId") Long parentId);

    @Query("SELECT m FROM ParentTeacherMessage m WHERE m.teacher.id = :teacherId ORDER BY m.createdAt DESC")
    List<ParentTeacherMessage> findByTeacherIdOrderByCreatedAtDesc(@Param("teacherId") Long teacherId);

    @Query("SELECT m FROM ParentTeacherMessage m WHERE m.parent.id = :parentId AND m.isRead = false")
    List<ParentTeacherMessage> findUnreadMessagesByParentId(@Param("parentId") Long parentId);

    @Query("SELECT m FROM ParentTeacherMessage m WHERE m.teacher.id = :teacherId AND m.isRead = false")
    List<ParentTeacherMessage> findUnreadMessagesByTeacherId(@Param("teacherId") Long teacherId);

    @Query("SELECT m FROM ParentTeacherMessage m WHERE m.student.id = :studentId ORDER BY m.createdAt DESC")
    List<ParentTeacherMessage> findByStudentIdOrderByCreatedAtDesc(@Param("studentId") Long studentId);
}

