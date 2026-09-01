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
    long countByTransactionTypeAndReturnDateIsNull(TransactionType type);

    // Active (not yet returned) borrows - powers the ADMIN/LIBRARIAN
    // circulation view (Tuần 4 Ngày 1, Track Frontend) and, per-user, which
    // "Trả" button a STUDENT/TEACHER should see enabled.
    List<BookTransaction> findByTransactionTypeAndReturnDateIsNull(TransactionType type);
    List<BookTransaction> findByUserAndTransactionTypeAndReturnDateIsNull(User user, TransactionType type);
}

