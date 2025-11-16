package com.schoolmanagement.service;

import com.schoolmanagement.dto.StudentDTO;
import com.schoolmanagement.entity.Student;
import com.schoolmanagement.entity.StudentStatus;
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

        if (!student.getRollNumber().equals(studentDetails.getRollNumber()) &&
                studentRepository.existsByRollNumber(studentDetails.getRollNumber())) {
            throw new DuplicateResourceException("Roll number already exists");
        }

        if (!student.getAdmissionNumber().equals(studentDetails.getAdmissionNumber()) &&
                studentRepository.existsByAdmissionNumber(studentDetails.getAdmissionNumber())) {
            throw new DuplicateResourceException("Admission number already exists");
        }

        student.setRollNumber(studentDetails.getRollNumber());
        student.setAdmissionNumber(studentDetails.getAdmissionNumber());
        student.setDateOfBirth(studentDetails.getDateOfBirth());
        student.setGender(studentDetails.getGender());
        student.setBloodGroup(studentDetails.getBloodGroup());
        student.setClassName(studentDetails.getClassName());
        student.setSection(studentDetails.getSection());
        student.setDateOfAdmission(studentDetails.getDateOfAdmission());
        student.setStatus(studentDetails.getStatus());
        student.setFatherName(studentDetails.getFatherName());
        student.setFatherPhone(studentDetails.getFatherPhone());
        student.setFatherOccupation(studentDetails.getFatherOccupation());
        student.setMotherName(studentDetails.getMotherName());
        student.setMotherPhone(studentDetails.getMotherPhone());
        student.setMotherOccupation(studentDetails.getMotherOccupation());
        student.setAddress(studentDetails.getAddress());
        student.setCity(studentDetails.getCity());
        student.setState(studentDetails.getState());
        student.setPostalCode(studentDetails.getPostalCode());
        student.setEmergencyContactName(studentDetails.getEmergencyContactName());
        student.setEmergencyContactPhone(studentDetails.getEmergencyContactPhone());
        student.setEmergencyContactRelation(studentDetails.getEmergencyContactRelation());

        Student updatedStudent = studentRepository.save(student);
        return mapToDTO(updatedStudent);
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
}

