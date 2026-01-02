package com.schoolmanagement.service;

import com.schoolmanagement.entity.ClassSubjectAssignment;
import com.schoolmanagement.repository.ClassSubjectAssignmentRepository;
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
public class ClassSubjectAssignmentService {

    private final ClassSubjectAssignmentRepository assignmentRepository;

    public List<ClassSubjectAssignment> getAllAssignments() {
        return assignmentRepository.findAll();
    }

    public Optional<ClassSubjectAssignment> getAssignmentById(Long id) {
        return assignmentRepository.findById(id);
    }

    public List<ClassSubjectAssignment> getAssignmentsByClass(Long classId) {
        return assignmentRepository.findBySchoolClassId(classId);
    }

    public List<ClassSubjectAssignment> getAssignmentsByTeacher(Long teacherId) {
        return assignmentRepository.findByTeacherId(teacherId);
    }

    public List<ClassSubjectAssignment> getAssignmentsBySemester(String academicYear, Integer semester) {
        return assignmentRepository.findByAcademicYearAndSemester(academicYear, semester);
    }

    public Integer calculateTeacherWorkload(Long teacherId, String academicYear, Integer semester) {
        return assignmentRepository.calculateTeacherWorkload(teacherId, academicYear, semester);
    }

    public ClassSubjectAssignment createAssignment(ClassSubjectAssignment assignment) {
        if (assignmentRepository.existsBySchoolClassIdAndSubjectIdAndSemesterAndAcademicYear(
                assignment.getSchoolClass().getId(),
                assignment.getSubject().getId(),
                assignment.getSemester(),
                assignment.getAcademicYear())) {
            throw new IllegalArgumentException("Assignment already exists");
        }
        return assignmentRepository.save(assignment);
    }

    public ClassSubjectAssignment updateAssignment(Long id, ClassSubjectAssignment details) {
        ClassSubjectAssignment assignment = assignmentRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));

        if (details.getTeacher() != null) assignment.setTeacher(details.getTeacher());
        if (details.getPeriodsPerWeek() != null) assignment.setPeriodsPerWeek(details.getPeriodsPerWeek());
        if (details.getStatus() != null) assignment.setStatus(details.getStatus());

        return assignmentRepository.save(assignment);
    }

    public void deleteAssignment(Long id) {
        assignmentRepository.deleteById(id);
    }
}
