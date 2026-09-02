package com.schoolmanagement.service;

import com.schoolmanagement.dto.BookTransactionDTO;
import com.schoolmanagement.dto.LibraryBookDTO;
import com.schoolmanagement.entity.*;
import com.schoolmanagement.exception.DuplicateResourceException;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.repository.BookTransactionRepository;
import com.schoolmanagement.repository.LibraryBookRepository;
import com.schoolmanagement.repository.StudentRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class LibraryService {

    private LibraryBookRepository bookRepository;
    private BookTransactionRepository transactionRepository;
    private StudentRepository studentRepository;

    public LibraryBookDTO addBook(LibraryBook book) {
        if (bookRepository.existsByIsbn(book.getIsbn())) {
            throw new DuplicateResourceException("Book with ISBN already exists: " + book.getIsbn());
        }

        LibraryBook savedBook = bookRepository.save(book);
        return mapToDTO(savedBook);
    }

    public LibraryBookDTO updateBook(Long id, LibraryBook bookDetails) {
        LibraryBook book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));

        if (!book.getIsbn().equals(bookDetails.getIsbn()) &&
                bookRepository.existsByIsbn(bookDetails.getIsbn())) {
            throw new DuplicateResourceException("ISBN already exists");
        }

        book.setIsbn(bookDetails.getIsbn());
        book.setTitle(bookDetails.getTitle());
        book.setAuthor(bookDetails.getAuthor());
        book.setPublisher(bookDetails.getPublisher());
        book.setPublicationYear(bookDetails.getPublicationYear());
        book.setEdition(bookDetails.getEdition());
        book.setCategory(bookDetails.getCategory());
        book.setTotalCopies(bookDetails.getTotalCopies());
        book.setAvailableCopies(bookDetails.getAvailableCopies());
        book.setDescription(bookDetails.getDescription());
        book.setLocationRack(bookDetails.getLocationRack());
        book.setCallNumber(bookDetails.getCallNumber());
        book.setStatus(bookDetails.getStatus());
        book.setPrice(bookDetails.getPrice());

        LibraryBook updatedBook = bookRepository.save(book);
        return mapToDTO(updatedBook);
    }

    public LibraryBookDTO getBookById(Long id) {
        LibraryBook book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
        return mapToDTO(book);
    }

    public List<LibraryBookDTO> getAllBooks() {
        return bookRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public Page<LibraryBookDTO> getAllBooks(Pageable pageable) {
        return bookRepository.findAll(pageable).map(this::mapToDTO);
    }

    public List<LibraryBookDTO> searchBooks(String title) {
        return bookRepository.findByTitleContainingIgnoreCase(title)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<LibraryBookDTO> getBooksByCategory(BookCategory category) {
        return bookRepository.findByCategory(category)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<LibraryBookDTO> getBooksByAuthor(String author) {
        return bookRepository.findByAuthor(author)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<LibraryBookDTO> getAvailableBooks() {
        return bookRepository.findByStatus(BookStatus.AVAILABLE)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public void deleteBook(Long id) {
        LibraryBook book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
        bookRepository.delete(book);
    }

    public void borrowBook(Long bookId, User user, int borrowDays) {
        LibraryBook book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        if (book.getAvailableCopies() <= 0) {
            throw new ResourceNotFoundException("No copies available for borrowing");
        }

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        if (book.getAvailableCopies() == 0) {
            book.setStatus(BookStatus.BORROWED);
        }

        BookTransaction transaction = BookTransaction.builder()
                .book(book)
                .user(user)
                .transactionType(TransactionType.BORROW)
                .borrowDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(borrowDays))
                .build();

        bookRepository.save(book);
        transactionRepository.save(transaction);
    }

    /**
     * "Ghi mượn hộ" — a LIBRARIAN (or ADMIN) records a borrow against a
     * student who isn't the caller (G.4 mục 5 / H.2.3). Reuses
     * {@link #borrowBook} with the student's linked user account so the
     * BookTransaction / availableCopies bookkeeping is identical to a
     * self-service borrow.
     */
    public void lendBookToStudent(Long bookId, Long studentId, int borrowDays) {
        borrowBook(bookId, resolveStudentUser(studentId), borrowDays);
    }

    /** "Ghi trả hộ" — the return-side counterpart of {@link #lendBookToStudent}. */
    public void returnBookForStudent(Long bookId, Long studentId) {
        returnBook(bookId, resolveStudentUser(studentId));
    }

    private User resolveStudentUser(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        if (student.getUser() == null) {
            throw new ResourceNotFoundException("Student " + studentId + " has no linked user account to borrow against");
        }
        return student.getUser();
    }

    public void returnBook(Long bookId, User user) {
        LibraryBook book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        List<BookTransaction> activeTransaction = transactionRepository.findByUserAndTransactionType(user, TransactionType.BORROW)
                .stream()
                .filter(t -> t.getBook().getId().equals(bookId) && t.getReturnDate() == null)
                .collect(Collectors.toList());

        if (activeTransaction.isEmpty()) {
            throw new ResourceNotFoundException("No active borrow transaction found");
        }

        BookTransaction transaction = activeTransaction.get(0);
        transaction.setReturnDate(LocalDate.now());

        if (LocalDate.now().isAfter(transaction.getDueDate())) {
            long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(transaction.getDueDate(), LocalDate.now());
            transaction.setFineAmount(daysOverdue * 10.0);
        }

        book.setAvailableCopies(book.getAvailableCopies() + 1);
        if (book.getStatus() == BookStatus.BORROWED && book.getAvailableCopies() > 0) {
            book.setStatus(BookStatus.AVAILABLE);
        }

        bookRepository.save(book);
        transactionRepository.save(transaction);
    }

    /**
     * The caller's own borrow/return history - drives the frontend's
     * per-book "Mượn"/"Trả" button state for a STUDENT/TEACHER (they can't
     * otherwise know which books they currently have out; see
     * LibraryController for why this and getActiveBorrows were added -
     * there was previously no endpoint exposing BookTransaction at all).
     */
    public List<BookTransactionDTO> getMyTransactions(User user) {
        return transactionRepository.findByUser(user)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /** All currently-outstanding (not yet returned) borrows - ADMIN/LIBRARIAN circulation view. */
    public List<BookTransactionDTO> getActiveBorrows() {
        return transactionRepository.findByTransactionTypeAndReturnDateIsNull(TransactionType.BORROW)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private BookTransactionDTO mapToDTO(BookTransaction transaction) {
        return BookTransactionDTO.builder()
                .id(transaction.getId())
                .bookId(transaction.getBook().getId())
                .bookTitle(transaction.getBook().getTitle())
                .userId(transaction.getUser().getId())
                .userName(transaction.getUser().getFirstName() + " " + transaction.getUser().getLastName())
                .transactionType(transaction.getTransactionType())
                .borrowDate(transaction.getBorrowDate())
                .dueDate(transaction.getDueDate())
                .returnDate(transaction.getReturnDate())
                .fineAmount(transaction.getFineAmount())
                .finePaid(transaction.getFinePaid())
                .build();
    }

    private LibraryBookDTO mapToDTO(LibraryBook book) {
        return LibraryBookDTO.builder()
                .id(book.getId())
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .author(book.getAuthor())
                .publisher(book.getPublisher())
                .publicationYear(book.getPublicationYear())
                .edition(book.getEdition())
                .category(book.getCategory())
                .totalCopies(book.getTotalCopies())
                .availableCopies(book.getAvailableCopies())
                .description(book.getDescription())
                .locationRack(book.getLocationRack())
                .callNumber(book.getCallNumber())
                .status(book.getStatus())
                .price(book.getPrice())
                .acquisitionDate(book.getAcquisitionDate())
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .build();
    }
}

