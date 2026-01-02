package com.schoolmanagement.controller;

import com.schoolmanagement.entity.ClassSubjectAssignment;
import com.schoolmanagement.service.ClassSubjectAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ClassSubjectAssignmentController {

    private final ClassSubjectAssignmentService assignmentService;

    @GetMapping
    public ResponseEntity<List<ClassSubjectAssignment>> getAllAssignments() {
        return ResponseEntity.ok(assignmentService.getAllAssignments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClassSubjectAssignment> getAssignmentById(@PathVariable Long id) {
        return assignmentService.getAssignmentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/class/{classId}")
    public ResponseEntity<List<ClassSubjectAssignment>> getByClass(@PathVariable Long classId) {
        return ResponseEntity.ok(assignmentService.getAssignmentsByClass(classId));
    }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<ClassSubjectAssignment>> getByTeacher(@PathVariable Long teacherId) {
        return ResponseEntity.ok(assignmentService.getAssignmentsByTeacher(teacherId));
    }

    @GetMapping("/semester")
    public ResponseEntity<List<ClassSubjectAssignment>> getBySemester(
            @RequestParam String academicYear,
            @RequestParam Integer semester) {
        return ResponseEntity.ok(assignmentService.getAssignmentsBySemester(academicYear, semester));
    }

    @GetMapping("/teacher/{teacherId}/workload")
    public ResponseEntity<Integer> getTeacherWorkload(
            @PathVariable Long teacherId,
            @RequestParam String academicYear,
            @RequestParam Integer semester) {
        Integer workload = assignmentService.calculateTeacherWorkload(teacherId, academicYear, semester);
        return ResponseEntity.ok(workload != null ? workload : 0);
    }

    @PostMapping
    public ResponseEntity<ClassSubjectAssignment> createAssignment(@RequestBody ClassSubjectAssignment assignment) {
        try {
            ClassSubjectAssignment created = assignmentService.createAssignment(assignment);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClassSubjectAssignment> updateAssignment(
            @PathVariable Long id,
            @RequestBody ClassSubjectAssignment assignment) {
        try {
            ClassSubjectAssignment updated = assignmentService.updateAssignment(id, assignment);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long id) {
        try {
            assignmentService.deleteAssignment(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}
