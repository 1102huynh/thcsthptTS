package com.schoolmanagement.service;

import com.schoolmanagement.entity.SchoolClass;
import com.schoolmanagement.repository.SchoolClassRepository;
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
public class SchoolClassService {

    private final SchoolClassRepository schoolClassRepository;

    /**
     * Get all classes
     */
    public List<SchoolClass> getAllClasses() {
        log.info("Fetching all classes");
        return schoolClassRepository.findAll();
    }

    /**
     * Get class by ID
     */
    public Optional<SchoolClass> getClassById(Long id) {
        log.info("Fetching class with id: {}", id);
        return schoolClassRepository.findById(id);
    }

    /**
     * Get classes by academic year
     */
    public List<SchoolClass> getClassesByAcademicYear(String academicYear) {
        log.info("Fetching classes for academic year: {}", academicYear);
        return schoolClassRepository.findByAcademicYear(academicYear);
    }

    /**
     * Get classes by grade level
     */
    public List<SchoolClass> getClassesByGradeLevel(Long gradeLevelId) {
        log.info("Fetching classes for grade level: {}", gradeLevelId);
        return schoolClassRepository.findByGradeLevelId(gradeLevelId);
    }

    /**
     * Get classes by homeroom teacher
     */
    public List<SchoolClass> getClassesByHomeroomTeacher(Long teacherId) {
        log.info("Fetching classes for homeroom teacher: {}", teacherId);
        return schoolClassRepository.findByHomeroomTeacherId(teacherId);
    }

    /**
     * Get classes with available slots
     */
    public List<SchoolClass> getClassesWithAvailableSlots() {
        log.info("Fetching classes with available slots");
        return schoolClassRepository.findClassesWithAvailableSlots();
    }

    /**
     * Create new class
     */
    public SchoolClass createClass(SchoolClass schoolClass) {
        log.info("Creating new class: {}", schoolClass.getClassName());
        
        // Validate
        if (schoolClassRepository.existsByClassNameAndAcademicYear(
                schoolClass.getClassName(), schoolClass.getAcademicYear())) {
            throw new IllegalArgumentException(
                "Class " + schoolClass.getClassName() + " already exists for academic year " + schoolClass.getAcademicYear()
            );
        }

        // Validate max students
        if (schoolClass.getMaxStudents() == null || schoolClass.getMaxStudents() <= 0) {
            schoolClass.setMaxStudents(40); // Default
        }

        // Initialize current students
        if (schoolClass.getCurrentStudents() == null) {
            schoolClass.setCurrentStudents(0);
        }

        return schoolClassRepository.save(schoolClass);
    }

    /**
     * Update class
     */
    public SchoolClass updateClass(Long id, SchoolClass classDetails) {
        log.info("Updating class with id: {}", id);
        
        SchoolClass schoolClass = schoolClassRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Class not found with id: " + id));

        // Update fields
        if (classDetails.getFullName() != null) {
            schoolClass.setFullName(classDetails.getFullName());
        }
        if (classDetails.getHomeroomTeacher() != null) {
            schoolClass.setHomeroomTeacher(classDetails.getHomeroomTeacher());
        }
        if (classDetails.getMaxStudents() != null) {
            schoolClass.setMaxStudents(classDetails.getMaxStudents());
        }
        if (classDetails.getRoomNumber() != null) {
            schoolClass.setRoomNumber(classDetails.getRoomNumber());
        }
        if (classDetails.getStatus() != null) {
            schoolClass.setStatus(classDetails.getStatus());
        }

        return schoolClassRepository.save(schoolClass);
    }

    /**
     * Assign homeroom teacher
     */
    public SchoolClass assignHomeroomTeacher(Long classId, Long teacherId) {
        log.info("Assigning homeroom teacher {} to class {}", teacherId, classId);
        
        SchoolClass schoolClass = schoolClassRepository.findById(classId)
            .orElseThrow(() -> new IllegalArgumentException("Class not found with id: " + classId));

        // Note: Staff entity should be fetched from StaffRepository
        // For now, we assume teacherId is valid
        // schoolClass.setHomeroomTeacher(staff);

        return schoolClassRepository.save(schoolClass);
    }

    /**
     * Update student count
     */
    public SchoolClass updateStudentCount(Long classId, Integer newCount) {
        log.info("Updating student count for class {} to {}", classId, newCount);
        
        SchoolClass schoolClass = schoolClassRepository.findById(classId)
            .orElseThrow(() -> new IllegalArgumentException("Class not found with id: " + classId));

        if (newCount > schoolClass.getMaxStudents()) {
            throw new IllegalArgumentException(
                "Student count (" + newCount + ") exceeds max students (" + schoolClass.getMaxStudents() + ")"
            );
        }

        schoolClass.setCurrentStudents(newCount);
        return schoolClassRepository.save(schoolClass);
    }

    /**
     * Increment student count (when new student joins)
     */
    public SchoolClass incrementStudentCount(Long classId) {
        log.info("Incrementing student count for class {}", classId);
        
        SchoolClass schoolClass = schoolClassRepository.findById(classId)
            .orElseThrow(() -> new IllegalArgumentException("Class not found with id: " + classId));

        if (schoolClass.isFull()) {
            throw new IllegalStateException("Class is full. Cannot add more students.");
        }

        schoolClass.setCurrentStudents(schoolClass.getCurrentStudents() + 1);
        return schoolClassRepository.save(schoolClass);
    }

    /**
     * Decrement student count (when student leaves)
     */
    public SchoolClass decrementStudentCount(Long classId) {
        log.info("Decrementing student count for class {}", classId);
        
        SchoolClass schoolClass = schoolClassRepository.findById(classId)
            .orElseThrow(() -> new IllegalArgumentException("Class not found with id: " + classId));

        if (schoolClass.getCurrentStudents() > 0) {
            schoolClass.setCurrentStudents(schoolClass.getCurrentStudents() - 1);
        }

        return schoolClassRepository.save(schoolClass);
    }

    /**
     * Delete class
     */
    public void deleteClass(Long id) {
        log.info("Deleting class with id: {}", id);
        
        SchoolClass schoolClass = schoolClassRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Class not found with id: " + id));

        // Check if has students
        if (schoolClass.getCurrentStudents() != null && schoolClass.getCurrentStudents() > 0) {
            throw new IllegalStateException("Cannot delete class with students");
        }

        schoolClassRepository.delete(schoolClass);
    }

    /**
     * Check if class is full
     */
    public boolean isClassFull(Long classId) {
        SchoolClass schoolClass = schoolClassRepository.findById(classId)
            .orElseThrow(() -> new IllegalArgumentException("Class not found with id: " + classId));
        return schoolClass.isFull();
    }

    /**
     * Get class occupancy rate
     */
    public double getOccupancyRate(Long classId) {
        SchoolClass schoolClass = schoolClassRepository.findById(classId)
            .orElseThrow(() -> new IllegalArgumentException("Class not found with id: " + classId));
        return schoolClass.getOccupancyRate();
    }
}
