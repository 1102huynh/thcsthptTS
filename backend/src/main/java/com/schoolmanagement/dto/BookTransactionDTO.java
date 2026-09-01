package com.schoolmanagement.dto;

import com.schoolmanagement.entity.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Schema(description = "One book borrow/return record.")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookTransactionDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long bookId;
    private String bookTitle;
    private Long userId;
    private String userName;
    private TransactionType transactionType;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private Double fineAmount;
    private Boolean finePaid;
}
