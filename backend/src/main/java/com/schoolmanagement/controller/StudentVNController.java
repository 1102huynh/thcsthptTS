package com.schoolmanagement.controller;

import com.schoolmanagement.constants.VietnamConstants;
import com.schoolmanagement.entity.StudentVN;
import com.schoolmanagement.service.StudentVNService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vn/students")
@AllArgsConstructor
@Tag(name = "Student Management (VN)", description = "Vietnam-standard student management")
@CrossOrigin(origins = "*")
public class StudentVNController {

    private StudentVNService studentVNService;

    // ============ CRUD OPERATIONS ============

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER')")
    @Operation(summary = "Get all students")
    public ResponseEntity<List<StudentVN>> getAllStudents() {
        return ResponseEntity.ok(studentVNService.getAllStudents());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER')")
    @Operation(summary = "Get student by ID")
    public ResponseEntity<StudentVN> getStudentById(@PathVariable Long id) {
        return ResponseEntity.ok(studentVNService.getStudentById(id));
    }

    @GetMapping("/code/{studentCode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER')")
    @Operation(summary = "Get student by student code")
    public ResponseEntity<StudentVN> getStudentByCode(@PathVariable String studentCode) {
        return studentVNService.getStudentByCode(studentCode)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/id-number/{idNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL')")
    @Operation(summary = "Get student by ID number (CMND/CCCD)")
    public ResponseEntity<StudentVN> getStudentByIdNumber(@PathVariable String idNumber) {
        return studentVNService.getStudentByIdNumber(idNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRINCIPAL')")
    @Operation(summary = "Create new student")
    public ResponseEntity<StudentVN> createStudent(@RequestBody StudentVN student) {
        try {
            StudentVN created = studentVNService.createStudent(student);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRINCIPAL')")
    @Operation(summary = "Update student")
    public ResponseEntity<StudentVN> updateStudent(@PathVariable Long id, @RequestBody StudentVN studentDetails) {
        try {
            StudentVN updated = studentVNService.updateStudent(id, studentDetails);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRINCIPAL')")
    @Operation(summary = "Delete student")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        try {
            studentVNService.deleteStudent(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRINCIPAL')")
    @Operation(summary = "Update student status")
    public ResponseEntity<StudentVN> updateStudentStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            StudentVN updated = studentVNService.updateStudentStatus(id, status);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ============ QUERY OPERATIONS ============

    @GetMapping("/class/{classId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER')")
    @Operation(summary = "Get students by class")
    public ResponseEntity<List<StudentVN>> getStudentsByClass(@PathVariable Long classId) {
        return ResponseEntity.ok(studentVNService.getStudentsByClass(classId));
    }

    @GetMapping("/grade-level/{gradeLevelId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER')")
    @Operation(summary = "Get students by grade level")
    public ResponseEntity<List<StudentVN>> getStudentsByGradeLevel(@PathVariable Long gradeLevelId) {
        return ResponseEntity.ok(studentVNService.getStudentsByGradeLevel(gradeLevelId));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL')")
    @Operation(summary = "Get students by status")
    public ResponseEntity<List<StudentVN>> getStudentsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(studentVNService.getStudentsByStatus(status));
    }

    @GetMapping("/academic-year/{academicYear}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER')")
    @Operation(summary = "Get students by academic year")
    public ResponseEntity<List<StudentVN>> getStudentsByAcademicYear(@PathVariable String academicYear) {
        return ResponseEntity.ok(studentVNService.getStudentsByAcademicYear(academicYear));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER')")
    @Operation(summary = "Search students by name")
    public ResponseEntity<List<StudentVN>> searchStudents(@RequestParam String name) {
        return ResponseEntity.ok(studentVNService.searchStudentsByName(name));
    }

    @GetMapping("/advanced-search")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER')")
    @Operation(summary = "Advanced search with multiple filters")
    public ResponseEntity<List<StudentVN>> advancedSearch(
            @RequestParam(required = false) String studentCode,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long gradeLevelId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String name) {
        return ResponseEntity.ok(studentVNService.advancedSearch(studentCode, classId, gradeLevelId, status, name));
    }

    // ============ STATISTICS ============

    @GetMapping("/statistics/class/{classId}/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER')")
    @Operation(summary = "Count students by class")
    public ResponseEntity<Map<String, Long>> countStudentsByClass(@PathVariable Long classId) {
        Long count = studentVNService.countStudentsByClass(classId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @GetMapping("/statistics/grade-level/{gradeLevelId}/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER')")
    @Operation(summary = "Count students by grade level")
    public ResponseEntity<Map<String, Long>> countStudentsByGradeLevel(@PathVariable Long gradeLevelId) {
        Long count = studentVNService.countStudentsByGradeLevel(gradeLevelId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @GetMapping("/statistics/status/{status}/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL')")
    @Operation(summary = "Count students by status")
    public ResponseEntity<Map<String, Long>> countStudentsByStatus(@PathVariable String status) {
        Long count = studentVNService.countStudentsByStatus(status);
        return ResponseEntity.ok(Map.of("count", count));
    }

    // ============ VIETNAM CONSTANTS ENDPOINTS ============

    @GetMapping("/constants/provinces")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER')")
    @Operation(summary = "Get list of all Vietnamese provinces")
    public ResponseEntity<List<String>> getProvinces() {
        return ResponseEntity.ok(VietnamConstants.PROVINCES);
    }

    @GetMapping("/constants/ethnicities")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER')")
    @Operation(summary = "Get list of all Vietnamese ethnicities")
    public ResponseEntity<List<String>> getEthnicities() {
        return ResponseEntity.ok(VietnamConstants.ETHNICITIES);
    }

    @GetMapping("/constants/priority-objects")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER')")
    @Operation(summary = "Get list of priority objects")
    public ResponseEntity<List<String>> getPriorityObjects() {
        return ResponseEntity.ok(VietnamConstants.PRIORITY_OBJECTS);
    }

    @GetMapping("/constants/religions")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER')")
    @Operation(summary = "Get list of religions")
    public ResponseEntity<List<String>> getReligions() {
        return ResponseEntity.ok(VietnamConstants.RELIGIONS);
    }

    @GetMapping("/constants/genders")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER')")
    @Operation(summary = "Get list of genders")
    public ResponseEntity<List<String>> getGenders() {
        return ResponseEntity.ok(VietnamConstants.GENDERS);
    }

    @GetMapping("/constants/blood-types")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER')")
    @Operation(summary = "Get list of blood types")
    public ResponseEntity<List<String>> getBloodTypes() {
        return ResponseEntity.ok(VietnamConstants.BLOOD_TYPES);
    }

    @GetMapping("/constants/academic-ranks")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER')")
    @Operation(summary = "Get list of academic ranks")
    public ResponseEntity<List<String>> getAcademicRanks() {
        return ResponseEntity.ok(VietnamConstants.ACADEMIC_RANKS);
    }

    @GetMapping("/constants/conduct-ranks")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER')")
    @Operation(summary = "Get list of conduct ranks")
    public ResponseEntity<List<String>> getConductRanks() {
        return ResponseEntity.ok(VietnamConstants.CONDUCT_RANKS);
    }

    @GetMapping("/constants/guardian-relationships")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER')")
    @Operation(summary = "Get list of guardian relationships")
    public ResponseEntity<List<String>> getGuardianRelationships() {
        return ResponseEntity.ok(VietnamConstants.GUARDIAN_RELATIONSHIPS);
    }

    // ============ UTILITY ENDPOINTS ============

    @PostMapping("/generate-code")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRINCIPAL')")
    @Operation(summary = "Generate student code")
    public ResponseEntity<Map<String, String>> generateStudentCode(@RequestBody StudentVN student) {
        String code = studentVNService.generateStudentCode(student);
        return ResponseEntity.ok(Map.of("studentCode", code));
    }

    @GetMapping("/validate-code/{studentCode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL')")
    @Operation(summary = "Validate student code format and check if exists")
    public ResponseEntity<Map<String, Object>> validateStudentCode(@PathVariable String studentCode) {
        boolean validFormat = VietnamConstants.isValidStudentCode(studentCode);
        boolean exists = studentVNService.getStudentByCode(studentCode).isPresent();
        
        return ResponseEntity.ok(Map.of(
            "validFormat", validFormat,
            "exists", exists,
            "available", validFormat && !exists
        ));
    }
}
