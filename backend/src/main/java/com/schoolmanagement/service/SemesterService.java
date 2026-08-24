package com.schoolmanagement.service;

import com.schoolmanagement.dto.SemesterDTO;
import com.schoolmanagement.entity.AcademicYear;
import com.schoolmanagement.entity.Semester;
import com.schoolmanagement.exception.DuplicateResourceException;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.repository.AcademicYearRepository;
import com.schoolmanagement.repository.SemesterRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class SemesterService {

    private SemesterRepository semesterRepository;
    private AcademicYearRepository academicYearRepository;

    public SemesterDTO createSemester(Semester semester) {
        AcademicYear academicYear = resolveAcademicYear(semester.getAcademicYear());
        semester.setAcademicYear(academicYear);

        semesterRepository.findByAcademicYearAndName(academicYear, semester.getName())
                .ifPresent(existing -> {
                    throw new DuplicateResourceException(
                            "Semester " + semester.getName() + " already exists for " + academicYear.getName());
                });

        return mapToDTO(semesterRepository.save(semester));
    }

    public SemesterDTO updateSemester(Long id, Semester details) {
        Semester semester = semesterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Semester not found with id: " + id));

        AcademicYear academicYear = resolveAcademicYear(details.getAcademicYear());

        boolean changingKey = !semester.getAcademicYear().getId().equals(academicYear.getId())
                || semester.getName() != details.getName();
        if (changingKey) {
            semesterRepository.findByAcademicYearAndName(academicYear, details.getName())
                    .filter(existing -> !existing.getId().equals(id))
                    .ifPresent(existing -> {
                        throw new DuplicateResourceException(
                                "Semester " + details.getName() + " already exists for " + academicYear.getName());
                    });
        }

        semester.setAcademicYear(academicYear);
        semester.setName(details.getName());
        semester.setStartDate(details.getStartDate());
        semester.setEndDate(details.getEndDate());

        return mapToDTO(semesterRepository.save(semester));
    }

    public SemesterDTO getSemesterById(Long id) {
        return mapToDTO(semesterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Semester not found with id: " + id)));
    }

    public List<SemesterDTO> getAllSemesters() {
        return semesterRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<SemesterDTO> getSemestersByAcademicYear(Long academicYearId) {
        AcademicYear academicYear = academicYearRepository.findById(academicYearId)
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with id: " + academicYearId));
        return semesterRepository.findByAcademicYear(academicYear)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public void deleteSemester(Long id) {
        Semester semester = semesterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Semester not found with id: " + id));
        semesterRepository.delete(semester);
    }

    private AcademicYear resolveAcademicYear(AcademicYear reference) {
        if (reference == null || reference.getId() == null) {
            throw new ResourceNotFoundException("An academic year id is required");
        }
        return academicYearRepository.findById(reference.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Academic year not found with id: " + reference.getId()));
    }

    private SemesterDTO mapToDTO(Semester semester) {
        AcademicYear academicYear = semester.getAcademicYear();
        return SemesterDTO.builder()
                .id(semester.getId())
                .academicYearId(academicYear != null ? academicYear.getId() : null)
                .academicYearName(academicYear != null ? academicYear.getName() : null)
                .name(semester.getName())
                .startDate(semester.getStartDate())
                .endDate(semester.getEndDate())
                .createdAt(semester.getCreatedAt())
                .updatedAt(semester.getUpdatedAt())
                .build();
    }
}
