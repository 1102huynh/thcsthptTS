package com.schoolmanagement.controller;

import com.schoolmanagement.dto.StudentDTO;
import com.schoolmanagement.entity.Student;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.security.StudentAccessGuard;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/students")
@AllArgsConstructor
@Tag(name = "Student Management", description = "Student management endpoints")
public class StudentController {

    private StudentService studentService;
    private StudentAccessGuard studentAccessGuard;

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

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Get the calling student's own record",
            description = "Resolves the Student row linked to the authenticated STUDENT account - the self-service portal (C3) has the user id from the JWT but not the student id. 404 if the account has no linked student profile.")
    public ResponseEntity<StudentDTO> getMyStudentRecord(Authentication authentication) {
        User requester = (User) authentication.getPrincipal();
        return new ResponseEntity<>(studentService.getStudentByUserId(requester.getId()), HttpStatus.OK);
    }

    // ACCOUNTANT read-only since Mức 2.2 (v4.10): an accountant needs the
    // student directory to know whose fee they're recording - the create-fee
    // form's student picker calls GET /v1/students, which 403'd for
    // ACCOUNTANT before this. StudentAccessGuard leaves non-STUDENT/PARENT
    // callers alone, so ACCOUNTANT sees every student like ADMIN/TEACHER do.
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER', 'ACCOUNTANT', 'STUDENT', 'PARENT')")
    @Operation(summary = "Get student by ID",
            description = "A STUDENT caller may only fetch their own record, a PARENT only their child's (403 otherwise) - see StudentAccessGuard.")
    public ResponseEntity<StudentDTO> getStudentById(@PathVariable Long id, Authentication authentication) {
        User requester = (User) authentication.getPrincipal();
        studentAccessGuard.enforceCanAccessStudent(id, requester);
        StudentDTO student = studentService.getStudentById(id);
        return new ResponseEntity<>(student, HttpStatus.OK);
    }

    @GetMapping("/roll/{rollNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER', 'STUDENT', 'PARENT')")
    @Operation(summary = "Get student by roll number",
            description = "A STUDENT caller may only fetch their own record, a PARENT only their child's (403 otherwise) - see StudentAccessGuard.")
    public ResponseEntity<StudentDTO> getStudentByRollNumber(@PathVariable String rollNumber, Authentication authentication) {
        User requester = (User) authentication.getPrincipal();
        StudentDTO student = studentService.getStudentByRollNumber(rollNumber);
        studentAccessGuard.enforceCanAccessStudent(student.getId(), requester);
        return new ResponseEntity<>(student, HttpStatus.OK);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER', 'ACCOUNTANT')")
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
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id, Authentication authentication) {
        User actor = (User) authentication.getPrincipal();
        studentService.deleteStudent(id, actor);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

