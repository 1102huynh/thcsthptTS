package com.schoolmanagement.controller;

import com.schoolmanagement.dto.TimetableDTO;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.service.TimetableService;
import com.schoolmanagement.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/timetables")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TimetableController {

    @Autowired
    private TimetableService timetableService;

    /**
     * Get timetable for a specific class
     * Accessible by: All students in the class, teachers, admins
     * GET /api/v1/timetables/class/{classId}?academicYear=2024-2025
     */
    @GetMapping("/class/{classId}")
    public ResponseEntity<?> getTimetableByClass(
            @PathVariable Long classId,
            @RequestParam(defaultValue = "2024-2025") String academicYear) {
        try {
            List<TimetableDTO> timetables = timetableService.getTimetableByClass(classId, academicYear);
            return ResponseEntity.ok(timetables);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Get timetable for a specific day
     * GET /api/v1/timetables/class/{classId}/day/{dayOfWeek}?academicYear=2024-2025
     */
    @GetMapping("/class/{classId}/day/{dayOfWeek}")
    public ResponseEntity<?> getTimetableByDay(
            @PathVariable Long classId,
            @PathVariable String dayOfWeek,
            @RequestParam(defaultValue = "2024-2025") String academicYear) {
        try {
            DayOfWeek day = DayOfWeek.valueOf(dayOfWeek.toUpperCase());
            List<TimetableDTO> timetables = timetableService.getTimetableByDay(classId, day, academicYear);
            return ResponseEntity.ok(timetables);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Get timetable for a specific session (morning or afternoon)
     * GET /api/v1/timetables/class/{classId}/day/{dayOfWeek}/session/{sessionType}?academicYear=2024-2025
     */
    @GetMapping("/class/{classId}/day/{dayOfWeek}/session/{sessionType}")
    public ResponseEntity<?> getTimetableBySession(
            @PathVariable Long classId,
            @PathVariable String dayOfWeek,
            @PathVariable String sessionType,
            @RequestParam(defaultValue = "2024-2025") String academicYear) {
        try {
            DayOfWeek day = DayOfWeek.valueOf(dayOfWeek.toUpperCase());
            List<TimetableDTO> timetables = timetableService.getTimetableBySession(classId, day, sessionType.toUpperCase(), academicYear);
            return ResponseEntity.ok(timetables);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Create new timetable entry
     * Only homeroom teacher can create
     * POST /api/v1/timetables/class/{classId}
     */
    @PostMapping("/class/{classId}")
    public ResponseEntity<?> createTimetable(
            @PathVariable Long classId,
            @RequestBody TimetableDTO timetableDTO,
            Authentication authentication) {
        try {
            // Get current user from authentication
            User currentUser = (User) authentication.getPrincipal();
            TimetableDTO created = timetableService.createTimetable(classId, timetableDTO, currentUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
    }

    /**
     * Update timetable entry
     * Only homeroom teacher can update
     * PUT /api/v1/timetables/{timetableId}
     */
    @PutMapping("/{timetableId}")
    public ResponseEntity<?> updateTimetable(
            @PathVariable Long timetableId,
            @RequestBody TimetableDTO timetableDTO,
            Authentication authentication) {
        try {
            // Get current user from authentication
            User currentUser = (User) authentication.getPrincipal();
            TimetableDTO updated = timetableService.updateTimetable(timetableId, timetableDTO, currentUser);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
    }

    /**
     * Delete timetable entry
     * Only homeroom teacher can delete
     * DELETE /api/v1/timetables/{timetableId}
     */
    @DeleteMapping("/{timetableId}")
    public ResponseEntity<?> deleteTimetable(
            @PathVariable Long timetableId,
            Authentication authentication) {
        try {
            // Get current user from authentication
            User currentUser = (User) authentication.getPrincipal();
            timetableService.deleteTimetable(timetableId, currentUser);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Timetable entry deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
    }
}

