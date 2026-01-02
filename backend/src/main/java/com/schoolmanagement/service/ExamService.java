package com.schoolmanagement.service;

import com.schoolmanagement.entity.Exam;
import com.schoolmanagement.repository.ExamRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
public class ExamService {

    private ExamRepository examRepository;

    public List<Exam> getAllExams() {
        return examRepository.findAll();
    }

    public Exam getExamById(Long id) {
        return examRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exam not found with id: " + id));
    }

    public List<Exam> getExamsByDateRange(LocalDate startDate, LocalDate endDate) {
        return examRepository.findByExamDateBetween(startDate, endDate);
    }

    public List<Exam> getExamsByGradeLevel(Long gradeLevelId) {
        return examRepository.findByGradeLevel_Id(gradeLevelId);
    }

    public List<Exam> getExamsBySubject(Long subjectId) {
        return examRepository.findBySubject_Id(subjectId);
    }

    public List<Exam> getExamsByAcademicYear(Long academicYearId) {
        return examRepository.findByAcademicYear_Id(academicYearId);
    }

    public List<Exam> getExamsByStatus(String status) {
        return examRepository.findByStatus(status);
    }

    @Transactional
    public Exam createExam(Exam exam) {
        if (exam.getDurationMinutes() == null && exam.getStartTime() != null && exam.getEndTime() != null) {
            long duration = java.time.Duration.between(exam.getStartTime(), exam.getEndTime()).toMinutes();
            exam.setDurationMinutes((int) duration);
        }
        return examRepository.save(exam);
    }

    @Transactional
    public Exam updateExam(Long id, Exam examDetails) {
        Exam exam = getExamById(id);
        
        exam.setExamName(examDetails.getExamName());
        exam.setSubject(examDetails.getSubject());
        exam.setGradeLevel(examDetails.getGradeLevel());
        exam.setExamDate(examDetails.getExamDate());
        exam.setStartTime(examDetails.getStartTime());
        exam.setEndTime(examDetails.getEndTime());
        exam.setDurationMinutes(examDetails.getDurationMinutes());
        exam.setTotalMarks(examDetails.getTotalMarks());
        exam.setPassingMarks(examDetails.getPassingMarks());
        exam.setRoomNumber(examDetails.getRoomNumber());
        exam.setInvigilator(examDetails.getInvigilator());
        exam.setExamType(examDetails.getExamType());
        exam.setSemester(examDetails.getSemester());
        exam.setAcademicYear(examDetails.getAcademicYear());
        exam.setStatus(examDetails.getStatus());
        exam.setInstructions(examDetails.getInstructions());

        return examRepository.save(exam);
    }

    @Transactional
    public void deleteExam(Long id) {
        Exam exam = getExamById(id);
        examRepository.delete(exam);
    }

    @Transactional
    public Exam updateExamStatus(Long id, String status) {
        Exam exam = getExamById(id);
        exam.setStatus(status);
        return examRepository.save(exam);
    }
}
