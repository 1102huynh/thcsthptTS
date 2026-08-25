package com.schoolmanagement.service;

import com.schoolmanagement.dto.PromotionThresholdConfigDTO;
import com.schoolmanagement.entity.PromotionThresholdConfig;
import com.schoolmanagement.exception.DuplicateResourceException;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.repository.PromotionThresholdConfigRepository;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class PromotionThresholdConfigService {

    private PromotionThresholdConfigRepository promotionThresholdConfigRepository;

    public PromotionThresholdConfigDTO createConfig(PromotionThresholdConfig request) {
        if (promotionThresholdConfigRepository.existsByAppliesFrom(request.getAppliesFrom())) {
            throw new DuplicateResourceException(
                    "A promotion threshold starting " + request.getAppliesFrom() + " already exists");
        }

        // Build fresh from validated fields only - never save the raw request
        // body as-is, or a client-supplied "id" would silently overwrite an
        // unrelated existing row (see GradeComponentConfigService, 3.3 review).
        PromotionThresholdConfig config = PromotionThresholdConfig.builder()
                .appliesFrom(request.getAppliesFrom())
                .minSubjectAverage(request.getMinSubjectAverage())
                .minConduct(request.getMinConduct())
                .maxAbsenceRate(request.getMaxAbsenceRate())
                .build();

        try {
            return mapToDTO(promotionThresholdConfigRepository.save(config));
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateResourceException(
                    "A promotion threshold starting " + request.getAppliesFrom() + " already exists");
        }
    }

    public PromotionThresholdConfigDTO updateConfig(Long id, PromotionThresholdConfig details) {
        PromotionThresholdConfig config = promotionThresholdConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion threshold not found with id: " + id));

        boolean changingKey = !config.getAppliesFrom().equals(details.getAppliesFrom());
        if (changingKey && promotionThresholdConfigRepository.existsByAppliesFrom(details.getAppliesFrom())) {
            throw new DuplicateResourceException(
                    "A promotion threshold starting " + details.getAppliesFrom() + " already exists");
        }

        config.setAppliesFrom(details.getAppliesFrom());
        config.setMinSubjectAverage(details.getMinSubjectAverage());
        config.setMinConduct(details.getMinConduct());
        config.setMaxAbsenceRate(details.getMaxAbsenceRate());

        try {
            return mapToDTO(promotionThresholdConfigRepository.save(config));
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateResourceException(
                    "A promotion threshold starting " + details.getAppliesFrom() + " already exists");
        }
    }

    public PromotionThresholdConfigDTO getConfigById(Long id) {
        return mapToDTO(promotionThresholdConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion threshold not found with id: " + id)));
    }

    public List<PromotionThresholdConfigDTO> getAllConfigs() {
        return promotionThresholdConfigRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public void deleteConfig(Long id) {
        PromotionThresholdConfig config = promotionThresholdConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion threshold not found with id: " + id));
        promotionThresholdConfigRepository.delete(config);
    }

    private PromotionThresholdConfigDTO mapToDTO(PromotionThresholdConfig config) {
        return PromotionThresholdConfigDTO.builder()
                .id(config.getId())
                .appliesFrom(config.getAppliesFrom())
                .minSubjectAverage(config.getMinSubjectAverage())
                .minConduct(config.getMinConduct())
                .maxAbsenceRate(config.getMaxAbsenceRate())
                .createdAt(config.getCreatedAt())
                .updatedAt(config.getUpdatedAt())
                .build();
    }
}
