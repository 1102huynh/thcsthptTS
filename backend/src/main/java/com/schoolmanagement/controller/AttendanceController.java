package com.schoolmanagement.controller;

import com.schoolmanagement.dto.AttendanceDTO;
import com.schoolmanagement.entity.Attendance;
import com.schoolmanagement.entity.AttendanceStatus;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/v1/attendance")
@AllArgsConstructor
@Tag(name = "Attendance Management", description = "Attendance management endpoints")
public class AttendanceController {

    private AttendanceService attendanceService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    @Operation(summary = "Mark attendance for a student",
            description = "A TEACHER may only mark attendance for a student in a class they are GVCN (homeroom teacher) of (403 otherwise).")
    public ResponseEntity<Attendance> markAttendance(@Valid @RequestBody Attendance attendance, Authentication authentication) {
        User requester = (User) authentication.getPrincipal();
        Attendance savedAttendance = attendanceService.markAttendance(attendance, requester);
        return new ResponseEntity<>(savedAttendance, HttpStatus.CREATED);
    }

    @PostMapping("/class")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    @Operation(summary = "Mark attendance for entire class",
            description = "Re-marking the same class+date replaces the previous rows rather than duplicating them.")
    public ResponseEntity<String> markClassAttendance(
            @RequestParam String className,
            @RequestParam String section,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam List<Long> presentStudentIds,
            @RequestParam(defaultValue = "ABSENT") AttendanceStatus status,
            Authentication authentication) {
        User marker = (User) authentication.getPrincipal();
        attendanceService.markAttendanceForClass(className, section, date, presentStudentIds, status, marker);
        return new ResponseEntity<>("Attendance marked successfully", HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    @Operation(summary = "Update attendance record",
            description = "A TEACHER may only update attendance for a student in a class they are GVCN (homeroom teacher) of (403 otherwise).")
    public ResponseEntity<AttendanceDTO> updateAttendance(@PathVariable Long id, @Valid @RequestBody Attendance attendanceDetails, Authentication authentication) {
        User requester = (User) authentication.getPrincipal();
        AttendanceDTO updatedAttendance = attendanceService.updateAttendance(id, attendanceDetails, requester);
        return new ResponseEntity<>(updatedAttendance, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER', 'STUDENT', 'PARENT')")
    @Operation(summary = "Get attendance record by ID",
            description = "A STUDENT may only fetch their own attendance; a PARENT only their own child's (403 otherwise).")
    public ResponseEntity<AttendanceDTO> getAttendanceById(@PathVariable Long id, Authentication authentication) {
        AttendanceDTO attendance = attendanceService.getAttendanceById(id, (User) authentication.getPrincipal());
        return new ResponseEntity<>(attendance, HttpStatus.OK);
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER', 'STUDENT', 'PARENT')")
    @Operation(summary = "Get attendance records for a student",
            description = "A STUDENT may only fetch their own attendance; a PARENT only their own child's (403 otherwise).")
    public ResponseEntity<List<AttendanceDTO>> getStudentAttendance(@PathVariable Long studentId, Authentication authentication) {
        List<AttendanceDTO> attendances = attendanceService.getStudentAttendance(studentId, (User) authentication.getPrincipal());
        return new ResponseEntity<>(attendances, HttpStatus.OK);
    }

    @GetMapping("/student/{studentId}/between")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER', 'STUDENT', 'PARENT')")
    @Operation(summary = "Get student attendance between dates",
            description = "A STUDENT may only fetch their own attendance; a PARENT only their own child's (403 otherwise).")
    public ResponseEntity<List<AttendanceDTO>> getStudentAttendanceBetweenDates(
            @PathVariable Long studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {
        List<AttendanceDTO> attendances = attendanceService.getStudentAttendanceBetweenDates(
                studentId, startDate, endDate, (User) authentication.getPrincipal());
        return new ResponseEntity<>(attendances, HttpStatus.OK);
    }

    @GetMapping("/date/{date}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER')")
    @Operation(summary = "Get attendance records by date")
    public ResponseEntity<List<AttendanceDTO>> getAttendanceByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<AttendanceDTO> attendances = attendanceService.getAttendanceByDate(date);
        return new ResponseEntity<>(attendances, HttpStatus.OK);
    }

    @GetMapping("/between")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER')")
    @Operation(summary = "Get attendance records between dates")
    public ResponseEntity<List<AttendanceDTO>> getAttendanceBetweenDates(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<AttendanceDTO> attendances = attendanceService.getAttendanceBetweenDates(startDate, endDate);
        return new ResponseEntity<>(attendances, HttpStatus.OK);
    }

    @GetMapping("/student/{studentId}/percentage")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER', 'STUDENT', 'PARENT')")
    @Operation(summary = "Get attendance percentage for a student",
            description = "A STUDENT may only fetch their own percentage; a PARENT only their own child's (403 otherwise).")
    public ResponseEntity<Double> getAttendancePercentage(
            @PathVariable Long studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {
        Double percentage = attendanceService.getAttendancePercentage(
                studentId, startDate, endDate, (User) authentication.getPrincipal());
        return new ResponseEntity<>(percentage, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    @Operation(summary = "Delete attendance record",
            description = "A TEACHER may only delete attendance for a student in a class they are GVCN (homeroom teacher) of (403 otherwise).")
    public ResponseEntity<Void> deleteAttendance(@PathVariable Long id, Authentication authentication) {
        User requester = (User) authentication.getPrincipal();
        attendanceService.deleteAttendance(id, requester);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

