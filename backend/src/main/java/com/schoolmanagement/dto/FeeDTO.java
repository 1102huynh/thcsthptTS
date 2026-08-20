package com.schoolmanagement.dto;

import com.schoolmanagement.entity.FeeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeeDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long studentId;
    private String studentName;
    private String academicYear;
    private String feeType;
    private Double amount;
    private LocalDate dueDate;
    private LocalDate paidDate;
    private FeeStatus status;
    private Double paidAmount;
    private Double remainingAmount;
    private String paymentMethod;
    private String transactionId;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
