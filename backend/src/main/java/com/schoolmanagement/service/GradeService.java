package com.schoolmanagement.service;

import com.schoolmanagement.dto.GradeDTO;
import com.schoolmanagement.entity.Grade;
import com.schoolmanagement.entity.Staff;
import com.schoolmanagement.entity.Student;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.repository.GradeRepository;
import com.schoolmanagement.repository.StudentRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
@Transactional
public class GradeService {

    private GradeRepository gradeRepository;
    private StudentRepository studentRepository;

    public Grade createGrade(Grade grade) {
        Student student = studentRepository.findById(grade.getStudent().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        grade.setPercentage((grade.getMarksObtained() / grade.getTotalMarks()) * 100);
        grade.setGrade(calculateGrade(grade.getPercentage()));

        return gradeRepository.save(grade);
    }

    public Grade updateGrade(Long id, Grade gradeDetails) {
        Grade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade record not found"));

        grade.setMarksObtained(gradeDetails.getMarksObtained());
        grade.setTotalMarks(gradeDetails.getTotalMarks());
        grade.setPercentage((gradeDetails.getMarksObtained() / gradeDetails.getTotalMarks()) * 100);
        grade.setGrade(calculateGrade(grade.getPercentage()));
        grade.setRemarks(gradeDetails.getRemarks());

        return gradeRepository.save(grade);
    }

    public Grade getGradeById(Long id) {
        return gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade record not found"));
    }

    public List<Grade> getStudentGrades(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        return gradeRepository.findByStudent(student);
    }

    public List<Grade> getStudentGradesByAcademicYear(Long studentId, String academicYear) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        return gradeRepository.findByStudentAndAcademicYear(student, academicYear);
    }

    public List<Grade> getStudentGradesBySubject(Long studentId, String subject) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        return gradeRepository.findByStudentAndSubject(student, subject);
    }

    /**
     * Returns GradeDTO (not the raw entity) because this listing spans many
     * students/teachers, whose lazy `student`/`teacher` associations are not
     * already resolved in the persistence context the way the single-student
     * queries above are — serializing the raw entity after the transaction
     * closes (open-in-view is off) throws LazyInitializationException.
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

    public double getStudentAveragePercentage(Long studentId) {
        List<Grade> grades = getStudentGrades(studentId);
        if (grades.isEmpty()) {
            return 0;
        }
        return grades.stream()
                .mapToDouble(Grade::getPercentage)
                .average()
                .orElse(0);
    }

    public double getStudentAveragePercentageByYear(Long studentId, String academicYear) {
        List<Grade> grades = getStudentGradesByAcademicYear(studentId, academicYear);
        if (grades.isEmpty()) {
            return 0;
        }
        return grades.stream()
                .mapToDouble(Grade::getPercentage)
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

