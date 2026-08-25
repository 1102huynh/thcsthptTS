package com.schoolmanagement.service;

import com.schoolmanagement.dto.FeeDTO;
import com.schoolmanagement.entity.Fee;
import com.schoolmanagement.entity.FeeStatus;
import com.schoolmanagement.entity.Student;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.repository.FeeRepository;
import com.schoolmanagement.repository.StudentRepository;
import com.schoolmanagement.security.StudentAccessGuard;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
@Transactional
public class FeeService {

    private FeeRepository feeRepository;
    private StudentRepository studentRepository;
    private StudentAccessGuard studentAccessGuard;

    public Fee createFee(Fee fee) {
        Student student = studentRepository.findById(fee.getStudent().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        fee.setRemainingAmount(fee.getAmount());
        return feeRepository.save(fee);
    }

    public FeeDTO updateFee(Long id, Fee feeDetails) {
        Fee fee = feeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fee record not found"));

        fee.setAmount(feeDetails.getAmount());
        fee.setDueDate(feeDetails.getDueDate());
        fee.setFeeType(feeDetails.getFeeType());

        return mapToDTO(feeRepository.save(fee));
    }

    public FeeDTO getFeeById(Long id, User requester) {
        Fee fee = feeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fee record not found"));
        studentAccessGuard.enforceCanAccessStudent(fee.getStudent().getId(), requester);
        return mapToDTO(fee);
    }

    public List<FeeDTO> getStudentFees(Long studentId, User requester) {
        studentAccessGuard.enforceCanAccessStudent(studentId, requester);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        return feeRepository.findByStudent(student).stream().map(this::mapToDTO).toList();
    }

    public List<FeeDTO> getStudentFeesByYear(Long studentId, String academicYear, User requester) {
        studentAccessGuard.enforceCanAccessStudent(studentId, requester);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        return feeRepository.findByStudentAndAcademicYear(student, academicYear).stream().map(this::mapToDTO).toList();
    }

    public List<FeeDTO> getStudentPendingFees(Long studentId, User requester) {
        studentAccessGuard.enforceCanAccessStudent(studentId, requester);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        return feeRepository.findByStudentAndStatus(student, FeeStatus.PENDING).stream().map(this::mapToDTO).toList();
    }

    public List<FeeDTO> getFeesByStatus(FeeStatus status) {
        return feeRepository.findByStatus(status).stream().map(this::mapToDTO).toList();
    }

    /**
     * Every read path here returns FeeDTO, never the raw entity — its lazy
     * `student` association is not resolved by the time Jackson serializes
     * the response (open-in-view is off, so the persistence context is
     * already closed), which throws LazyInitializationException. (Found live
     * while retrofitting PARENT access in 3.6 — pre-existing, affected every
     * role, not just the new one; fixed for all of these methods at once
     * rather than only the ones PARENT needed.)
     */
    public List<FeeDTO> getFeesByAcademicYear(String academicYear) {
        return feeRepository.findByAcademicYear(academicYear)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    public Page<FeeDTO> getFeesByAcademicYear(String academicYear, Pageable pageable) {
        return feeRepository.findByAcademicYear(academicYear, pageable).map(this::mapToDTO);
    }

    public FeeDTO processPayment(Long feeId, Double paidAmount, String paymentMethod, User requester) {
        Fee fee = feeRepository.findById(feeId)
                .orElseThrow(() -> new ResourceNotFoundException("Fee record not found"));
        studentAccessGuard.enforceCanAccessStudent(fee.getStudent().getId(), requester);

        if (paidAmount <= 0) {
            throw new IllegalArgumentException("Paid amount must be greater than zero");
        }

        fee.setPaidDate(LocalDate.now());
        fee.setPaymentMethod(paymentMethod);

        if (fee.getPaidAmount() == null) {
            fee.setPaidAmount(0.0);
        }

        Double newPaidAmount = fee.getPaidAmount() + paidAmount;
        fee.setPaidAmount(newPaidAmount);
        fee.setRemainingAmount(fee.getAmount() - newPaidAmount);

        if (fee.getRemainingAmount() <= 0) {
            fee.setStatus(FeeStatus.PAID);
        } else if (fee.getRemainingAmount() < fee.getAmount()) {
            fee.setStatus(FeeStatus.PARTIAL_PAID);
        }

        if (fee.getDueDate() != null && LocalDate.now().isAfter(fee.getDueDate()) && fee.getStatus() != FeeStatus.PAID) {
            fee.setStatus(FeeStatus.OVERDUE);
        }

        return mapToDTO(feeRepository.save(fee));
    }

    public Double getStudentTotalDues(Long studentId, User requester) {
        studentAccessGuard.enforceCanAccessStudent(studentId, requester);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        List<Fee> fees = feeRepository.findByStudent(student);
        return fees.stream()
                .filter(fee -> fee.getStatus() != FeeStatus.PAID && fee.getStatus() != FeeStatus.EXEMPTED)
                .mapToDouble(Fee::getRemainingAmount)
                .sum();
    }

    public void deleteFee(Long id) {
        Fee fee = feeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fee record not found"));
        feeRepository.delete(fee);
    }

    private FeeDTO mapToDTO(Fee fee) {
        Student student = fee.getStudent();

        return FeeDTO.builder()
                .id(fee.getId())
                .studentId(student != null ? student.getId() : null)
                .studentName(student != null && student.getUser() != null
                        ? student.getUser().getFirstName() + " " + student.getUser().getLastName()
                        : null)
                .academicYear(fee.getAcademicYear())
                .feeType(fee.getFeeType())
                .amount(fee.getAmount())
                .dueDate(fee.getDueDate())
                .paidDate(fee.getPaidDate())
                .status(fee.getStatus())
                .paidAmount(fee.getPaidAmount())
                .remainingAmount(fee.getRemainingAmount())
                .paymentMethod(fee.getPaymentMethod())
                .transactionId(fee.getTransactionId())
                .remarks(fee.getRemarks())
                .createdAt(fee.getCreatedAt())
                .updatedAt(fee.getUpdatedAt())
                .build();
    }
}

