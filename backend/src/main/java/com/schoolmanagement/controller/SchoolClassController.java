package com.schoolmanagement.controller;

import com.schoolmanagement.dto.SchoolClassDTO;
import com.schoolmanagement.dto.StudentDTO;
import com.schoolmanagement.entity.SchoolClass;
import com.schoolmanagement.service.SchoolClassService;
import com.schoolmanagement.util.PaginationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/classes")
@AllArgsConstructor
@Tag(name = "Class Management", description = "School class management endpoints")
public class SchoolClassController {

    private SchoolClassService schoolClassService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRINCIPAL')")
    @Operation(summary = "Create new class")
    public ResponseEntity<SchoolClassDTO> createClass(@Valid @RequestBody SchoolClass schoolClass) {
        SchoolClassDTO createdClass = schoolClassService.createClass(schoolClass);
        return new ResponseEntity<>(createdClass, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRINCIPAL')")
    @Operation(summary = "Update class")
    public ResponseEntity<SchoolClassDTO> updateClass(@PathVariable Long id, @Valid @RequestBody SchoolClass classDetails) {
        SchoolClassDTO updatedClass = schoolClassService.updateClass(id, classDetails);
        return new ResponseEntity<>(updatedClass, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER')")
    @Operation(summary = "Get class by ID")
    public ResponseEntity<SchoolClassDTO> getClassById(@PathVariable Long id) {
        SchoolClassDTO schoolClass = schoolClassService.getClassById(id);
        return new ResponseEntity<>(schoolClass, HttpStatus.OK);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER')")
    @Operation(summary = "Get all classes",
            description = "Optional page/size query params paginate the result (0-indexed page); omit both to get the full list. Total count is returned in the X-Total-Count header when paginated.")
    public ResponseEntity<List<SchoolClassDTO>> getAllClasses(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        Pageable pageable = PaginationUtil.toPageable(page, size);
        if (pageable == null) {
            return new ResponseEntity<>(schoolClassService.getAllClasses(), HttpStatus.OK);
        }
        Page<SchoolClassDTO> result = schoolClassService.getAllClasses(pageable);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(result.getTotalElements()))
                .body(result.getContent());
    }

    @GetMapping("/year/{academicYear}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRINCIPAL')")
    @Operation(summary = "Get classes by academic year")
    public ResponseEntity<List<SchoolClassDTO>> getClassesByAcademicYear(@PathVariable String academicYear) {
        List<SchoolClassDTO> classes = schoolClassService.getClassesByAcademicYear(academicYear);
        return new ResponseEntity<>(classes, HttpStatus.OK);
    }

    @GetMapping("/{id}/students")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER')")
    @Operation(summary = "Get students in a class")
    public ResponseEntity<List<StudentDTO>> getStudentsInClass(@PathVariable Long id) {
        List<StudentDTO> students = schoolClassService.getStudentsInClass(id);
        return new ResponseEntity<>(students, HttpStatus.OK);
    }

    @PutMapping("/{id}/teacher/{staffId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRINCIPAL')")
    @Operation(summary = "Assign or change the homeroom teacher for a class")
    public ResponseEntity<SchoolClassDTO> assignClassTeacher(@PathVariable Long id, @PathVariable Long staffId) {
        SchoolClassDTO updatedClass = schoolClassService.assignClassTeacher(id, staffId);
        return new ResponseEntity<>(updatedClass, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRINCIPAL')")
    @Operation(summary = "Delete class", description = "Refused with 409 if the class still has students assigned to it.")
    public ResponseEntity<Void> deleteClass(@PathVariable Long id) {
        schoolClassService.deleteClass(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
