package com.schoolmanagement.repository;

import com.schoolmanagement.entity.Parent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParentRepository extends JpaRepository<Parent, Long> {

    Optional<Parent> findByUserId(Long userId);

    @Query("SELECT p FROM Parent p LEFT JOIN FETCH p.children WHERE p.id = :id")
    Optional<Parent> findByIdWithChildren(@Param("id") Long id);

    @Query("SELECT p FROM Parent p LEFT JOIN FETCH p.children WHERE p.user.id = :userId")
    Optional<Parent> findByUserIdWithChildren(@Param("userId") Long userId);
}
