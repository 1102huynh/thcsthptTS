package com.schoolmanagement.service;

import com.schoolmanagement.dto.StudentDTO;
import com.schoolmanagement.dto.UserDTO;
import com.schoolmanagement.entity.DocumentAttachment;
import com.schoolmanagement.entity.DocumentOwnerType;
import com.schoolmanagement.entity.Role;
import com.schoolmanagement.entity.Student;
import com.schoolmanagement.entity.StudentStatus;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.exception.DuplicateResourceException;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.repository.DocumentAttachmentRepository;
import com.schoolmanagement.repository.StudentRepository;
import com.schoolmanagement.repository.UserRepository;
import com.schoolmanagement.security.TeacherHomeroomGuard;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class StudentService {

    private StudentRepository studentRepository;
    private AuditLogService auditLogService;
    private DocumentAttachmentRepository documentAttachmentRepository;
    private FileStorageService fileStorageService;
    private UserRepository userRepository;
    private TeacherHomeroomGuard teacherHomeroomGuard;

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

    /**
     * The student record linked to a given user account — backs
     * {@code GET /v1/students/me} for the STUDENT self-service portal (C3).
     * A 404 here means the account has the STUDENT role but no Student row
     * points at it (a data-setup problem), not an auth failure.
     */
    public StudentDTO getStudentByUserId(Long userId) {
        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No student profile is linked to this account"));
        return mapToDTO(student);
    }

    public List<StudentDTO> getAllStudents() {
        return studentRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public Page<StudentDTO> getAllStudents(Pageable pageable) {
        return studentRepository.findAll(pageable).map(this::mapToDTO);
    }

    /**
     * GVCN scoping (H.3.1) — a TEACHER only ever sees students in the class(es)
     * they are homeroom teacher of; every other role is unaffected (see
     * TeacherHomeroomGuard's "only narrows, never grants" contract).
     */
    public List<StudentDTO> getAllStudents(User requester) {
        List<Student> students = teacherHomeroomGuard.filterToHomeroom(studentRepository.findAll(), requester);
        return students.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    /**
     * Same GVCN scoping as {@link #getAllStudents(User)}, applied in memory
     * because the filter isn't expressible as a repository query (Student has
     * no FK to SchoolClass, only className/section strings — see
     * TeacherHomeroomGuard). Fine at the scale a single TEACHER's homeroom
     * roster or even a whole school's student list reaches; would need a
     * proper query if this ever had to paginate across the whole DB for a
     * non-TEACHER caller too (it doesn't - see the plain Pageable overload above).
     */
    public Page<StudentDTO> getAllStudents(Pageable pageable, User requester) {
        if (requester == null || requester.getRole() != Role.TEACHER) {
            return getAllStudents(pageable);
        }
        List<Student> filtered = teacherHomeroomGuard.filterToHomeroom(studentRepository.findAll(), requester);
        int start = Math.min((int) pageable.getOffset(), filtered.size());
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        List<StudentDTO> content = filtered.subList(start, end).stream().map(this::mapToDTO).collect(Collectors.toList());
        return new PageImpl<>(content, pageable, filtered.size());
    }

    public List<StudentDTO> getStudentsByClass(String className, User requester) {
        List<Student> students = teacherHomeroomGuard.filterToHomeroom(
                studentRepository.findByClassName(className), requester);
        return students.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<StudentDTO> getStudentsByClassAndSection(String className, String section, User requester) {
        teacherHomeroomGuard.enforceHomeroomClassNameSection(className, section, requester);
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

    public void deleteStudent(Long id, User actor) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
        String rollNumber = student.getRollNumber();

        // document_attachments.owner_id has no FK to students (it's polymorphic
        // - see DocumentOwnerType) so nothing at the DB level would catch this:
        // without cleaning these up first, deleting the student would silently
        // leave orphaned DocumentAttachment rows and their files on disk with
        // no owner and no code path that will ever remove them.
        List<DocumentAttachment> documents = documentAttachmentRepository
                .findByOwnerTypeAndOwnerIdOrderByUploadedAtDesc(DocumentOwnerType.STUDENT, id);
        documentAttachmentRepository.deleteAll(documents);
        documents.forEach(doc -> fileStorageService.delete(doc.getStoredFileName()));

        studentRepository.delete(student);

        auditLogService.log(actor, "DELETE", "Student", id, Map.of("rollNumber", rollNumber));
    }

    private StudentDTO mapToDTO(Student student) {
        return StudentDTO.builder()
                .id(student.getId())
                .rollNumber(student.getRollNumber())
                .admissionNumber(student.getAdmissionNumber())
                .user(fetchUserDTO(student.getUser()))
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

    // StudentDTO.user was never populated before this - same gap as
    // StaffService.mapToDTO, fixed there too, see its comment for how it
    // was found. Re-fetches by id for the same reason as StaffService's
    // fetchUserDTO: right after a fresh create, student.getUser() is still
    // the transient request-body stub, not a hydrated entity.
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

