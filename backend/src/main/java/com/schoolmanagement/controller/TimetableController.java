package com.schoolmanagement.controller;

import com.schoolmanagement.dto.TimetableSlotDTO;
import com.schoolmanagement.entity.TimetableSlot;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.service.TimetableService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/timetable")
@AllArgsConstructor
@Tag(name = "Timetable", description = "Thời khoá biểu — weekly schedule slots")
public class TimetableController {

    private TimetableService timetableService;

    // TEACHER removed (H.3.1) - a TEACHER only ever views their own teaching
    // schedule via GET /teacher/{teacherId} below, never any class's; browsing
    // by class stays ADMIN/PRINCIPAL only, who set the timetable.
    @GetMapping("/class/{classId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL')")
    @Operation(summary = "Get a class's weekly timetable",
            description = "Optional semesterId query param filters to one semester; omit to get every slot ever scheduled for the class.")
    public ResponseEntity<List<TimetableSlotDTO>> getClassTimetable(
            @PathVariable Long classId,
            @RequestParam(required = false) Long semesterId) {
        return new ResponseEntity<>(timetableService.getClassTimetable(classId, semesterId), HttpStatus.OK);
    }

    @GetMapping("/teacher/{teacherId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER')")
    @Operation(summary = "Get a teacher's weekly timetable",
            description = "Optional semesterId query param filters to one semester; omit to get every slot ever scheduled for the teacher. A TEACHER may only pass their own teacherId (403 otherwise).")
    public ResponseEntity<List<TimetableSlotDTO>> getTeacherTimetable(
            @PathVariable Long teacherId,
            @RequestParam(required = false) Long semesterId,
            Authentication authentication) {
        User requester = (User) authentication.getPrincipal();
        return new ResponseEntity<>(timetableService.getTeacherTimetable(teacherId, semesterId, requester), HttpStatus.OK);
    }

    @PostMapping("/slots")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRINCIPAL')")
    @Operation(summary = "Create a timetable slot",
            description = "Refused with 409 if the teacher, room, or class already has a slot at that day/period in the same semester.")
    public ResponseEntity<TimetableSlotDTO> createSlot(@Valid @RequestBody TimetableSlot request) {
        return new ResponseEntity<>(timetableService.createSlot(request), HttpStatus.CREATED);
    }

    @PutMapping("/slots/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRINCIPAL')")
    @Operation(summary = "Update a timetable slot", description = "Same conflict checks as create, excluding this slot itself.")
    public ResponseEntity<TimetableSlotDTO> updateSlot(@PathVariable Long id, @Valid @RequestBody TimetableSlot request) {
        return new ResponseEntity<>(timetableService.updateSlot(id, request), HttpStatus.OK);
    }

    @DeleteMapping("/slots/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRINCIPAL')")
    @Operation(summary = "Delete a timetable slot")
    public ResponseEntity<Void> deleteSlot(@PathVariable Long id) {
        timetableService.deleteSlot(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
