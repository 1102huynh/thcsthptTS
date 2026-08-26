package com.schoolmanagement.service;

import com.schoolmanagement.dto.AdmissionApplicationDTO;
import com.schoolmanagement.dto.AdmissionApprovalResultDTO;
import com.schoolmanagement.dto.ApproveAndCreateRequest;
import com.schoolmanagement.dto.SubmitAdmissionRequest;
import com.schoolmanagement.dto.UpdateAdmissionStatusRequest;
import com.schoolmanagement.entity.AdmissionApplication;
import com.schoolmanagement.entity.AdmissionStatus;
import com.schoolmanagement.entity.Role;
import com.schoolmanagement.entity.Student;
import com.schoolmanagement.entity.StudentStatus;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.exception.DuplicateResourceException;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.repository.AdmissionApplicationRepository;
import com.schoolmanagement.repository.StudentRepository;
import com.schoolmanagement.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tuyển sinh đầu cấp per IMPLEMENTATION_PLAN.md 3.7. Submission is public
 * (see AdmissionController / AdmissionRateLimitFilter); everything else here
 * is ADMIN-only.
 */
@Service
@AllArgsConstructor
@Transactional
public class AdmissionService {

    private AdmissionApplicationRepository admissionApplicationRepository;
    private UserRepository userRepository;
    private StudentRepository studentRepository;
    private PasswordEncoder passwordEncoder;

    public AdmissionApplicationDTO submit(SubmitAdmissionRequest request) {
        AdmissionApplication application = AdmissionApplication.builder()
                .applicantName(request.getApplicantName())
                .dateOfBirth(request.getDateOfBirth())
                .contactPhone(request.getContactPhone())
                .desiredGradeLevel(request.getDesiredGradeLevel())
                .priorSchool(request.getPriorSchool())
                .status(AdmissionStatus.PENDING)
                .submittedAt(LocalDateTime.now())
                .build();

        return mapToDTO(admissionApplicationRepository.save(application));
    }

