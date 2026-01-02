package com.schoolmanagement.service;

import com.schoolmanagement.entity.Subject;
import com.schoolmanagement.repository.SubjectRepository;
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
public class SubjectService {

    private final SubjectRepository subjectRepository;

    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }

    public Optional<Subject> getSubjectById(Long id) {
        return subjectRepository.findById(id);
    }

    public Optional<Subject> getSubjectByCode(String code) {
        return subjectRepository.findBySubjectCode(code);
    }

    public List<Subject> getSubjectsBySchoolType(Subject.SchoolType schoolType) {
        return subjectRepository.findBySchoolType(schoolType);
    }

    public List<Subject> getMiddleSchoolSubjects() {
        return subjectRepository.findMiddleSchoolSubjects();
    }

    public List<Subject> getHighSchoolSubjects() {
        return subjectRepository.findHighSchoolSubjects();
    }

    public List<Subject> getRequiredSubjects() {
        return subjectRepository.findByIsRequired(true);
    }

    public List<Subject> getOptionalSubjects() {
        return subjectRepository.findByIsRequired(false);
    }

    public Subject createSubject(Subject subject) {
        if (subjectRepository.existsBySubjectCode(subject.getSubjectCode())) {
            throw new IllegalArgumentException("Subject code already exists: " + subject.getSubjectCode());
        }
        return subjectRepository.save(subject);
    }

    public Subject updateSubject(Long id, Subject subjectDetails) {
        Subject subject = subjectRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Subject not found"));

        if (subjectDetails.getSubjectName() != null) subject.setSubjectName(subjectDetails.getSubjectName());
        if (subjectDetails.getCategory() != null) subject.setCategory(subjectDetails.getCategory());
        if (subjectDetails.getCoefficient() != null) subject.setCoefficient(subjectDetails.getCoefficient());
        if (subjectDetails.getTotalPeriodsPerWeek() != null) subject.setTotalPeriodsPerWeek(subjectDetails.getTotalPeriodsPerWeek());

        return subjectRepository.save(subject);
    }

    public void deleteSubject(Long id) {
        subjectRepository.deleteById(id);
    }
}
