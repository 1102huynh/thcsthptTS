package com.schoolmanagement.controller;

import com.schoolmanagement.dto.AnalyticsDTO;
import com.schoolmanagement.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Advanced analytics and reporting endpoints")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/student/{studentId}/performance")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER', 'PARENT', 'STUDENT')")
    @Operation(summary = "Get student performance analytics")
    public ResponseEntity<AnalyticsDTO.StudentPerformanceDTO> getStudentPerformance(@PathVariable Long studentId) {
        AnalyticsDTO.StudentPerformanceDTO performance = analyticsService.getStudentPerformance(studentId);
        return new ResponseEntity<>(performance, HttpStatus.OK);
    }

    @GetMapping("/class/{classId}/analytics")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER')")
    @Operation(summary = "Get class analytics")
    public ResponseEntity<AnalyticsDTO.ClassAnalyticsDTO> getClassAnalytics(@PathVariable Long classId) {
        AnalyticsDTO.ClassAnalyticsDTO analytics = analyticsService.getClassAnalytics(classId);
        return new ResponseEntity<>(analytics, HttpStatus.OK);
    }

    @GetMapping("/student/{studentId}/attendance")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER', 'PARENT', 'STUDENT')")
    @Operation(summary = "Get attendance analytics")
    public ResponseEntity<AnalyticsDTO.AttendanceAnalyticsDTO> getAttendanceAnalytics(
            @PathVariable Long studentId,
            @RequestParam(defaultValue = "6") int months) {
        AnalyticsDTO.AttendanceAnalyticsDTO analytics = analyticsService.getAttendanceAnalytics(studentId, months);
        return new ResponseEntity<>(analytics, HttpStatus.OK);
    }

    @GetMapping("/class/{classId}/grade-distribution")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER')")
    @Operation(summary = "Get grade distribution")
    public ResponseEntity<AnalyticsDTO.GradeDistributionDTO> getGradeDistribution(@PathVariable Long classId) {
        AnalyticsDTO.GradeDistributionDTO distribution = analyticsService.getGradeDistribution(classId);
        return new ResponseEntity<>(distribution, HttpStatus.OK);
    }

    @GetMapping("/student/{studentId}/prediction")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER')")
    @Operation(summary = "Get performance prediction")
    public ResponseEntity<AnalyticsDTO.PerformancePredictionDTO> predictPerformance(@PathVariable Long studentId) {
        AnalyticsDTO.PerformancePredictionDTO prediction = analyticsService.predictPerformance(studentId);
        if (prediction == null) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(prediction, HttpStatus.OK);
    }
}

