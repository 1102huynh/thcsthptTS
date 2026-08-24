package com.schoolmanagement.repository;

import com.schoolmanagement.entity.SchoolClass;
import com.schoolmanagement.entity.Semester;
import com.schoolmanagement.entity.Staff;
import com.schoolmanagement.entity.TeachingAssignment;
import com.schoolmanagement.entity.TimetableSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimetableSlotRepository extends JpaRepository<TimetableSlot, Long> {
    List<TimetableSlot> findByTeachingAssignment(TeachingAssignment teachingAssignment);
    List<TimetableSlot> findByTeachingAssignment_SchoolClass(SchoolClass schoolClass);
    List<TimetableSlot> findByTeachingAssignment_SchoolClassAndTeachingAssignment_Semester(
            SchoolClass schoolClass, Semester semester);
    List<TimetableSlot> findByTeachingAssignment_Teacher(Staff teacher);
    List<TimetableSlot> findByTeachingAssignment_TeacherAndTeachingAssignment_Semester(
            Staff teacher, Semester semester);

    /** True if this teacher already has a slot at the same day/period within the same semester. */
    @Query("SELECT COUNT(ts) > 0 FROM TimetableSlot ts " +
            "WHERE ts.teachingAssignment.teacher.id = :teacherId " +
            "AND ts.teachingAssignment.semester.id = :semesterId " +
            "AND ts.dayOfWeek = :dayOfWeek AND ts.period = :period " +
            "AND (:excludeSlotId IS NULL OR ts.id <> :excludeSlotId)")
    boolean existsTeacherConflict(@Param("teacherId") Long teacherId,
                                   @Param("semesterId") Long semesterId,
                                   @Param("dayOfWeek") Integer dayOfWeek,
                                   @Param("period") Integer period,
                                   @Param("excludeSlotId") Long excludeSlotId);

    /** True if this room is already booked at the same day/period within the same semester. */
    @Query("SELECT COUNT(ts) > 0 FROM TimetableSlot ts " +
            "WHERE ts.room = :room " +
            "AND ts.teachingAssignment.semester.id = :semesterId " +
            "AND ts.dayOfWeek = :dayOfWeek AND ts.period = :period " +
            "AND (:excludeSlotId IS NULL OR ts.id <> :excludeSlotId)")
    boolean existsRoomConflict(@Param("room") String room,
                                @Param("semesterId") Long semesterId,
                                @Param("dayOfWeek") Integer dayOfWeek,
                                @Param("period") Integer period,
                                @Param("excludeSlotId") Long excludeSlotId);

    /** True if this class already has a slot at the same day/period within the same semester. */
    @Query("SELECT COUNT(ts) > 0 FROM TimetableSlot ts " +
            "WHERE ts.teachingAssignment.schoolClass.id = :schoolClassId " +
            "AND ts.teachingAssignment.semester.id = :semesterId " +
            "AND ts.dayOfWeek = :dayOfWeek AND ts.period = :period " +
            "AND (:excludeSlotId IS NULL OR ts.id <> :excludeSlotId)")
    boolean existsClassConflict(@Param("schoolClassId") Long schoolClassId,
                                 @Param("semesterId") Long semesterId,
                                 @Param("dayOfWeek") Integer dayOfWeek,
                                 @Param("period") Integer period,
                                 @Param("excludeSlotId") Long excludeSlotId);
}
