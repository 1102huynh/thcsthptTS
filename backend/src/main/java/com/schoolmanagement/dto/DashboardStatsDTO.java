package com.schoolmanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Schema(description = "Summary statistics for the admin dashboard.")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "Students with status ACTIVE")
    private Long activeStudentCount;

    @Schema(description = "Staff with status ACTIVE")
    private Long activeStaffCount;

    @Schema(description = "% of attendance records marked PRESENT over the last 30 days (school-wide, all students)")
    private Double averageAttendanceRate;

    @Schema(description = "Sum of remainingAmount across all fees not yet PAID or EXEMPTED")
    private Double totalOutstandingFees;

    @Schema(description = "Book loans currently out (BORROW transactions with no return date yet)")
    private Long booksBorrowedCount;
}
