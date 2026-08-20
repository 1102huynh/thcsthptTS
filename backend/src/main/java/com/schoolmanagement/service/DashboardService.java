package com.schoolmanagement.service;

import com.schoolmanagement.dto.DashboardStatsDTO;
import com.schoolmanagement.entity.AttendanceStatus;
import com.schoolmanagement.entity.EmploymentStatus;
import com.schoolmanagement.entity.FeeStatus;
import com.schoolmanagement.entity.StudentStatus;
import com.schoolmanagement.entity.TransactionType;
import com.schoolmanagement.repository.AttendanceRepository;
import com.schoolmanagement.repository.BookTransactionRepository;
import com.schoolmanagement.repository.FeeRepository;
import com.schoolmanagement.repository.StaffRepository;
import com.schoolmanagement.repository.StudentRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private static final int ATTENDANCE_WINDOW_DAYS = 30;
    private static final List<FeeStatus> FEE_STATUSES_NOT_OUTSTANDING = List.of(FeeStatus.PAID, FeeStatus.EXEMPTED);

    private StudentRepository studentRepository;
    private StaffRepository staffRepository;
    private AttendanceRepository attendanceRepository;
    private FeeRepository feeRepository;
    private BookTransactionRepository bookTransactionRepository;

    public DashboardStatsDTO getStats() {
        long activeStudentCount = studentRepository.countByStatus(StudentStatus.ACTIVE);
        long activeStaffCount = staffRepository.countByStatus(EmploymentStatus.ACTIVE);
        double averageAttendanceRate = calculateAverageAttendanceRate();
        Double totalOutstandingFees = feeRepository.sumRemainingAmountByStatusNotIn(FEE_STATUSES_NOT_OUTSTANDING);
        long booksBorrowedCount = bookTransactionRepository.countByTransactionTypeAndReturnDateIsNull(TransactionType.BORROW);

        return DashboardStatsDTO.builder()
                .activeStudentCount(activeStudentCount)
                .activeStaffCount(activeStaffCount)
                .averageAttendanceRate(averageAttendanceRate)
                .totalOutstandingFees(totalOutstandingFees != null ? totalOutstandingFees : 0.0)
                .booksBorrowedCount(booksBorrowedCount)
                .build();
    }

    private double calculateAverageAttendanceRate() {
        LocalDate today = LocalDate.now();
        LocalDate windowStart = today.minusDays(ATTENDANCE_WINDOW_DAYS);

        long totalRecords = attendanceRepository.countByAttendanceDateBetween(windowStart, today);
        if (totalRecords == 0) {
            return 0.0;
        }

        long presentRecords = attendanceRepository.countByStatusAndAttendanceDateBetween(
                AttendanceStatus.PRESENT, windowStart, today);

        double rate = (presentRecords * 100.0) / totalRecords;
        return Math.round(rate * 100.0) / 100.0; // 2 decimal places
    }
}
