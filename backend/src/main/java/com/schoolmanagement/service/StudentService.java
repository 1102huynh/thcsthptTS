package com.schoolmanagement.service;

import com.schoolmanagement.dto.StudentDTO;
import com.schoolmanagement.dto.UserDTO;
import com.schoolmanagement.entity.Student;
import com.schoolmanagement.entity.StudentStatus;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.exception.DuplicateResourceException;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.repository.StudentRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class StudentService {

    private StudentRepository studentRepository;

    public StudentDTO createStudent(Student student) {
        if (studentRepository.existsByRollNumber(student.getRollNumber())) {
            throw new DuplicateResourceException("Roll number already exists: " + student.getRollNumber());
        }

        if (studentRepository.existsByAdmissionNumber(student.getAdmissionNumber())) {
            throw new DuplicateResourceException("Admission number already exists: " + student.getAdmissionNumber());
        }

        Student savedStudent = studentRepository.save(student);
        return mapToDTO(savedStudent);
    }

    public StudentDTO updateStudent(Long id, Student studentDetails) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));

        // Log incoming data for debugging
        System.out.println("\n========================================");
        System.out.println("UPDATE STUDENT REQUEST");
        System.out.println("========================================");
        System.out.println("Student ID: " + id);
        System.out.println("Current values in database:");
        System.out.println("  - address: '" + student.getAddress() + "'");
        System.out.println("  - gender: '" + student.getGender() + "'");
        System.out.println("  - fatherName: '" + student.getFatherName() + "'");
        System.out.println("  - motherName: '" + student.getMotherName() + "'");
        System.out.println("  - dateOfBirth: " + student.getDateOfBirth());

        System.out.println("\nReceived from frontend:");
        System.out.println("  - dateOfBirth: " + studentDetails.getDateOfBirth());
        System.out.println("  - gender: '" + studentDetails.getGender() + "'");
        System.out.println("  - address: '" + studentDetails.getAddress() + "'");
        System.out.println("  - fatherName: '" + studentDetails.getFatherName() + "'");
        System.out.println("  - fatherPhone: '" + studentDetails.getFatherPhone() + "'");
        System.out.println("  - motherName: '" + studentDetails.getMotherName() + "'");
        System.out.println("  - motherPhone: '" + studentDetails.getMotherPhone() + "'");
        System.out.println("  - User firstName: '" + (studentDetails.getUser() != null ? studentDetails.getUser().getFirstName() : "null") + "'");
        System.out.println("  - User lastName: '" + (studentDetails.getUser() != null ? studentDetails.getUser().getLastName() : "null") + "'");

        // Only update editable fields - preserve read-only fields
        boolean hasUpdates = false;

        // Update User firstName and lastName if provided
        if (studentDetails.getUser() != null) {
            if (isNotEmpty(studentDetails.getUser().getFirstName())) {
                System.out.println("\n✓ Updating User firstName: '" + studentDetails.getUser().getFirstName() + "'");
                student.getUser().setFirstName(studentDetails.getUser().getFirstName());
                hasUpdates = true;
            }
            if (isNotEmpty(studentDetails.getUser().getLastName())) {
                System.out.println("✓ Updating User lastName: '" + studentDetails.getUser().getLastName() + "'");
                student.getUser().setLastName(studentDetails.getUser().getLastName());
                hasUpdates = true;
            }
        }

        // Update date fields if provided
        if (studentDetails.getDateOfBirth() != null) {
            System.out.println("\n✓ Updating dateOfBirth: " + studentDetails.getDateOfBirth());
            student.setDateOfBirth(studentDetails.getDateOfBirth());
            hasUpdates = true;
        }

        // Update string fields only if not null and not empty
        if (isNotEmpty(studentDetails.getGender())) {
            System.out.println("✓ Updating gender: '" + studentDetails.getGender() + "'");
            student.setGender(studentDetails.getGender());
            hasUpdates = true;
        }
        if (isNotEmpty(studentDetails.getBloodGroup())) {
            System.out.println("✓ Updating bloodGroup: '" + studentDetails.getBloodGroup() + "'");
            student.setBloodGroup(studentDetails.getBloodGroup());
            hasUpdates = true;
        }
        if (isNotEmpty(studentDetails.getFatherName())) {
            System.out.println("✓ Updating fatherName: '" + studentDetails.getFatherName() + "'");
            student.setFatherName(studentDetails.getFatherName());
            hasUpdates = true;
        }
        if (isNotEmpty(studentDetails.getFatherPhone())) {
            System.out.println("✓ Updating fatherPhone: '" + studentDetails.getFatherPhone() + "'");
            student.setFatherPhone(studentDetails.getFatherPhone());
            hasUpdates = true;
        }
        if (isNotEmpty(studentDetails.getFatherOccupation())) {
            System.out.println("✓ Updating fatherOccupation: '" + studentDetails.getFatherOccupation() + "'");
            student.setFatherOccupation(studentDetails.getFatherOccupation());
            hasUpdates = true;
        }
        if (isNotEmpty(studentDetails.getMotherName())) {
            System.out.println("✓ Updating motherName: '" + studentDetails.getMotherName() + "'");
            student.setMotherName(studentDetails.getMotherName());
            hasUpdates = true;
        }
        if (isNotEmpty(studentDetails.getMotherPhone())) {
            System.out.println("✓ Updating motherPhone: '" + studentDetails.getMotherPhone() + "'");
            student.setMotherPhone(studentDetails.getMotherPhone());
            hasUpdates = true;
        }
        if (isNotEmpty(studentDetails.getMotherOccupation())) {
            System.out.println("✓ Updating motherOccupation: '" + studentDetails.getMotherOccupation() + "'");
            student.setMotherOccupation(studentDetails.getMotherOccupation());
            hasUpdates = true;
        }
        if (isNotEmpty(studentDetails.getAddress())) {
            System.out.println("✓ Updating address: '" + studentDetails.getAddress() + "'");
            student.setAddress(studentDetails.getAddress());
            hasUpdates = true;
        }
        if (isNotEmpty(studentDetails.getCity())) {
            System.out.println("✓ Updating city: '" + studentDetails.getCity() + "'");
            student.setCity(studentDetails.getCity());
            hasUpdates = true;
        }
        if (isNotEmpty(studentDetails.getState())) {
            System.out.println("✓ Updating state: '" + studentDetails.getState() + "'");
            student.setState(studentDetails.getState());
            hasUpdates = true;
        }
        if (isNotEmpty(studentDetails.getPostalCode())) {
            System.out.println("✓ Updating postalCode: '" + studentDetails.getPostalCode() + "'");
            student.setPostalCode(studentDetails.getPostalCode());
            hasUpdates = true;
        }
        if (isNotEmpty(studentDetails.getEmergencyContactName())) {
            System.out.println("✓ Updating emergencyContactName: '" + studentDetails.getEmergencyContactName() + "'");
            student.setEmergencyContactName(studentDetails.getEmergencyContactName());
            hasUpdates = true;
        }
        if (isNotEmpty(studentDetails.getEmergencyContactPhone())) {
            System.out.println("✓ Updating emergencyContactPhone: '" + studentDetails.getEmergencyContactPhone() + "'");
            student.setEmergencyContactPhone(studentDetails.getEmergencyContactPhone());
            hasUpdates = true;
        }
        if (isNotEmpty(studentDetails.getEmergencyContactRelation())) {
            System.out.println("✓ Updating emergencyContactRelation: '" + studentDetails.getEmergencyContactRelation() + "'");
            student.setEmergencyContactRelation(studentDetails.getEmergencyContactRelation());
            hasUpdates = true;
        }

        if (!hasUpdates) {
            System.out.println("\n⚠ WARNING: No updates found! All fields were empty or null");
        }

        System.out.println("\nSaving to database...");
        Student updatedStudent = studentRepository.save(student);
        System.out.println("✓ Save successful!");

        System.out.println("\nValues after save:");
        System.out.println("  - address: '" + updatedStudent.getAddress() + "'");
        System.out.println("  - gender: '" + updatedStudent.getGender() + "'");
        System.out.println("  - fatherName: '" + updatedStudent.getFatherName() + "'");
        System.out.println("  - motherName: '" + updatedStudent.getMotherName() + "'");
        System.out.println("  - dateOfBirth: " + updatedStudent.getDateOfBirth());
        System.out.println("  - User firstName: '" + updatedStudent.getUser().getFirstName() + "'");
        System.out.println("  - User lastName: '" + updatedStudent.getUser().getLastName() + "'");
        System.out.println("  - updated_at: " + updatedStudent.getUpdatedAt());
        System.out.println("========================================\n");

        return mapToDTO(updatedStudent);
    }

    /**
     * Helper method to check if a string is not null and not empty
     */
    private boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public StudentDTO getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
        return mapToDTO(student);
    }

    public StudentDTO getStudentByRollNumber(String rollNumber) {
        Student student = studentRepository.findByRollNumber(rollNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with roll number: " + rollNumber));
        return mapToDTO(student);
    }

    public StudentDTO getStudentByUserId(Long userId) {
        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with user id: " + userId));
        return mapToDTO(student);
    }

    public List<StudentDTO> getAllStudents() {
        return studentRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<StudentDTO> getStudentsByClass(String className) {
        return studentRepository.findByClassName(className)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<StudentDTO> getStudentsByClassAndSection(String className, String section) {
        return studentRepository.findByClassNameAndSection(className, section)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<StudentDTO> getActiveStudents() {
        return studentRepository.findByStatus(StudentStatus.ACTIVE)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
        studentRepository.delete(student);
    }

    private StudentDTO mapToDTO(Student student) {
        return StudentDTO.builder()
                .id(student.getId())
                .rollNumber(student.getRollNumber())
                .admissionNumber(student.getAdmissionNumber())
                .user(student.getUser() != null ? mapUserToDTO(student.getUser()) : null)
                .dateOfBirth(student.getDateOfBirth())
                .gender(student.getGender())
                .bloodGroup(student.getBloodGroup())
                .className(student.getClassName())
                .section(student.getSection())
                .dateOfAdmission(student.getDateOfAdmission())
                .status(student.getStatus())
                .fatherName(student.getFatherName())
                .fatherPhone(student.getFatherPhone())
                .fatherOccupation(student.getFatherOccupation())
                .motherName(student.getMotherName())
                .motherPhone(student.getMotherPhone())
                .motherOccupation(student.getMotherOccupation())
                .address(student.getAddress())
                .city(student.getCity())
                .state(student.getState())
                .postalCode(student.getPostalCode())
                .emergencyContactName(student.getEmergencyContactName())
                .emergencyContactPhone(student.getEmergencyContactPhone())
                .emergencyContactRelation(student.getEmergencyContactRelation())
                .createdAt(student.getCreatedAt())
                .updatedAt(student.getUpdatedAt())
                .build();
    }

    private UserDTO mapUserToDTO(User user) {
        if (user == null) {
            return null;
        }
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

    /**
     * Check if a student record belongs to the current user
     * Used for authorization - allows students to update only their own profile
     */
    public boolean isStudentOwnRecord(Long studentId, String username) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        return student.getUser() != null && student.getUser().getUsername().equals(username);
    }
}