    public List<AdmissionApplicationDTO> getAllApplications() {
        return admissionApplicationRepository.findAllWithReviewer()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<AdmissionApplicationDTO> getApplicationsByStatus(AdmissionStatus status) {
        return admissionApplicationRepository.findByStatusWithReviewer(status)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public AdmissionApplicationDTO getApplicationById(Long id) {
        return mapToDTO(admissionApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admission application not found with id: " + id)));
    }

    public AdmissionApplicationDTO updateStatus(Long id, UpdateAdmissionStatusRequest request, User reviewer) {
        AdmissionApplication application = admissionApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admission application not found with id: " + id));

        application.setStatus(request.getStatus());
        // note is optional on the request - omitting it (a later reviewer just
        // changing the status) must not silently wipe out a previous reviewer's
        // note. Send an explicit empty string to actually clear it.
        if (request.getNote() != null) {
            application.setNote(request.getNote());
        }
        application.setReviewedBy(reviewer);

        return mapToDTO(admissionApplicationRepository.save(application));
    }

    /**
     * Turns an already-APPROVED application into a real STUDENT account —
     * name/DOB/phone/priorSchool come from the application; username/email/
     * password/rollNumber/admissionNumber must be supplied (nothing to pull
     * them from — see ApproveAndCreateRequest's Javadoc).
     */
    public AdmissionApprovalResultDTO approveAndCreate(Long id, ApproveAndCreateRequest request) {
        AdmissionApplication application = admissionApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admission application not found with id: " + id));

        if (application.getStatus() != AdmissionStatus.APPROVED) {
            throw new IllegalArgumentException(
                    "Application " + id + " must be APPROVED before an account can be created from it (current status: "
                            + application.getStatus() + ") — see PUT /v1/admissions/" + id + "/status");
        }
        if (application.getCreatedStudent() != null) {
            throw new DuplicateResourceException(
                    "A student account was already created from application " + id
                            + " (student id " + application.getCreatedStudent().getId() + ")");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }
        if (studentRepository.existsByRollNumber(request.getRollNumber())) {
            throw new DuplicateResourceException("Roll number already exists");
        }
        if (studentRepository.existsByAdmissionNumber(request.getAdmissionNumber())) {
            throw new DuplicateResourceException("Admission number already exists");
        }

        String[] nameParts = splitName(application.getApplicantName());

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(nameParts[0])
                .lastName(nameParts[1])
                .phoneNumber(application.getContactPhone())
                .role(Role.STUDENT)
                .enabled(true)
                .build();
        Student student = Student.builder()
                .rollNumber(request.getRollNumber())
                .admissionNumber(request.getAdmissionNumber())
                .dateOfBirth(application.getDateOfBirth())
                .dateOfAdmission(LocalDate.now())
                .status(StudentStatus.ACTIVE)
                .build();

        try {
            // IDENTITY-strategy @GeneratedValue means these INSERTs execute
            // immediately, not deferred to commit - so a unique-constraint hit
            // here (e.g. two applications approved concurrently landing on the
            // same username/rollNumber, which the exists() checks above can't
            // rule out on their own — TOCTOU) is caught here, not after this
            // method has already returned.
            user = userRepository.save(user);
            student.setUser(user);
            student = studentRepository.save(student);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateResourceException(
                    "Username, email, roll number, or admission number was just taken by another request — retry with different values");
        }

        application.setCreatedStudent(student);
        try {
            // saveAndFlush, not save: the UPDATE (and thus the @Version check)
            // would otherwise be deferred to the transaction's commit-time flush,
            // which happens after this method returns — outside this try/catch —
            // so a losing concurrent call's ObjectOptimisticLockingFailureException
            // would go uncaught and surface as a masked 500 instead of 409.
            admissionApplicationRepository.saveAndFlush(application);
        } catch (ObjectOptimisticLockingFailureException ex) {
            // Two concurrent approve-and-create calls both read this application
            // before either committed - the @Version check on this second save
            // caught the race the createdStudent==null check above couldn't.
            // This whole method is @Transactional, so the User/Student this call
            // just created above are rolled back along with this failed save -
            // they never actually persist. Surfacing 409 so the caller knows to
            // check the application's current state before retrying.
            throw new DuplicateResourceException(
                    "Application " + id + " was just processed by another request — check its current status before retrying");
        }

        return AdmissionApprovalResultDTO.builder()
                .applicationId(application.getId())
                .userId(user.getId())
                .username(user.getUsername())
                .studentId(student.getId())
                .rollNumber(student.getRollNumber())
                .admissionNumber(student.getAdmissionNumber())
                .build();
    }

    /**
     * Vietnamese full names are Họ + (tên đệm) + Tên, not "first/last" — but
     * User only has firstName/lastName (Phase 1 schema). Splits on the first
     * space: firstName = họ (family name, first word), lastName = everything
     * else, matching the convention already used for Vietnamese test/seed
     * names elsewhere in this codebase.
     */
    private String[] splitName(String fullName) {
        String[] parts = fullName.trim().split("\\s+", 2);
        return parts.length > 1 ? parts : new String[]{parts[0], ""};
    }

    private AdmissionApplicationDTO mapToDTO(AdmissionApplication application) {
        User reviewedBy = application.getReviewedBy();
        Student createdStudent = application.getCreatedStudent();

        return AdmissionApplicationDTO.builder()
                .id(application.getId())
                .applicantName(application.getApplicantName())
                .dateOfBirth(application.getDateOfBirth())
                .contactPhone(application.getContactPhone())
                .desiredGradeLevel(application.getDesiredGradeLevel())
                .priorSchool(application.getPriorSchool())
                .status(application.getStatus())
                .submittedAt(application.getSubmittedAt())
                .reviewedById(reviewedBy != null ? reviewedBy.getId() : null)
                .reviewedByName(reviewedBy != null ? reviewedBy.getFirstName() + " " + reviewedBy.getLastName() : null)
                .note(application.getNote())
                .createdStudentId(createdStudent != null ? createdStudent.getId() : null)
                .createdAt(application.getCreatedAt())
                .updatedAt(application.getUpdatedAt())
                .build();
    }
}
