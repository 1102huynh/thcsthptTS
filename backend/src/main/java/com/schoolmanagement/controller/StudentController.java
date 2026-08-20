package com.schoolmanagement.controller;

import com.schoolmanagement.dto.StudentDTO;
import com.schoolmanagement.entity.Student;
import com.schoolmanagement.service.StudentService;
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
@RequestMapping("/v1/students")
@AllArgsConstructor
@Tag(name = "Student Management", description = "Student management endpoints")
public class StudentController {

    private StudentService studentService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRINCIPAL')")
    @Operation(summary = "Create new student")
    public ResponseEntity<StudentDTO> createStudent(@Valid @RequestBody Student student) {
        StudentDTO createdStudent = studentService.createStudent(student);
        return new ResponseEntity<>(createdStudent, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRINCIPAL')")
    @Operation(summary = "Update student")
    public ResponseEntity<StudentDTO> updateStudent(@PathVariable Long id, @Valid @RequestBody Student studentDetails) {
        StudentDTO updatedStudent = studentService.updateStudent(id, studentDetails);
        return new ResponseEntity<>(updatedStudent, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get student by ID")
    public ResponseEntity<StudentDTO> getStudentById(@PathVariable Long id) {
        StudentDTO student = studentService.getStudentById(id);
        return new ResponseEntity<>(student, HttpStatus.OK);
    }

    @GetMapping("/roll/{rollNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get student by roll number")
    public ResponseEntity<StudentDTO> getStudentByRollNumber(@PathVariable String rollNumber) {
        StudentDTO student = studentService.getStudentByRollNumber(rollNumber);
        return new ResponseEntity<>(student, HttpStatus.OK);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER')")
    @Operation(summary = "Get all students",
            description = "Optional page/size query params paginate the result (0-indexed page); omit both to get the full list. Total count is returned in the X-Total-Count header when paginated.")
    public ResponseEntity<List<StudentDTO>> getAllStudents(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        Pageable pageable = PaginationUtil.toPageable(page, size);
        if (pageable == null) {
            return new ResponseEntity<>(studentService.getAllStudents(), HttpStatus.OK);
        }
        Page<StudentDTO> result = studentService.getAllStudents(pageable);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(result.getTotalElements()))
                .body(result.getContent());
    }

    @GetMapping("/class/{className}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER')")
    @Operation(summary = "Get students by class")
    public ResponseEntity<List<StudentDTO>> getStudentsByClass(@PathVariable String className) {
        List<StudentDTO> students = studentService.getStudentsByClass(className);
        return new ResponseEntity<>(students, HttpStatus.OK);
    }

    @GetMapping("/class/{className}/section/{section}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER')")
    @Operation(summary = "Get students by class and section")
    public ResponseEntity<List<StudentDTO>> getStudentsByClassAndSection(
            @PathVariable String className,
            @PathVariable String section) {
        List<StudentDTO> students = studentService.getStudentsByClassAndSection(className, section);
        return new ResponseEntity<>(students, HttpStatus.OK);
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL')")
    @Operation(summary = "Get all active students")
    public ResponseEntity<List<StudentDTO>> getActiveStudents() {
        List<StudentDTO> students = studentService.getActiveStudents();
        return new ResponseEntity<>(students, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRINCIPAL')")
    @Operation(summary = "Delete student")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

