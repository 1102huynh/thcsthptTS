package com.schoolmanagement.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.schoolmanagement.dto.AdmissionDTO;
import com.schoolmanagement.entity.Admission;
import com.schoolmanagement.entity.AdmissionStatus;
import com.schoolmanagement.repository.AdmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdmissionService {

    private final AdmissionRepository admissionRepository;
    private final ObjectMapper objectMapper;

    /**
     * Get open admissions (public)
     */
    public Page<AdmissionDTO> getOpenAdmissions(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Admission> admissions = admissionRepository.findByStatusOrderByDisplayOrderAsc(
                AdmissionStatus.OPEN, pageable);
        return admissions.map(this::convertToDTO);
    }

    /**
     * Get all admissions (admin)
     */
    public Page<AdmissionDTO> getAllAdmissions(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Admission> admissions = admissionRepository.findAllByOrderByDisplayOrderAsc(pageable);
        return admissions.map(this::convertToDTO);
    }

    /**
     * Get admission by ID
     */
    public AdmissionDTO getAdmissionById(Long id) {
        Admission admission = admissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Admission not found with id: " + id));
        return convertToDTO(admission);
    }

    /**
     * Create new admission
     */
    @Transactional
    public AdmissionDTO createAdmission(AdmissionDTO admissionDTO) {
        Admission admission = convertToEntity(admissionDTO);
        Admission saved = admissionRepository.save(admission);
        log.info("Created new admission: {}", saved.getTitle());
        return convertToDTO(saved);
    }

    /**
     * Update admission
     */
    @Transactional
    public AdmissionDTO updateAdmission(Long id, AdmissionDTO admissionDTO) {
        Admission existing = admissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Admission not found with id: " + id));

        existing.setTitle(admissionDTO.getTitle());
        existing.setGrade(admissionDTO.getGrade());
        existing.setDescription(admissionDTO.getDescription());
        existing.setRequirements(convertListToJson(admissionDTO.getRequirements()));
        existing.setEligibility(admissionDTO.getEligibility());
        existing.setExamDate(admissionDTO.getExamDate());
        existing.setDeadline(admissionDTO.getDeadline());
        existing.setStatus(admissionDTO.getStatus());
        existing.setSeats(admissionDTO.getSeats());
        existing.setClassStructure(admissionDTO.getClassStructure());
        existing.setStudentsPerClass(admissionDTO.getStudentsPerClass());
        existing.setContact(admissionDTO.getContact());
        existing.setAcademicYear(admissionDTO.getAcademicYear());
        existing.setDisplayOrder(admissionDTO.getDisplayOrder());

        Admission updated = admissionRepository.save(existing);
        log.info("Updated admission: {}", updated.getTitle());
        return convertToDTO(updated);
    }

    /**
     * Delete admission
     */
    @Transactional
    public void deleteAdmission(Long id) {
        if (!admissionRepository.existsById(id)) {
            throw new RuntimeException("Admission not found with id: " + id);
        }
        admissionRepository.deleteById(id);
        log.info("Deleted admission with id: {}", id);
    }

    /**
     * Update admission status
     */
    @Transactional
    public AdmissionDTO updateStatus(Long id, AdmissionStatus status) {
        Admission admission = admissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Admission not found with id: " + id));
        admission.setStatus(status);
        Admission updated = admissionRepository.save(admission);
        log.info("Updated admission status to {}: {}", status, updated.getTitle());
        return convertToDTO(updated);
    }

    // Convert Entity to DTO
    private AdmissionDTO convertToDTO(Admission admission) {
        AdmissionDTO dto = new AdmissionDTO();
        dto.setId(admission.getId());
        dto.setTitle(admission.getTitle());
        dto.setGrade(admission.getGrade());
        dto.setDescription(admission.getDescription());
        dto.setRequirements(convertJsonToList(admission.getRequirements()));
        dto.setEligibility(admission.getEligibility());
        dto.setExamDate(admission.getExamDate());
        dto.setDeadline(admission.getDeadline());
        dto.setStatus(admission.getStatus());
        dto.setSeats(admission.getSeats());
        dto.setClassStructure(admission.getClassStructure());
        dto.setStudentsPerClass(admission.getStudentsPerClass());
        dto.setContact(admission.getContact());
        dto.setAcademicYear(admission.getAcademicYear());
        dto.setDisplayOrder(admission.getDisplayOrder());
        dto.setCreatedAt(admission.getCreatedAt());
        dto.setUpdatedAt(admission.getUpdatedAt());
        return dto;
    }

    // Convert DTO to Entity
    private Admission convertToEntity(AdmissionDTO dto) {
        Admission admission = new Admission();
        admission.setTitle(dto.getTitle());
        admission.setGrade(dto.getGrade());
        admission.setDescription(dto.getDescription());
        admission.setRequirements(convertListToJson(dto.getRequirements()));
        admission.setEligibility(dto.getEligibility());
        admission.setExamDate(dto.getExamDate());
        admission.setDeadline(dto.getDeadline());
        admission.setStatus(dto.getStatus() != null ? dto.getStatus() : AdmissionStatus.OPEN);
        admission.setSeats(dto.getSeats());
        admission.setClassStructure(dto.getClassStructure());
        admission.setStudentsPerClass(dto.getStudentsPerClass());
        admission.setContact(dto.getContact());
        admission.setAcademicYear(dto.getAcademicYear());
        admission.setDisplayOrder(dto.getDisplayOrder() != null ? dto.getDisplayOrder() : 0);
        return admission;
    }

    // Convert JSON string to List
    private List<String> convertJsonToList(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.error("Error converting JSON to List: {}", e.getMessage());
            return List.of();
        }
    }

    // Convert List to JSON string
    private String convertListToJson(List<String> list) {
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            log.error("Error converting List to JSON: {}", e.getMessage());
            return "[]";
        }
    }
}

