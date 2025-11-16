package com.schoolmanagement.repository;

import com.schoolmanagement.entity.UserPermission;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPermissionRepository extends JpaRepository<UserPermission, Long> {
    List<UserPermission> findByUser(User user);
    Optional<UserPermission> findByUserAndPermission(User user, Permission permission);
    boolean existsByUserAndPermission(User user, Permission permission);
}

