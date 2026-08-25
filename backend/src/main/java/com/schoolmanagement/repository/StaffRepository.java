package com.schoolmanagement.repository;

import com.schoolmanagement.entity.Staff;
import com.schoolmanagement.entity.EmploymentStatus;
import com.schoolmanagement.entity.StaffPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {
    Optional<Staff> findByEmployeeId(String employeeId);
    Optional<Staff> findByUserId(Long userId);
    List<Staff> findByStatus(EmploymentStatus status);
    long countByStatus(EmploymentStatus status);
    List<Staff> findByPosition(StaffPosition position);
    List<Staff> findByDepartment(String department);
    boolean existsByEmployeeId(String employeeId);
}

