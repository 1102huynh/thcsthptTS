package com.schoolmanagement.repository;

import com.schoolmanagement.entity.GradeComponentConfig;
import com.schoolmanagement.entity.GradeComponentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GradeComponentConfigRepository extends JpaRepository<GradeComponentConfig, Long> {
    List<GradeComponentConfig> findByComponentType(GradeComponentType componentType);
    boolean existsByComponentTypeAndAppliesFrom(GradeComponentType componentType, String appliesFrom);
}
