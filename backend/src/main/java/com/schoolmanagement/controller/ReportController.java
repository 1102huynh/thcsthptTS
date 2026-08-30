package com.schoolmanagement.controller;

import com.schoolmanagement.entity.User;
import com.schoolmanagement.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * PDF/Excel exports per IMPLEMENTATION_PLAN.md 3.8. Every endpoint requires
 * login — unlike /v1/admissions there is no public report. Object-level
 * access (a STUDENT/PARENT only reaching their own/child's data) is enforced
 * inside {@link ReportService}, reusing the same guards the underlying data's
 * own endpoints already use — see the Javadoc on each ReportService method.
 *
 * <p>Each endpoint's role list is deliberately kept identical to the
 * equivalent existing endpoint for the same underlying data (see the
 * per-method comments below) rather than independently deciding who should
 * see it — a report is a different *shape* of the same data, not a different
 * *policy*. If PRINCIPAL (or any other role) should see more than the
 * existing grades/fees/conduct endpoints already allow, that's a deliberate
 * access-policy change to make consistently across all of them, not a
 * side effect of adding an export endpoint.
 */
@RestController
@RequestMapping("/v1/reports")
@AllArgsConstructor
@Tag(name = "Reports (Báo cáo)", description = "Xuất PDF/Excel — bảng điểm/học bạ, điểm danh lớp, biên lai học phí.")
public class ReportController {

    private ReportService reportService;

    @GetMapping("/student/{id}/transcript")
    // Same role set as GradeRecordController.getStudentYearSummary
    // (/v1/grade-records/student/{id}/year-summary), the endpoint this
    // transcript is built from.
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT','PARENT')")
    @Operation(summary = "Xuất bảng điểm/học bạ PDF cho một học sinh trong một năm học",
            description = "STUDENT/PARENT chỉ xem được của chính mình/con mình — xem StudentAccessGuard.")
    public ResponseEntity<byte[]> studentTranscript(
            @PathVariable Long id, @RequestParam Long academicYearId, Authentication authentication) {
        User requester = (User) authentication.getPrincipal();
        byte[] pdf = reportService.generateStudentTranscriptPdf(id, academicYearId, requester);
        return fileResponse(pdf, "hoc-ba-hs" + id + "-nam" + academicYearId + ".pdf", "application/pdf");
    }

    @GetMapping("/class/{id}/attendance")
    // Same role set as ConductController.getClassSemesterRoster
    // (/v1/conduct/class/{classId}/semester/{semesterId}), the closest
    // existing "whole-class roster export" endpoint.
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @Operation(summary = "Xuất bảng điểm danh Excel cho một lớp trong khoảng ngày [from, to]",
            description = "Khoảng [from, to] tối đa 366 ngày (một năm học) — mỗi ngày là một cột trong file Excel.")
    public ResponseEntity<byte[]> classAttendance(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        byte[] excel = reportService.generateClassAttendanceExcel(id, from, to);
        return fileResponse(excel, "diem-danh-lop" + id + ".xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    @GetMapping("/fees/receipt/{feeId}")
    // Same role set as FeeController.getFeeById (/v1/fees/{id}), the
    // endpoint this receipt's data comes from.
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT','STUDENT','PARENT')")
    @Operation(summary = "Xuất biên lai thu học phí PDF cho một khoản thu đã có thanh toán",
            description = "Trả về 400 nếu khoản thu chưa ghi nhận thanh toán nào (paidAmount rỗng/0).")
    public ResponseEntity<byte[]> feeReceipt(@PathVariable Long feeId, Authentication authentication) {
        User requester = (User) authentication.getPrincipal();
        byte[] pdf = reportService.generateFeeReceiptPdf(feeId, requester);
        return fileResponse(pdf, "bien-lai-hp" + feeId + ".pdf", "application/pdf");
    }

    private ResponseEntity<byte[]> fileResponse(byte[] body, String filename, String contentType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(contentType))
                .body(body);
    }
}
