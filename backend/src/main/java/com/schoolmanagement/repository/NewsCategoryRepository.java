package com.schoolmanagement.repository;

import com.schoolmanagement.entity.NewsCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NewsCategoryRepository extends JpaRepository<NewsCategory, Long> {
    Optional<NewsCategory> findBySlug(String slug);
    boolean existsBySlug(String slug);
    List<NewsCategory> findAllByOrderByDisplayOrderAscNameAsc();
}
