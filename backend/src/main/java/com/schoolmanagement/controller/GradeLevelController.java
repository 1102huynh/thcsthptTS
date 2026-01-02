package com.schoolmanagement.controller;

import com.schoolmanagement.entity.GradeLevel;
import com.schoolmanagement.service.GradeLevelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grade-levels")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GradeLevelController {

    private final GradeLevelService gradeLevelService;

    @GetMapping
    public ResponseEntity<List<GradeLevel>> getAllGradeLevels() {
        return ResponseEntity.ok(gradeLevelService.getAllGradeLevels());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GradeLevel> getGradeLevelById(@PathVariable Long id) {
        return gradeLevelService.getGradeLevelById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/academic-year/{year}")
    public ResponseEntity<List<GradeLevel>> getByAcademicYear(@PathVariable String year) {
        return ResponseEntity.ok(gradeLevelService.getGradeLevelsByAcademicYear(year));
    }

    @GetMapping("/school-type/{type}")
    public ResponseEntity<List<GradeLevel>> getBySchoolType(@PathVariable String type) {
        GradeLevel.SchoolType schoolType = GradeLevel.SchoolType.valueOf(type.toUpperCase());
        return ResponseEntity.ok(gradeLevelService.getGradeLevelsBySchoolType(schoolType));
    }

    @GetMapping("/current")
    public ResponseEntity<List<GradeLevel>> getCurrentAcademicYear() {
        return ResponseEntity.ok(gradeLevelService.getCurrentAcademicYearGradeLevels());
    }

    @GetMapping("/middle-school")
    public ResponseEntity<List<GradeLevel>> getMiddleSchool(@RequestParam String year) {
        return ResponseEntity.ok(gradeLevelService.getMiddleSchoolGradeLevels(year));
    }

    @GetMapping("/high-school")
    public ResponseEntity<List<GradeLevel>> getHighSchool(@RequestParam String year) {
        return ResponseEntity.ok(gradeLevelService.getHighSchoolGradeLevels(year));
    }

    @PostMapping
    public ResponseEntity<GradeLevel> createGradeLevel(@RequestBody GradeLevel gradeLevel) {
        try {
            GradeLevel created = gradeLevelService.createGradeLevel(gradeLevel);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<GradeLevel> updateGradeLevel(@PathVariable Long id, @RequestBody GradeLevel gradeLevel) {
        try {
            GradeLevel updated = gradeLevelService.updateGradeLevel(id, gradeLevel);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGradeLevel(@PathVariable Long id) {
        try {
            gradeLevelService.deleteGradeLevel(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}
