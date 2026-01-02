package com.schoolmanagement.controller;

import com.schoolmanagement.entity.SchoolClass;
import com.schoolmanagement.service.SchoolClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SchoolClassController {

    private final SchoolClassService schoolClassService;

    @GetMapping
    public ResponseEntity<List<SchoolClass>> getAllClasses() {
        return ResponseEntity.ok(schoolClassService.getAllClasses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SchoolClass> getClassById(@PathVariable Long id) {
        return schoolClassService.getClassById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/academic-year/{year}")
    public ResponseEntity<List<SchoolClass>> getByAcademicYear(@PathVariable String year) {
        return ResponseEntity.ok(schoolClassService.getClassesByAcademicYear(year));
    }

    @GetMapping("/grade-level/{gradeLevelId}")
    public ResponseEntity<List<SchoolClass>> getByGradeLevel(@PathVariable Long gradeLevelId) {
        return ResponseEntity.ok(schoolClassService.getClassesByGradeLevel(gradeLevelId));
    }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<SchoolClass>> getByHomeroomTeacher(@PathVariable Long teacherId) {
        return ResponseEntity.ok(schoolClassService.getClassesByHomeroomTeacher(teacherId));
    }

    @GetMapping("/available")
    public ResponseEntity<List<SchoolClass>> getAvailable() {
        return ResponseEntity.ok(schoolClassService.getClassesWithAvailableSlots());
    }

    @PostMapping
    public ResponseEntity<SchoolClass> createClass(@RequestBody SchoolClass schoolClass) {
        try {
            SchoolClass created = schoolClassService.createClass(schoolClass);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<SchoolClass> updateClass(@PathVariable Long id, @RequestBody SchoolClass schoolClass) {
        try {
            SchoolClass updated = schoolClassService.updateClass(id, schoolClass);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/students/increment")
    public ResponseEntity<SchoolClass> incrementStudents(@PathVariable Long id) {
        try {
            SchoolClass updated = schoolClassService.incrementStudentCount(id);
            return ResponseEntity.ok(updated);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PutMapping("/{id}/students/decrement")
    public ResponseEntity<SchoolClass> decrementStudents(@PathVariable Long id) {
        SchoolClass updated = schoolClassService.decrementStudentCount(id);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClass(@PathVariable Long id) {
        try {
            schoolClassService.deleteClass(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}
