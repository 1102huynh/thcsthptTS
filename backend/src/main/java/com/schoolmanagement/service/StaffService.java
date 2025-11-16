package com.schoolmanagement.service;

import com.schoolmanagement.dto.StaffDTO;
import com.schoolmanagement.entity.Staff;
import com.schoolmanagement.entity.EmploymentStatus;
import com.schoolmanagement.entity.StaffPosition;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.exception.DuplicateResourceException;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.repository.StaffRepository;
import com.schoolmanagement.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class StaffService {

    private StaffRepository staffRepository;
    private UserRepository userRepository;

    public StaffDTO createStaff(Staff staff) {
        if (staffRepository.existsByEmployeeId(staff.getEmployeeId())) {
            throw new DuplicateResourceException("Employee ID already exists: " + staff.getEmployeeId());
        }

        Staff savedStaff = staffRepository.save(staff);
        return mapToDTO(savedStaff);
    }

    public StaffDTO updateStaff(Long id, Staff staffDetails) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + id));

        if (!staff.getEmployeeId().equals(staffDetails.getEmployeeId()) &&
                staffRepository.existsByEmployeeId(staffDetails.getEmployeeId())) {
            throw new DuplicateResourceException("Employee ID already exists");
        }

        staff.setEmployeeId(staffDetails.getEmployeeId());
        staff.setPosition(staffDetails.getPosition());
        staff.setDepartment(staffDetails.getDepartment());
        staff.setDateOfBirth(staffDetails.getDateOfBirth());
        staff.setDateOfJoining(staffDetails.getDateOfJoining());
        staff.setQualification(staffDetails.getQualification());
        staff.setSubjectSpecialization(staffDetails.getSubjectSpecialization());
        staff.setSalary(staffDetails.getSalary());
        staff.setStatus(staffDetails.getStatus());
        staff.setAddress(staffDetails.getAddress());
        staff.setCity(staffDetails.getCity());
        staff.setState(staffDetails.getState());
        staff.setPostalCode(staffDetails.getPostalCode());
        staff.setEmergencyContactName(staffDetails.getEmergencyContactName());
        staff.setEmergencyContactPhone(staffDetails.getEmergencyContactPhone());

        Staff updatedStaff = staffRepository.save(staff);
        return mapToDTO(updatedStaff);
    }

    public StaffDTO getStaffById(Long id) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + id));
        return mapToDTO(staff);
    }

    public StaffDTO getStaffByEmployeeId(String employeeId) {
        Staff staff = staffRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with employee id: " + employeeId));
        return mapToDTO(staff);
    }

    public List<StaffDTO> getAllStaff() {
        return staffRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<StaffDTO> getStaffByPosition(StaffPosition position) {
        return staffRepository.findByPosition(position)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<StaffDTO> getStaffByDepartment(String department) {
        return staffRepository.findByDepartment(department)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<StaffDTO> getActiveStaff() {
        return staffRepository.findByStatus(EmploymentStatus.ACTIVE)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public void deleteStaff(Long id) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + id));
        staffRepository.delete(staff);
    }

    private StaffDTO mapToDTO(Staff staff) {
        return StaffDTO.builder()
                .id(staff.getId())
                .employeeId(staff.getEmployeeId())
                .position(staff.getPosition())
                .department(staff.getDepartment())
                .dateOfBirth(staff.getDateOfBirth())
                .dateOfJoining(staff.getDateOfJoining())
                .qualification(staff.getQualification())
                .subjectSpecialization(staff.getSubjectSpecialization())
                .salary(staff.getSalary())
                .status(staff.getStatus())
                .address(staff.getAddress())
                .city(staff.getCity())
                .state(staff.getState())
                .postalCode(staff.getPostalCode())
                .emergencyContactName(staff.getEmergencyContactName())
                .emergencyContactPhone(staff.getEmergencyContactPhone())
                .createdAt(staff.getCreatedAt())
                .updatedAt(staff.getUpdatedAt())
                .build();
    }
}

