package com.schoolmanagement.repository;

import com.schoolmanagement.entity.PromotionThresholdConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PromotionThresholdConfigRepository extends JpaRepository<PromotionThresholdConfig, Long> {
    Optional<PromotionThresholdConfig> findByAppliesFrom(String appliesFrom);
    boolean existsByAppliesFrom(String appliesFrom);
}
