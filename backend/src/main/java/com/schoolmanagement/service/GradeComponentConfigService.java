package com.schoolmanagement.service;

import com.schoolmanagement.dto.GradeComponentConfigDTO;
import com.schoolmanagement.entity.GradeComponentConfig;
import com.schoolmanagement.exception.DuplicateResourceException;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.repository.GradeComponentConfigRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class GradeComponentConfigService {

    private GradeComponentConfigRepository gradeComponentConfigRepository;

    public GradeComponentConfigDTO createConfig(GradeComponentConfig config) {
        if (gradeComponentConfigRepository.existsByComponentTypeAndAppliesFrom(
                config.getComponentType(), config.getAppliesFrom())) {
            throw new DuplicateResourceException(
                    "A weight for " + config.getComponentType() + " starting " + config.getAppliesFrom() + " already exists");
        }
        return mapToDTO(gradeComponentConfigRepository.save(config));
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

        return mapToDTO(gradeComponentConfigRepository.save(config));
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
