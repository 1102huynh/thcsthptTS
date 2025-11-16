package com.schoolmanagement.repository;

import com.schoolmanagement.entity.BookTransaction;
import com.schoolmanagement.entity.TransactionType;
import com.schoolmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookTransactionRepository extends JpaRepository<BookTransaction, Long> {
    List<BookTransaction> findByUser(User user);
    List<BookTransaction> findByTransactionType(TransactionType type);
    List<BookTransaction> findByBorrowDateBetween(LocalDate startDate, LocalDate endDate);
    List<BookTransaction> findByDueDateBeforeAndReturnDateIsNull(LocalDate dueDate);
    List<BookTransaction> findByUserAndTransactionType(User user, TransactionType type);
}

