package com.schoolmanagement.repository;

import com.schoolmanagement.entity.Timetable;
import com.schoolmanagement.entity.SchoolClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimetableRepository extends JpaRepository<Timetable, Long> {

    // Get timetable for a specific class
    List<Timetable> findBySchoolClass(SchoolClass schoolClass);

    // Get timetable for a class by academic year
    List<Timetable> findBySchoolClassAndAcademicYear(SchoolClass schoolClass, String academicYear);

    // Get timetable for a specific day and class
    List<Timetable> findBySchoolClassAndDayOfWeek(SchoolClass schoolClass, DayOfWeek dayOfWeek);

    // Get timetable for a specific day, session, and class
    List<Timetable> findBySchoolClassAndDayOfWeekAndSessionType(SchoolClass schoolClass, DayOfWeek dayOfWeek, String sessionType);

    // Get all timetables for academic year
    List<Timetable> findByAcademicYear(String academicYear);

    // Check if timetable slot already exists
    Optional<Timetable> findBySchoolClassAndDayOfWeekAndTimeSlotAndSessionType(SchoolClass schoolClass, DayOfWeek dayOfWeek, Integer timeSlot, String sessionType);
}

