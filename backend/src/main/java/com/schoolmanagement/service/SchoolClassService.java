package com.schoolmanagement.service;

import com.schoolmanagement.dto.SchoolClassDTO;
import com.schoolmanagement.dto.StudentDTO;
import com.schoolmanagement.entity.SchoolClass;
import com.schoolmanagement.entity.Staff;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.exception.DuplicateResourceException;
import com.schoolmanagement.exception.ResourceInUseException;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.repository.SchoolClassRepository;
import com.schoolmanagement.repository.StaffRepository;
import com.schoolmanagement.repository.StudentRepository;
import com.schoolmanagement.security.TeacherHomeroomGuard;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class SchoolClassService {

    private SchoolClassRepository schoolClassRepository;
    private StaffRepository staffRepository;
    private StudentRepository studentRepository;
    private StudentService studentService;
    private TeacherHomeroomGuard teacherHomeroomGuard;

    public SchoolClassDTO createClass(SchoolClass schoolClass) {
        assertNoDuplicate(schoolClass.getClassName(), schoolClass.getSection(), schoolClass.getAcademicYear(), null);

        SchoolClass savedClass = schoolClassRepository.save(schoolClass);
        return mapToDTO(savedClass);
    }

    public SchoolClassDTO updateClass(Long id, SchoolClass classDetails) {
        SchoolClass schoolClass = schoolClassRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + id));

        assertNoDuplicate(classDetails.getClassName(), classDetails.getSection(), classDetails.getAcademicYear(), id);

        schoolClass.setClassName(classDetails.getClassName());
        schoolClass.setSection(classDetails.getSection());
        schoolClass.setCapacity(classDetails.getCapacity());
        schoolClass.setAcademicYear(classDetails.getAcademicYear());
        schoolClass.setRoomNumber(classDetails.getRoomNumber());

        SchoolClass updatedClass = schoolClassRepository.save(schoolClass);
        return mapToDTO(updatedClass);
    }

    public SchoolClassDTO getClassById(Long id) {
        SchoolClass schoolClass = schoolClassRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + id));
        return mapToDTO(schoolClass);
    }

    public List<SchoolClassDTO> getAllClasses() {
        return schoolClassRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public Page<SchoolClassDTO> getAllClasses(Pageable pageable) {
        return schoolClassRepository.findAll(pageable).map(this::mapToDTO);
    }

    public List<SchoolClassDTO> getClassesByAcademicYear(String academicYear) {
        return schoolClassRepository.findByAcademicYear(academicYear)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public SchoolClassDTO assignClassTeacher(Long classId, Long staffId) {
        SchoolClass schoolClass = schoolClassRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + classId));
        Staff teacher = staffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + staffId));

        schoolClass.setClassTeacher(teacher);
        SchoolClass updatedClass = schoolClassRepository.save(schoolClass);
        return mapToDTO(updatedClass);
    }

    public List<StudentDTO> getStudentsInClass(Long classId, User requester) {
        SchoolClass schoolClass = schoolClassRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + classId));
        teacherHomeroomGuard.enforceHomeroomClassId(classId, requester);
        return studentService.getStudentsByClassAndSection(schoolClass.getClassName(), schoolClass.getSection(), requester);
    }

    public void deleteClass(Long id) {
        SchoolClass schoolClass = schoolClassRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + id));

        long studentCount = studentRepository.countByClassNameAndSection(schoolClass.getClassName(), schoolClass.getSection());
        if (studentCount > 0) {
            throw new ResourceInUseException(
                    "Cannot delete class: " + studentCount + " student(s) are still assigned to it");
        }

        schoolClassRepository.delete(schoolClass);
    }

    private void assertNoDuplicate(String className, String section, String academicYear, Long excludeId) {
        schoolClassRepository.findByClassNameAndSectionAndAcademicYear(className, section, academicYear)
                .filter(existing -> !existing.getId().equals(excludeId))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException(
                            "Class already exists: " + className + " - " + section + " (" + academicYear + ")");
                });
    }

    private SchoolClassDTO mapToDTO(SchoolClass schoolClass) {
        Staff teacher = schoolClass.getClassTeacher();
        long studentCount = studentRepository.countByClassNameAndSection(schoolClass.getClassName(), schoolClass.getSection());

        return SchoolClassDTO.builder()
                .id(schoolClass.getId())
                .className(schoolClass.getClassName())
                .section(schoolClass.getSection())
                .capacity(schoolClass.getCapacity())
                .classTeacherId(teacher != null ? teacher.getId() : null)
                .classTeacherName(teacher != null && teacher.getUser() != null
                        ? teacher.getUser().getFirstName() + " " + teacher.getUser().getLastName()
                        : null)
                .academicYear(schoolClass.getAcademicYear())
                .roomNumber(schoolClass.getRoomNumber())
                .studentCount((int) studentCount)
                .createdAt(schoolClass.getCreatedAt())
                .updatedAt(schoolClass.getUpdatedAt())
                .build();
    }
}
