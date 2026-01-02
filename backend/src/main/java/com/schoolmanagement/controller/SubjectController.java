package com.schoolmanagement.controller;

import com.schoolmanagement.entity.Subject;
import com.schoolmanagement.service.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SubjectController {

    private final SubjectService subjectService;

    @GetMapping
    public ResponseEntity<List<Subject>> getAllSubjects() {
        return ResponseEntity.ok(subjectService.getAllSubjects());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Subject> getSubjectById(@PathVariable Long id) {
        return subjectService.getSubjectById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<Subject> getByCode(@PathVariable String code) {
        return subjectService.getSubjectByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/school-type/{type}")
    public ResponseEntity<List<Subject>> getBySchoolType(@PathVariable String type) {
        Subject.SchoolType schoolType = Subject.SchoolType.valueOf(type.toUpperCase());
        return ResponseEntity.ok(subjectService.getSubjectsBySchoolType(schoolType));
    }

    @GetMapping("/middle-school")
    public ResponseEntity<List<Subject>> getMiddleSchool() {
        return ResponseEntity.ok(subjectService.getMiddleSchoolSubjects());
    }

    @GetMapping("/high-school")
    public ResponseEntity<List<Subject>> getHighSchool() {
        return ResponseEntity.ok(subjectService.getHighSchoolSubjects());
    }

    @GetMapping("/required")
    public ResponseEntity<List<Subject>> getRequired() {
        return ResponseEntity.ok(subjectService.getRequiredSubjects());
    }

    @GetMapping("/optional")
    public ResponseEntity<List<Subject>> getOptional() {
        return ResponseEntity.ok(subjectService.getOptionalSubjects());
    }

    @PostMapping
    public ResponseEntity<Subject> createSubject(@RequestBody Subject subject) {
        try {
            Subject created = subjectService.createSubject(subject);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Subject> updateSubject(@PathVariable Long id, @RequestBody Subject subject) {
        try {
            Subject updated = subjectService.updateSubject(id, subject);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubject(@PathVariable Long id) {
        try {
            subjectService.deleteSubject(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}
