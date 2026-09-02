package com.schoolmanagement.controller;

import com.schoolmanagement.dto.ConductRecordDTO;
import com.schoolmanagement.dto.ConductRosterEntryDTO;
import com.schoolmanagement.entity.ConductRecord;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.service.ConductRecordService;
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
@RequestMapping("/v1/conduct")
@AllArgsConstructor
@Tag(name = "Conduct (Hạnh kiểm)", description = "Đánh giá hạnh kiểm/rèn luyện theo học kỳ. TEACHER chỉ được ghi cho lớp mình làm GVCN (classTeacher).")
public class ConductController {

    private ConductRecordService conductRecordService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    @Operation(summary = "Create a conduct record",
            description = "A TEACHER may only record conduct for students in the class they are GVCN of (403 otherwise).")
    public ResponseEntity<ConductRecordDTO> createConductRecord(
            @Valid @RequestBody ConductRecord request, Authentication authentication) {
        User requester = (User) authentication.getPrincipal();
        return new ResponseEntity<>(conductRecordService.createConductRecord(request, requester), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    @Operation(summary = "Update a conduct record",
            description = "A TEACHER may only update conduct for students in the class they are GVCN of (403 otherwise).")
    public ResponseEntity<ConductRecordDTO> updateConductRecord(
            @PathVariable Long id, @Valid @RequestBody ConductRecord request, Authentication authentication) {
        User requester = (User) authentication.getPrincipal();
        return new ResponseEntity<>(conductRecordService.updateConductRecord(id, request, requester), HttpStatus.OK);
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER', 'STUDENT', 'PARENT')")
    @Operation(summary = "Get every conduct record for a student (all semesters)",
            description = "A STUDENT caller may only fetch their own conduct records (403 otherwise).")
    public ResponseEntity<List<ConductRecordDTO>> getStudentConductRecords(
            @PathVariable Long studentId, Authentication authentication) {
        User requester = (User) authentication.getPrincipal();
        return new ResponseEntity<>(conductRecordService.getStudentConductRecords(studentId, requester), HttpStatus.OK);
    }

    @GetMapping("/class/{classId}/semester/{semesterId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER')")
    @Operation(summary = "Bảng đánh giá hạnh kiểm hàng loạt cho GVCN",
            description = "One row per student currently in the class; rating/remarks are null for students not yet evaluated this semester.")
    public ResponseEntity<List<ConductRosterEntryDTO>> getClassSemesterRoster(
            @PathVariable Long classId, @PathVariable Long semesterId) {
        return new ResponseEntity<>(conductRecordService.getClassSemesterRoster(classId, semesterId), HttpStatus.OK);
    }
}
