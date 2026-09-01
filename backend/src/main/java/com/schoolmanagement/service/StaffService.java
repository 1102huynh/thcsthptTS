package com.schoolmanagement.service;

import com.schoolmanagement.dto.StaffDTO;
import com.schoolmanagement.dto.UserDTO;
import com.schoolmanagement.entity.Staff;
import com.schoolmanagement.entity.EmploymentStatus;
import com.schoolmanagement.entity.StaffPosition;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.exception.DuplicateResourceException;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.repository.StaffRepository;
import com.schoolmanagement.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public Page<StaffDTO> getAllStaff(Pageable pageable) {
        return staffRepository.findAll(pageable).map(this::mapToDTO);
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
                .user(fetchUserDTO(staff.getUser()))
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

    // StaffDTO.user was never populated before this - UserDTO.builder() was
    // called nowhere in the whole backend (StudentService has the exact
    // same gap, fixed there too). Found live: the frontend's Staff
    // Management table showed every name/email cell blank because there
    // was genuinely nothing there to read.
    //
    // Re-fetches by id rather than mapping staff.getUser() directly:
    // right after createStaff's staffRepository.save(staff), staff.getUser()
    // is still the SAME transient stub Jackson built from the request body
    // ({"id": 23}, every other field left at its Java default) - Hibernate
    // uses its id as the FK on insert but never swaps in a hydrated entity,
    // so mapping it directly serialized firstName/lastName/email as null
    // (caught by an integration test asserting on the create response, not
    // by the frontend - queryClient.invalidateQueries masked it there by
    // refetching the list, where a freshly-queried Staff's user IS a real
    // lazy-loaded proxy that hydrates correctly on first access).
    private UserDTO fetchUserDTO(User userRef) {
        if (userRef == null || userRef.getId() == null) {
            return null;
        }
        return userRepository.findById(userRef.getId()).map(this::toUserDTO).orElse(null);
    }

    private UserDTO toUserDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .enabled(user.getEnabled())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLogin(user.getLastLogin())
                .build();
    }
}

