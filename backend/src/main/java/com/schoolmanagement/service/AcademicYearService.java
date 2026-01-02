package com.schoolmanagement.service;

import com.schoolmanagement.entity.AcademicYear;
import com.schoolmanagement.repository.AcademicYearRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class AcademicYearService {

    private AcademicYearRepository academicYearRepository;

    public List<AcademicYear> getAllAcademicYears() {
        return academicYearRepository.findAll();
    }

    public AcademicYear getAcademicYearById(Long id) {
        return academicYearRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Academic year not found with id: " + id));
    }

    public AcademicYear getCurrentAcademicYear() {
        return academicYearRepository.findByIsCurrent(true)
                .orElse(null);
    }

    @Transactional
    public AcademicYear createAcademicYear(AcademicYear academicYear) {
        // If this is set as current, unset all others
        if (Boolean.TRUE.equals(academicYear.getIsCurrent())) {
            unsetCurrentAcademicYear();
        }
        return academicYearRepository.save(academicYear);
    }

    @Transactional
    public AcademicYear updateAcademicYear(Long id, AcademicYear academicYearDetails) {
        AcademicYear academicYear = getAcademicYearById(id);

        academicYear.setYearName(academicYearDetails.getYearName());
        academicYear.setStartDate(academicYearDetails.getStartDate());
        academicYear.setEndDate(academicYearDetails.getEndDate());
        academicYear.setSemester1Start(academicYearDetails.getSemester1Start());
        academicYear.setSemester1End(academicYearDetails.getSemester1End());
        academicYear.setSemester2Start(academicYearDetails.getSemester2Start());
        academicYear.setSemester2End(academicYearDetails.getSemester2End());
        academicYear.setIsActive(academicYearDetails.getIsActive());
        academicYear.setDescription(academicYearDetails.getDescription());

        // If this is set as current, unset all others
        if (Boolean.TRUE.equals(academicYearDetails.getIsCurrent()) && !Boolean.TRUE.equals(academicYear.getIsCurrent())) {
            unsetCurrentAcademicYear();
        }
        academicYear.setIsCurrent(academicYearDetails.getIsCurrent());

        return academicYearRepository.save(academicYear);
    }

    @Transactional
    public void deleteAcademicYear(Long id) {
        AcademicYear academicYear = getAcademicYearById(id);
        if (Boolean.TRUE.equals(academicYear.getIsCurrent())) {
            throw new RuntimeException("Cannot delete current academic year");
        }
        academicYearRepository.deleteById(id);
    }

    @Transactional
    public AcademicYear setCurrentAcademicYear(Long id) {
        unsetCurrentAcademicYear();
        AcademicYear academicYear = getAcademicYearById(id);
        academicYear.setIsCurrent(true);
        academicYear.setIsActive(true);
        return academicYearRepository.save(academicYear);
    }

    private void unsetCurrentAcademicYear() {
        academicYearRepository.findByIsCurrent(true).ifPresent(current -> {
            current.setIsCurrent(false);
            academicYearRepository.save(current);
        });
    }
}
