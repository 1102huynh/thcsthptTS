package com.schoolmanagement.service;

import com.schoolmanagement.entity.Timetable;
import com.schoolmanagement.entity.SchoolClass;
import com.schoolmanagement.entity.Staff;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.repository.TimetableRepository;
import com.schoolmanagement.repository.SchoolClassRepository;
import com.schoolmanagement.dto.TimetableDTO;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.exception.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TimetableService {

    @Autowired
    private TimetableRepository timetableRepository;

    @Autowired
    private SchoolClassRepository schoolClassRepository;

    /**
     * Get timetable for a specific class
     * Accessible by: All students in the class, teachers, admins
     */
    public List<TimetableDTO> getTimetableByClass(Long classId, String academicYear) {
        SchoolClass schoolClass = schoolClassRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + classId));

        List<Timetable> timetables = timetableRepository.findBySchoolClassAndAcademicYear(schoolClass, academicYear);
        return timetables.stream()
                .map(this::mapToDTO)
                .sorted((t1, t2) -> {
                    // Sort by day first, then by time slot
                    int dayComparison = getDayValue(t1.getDayOfWeek()).compareTo(getDayValue(t2.getDayOfWeek()));
                    if (dayComparison != 0) return dayComparison;
                    return t1.getTimeSlot().compareTo(t2.getTimeSlot());
                })
                .collect(Collectors.toList());
    }

    /**
     * Get timetable for a specific day
     */
    public List<TimetableDTO> getTimetableByDay(Long classId, DayOfWeek dayOfWeek, String academicYear) {
        SchoolClass schoolClass = schoolClassRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + classId));

        List<Timetable> timetables = timetableRepository.findBySchoolClassAndDayOfWeek(schoolClass, dayOfWeek);
        return timetables.stream()
                .filter(t -> t.getAcademicYear().equals(academicYear))
                .map(this::mapToDTO)
                .sorted((t1, t2) -> t1.getTimeSlot().compareTo(t2.getTimeSlot()))
                .collect(Collectors.toList());
    }

    /**
     * Get timetable for a specific session (morning or afternoon)
     */
    public List<TimetableDTO> getTimetableBySession(Long classId, DayOfWeek dayOfWeek, String sessionType, String academicYear) {
        SchoolClass schoolClass = schoolClassRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + classId));

        List<Timetable> timetables = timetableRepository.findBySchoolClassAndDayOfWeekAndSessionType(schoolClass, dayOfWeek, sessionType);
        return timetables.stream()
                .filter(t -> t.getAcademicYear().equals(academicYear))
                .map(this::mapToDTO)
                .sorted((t1, t2) -> t1.getTimeSlot().compareTo(t2.getTimeSlot()))
                .collect(Collectors.toList());
    }

    /**
     * Create new timetable entry
     * Only homeroom teacher of the class can create
     */
    public TimetableDTO createTimetable(Long classId, TimetableDTO timetableDTO, User currentUser) {
        SchoolClass schoolClass = schoolClassRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + classId));

        // Verify that current user is the homeroom teacher of this class
        if (schoolClass.getClassTeacher() == null || !schoolClass.getClassTeacher().getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Only the homeroom teacher can create timetable for this class");
        }

        // Check if slot already exists
        timetableRepository.findBySchoolClassAndDayOfWeekAndTimeSlotAndSessionType(
                schoolClass,
                DayOfWeek.valueOf(timetableDTO.getDayOfWeek().toUpperCase()),
                timetableDTO.getTimeSlot(),
                timetableDTO.getSessionType()
        ).ifPresent(existing -> {
            throw new IllegalArgumentException("Timetable slot already exists for this day and time");
        });

        Timetable timetable = new Timetable();
        timetable.setSchoolClass(schoolClass);
        timetable.setDayOfWeek(DayOfWeek.valueOf(timetableDTO.getDayOfWeek().toUpperCase()));
        timetable.setSessionType(timetableDTO.getSessionType());
        timetable.setTimeSlot(timetableDTO.getTimeSlot());
        timetable.setStartTime(timetableDTO.getStartTime());
        timetable.setEndTime(timetableDTO.getEndTime());
        timetable.setSubject(timetableDTO.getSubject());
        timetable.setClassroom(timetableDTO.getClassroom());
        timetable.setAcademicYear(timetableDTO.getAcademicYear());
        timetable.setStatus("ACTIVE");
        timetable.setCreatedBy(currentUser);

        Timetable saved = timetableRepository.save(timetable);
        return mapToDTO(saved);
    }

    /**
     * Update timetable entry
     * Only homeroom teacher of the class can update
     */
    public TimetableDTO updateTimetable(Long timetableId, TimetableDTO timetableDTO, User currentUser) {
        Timetable timetable = timetableRepository.findById(timetableId)
                .orElseThrow(() -> new ResourceNotFoundException("Timetable not found with id: " + timetableId));

        SchoolClass schoolClass = timetable.getSchoolClass();

        // Verify that current user is the homeroom teacher of this class
        if (schoolClass.getClassTeacher() == null || !schoolClass.getClassTeacher().getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Only the homeroom teacher can edit timetable for this class");
        }

        // Update fields
        timetable.setSubject(timetableDTO.getSubject());
        timetable.setStartTime(timetableDTO.getStartTime());
        timetable.setEndTime(timetableDTO.getEndTime());
        timetable.setClassroom(timetableDTO.getClassroom());
        timetable.setStatus(timetableDTO.getStatus());

        Timetable updated = timetableRepository.save(timetable);
        return mapToDTO(updated);
    }

    /**
     * Delete timetable entry
     * Only homeroom teacher of the class can delete
     */
    public void deleteTimetable(Long timetableId, User currentUser) {
        Timetable timetable = timetableRepository.findById(timetableId)
                .orElseThrow(() -> new ResourceNotFoundException("Timetable not found with id: " + timetableId));

        SchoolClass schoolClass = timetable.getSchoolClass();

        // Verify that current user is the homeroom teacher of this class
        if (schoolClass.getClassTeacher() == null || !schoolClass.getClassTeacher().getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Only the homeroom teacher can delete timetable for this class");
        }

        timetableRepository.deleteById(timetableId);
    }

    /**
     * Helper method to convert Timetable entity to DTO
     */
    private TimetableDTO mapToDTO(Timetable timetable) {
        TimetableDTO dto = new TimetableDTO();
        dto.setId(timetable.getId());
        dto.setClassId(timetable.getSchoolClass().getId());
        dto.setClassName(timetable.getSchoolClass().getClassName());
        dto.setDayOfWeek(timetable.getDayOfWeek().toString());
        dto.setSessionType(timetable.getSessionType());
        dto.setTimeSlot(timetable.getTimeSlot());
        dto.setStartTime(timetable.getStartTime());
        dto.setEndTime(timetable.getEndTime());
        dto.setSubject(timetable.getSubject());
        dto.setClassroom(timetable.getClassroom());
        dto.setAcademicYear(timetable.getAcademicYear());
        dto.setStatus(timetable.getStatus());
        dto.setCreatedAt(timetable.getCreatedAt());
        dto.setUpdatedAt(timetable.getUpdatedAt());

        // Map subject teacher information
        if (timetable.getSubjectTeacher() != null) {
            Staff teacher = timetable.getSubjectTeacher();
            dto.setSubjectTeacherId(teacher.getId());
            dto.setSubjectTeacherName(teacher.getUser().getFirstName() + " " + teacher.getUser().getLastName());
            dto.setSubjectTeacherEmail(teacher.getUser().getEmail());
            dto.setSubjectTeacherPhone(teacher.getUser().getPhoneNumber());
        }

        return dto;
    }

    /**
     * Helper method to get day value for sorting
     */
    private Integer getDayValue(String dayOfWeek) {
        switch (dayOfWeek.toUpperCase()) {
            case "MONDAY": return 1;
            case "TUESDAY": return 2;
            case "WEDNESDAY": return 3;
            case "THURSDAY": return 4;
            case "FRIDAY": return 5;
            case "SATURDAY": return 6;
            case "SUNDAY": return 7;
            default: return 0;
        }
    }
}

