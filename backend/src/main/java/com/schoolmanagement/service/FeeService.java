package com.schoolmanagement.service;

import com.schoolmanagement.entity.Fee;
import com.schoolmanagement.entity.FeeStatus;
import com.schoolmanagement.entity.Student;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.repository.FeeRepository;
import com.schoolmanagement.repository.StudentRepository;
import lombok.AllArgsConstructor;
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

    public Fee createFee(Fee fee) {
        Student student = studentRepository.findById(fee.getStudent().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        fee.setRemainingAmount(fee.getAmount());
        return feeRepository.save(fee);
    }

    public Fee updateFee(Long id, Fee feeDetails) {
        Fee fee = feeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fee record not found"));

        fee.setAmount(feeDetails.getAmount());
        fee.setDueDate(feeDetails.getDueDate());
        fee.setFeeType(feeDetails.getFeeType());

        return feeRepository.save(fee);
    }

    public Fee getFeeById(Long id) {
        return feeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fee record not found"));
    }

    public List<Fee> getStudentFees(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        return feeRepository.findByStudent(student);
    }

    public List<Fee> getStudentFeesByYear(Long studentId, String academicYear) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        return feeRepository.findByStudentAndAcademicYear(student, academicYear);
    }

    public List<Fee> getStudentPendingFees(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        return feeRepository.findByStudentAndStatus(student, FeeStatus.PENDING);
    }

    public List<Fee> getFeesByStatus(FeeStatus status) {
        return feeRepository.findByStatus(status);
    }

    public List<Fee> getFeesByAcademicYear(String academicYear) {
        return feeRepository.findByAcademicYear(academicYear);
    }

    public Fee processPayment(Long feeId, Double paidAmount, String paymentMethod) {
        Fee fee = feeRepository.findById(feeId)
                .orElseThrow(() -> new ResourceNotFoundException("Fee record not found"));

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

        return feeRepository.save(fee);
    }

    public Double getStudentTotalDues(Long studentId) {
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
}

