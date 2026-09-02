package com.schoolmanagement.service;

import com.schoolmanagement.dto.GradeDTO;
import com.schoolmanagement.entity.Grade;
import com.schoolmanagement.entity.Staff;
import com.schoolmanagement.entity.Student;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.repository.GradeRepository;
import com.schoolmanagement.repository.StudentRepository;
import com.schoolmanagement.security.StudentAccessGuard;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @deprecated Legacy percentage-based grade logic behind {@link com.schoolmanagement.controller.GradeController}.
 * Use {@link GradeRecordService} (TT22). Removed together with the rest of
 * the legacy Grade layer under Quyết định E.1 (KE_HOACH_NANG_CAP_V4.md).
 */
@Deprecated
@Service
@AllArgsConstructor
@Transactional
public class GradeService {

    private GradeRepository gradeRepository;
    private StudentRepository studentRepository;
    private StudentAccessGuard studentAccessGuard;

    public Grade createGrade(Grade grade) {
        Student student = studentRepository.findById(grade.getStudent().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        grade.setPercentage((grade.getMarksObtained() / grade.getTotalMarks()) * 100);
        grade.setGrade(calculateGrade(grade.getPercentage()));

        return gradeRepository.save(grade);
    }

    public GradeDTO updateGrade(Long id, Grade gradeDetails) {
        Grade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade record not found"));

        grade.setMarksObtained(gradeDetails.getMarksObtained());
        grade.setTotalMarks(gradeDetails.getTotalMarks());
        grade.setPercentage((gradeDetails.getMarksObtained() / gradeDetails.getTotalMarks()) * 100);
        grade.setGrade(calculateGrade(grade.getPercentage()));
        grade.setRemarks(gradeDetails.getRemarks());

        return mapToDTO(gradeRepository.save(grade));
    }

    public GradeDTO getGradeById(Long id, User requester) {
        Grade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade record not found"));
        studentAccessGuard.enforceCanAccessStudent(grade.getStudent().getId(), requester);
        return mapToDTO(grade);
    }

    public List<GradeDTO> getStudentGrades(Long studentId, User requester) {
        studentAccessGuard.enforceCanAccessStudent(studentId, requester);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        return gradeRepository.findByStudent(student).stream().map(this::mapToDTO).toList();
    }

    public List<GradeDTO> getStudentGradesByAcademicYear(Long studentId, String academicYear, User requester) {
        studentAccessGuard.enforceCanAccessStudent(studentId, requester);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        return gradeRepository.findByStudentAndAcademicYear(student, academicYear).stream().map(this::mapToDTO).toList();
    }

    public List<GradeDTO> getStudentGradesBySubject(Long studentId, String subject, User requester) {
        studentAccessGuard.enforceCanAccessStudent(studentId, requester);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        return gradeRepository.findByStudentAndSubject(student, subject).stream().map(this::mapToDTO).toList();
    }

    /**
     * Every read path here returns GradeDTO, never the raw entity — its lazy
     * `student`/`teacher` associations are not resolved by the time Jackson
     * serializes the response (open-in-view is off, so the persistence
     * context is already closed), which throws LazyInitializationException.
     * (Found live while retrofitting PARENT access in 3.6 — pre-existing,
     * affected every role, not just the new one; fixed for all of these
     * methods at once rather than only the ones PARENT needed.)
     */
    public List<GradeDTO> getGradesByAcademicYear(String academicYear) {
        return gradeRepository.findByAcademicYear(academicYear)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    public Page<GradeDTO> getGradesByAcademicYear(String academicYear, Pageable pageable) {
        return gradeRepository.findByAcademicYear(academicYear, pageable).map(this::mapToDTO);
    }

    public double getStudentAveragePercentage(Long studentId, User requester) {
        List<GradeDTO> grades = getStudentGrades(studentId, requester);
        if (grades.isEmpty()) {
            return 0;
        }
        return grades.stream()
                .mapToDouble(GradeDTO::getPercentage)
                .average()
                .orElse(0);
    }

    public double getStudentAveragePercentageByYear(Long studentId, String academicYear, User requester) {
        List<GradeDTO> grades = getStudentGradesByAcademicYear(studentId, academicYear, requester);
        if (grades.isEmpty()) {
            return 0;
        }
        return grades.stream()
                .mapToDouble(GradeDTO::getPercentage)
                .average()
                .orElse(0);
    }

    public void deleteGrade(Long id) {
        Grade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade record not found"));
        gradeRepository.delete(grade);
    }

    private GradeDTO mapToDTO(Grade grade) {
        Student student = grade.getStudent();
        Staff teacher = grade.getTeacher();

        return GradeDTO.builder()
                .id(grade.getId())
                .studentId(student != null ? student.getId() : null)
                .studentName(student != null && student.getUser() != null
                        ? student.getUser().getFirstName() + " " + student.getUser().getLastName()
                        : null)
                .subject(grade.getSubject())
                .examType(grade.getExamType())
                .marksObtained(grade.getMarksObtained())
                .totalMarks(grade.getTotalMarks())
                .percentage(grade.getPercentage())
                .grade(grade.getGrade())
                .teacherId(teacher != null ? teacher.getId() : null)
                .teacherName(teacher != null && teacher.getUser() != null
                        ? teacher.getUser().getFirstName() + " " + teacher.getUser().getLastName()
                        : null)
                .academicYear(grade.getAcademicYear())
                .remarks(grade.getRemarks())
                .createdAt(grade.getCreatedAt())
                .updatedAt(grade.getUpdatedAt())
                .build();
    }

    private String calculateGrade(Double percentage) {
        if (percentage >= 90) return "A+";
        if (percentage >= 80) return "A";
        if (percentage >= 70) return "B+";
        if (percentage >= 60) return "B";
        if (percentage >= 50) return "C";
        if (percentage >= 40) return "D";
        return "F";
    }
}

