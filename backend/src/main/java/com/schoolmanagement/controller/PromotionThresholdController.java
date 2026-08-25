package com.schoolmanagement.controller;

import com.schoolmanagement.dto.PromotionThresholdConfigDTO;
import com.schoolmanagement.entity.PromotionThresholdConfig;
import com.schoolmanagement.service.PromotionThresholdConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/promotion-thresholds")
@AllArgsConstructor
@Tag(name = "Promotion Thresholds", description = "ADMIN/PRINCIPAL-configurable cutoffs (điểm TB môn thấp nhất, hạnh kiểm tối thiểu, tỷ lệ nghỉ tối đa) used to suggest xét lên lớp decisions, scoped by the academic year they start applying from.")
public class PromotionThresholdController {

    private PromotionThresholdConfigService promotionThresholdConfigService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRINCIPAL')")
    @Operation(summary = "Create a promotion threshold config")
    public ResponseEntity<PromotionThresholdConfigDTO> createConfig(@Valid @RequestBody PromotionThresholdConfig config) {
        return new ResponseEntity<>(promotionThresholdConfigService.createConfig(config), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRINCIPAL')")
    @Operation(summary = "Update a promotion threshold config")
    public ResponseEntity<PromotionThresholdConfigDTO> updateConfig(
            @PathVariable Long id, @Valid @RequestBody PromotionThresholdConfig details) {
        return new ResponseEntity<>(promotionThresholdConfigService.updateConfig(id, details), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRINCIPAL')")
    @Operation(summary = "Get a promotion threshold config by ID")
    public ResponseEntity<PromotionThresholdConfigDTO> getConfigById(@PathVariable Long id) {
        return new ResponseEntity<>(promotionThresholdConfigService.getConfigById(id), HttpStatus.OK);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRINCIPAL')")
    @Operation(summary = "Get all promotion threshold configs")
    public ResponseEntity<List<PromotionThresholdConfigDTO>> getAllConfigs() {
        return new ResponseEntity<>(promotionThresholdConfigService.getAllConfigs(), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRINCIPAL')")
    @Operation(summary = "Delete a promotion threshold config")
    public ResponseEntity<Void> deleteConfig(@PathVariable Long id) {
        promotionThresholdConfigService.deleteConfig(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
