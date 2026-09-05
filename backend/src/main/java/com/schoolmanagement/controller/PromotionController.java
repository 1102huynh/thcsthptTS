package com.schoolmanagement.controller;

import com.schoolmanagement.dto.PromotionPreviewEntryDTO;
import com.schoolmanagement.dto.PromotionRecordDTO;
import com.schoolmanagement.entity.PromotionRecord;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.service.PromotionService;
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
@RequestMapping("/v1/promotions")
@AllArgsConstructor
@Tag(name = "Promotions (Xét lên lớp)", description = "Xét lên lớp/ở lại/tốt nghiệp cuối năm. Preview is a live, unsaved suggestion computed from PromotionThresholdConfig; only /confirm persists a decision.")
public class PromotionController {

    private PromotionService promotionService;

    @GetMapping("/class/{classId}/preview")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER')")
    @Operation(summary = "Bảng đề xuất xét lên lớp cho cả lớp",
            description = "One row per student in the class — not persisted. suggestedDecision is null if no PromotionThresholdConfig applies to this academic year yet. A TEACHER may only preview a class they are GVCN (homeroom teacher) of (403 otherwise).")
    public ResponseEntity<List<PromotionPreviewEntryDTO>> previewClassPromotions(
            @PathVariable Long classId, @RequestParam Long academicYearId, Authentication authentication) {
        User requester = (User) authentication.getPrincipal();
        return new ResponseEntity<>(promotionService.previewClassPromotions(classId, academicYearId, requester), HttpStatus.OK);
    }

    @PostMapping("/confirm")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRINCIPAL')")
    @Operation(summary = "Lưu quyết định xét lên lớp cuối cùng (hỗ trợ ghi đè hàng loạt)",
            description = "Each entry is a PromotionRecord {student, academicYear, decision, decidedBy, remarks}. Confirming again for the same (student, academicYear) overwrites the previous decision. decisionDate is always set server-side to today, regardless of what's sent.")
    public ResponseEntity<List<PromotionRecordDTO>> confirmPromotions(@Valid @RequestBody List<PromotionRecord> requests) {
        return new ResponseEntity<>(promotionService.confirmPromotions(requests), HttpStatus.OK);
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER', 'STUDENT', 'PARENT')")
    @Operation(summary = "Get every confirmed promotion decision for a student (all academic years)",
            description = "A STUDENT caller may only fetch their own records (403 otherwise).")
    public ResponseEntity<List<PromotionRecordDTO>> getStudentPromotionHistory(
            @PathVariable Long studentId, Authentication authentication) {
        User requester = (User) authentication.getPrincipal();
        return new ResponseEntity<>(promotionService.getStudentPromotionHistory(studentId, requester), HttpStatus.OK);
    }
}
