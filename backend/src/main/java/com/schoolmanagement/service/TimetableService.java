package com.schoolmanagement.service;

import com.schoolmanagement.dto.TimetableSlotDTO;
import com.schoolmanagement.entity.SchoolClass;
import com.schoolmanagement.entity.Semester;
import com.schoolmanagement.entity.Staff;
import com.schoolmanagement.entity.Subject;
import com.schoolmanagement.entity.TeachingAssignment;
import com.schoolmanagement.entity.TimetableSlot;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.exception.ScheduleConflictException;
import com.schoolmanagement.repository.SchoolClassRepository;
import com.schoolmanagement.repository.SemesterRepository;
import com.schoolmanagement.repository.StaffRepository;
import com.schoolmanagement.repository.TeachingAssignmentRepository;
import com.schoolmanagement.repository.TimetableSlotRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class TimetableService {

    private TimetableSlotRepository timetableSlotRepository;
    private TeachingAssignmentRepository teachingAssignmentRepository;
    private SchoolClassRepository schoolClassRepository;
    private StaffRepository staffRepository;
    private SemesterRepository semesterRepository;

    public TimetableSlotDTO createSlot(TimetableSlot request) {
        TeachingAssignment assignment = resolveTeachingAssignment(request.getTeachingAssignment());
        assertNoConflict(assignment, request.getDayOfWeek(), request.getPeriod(), request.getRoom(), null);

        TimetableSlot slot = TimetableSlot.builder()
                .teachingAssignment(assignment)
                .dayOfWeek(request.getDayOfWeek())
                .period(request.getPeriod())
                .room(request.getRoom())
                .build();

        return mapToDTO(timetableSlotRepository.save(slot));
    }

    public TimetableSlotDTO updateSlot(Long id, TimetableSlot request) {
        TimetableSlot slot = timetableSlotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Timetable slot not found with id: " + id));

        TeachingAssignment assignment = resolveTeachingAssignment(request.getTeachingAssignment());
        assertNoConflict(assignment, request.getDayOfWeek(), request.getPeriod(), request.getRoom(), id);

        slot.setTeachingAssignment(assignment);
        slot.setDayOfWeek(request.getDayOfWeek());
        slot.setPeriod(request.getPeriod());
        slot.setRoom(request.getRoom());

        return mapToDTO(timetableSlotRepository.save(slot));
    }

    public void deleteSlot(Long id) {
        TimetableSlot slot = timetableSlotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Timetable slot not found with id: " + id));
        timetableSlotRepository.delete(slot);
    }

    public List<TimetableSlotDTO> getClassTimetable(Long classId, Long semesterId) {
        SchoolClass schoolClass = schoolClassRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + classId));

        List<TimetableSlot> slots = semesterId == null
                ? timetableSlotRepository.findByTeachingAssignment_SchoolClass(schoolClass)
                : timetableSlotRepository.findByTeachingAssignment_SchoolClassAndTeachingAssignment_Semester(
                        schoolClass, resolveSemesterOrThrow(semesterId));

        return slots.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<TimetableSlotDTO> getTeacherTimetable(Long teacherId, Long semesterId) {
        Staff teacher = staffRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + teacherId));

        List<TimetableSlot> slots = semesterId == null
                ? timetableSlotRepository.findByTeachingAssignment_Teacher(teacher)
                : timetableSlotRepository.findByTeachingAssignment_TeacherAndTeachingAssignment_Semester(
                        teacher, resolveSemesterOrThrow(semesterId));

        return slots.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    private Semester resolveSemesterOrThrow(Long semesterId) {
        return semesterRepository.findById(semesterId)
                .orElseThrow(() -> new ResourceNotFoundException("Semester not found with id: " + semesterId));
    }

    private void assertNoConflict(TeachingAssignment assignment, Integer dayOfWeek, Integer period, String room, Long excludeSlotId) {
        Long semesterId = assignment.getSemester().getId();

        if (timetableSlotRepository.existsTeacherConflict(assignment.getTeacher().getId(), semesterId, dayOfWeek, period, excludeSlotId)) {
            throw new ScheduleConflictException("This teacher already has another class at that day/period this semester");
        }
        if (timetableSlotRepository.existsRoomConflict(room, semesterId, dayOfWeek, period, excludeSlotId)) {
            throw new ScheduleConflictException("This room is already booked at that day/period this semester");
        }
        if (timetableSlotRepository.existsClassConflict(assignment.getSchoolClass().getId(), semesterId, dayOfWeek, period, excludeSlotId)) {
            throw new ScheduleConflictException("This class already has another lesson at that day/period this semester");
        }
    }

    private TeachingAssignment resolveTeachingAssignment(TeachingAssignment reference) {
        if (reference == null || reference.getId() == null) {
            throw new ResourceNotFoundException("A teachingAssignment id is required");
        }
        return teachingAssignmentRepository.findById(reference.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Teaching assignment not found with id: " + reference.getId()));
    }

    private TimetableSlotDTO mapToDTO(TimetableSlot slot) {
        TeachingAssignment assignment = slot.getTeachingAssignment();
        SchoolClass schoolClass = assignment.getSchoolClass();
        Subject subject = assignment.getSubject();
        Staff teacher = assignment.getTeacher();

        return TimetableSlotDTO.builder()
                .id(slot.getId())
                .teachingAssignmentId(assignment.getId())
                .schoolClassId(schoolClass.getId())
                .schoolClassLabel(schoolClass.getClassName() + "-" + schoolClass.getSection())
                .subjectId(subject.getId())
                .subjectName(subject.getName())
                .teacherId(teacher.getId())
                .teacherName(teacher.getUser() != null
                        ? teacher.getUser().getFirstName() + " " + teacher.getUser().getLastName()
                        : null)
                .dayOfWeek(slot.getDayOfWeek())
                .period(slot.getPeriod())
                .room(slot.getRoom())
                .createdAt(slot.getCreatedAt())
                .updatedAt(slot.getUpdatedAt())
                .build();
    }
}
