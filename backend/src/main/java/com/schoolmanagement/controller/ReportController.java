package com.schoolmanagement.controller;

import com.schoolmanagement.entity.User;
import com.schoolmanagement.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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
 */
@RestController
@RequestMapping("/v1/reports")
@AllArgsConstructor
@Tag(name = "Reports (Báo cáo)", description = "Xuất PDF/Excel — bảng điểm/học bạ, điểm danh lớp, biên lai học phí.")
public class ReportController {

    private ReportService reportService;

    @GetMapping("/student/{id}/transcript")
    @PreAuthorize("hasAnyRole('ADMIN','PRINCIPAL','TEACHER','STUDENT','PARENT')")
    @Operation(summary = "Xuất bảng điểm/học bạ PDF cho một học sinh trong một năm học",
            description = "STUDENT/PARENT chỉ xem được của chính mình/con mình — xem StudentAccessGuard.")
    public ResponseEntity<byte[]> studentTranscript(
            @PathVariable Long id, @RequestParam Long academicYearId, Authentication authentication) {
        User requester = (User) authentication.getPrincipal();
        byte[] pdf = reportService.generateStudentTranscriptPdf(id, academicYearId, requester);
        return pdfResponse(pdf, "hoc-ba-hs" + id + "-nam" + academicYearId + ".pdf");
    }

    @GetMapping("/class/{id}/attendance")
    @PreAuthorize("hasAnyRole('ADMIN','PRINCIPAL','TEACHER')")
    @Operation(summary = "Xuất bảng điểm danh Excel cho một lớp trong khoảng ngày [from, to]")
    public ResponseEntity<byte[]> classAttendance(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        byte[] excel = reportService.generateClassAttendanceExcel(id, from, to);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(org.springframework.http.ContentDisposition
                .attachment().filename("diem-danh-lop" + id + ".xlsx").build());
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }

    @GetMapping("/fees/receipt/{feeId}")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT','PRINCIPAL','STUDENT','PARENT')")
    @Operation(summary = "Xuất biên lai thu học phí PDF cho một khoản thu đã có thanh toán",
            description = "Trả về 400 nếu khoản thu chưa ghi nhận thanh toán nào (paidAmount rỗng/0).")
    public ResponseEntity<byte[]> feeReceipt(@PathVariable Long feeId, Authentication authentication) {
        User requester = (User) authentication.getPrincipal();
        byte[] pdf = reportService.generateFeeReceiptPdf(feeId, requester);
        return pdfResponse(pdf, "bien-lai-hp" + feeId + ".pdf");
    }

    private ResponseEntity<byte[]> pdfResponse(byte[] pdf, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(org.springframework.http.ContentDisposition
                .attachment().filename(filename).build());
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/pdf"))
                .body(pdf);
    }
}
