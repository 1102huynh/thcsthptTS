package com.schoolmanagement.service;

import com.schoolmanagement.dto.GradeComponentConfigDTO;
import com.schoolmanagement.entity.GradeComponentConfig;
import com.schoolmanagement.exception.DuplicateResourceException;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.repository.GradeComponentConfigRepository;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class GradeComponentConfigService {

    private GradeComponentConfigRepository gradeComponentConfigRepository;

    public GradeComponentConfigDTO createConfig(GradeComponentConfig request) {
        if (gradeComponentConfigRepository.existsByComponentTypeAndAppliesFrom(
                request.getComponentType(), request.getAppliesFrom())) {
            throw new DuplicateResourceException(
                    "A weight for " + request.getComponentType() + " starting " + request.getAppliesFrom() + " already exists");
        }

        // Build a fresh entity from the validated fields only - never save the raw
        // request body as-is: if a client sent an "id" (whether by accident or not),
        // JpaRepository.save() would treat it as an update and silently overwrite
        // whatever unrelated row already has that id, bypassing the duplicate check
        // above (which only looked at componentType+appliesFrom, not that id).
        GradeComponentConfig config = GradeComponentConfig.builder()
                .componentType(request.getComponentType())
                .weight(request.getWeight())
                .appliesFrom(request.getAppliesFrom())
                .build();

        try {
            return mapToDTO(gradeComponentConfigRepository.save(config));
        } catch (DataIntegrityViolationException ex) {
            // Two concurrent requests can both pass the exists() check above before
            // either commits; the second insert then hits the DB's unique constraint.
            // Surface that race the same way a non-racing duplicate is surfaced (409),
            // not as a masked 500.
            throw new DuplicateResourceException(
                    "A weight for " + request.getComponentType() + " starting " + request.getAppliesFrom() + " already exists");
        }
    }

    public GradeComponentConfigDTO updateConfig(Long id, GradeComponentConfig details) {
        GradeComponentConfig config = gradeComponentConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade component config not found with id: " + id));

        boolean changingKey = config.getComponentType() != details.getComponentType()
                || !config.getAppliesFrom().equals(details.getAppliesFrom());
        if (changingKey && gradeComponentConfigRepository.existsByComponentTypeAndAppliesFrom(
                details.getComponentType(), details.getAppliesFrom())) {
            throw new DuplicateResourceException(
                    "A weight for " + details.getComponentType() + " starting " + details.getAppliesFrom() + " already exists");
        }

        config.setComponentType(details.getComponentType());
        config.setWeight(details.getWeight());
        config.setAppliesFrom(details.getAppliesFrom());

        try {
            return mapToDTO(gradeComponentConfigRepository.save(config));
        } catch (DataIntegrityViolationException ex) {
            // Same race as createConfig(): two concurrent updates changing to the same
            // (componentType, appliesFrom) can both pass the exists() check above.
            throw new DuplicateResourceException(
                    "A weight for " + details.getComponentType() + " starting " + details.getAppliesFrom() + " already exists");
        }
    }

    public GradeComponentConfigDTO getConfigById(Long id) {
        return mapToDTO(gradeComponentConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade component config not found with id: " + id)));
    }

    public List<GradeComponentConfigDTO> getAllConfigs() {
        return gradeComponentConfigRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public void deleteConfig(Long id) {
        GradeComponentConfig config = gradeComponentConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade component config not found with id: " + id));
        gradeComponentConfigRepository.delete(config);
    }

    private GradeComponentConfigDTO mapToDTO(GradeComponentConfig config) {
        return GradeComponentConfigDTO.builder()
                .id(config.getId())
                .componentType(config.getComponentType())
                .weight(config.getWeight())
                .appliesFrom(config.getAppliesFrom())
                .createdAt(config.getCreatedAt())
                .updatedAt(config.getUpdatedAt())
                .build();
    }
}
