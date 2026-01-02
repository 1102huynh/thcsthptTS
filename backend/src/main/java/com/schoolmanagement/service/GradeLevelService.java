package com.schoolmanagement.service;

import com.schoolmanagement.entity.GradeLevel;
import com.schoolmanagement.repository.GradeLevelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class GradeLevelService {

    private final GradeLevelRepository gradeLevelRepository;

    /**
     * Get all grade levels
     */
    public List<GradeLevel> getAllGradeLevels() {
        log.info("Fetching all grade levels");
        return gradeLevelRepository.findAll();
    }

    /**
     * Get grade level by ID
     */
    public Optional<GradeLevel> getGradeLevelById(Long id) {
        log.info("Fetching grade level with id: {}", id);
        return gradeLevelRepository.findById(id);
    }

    /**
     * Get grade levels by academic year
     */
    public List<GradeLevel> getGradeLevelsByAcademicYear(String academicYear) {
        log.info("Fetching grade levels for academic year: {}", academicYear);
        return gradeLevelRepository.findByAcademicYear(academicYear);
    }

    /**
     * Get grade levels by school type
     */
    public List<GradeLevel> getGradeLevelsBySchoolType(GradeLevel.SchoolType schoolType) {
        log.info("Fetching grade levels for school type: {}", schoolType);
        return gradeLevelRepository.findBySchoolType(schoolType);
    }

    /**
     * Get current academic year grade levels
     */
    public List<GradeLevel> getCurrentAcademicYearGradeLevels() {
        log.info("Fetching current academic year grade levels");
        return gradeLevelRepository.findCurrentAcademicYearGradeLevels();
    }

    /**
     * Get middle school grade levels (THCS)
     */
    public List<GradeLevel> getMiddleSchoolGradeLevels(String academicYear) {
        log.info("Fetching middle school grade levels for year: {}", academicYear);
        return gradeLevelRepository.findByAcademicYearAndSchoolType(academicYear, GradeLevel.SchoolType.THCS);
    }

    /**
     * Get high school grade levels (THPT)
     */
    public List<GradeLevel> getHighSchoolGradeLevels(String academicYear) {
        log.info("Fetching high school grade levels for year: {}", academicYear);
        return gradeLevelRepository.findByAcademicYearAndSchoolType(academicYear, GradeLevel.SchoolType.THPT);
    }

    /**
     * Create new grade level
     */
    public GradeLevel createGradeLevel(GradeLevel gradeLevel) {
        log.info("Creating new grade level: {} for year: {}", gradeLevel.getLevelName(), gradeLevel.getAcademicYear());
        
        // Validate
        if (gradeLevelRepository.existsByLevelNumberAndAcademicYear(
                gradeLevel.getLevelNumber(), gradeLevel.getAcademicYear())) {
            throw new IllegalArgumentException(
                "Grade level " + gradeLevel.getLevelNumber() + " already exists for academic year " + gradeLevel.getAcademicYear()
            );
        }

        // Auto-set level name if not provided
        if (gradeLevel.getLevelName() == null || gradeLevel.getLevelName().isEmpty()) {
            gradeLevel.setLevelName("Khối " + gradeLevel.getLevelNumber());
        }

        // Auto-determine school type based on level number
        if (gradeLevel.getSchoolType() == null) {
            gradeLevel.setSchoolType(
                gradeLevel.getLevelNumber() <= 9 ? GradeLevel.SchoolType.THCS : GradeLevel.SchoolType.THPT
            );
        }

        return gradeLevelRepository.save(gradeLevel);
    }

    /**
     * Update grade level
     */
    public GradeLevel updateGradeLevel(Long id, GradeLevel gradeLevelDetails) {
        log.info("Updating grade level with id: {}", id);
        
        GradeLevel gradeLevel = gradeLevelRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Grade level not found with id: " + id));

        // Update fields
        if (gradeLevelDetails.getLevelName() != null) {
            gradeLevel.setLevelName(gradeLevelDetails.getLevelName());
        }
        if (gradeLevelDetails.getDescription() != null) {
            gradeLevel.setDescription(gradeLevelDetails.getDescription());
        }
        if (gradeLevelDetails.getHeadTeacher() != null) {
            gradeLevel.setHeadTeacher(gradeLevelDetails.getHeadTeacher());
        }
        if (gradeLevelDetails.getStatus() != null) {
            gradeLevel.setStatus(gradeLevelDetails.getStatus());
        }

        return gradeLevelRepository.save(gradeLevel);
    }

    /**
     * Delete grade level
     */
    public void deleteGradeLevel(Long id) {
        log.info("Deleting grade level with id: {}", id);
        
        GradeLevel gradeLevel = gradeLevelRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Grade level not found with id: " + id));

        // Check if has classes
        if (gradeLevel.getClasses() != null && !gradeLevel.getClasses().isEmpty()) {
            throw new IllegalStateException("Cannot delete grade level with existing classes");
        }

        gradeLevelRepository.delete(gradeLevel);
    }

    /**
     * Check if grade level exists
     */
    public boolean existsByLevelNumberAndAcademicYear(Integer levelNumber, String academicYear) {
        return gradeLevelRepository.existsByLevelNumberAndAcademicYear(levelNumber, academicYear);
    }
}
