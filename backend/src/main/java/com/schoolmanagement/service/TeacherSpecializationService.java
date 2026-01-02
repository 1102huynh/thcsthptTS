package com.schoolmanagement.service;

import com.schoolmanagement.entity.TeacherSpecialization;
import com.schoolmanagement.repository.TeacherSpecializationRepository;
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
public class TeacherSpecializationService {

    private final TeacherSpecializationRepository specializationRepository;

    public List<TeacherSpecialization> getAllSpecializations() {
        return specializationRepository.findAll();
    }

    public Optional<TeacherSpecialization> getSpecializationById(Long id) {
        return specializationRepository.findById(id);
    }

    public List<TeacherSpecialization> getSpecializationsByTeacher(Long teacherId) {
        return specializationRepository.findByTeacherId(teacherId);
    }

    public List<TeacherSpecialization> getSpecializationsBySubject(Long subjectId) {
        return specializationRepository.findBySubjectId(subjectId);
    }

    public Optional<TeacherSpecialization> getTeacherPrimarySpecialization(Long teacherId) {
        return specializationRepository.findByTeacherIdAndIsPrimary(teacherId, true);
    }

    public List<TeacherSpecialization> getExperiencedTeachers(Integer minYears) {
        return specializationRepository.findExperiencedTeachers(minYears);
    }

    public TeacherSpecialization createSpecialization(TeacherSpecialization specialization) {
        if (specializationRepository.existsByTeacherIdAndSubjectId(
                specialization.getTeacher().getId(),
                specialization.getSubject().getId())) {
            throw new IllegalArgumentException("Specialization already exists");
        }
        return specializationRepository.save(specialization);
    }

    public TeacherSpecialization updateSpecialization(Long id, TeacherSpecialization details) {
        TeacherSpecialization specialization = specializationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Specialization not found"));

        if (details.getIsPrimary() != null) specialization.setIsPrimary(details.getIsPrimary());
        if (details.getCertificationLevel() != null) specialization.setCertificationLevel(details.getCertificationLevel());
        if (details.getYearsOfExperience() != null) specialization.setYearsOfExperience(details.getYearsOfExperience());

        return specializationRepository.save(specialization);
    }

    public void deleteSpecialization(Long id) {
        specializationRepository.deleteById(id);
    }
}
