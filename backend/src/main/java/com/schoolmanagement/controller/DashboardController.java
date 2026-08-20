package com.schoolmanagement.controller;

import com.schoolmanagement.dto.DashboardStatsDTO;
import com.schoolmanagement.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/dashboard")
@AllArgsConstructor
@Tag(name = "Dashboard", description = "Admin dashboard summary endpoints")
public class DashboardController {

    private DashboardService dashboardService;

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRINCIPAL')")
    @Operation(summary = "Get dashboard summary statistics",
            description = "Active student/staff counts, 30-day school-wide attendance rate, total outstanding fees, and books currently on loan.")
    public ResponseEntity<DashboardStatsDTO> getStats() {
        return ResponseEntity.ok(dashboardService.getStats());
    }
}
