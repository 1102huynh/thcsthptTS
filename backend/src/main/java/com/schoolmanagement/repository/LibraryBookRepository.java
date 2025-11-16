package com.schoolmanagement.repository;

import com.schoolmanagement.entity.LibraryBook;
import com.schoolmanagement.entity.BookCategory;
import com.schoolmanagement.entity.BookStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LibraryBookRepository extends JpaRepository<LibraryBook, Long> {
    Optional<LibraryBook> findByIsbn(String isbn);
    List<LibraryBook> findByCategory(BookCategory category);
    List<LibraryBook> findByStatus(BookStatus status);
    List<LibraryBook> findByAuthor(String author);
    List<LibraryBook> findByTitleContainingIgnoreCase(String title);
    boolean existsByIsbn(String isbn);
}

