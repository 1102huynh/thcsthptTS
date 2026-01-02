package com.schoolmanagement.repository;

import com.schoolmanagement.entity.ParentMeeting;
import com.schoolmanagement.entity.ParentMeeting.MeetingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ParentMeetingRepository extends JpaRepository<ParentMeeting, Long> {

    @Query("SELECT m FROM ParentMeeting m WHERE m.parent.id = :parentId ORDER BY m.meetingDate DESC")
    List<ParentMeeting> findByParentIdOrderByMeetingDateDesc(@Param("parentId") Long parentId);

    @Query("SELECT m FROM ParentMeeting m WHERE m.teacher.id = :teacherId ORDER BY m.meetingDate DESC")
    List<ParentMeeting> findByTeacherIdOrderByMeetingDateDesc(@Param("teacherId") Long teacherId);

    @Query("SELECT m FROM ParentMeeting m WHERE m.parent.id = :parentId AND m.status = :status")
    List<ParentMeeting> findByParentIdAndStatus(
        @Param("parentId") Long parentId,
        @Param("status") MeetingStatus status
    );

    @Query("SELECT m FROM ParentMeeting m WHERE m.parent.id = :parentId AND " +
           "m.meetingDate BETWEEN :startDate AND :endDate ORDER BY m.meetingDate")
    List<ParentMeeting> findUpcomingMeetingsForParent(
        @Param("parentId") Long parentId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}

