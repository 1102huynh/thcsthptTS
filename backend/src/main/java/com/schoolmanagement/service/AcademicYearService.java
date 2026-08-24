package com.schoolmanagement.service;

import com.schoolmanagement.dto.AcademicYearDTO;
import com.schoolmanagement.entity.AcademicYear;
import com.schoolmanagement.entity.AcademicYearStatus;
import com.schoolmanagement.exception.DuplicateResourceException;
import com.schoolmanagement.exception.ResourceInUseException;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.repository.AcademicYearRepository;
import com.schoolmanagement.repository.SchoolClassRepository;
import com.schoolmanagement.repository.SemesterRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class AcademicYearService {

    private AcademicYearRepository academicYearRepository;
    private SemesterRepository semesterRepository;
    private SchoolClassRepository schoolClassRepository;

    public AcademicYearDTO createAcademicYear(AcademicYear academicYear) {
        if (academicYearRepository.existsByName(academicYear.getName())) {
            throw new DuplicateResourceException("Academic year already exists: " + academicYear.getName());
        }
        return mapToDTO(academicYearRepository.save(academicYear));
    }

    public AcademicYearDTO updateAcademicYear(Long id, AcademicYear details) {
        AcademicYear academicYear = academicYearRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with id: " + id));

        if (!academicYear.getName().equals(details.getName())
                && academicYearRepository.existsByName(details.getName())) {
            throw new DuplicateResourceException("Academic year already exists: " + details.getName());
        }

        academicYear.setName(details.getName());
        academicYear.setStartDate(details.getStartDate());
        academicYear.setEndDate(details.getEndDate());
        academicYear.setStatus(details.getStatus());

        return mapToDTO(academicYearRepository.save(academicYear));
    }

    public AcademicYearDTO getAcademicYearById(Long id) {
        return mapToDTO(academicYearRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with id: " + id)));
    }

    public List<AcademicYearDTO> getAllAcademicYears() {
        return academicYearRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /** Marks the academic year CLOSED — does not touch its classes/semesters/grades. */
    public AcademicYearDTO closeAcademicYear(Long id) {
        AcademicYear academicYear = academicYearRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with id: " + id));
        academicYear.setStatus(AcademicYearStatus.CLOSED);
        return mapToDTO(academicYearRepository.save(academicYear));
    }

    public void deleteAcademicYear(Long id) {
        AcademicYear academicYear = academicYearRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with id: " + id));

        if (!semesterRepository.findByAcademicYear(academicYear).isEmpty()) {
            throw new ResourceInUseException("Cannot delete academic year: it still has semesters");
        }
        if (!schoolClassRepository.findByAcademicYearRef(academicYear).isEmpty()) {
            throw new ResourceInUseException("Cannot delete academic year: it still has classes assigned to it");
        }

        academicYearRepository.delete(academicYear);
    }

    private AcademicYearDTO mapToDTO(AcademicYear academicYear) {
        return AcademicYearDTO.builder()
                .id(academicYear.getId())
                .name(academicYear.getName())
                .startDate(academicYear.getStartDate())
                .endDate(academicYear.getEndDate())
                .status(academicYear.getStatus())
                .createdAt(academicYear.getCreatedAt())
                .updatedAt(academicYear.getUpdatedAt())
                .build();
    }
}
