package com.schoolmanagement.service;

import com.schoolmanagement.constants.VietnamConstants;
import com.schoolmanagement.entity.StudentVN;
import com.schoolmanagement.repository.StudentVNRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class StudentVNService {

    private StudentVNRepository studentVNRepository;

    // ============ CRUD OPERATIONS ============

    public List<StudentVN> getAllStudents() {
        return studentVNRepository.findAll();
    }

    public StudentVN getStudentById(Long id) {
        return studentVNRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + id));
    }

    public Optional<StudentVN> getStudentByCode(String studentCode) {
        return studentVNRepository.findByStudentCode(studentCode);
    }

    public Optional<StudentVN> getStudentByIdNumber(String idNumber) {
        return studentVNRepository.findByIdNumber(idNumber);
    }

    @Transactional
    public StudentVN createStudent(StudentVN student) {
        // Validate required fields
        validateStudent(student);
        
        // Generate student code if not provided
        if (student.getStudentCode() == null || student.getStudentCode().isEmpty()) {
            student.setStudentCode(generateStudentCode(student));
        }
        
        // Validate student code
        if (studentVNRepository.existsByStudentCode(student.getStudentCode())) {
            throw new RuntimeException("Student code already exists: " + student.getStudentCode());
        }
        
        // Validate ID number if provided
        if (student.getIdNumber() != null && !student.getIdNumber().isEmpty()) {
            if (studentVNRepository.existsByIdNumber(student.getIdNumber())) {
                throw new RuntimeException("ID number already exists: " + student.getIdNumber());
            }
        }
        
        // Calculate expected graduation year
        if (student.getAdmissionYear() != null && student.getGradeLevel() != null) {
            student.setExpectedGraduationYear(
                calculateExpectedGraduationYear(student.getAdmissionYear(), student.getGradeLevel().getLevelNumber())
            );
        }
        
        // Set timestamps
        student.setCreatedAt(java.time.LocalDateTime.now());
        student.setUpdatedAt(java.time.LocalDateTime.now());
        
        return studentVNRepository.save(student);
    }

    @Transactional
    public StudentVN updateStudent(Long id, StudentVN studentDetails) {
        StudentVN student = getStudentById(id);
        
        // Update basic info
        student.setLastName(studentDetails.getLastName());
        student.setFirstName(studentDetails.getFirstName());
        student.setDateOfBirth(studentDetails.getDateOfBirth());
        student.setGender(studentDetails.getGender());
        student.setPlaceOfBirth(studentDetails.getPlaceOfBirth());
        
        // Update ID info (validate if changed)
        if (studentDetails.getIdNumber() != null && 
            !studentDetails.getIdNumber().equals(student.getIdNumber())) {
            if (studentVNRepository.existsByIdNumber(studentDetails.getIdNumber())) {
                throw new RuntimeException("ID number already exists: " + studentDetails.getIdNumber());
            }
            student.setIdNumber(studentDetails.getIdNumber());
            student.setIdIssueDate(studentDetails.getIdIssueDate());
            student.setIdIssuePlace(studentDetails.getIdIssuePlace());
        }
        
        // Update address
        student.setProvince(studentDetails.getProvince());
        student.setDistrict(studentDetails.getDistrict());
        student.setWard(studentDetails.getWard());
        student.setDetailedAddress(studentDetails.getDetailedAddress());
        student.setPhoneNumber(studentDetails.getPhoneNumber());
        
        // Update ethnicity & religion
        student.setEthnicity(studentDetails.getEthnicity());
        student.setReligion(studentDetails.getReligion());
        student.setPriorityObject(studentDetails.getPriorityObject());
        
        // Update academic info
        student.setGradeLevel(studentDetails.getGradeLevel());
        student.setSchoolClass(studentDetails.getSchoolClass());
        student.setAcademicYear(studentDetails.getAcademicYear());
        student.setStatus(studentDetails.getStatus());
        
        // Update parent info
        updateParentInfo(student, studentDetails);
        
        // Update previous school info
        updatePreviousSchoolInfo(student, studentDetails);
        
        // Update health info
        updateHealthInfo(student, studentDetails);
        
        // Update file URLs
        updateFileUrls(student, studentDetails);
        
        // Update notes
        student.setNotes(studentDetails.getNotes());
        
        // Update timestamp
        student.setUpdatedAt(java.time.LocalDateTime.now());
        
        return studentVNRepository.save(student);
    }

    @Transactional
    public void deleteStudent(Long id) {
        StudentVN student = getStudentById(id);
        studentVNRepository.delete(student);
    }

    @Transactional
    public StudentVN updateStudentStatus(Long id, String status) {
        StudentVN student = getStudentById(id);
        student.setStatus(status);
        student.setUpdatedAt(java.time.LocalDateTime.now());
        return studentVNRepository.save(student);
    }

    // ============ QUERY OPERATIONS ============

    public List<StudentVN> getStudentsByClass(Long classId) {
        return studentVNRepository.findBySchoolClass_Id(classId);
    }

    public List<StudentVN> getStudentsByGradeLevel(Long gradeLevelId) {
        return studentVNRepository.findByGradeLevel_Id(gradeLevelId);
    }

    public List<StudentVN> getStudentsByStatus(String status) {
        return studentVNRepository.findByStatus(status);
    }

    public List<StudentVN> getStudentsByAcademicYear(String academicYear) {
        return studentVNRepository.findByAcademicYear(academicYear);
    }

    public List<StudentVN> searchStudentsByName(String name) {
        return studentVNRepository.searchByName(name);
    }

    public List<StudentVN> advancedSearch(String studentCode, Long classId, Long gradeLevelId, 
                                          String status, String name) {
        return studentVNRepository.advancedSearch(studentCode, classId, gradeLevelId, status, name);
    }

    // ============ STATISTICS ============

    public Long countStudentsByClass(Long classId) {
        return studentVNRepository.countBySchoolClass_Id(classId);
    }

    public Long countStudentsByGradeLevel(Long gradeLevelId) {
        return studentVNRepository.countByGradeLevel_Id(gradeLevelId);
    }

    public Long countStudentsByStatus(String status) {
        return studentVNRepository.countByStatus(status);
    }

    // ============ BUSINESS LOGIC ============

    /**
     * Generate student code automatically
     * Format: XXYYZZNNNN (SchoolCode-Year-Grade-Sequential)
     */
    public String generateStudentCode(StudentVN student) {
        int schoolCode = 1; // TODO: Get from configuration
        int admissionYear = student.getAdmissionYear() != null ? student.getAdmissionYear() : LocalDate.now().getYear();
        int gradeLevel = student.getGradeLevel() != null ? student.getGradeLevel().getLevelNumber() : 6;
        
        // Get the latest student of this admission year to determine sequential number
        List<StudentVN> latestStudents = studentVNRepository.findLatestByAdmissionYear(admissionYear);
        int sequentialNumber = 1;
        
        if (!latestStudents.isEmpty()) {
            String latestCode = latestStudents.get(0).getStudentCode();
            if (latestCode != null && latestCode.length() == 10) {
                try {
                    int lastSeq = Integer.parseInt(latestCode.substring(6));
                    sequentialNumber = lastSeq + 1;
                } catch (NumberFormatException e) {
                    // Use default 1
                }
            }
        }
        
        return VietnamConstants.generateStudentCode(schoolCode, admissionYear, gradeLevel, sequentialNumber);
    }

    /**
     * Calculate expected graduation year
     */
    private Integer calculateExpectedGraduationYear(Integer admissionYear, Integer currentGrade) {
        // THCS: Grade 6-9 (4 years)
        // THPT: Grade 10-12 (3 years)
        int yearsRemaining;
        if (currentGrade <= 9) {
            yearsRemaining = 10 - currentGrade; // THCS graduation at grade 9
        } else {
            yearsRemaining = 13 - currentGrade; // THPT graduation at grade 12
        }
        return admissionYear + yearsRemaining;
    }

    /**
     * Validate student data
     */
    private void validateStudent(StudentVN student) {
        // Required fields
        if (student.getLastName() == null || student.getLastName().trim().isEmpty()) {
            throw new RuntimeException("Last name is required");
        }
        if (student.getFirstName() == null || student.getFirstName().trim().isEmpty()) {
            throw new RuntimeException("First name is required");
        }
        if (student.getDateOfBirth() == null) {
            throw new RuntimeException("Date of birth is required");
        }
        if (student.getGender() == null || student.getGender().trim().isEmpty()) {
            throw new RuntimeException("Gender is required");
        }
        
        // Age validation (must be between 5-25 years old)
        int age = Period.between(student.getDateOfBirth(), LocalDate.now()).getYears();
        if (age < 5 || age > 25) {
            throw new RuntimeException("Invalid age. Student must be between 5-25 years old");
        }
        
        // Validate province if provided
        if (student.getProvince() != null && !student.getProvince().isEmpty()) {
            if (!VietnamConstants.isValidProvince(student.getProvince())) {
                throw new RuntimeException("Invalid province: " + student.getProvince());
            }
        }
        
        // Validate ethnicity if provided
        if (student.getEthnicity() != null && !student.getEthnicity().isEmpty()) {
            if (!VietnamConstants.isValidEthnicity(student.getEthnicity())) {
                throw new RuntimeException("Invalid ethnicity: " + student.getEthnicity());
            }
        }
        
        // Validate phone numbers format
        if (student.getPhoneNumber() != null && !student.getPhoneNumber().isEmpty()) {
            if (!student.getPhoneNumber().matches("^\\d{10,11}$")) {
                throw new RuntimeException("Invalid phone number format. Must be 10-11 digits");
            }
        }
        
        // Validate CMND/CCCD format if provided
        if (student.getIdNumber() != null && !student.getIdNumber().isEmpty()) {
            if (!student.getIdNumber().matches("^\\d{9,12}$")) {
                throw new RuntimeException("Invalid ID number format. Must be 9-12 digits");
            }
        }
    }

    // ============ HELPER METHODS ============

    private void updateParentInfo(StudentVN student, StudentVN details) {
        student.setFatherName(details.getFatherName());
        student.setFatherYearOfBirth(details.getFatherYearOfBirth());
        student.setFatherOccupation(details.getFatherOccupation());
        student.setFatherWorkplace(details.getFatherWorkplace());
        student.setFatherPhone(details.getFatherPhone());
        student.setFatherEmail(details.getFatherEmail());
        
        student.setMotherName(details.getMotherName());
        student.setMotherYearOfBirth(details.getMotherYearOfBirth());
        student.setMotherOccupation(details.getMotherOccupation());
        student.setMotherWorkplace(details.getMotherWorkplace());
        student.setMotherPhone(details.getMotherPhone());
        student.setMotherEmail(details.getMotherEmail());
        
        student.setGuardianName(details.getGuardianName());
        student.setGuardianRelationship(details.getGuardianRelationship());
        student.setGuardianPhone(details.getGuardianPhone());
        student.setGuardianAddress(details.getGuardianAddress());
    }

    private void updatePreviousSchoolInfo(StudentVN student, StudentVN details) {
        student.setPreviousSchool(details.getPreviousSchool());
        student.setPreviousSchoolAddress(details.getPreviousSchoolAddress());
        student.setPreviousSchoolFrom(details.getPreviousSchoolFrom());
        student.setPreviousSchoolTo(details.getPreviousSchoolTo());
        student.setTransferReason(details.getTransferReason());
        student.setPreviousAcademicRank(details.getPreviousAcademicRank());
        student.setPreviousConductRank(details.getPreviousConductRank());
        student.setAwards(details.getAwards());
    }

    private void updateHealthInfo(StudentVN student, StudentVN details) {
        student.setHeight(details.getHeight());
        student.setWeight(details.getWeight());
        student.setBloodType(details.getBloodType());
        student.setDiseases(details.getDiseases());
        student.setAllergies(details.getAllergies());
    }

    private void updateFileUrls(StudentVN student, StudentVN details) {
        if (details.getPhotoUrl() != null) {
            student.setPhotoUrl(details.getPhotoUrl());
        }
        if (details.getBirthCertificateUrl() != null) {
            student.setBirthCertificateUrl(details.getBirthCertificateUrl());
        }
        if (details.getHouseholdBookUrl() != null) {
            student.setHouseholdBookUrl(details.getHouseholdBookUrl());
        }
        if (details.getOtherDocumentsUrl() != null) {
            student.setOtherDocumentsUrl(details.getOtherDocumentsUrl());
        }
    }
}
