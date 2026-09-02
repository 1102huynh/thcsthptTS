package com.schoolmanagement.controller;

import com.schoolmanagement.dto.BookTransactionDTO;
import com.schoolmanagement.dto.LibraryBookDTO;
import com.schoolmanagement.entity.BookCategory;
import com.schoolmanagement.entity.LibraryBook;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.service.LibraryService;
import com.schoolmanagement.util.PaginationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/library")
@AllArgsConstructor
@Tag(name = "Library Management", description = "Library management endpoints")
public class LibraryController {

    private LibraryService libraryService;

    @PostMapping("/books")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(summary = "Add new book to library")
    public ResponseEntity<LibraryBookDTO> addBook(@Valid @RequestBody LibraryBook book) {
        LibraryBookDTO addedBook = libraryService.addBook(book);
        return new ResponseEntity<>(addedBook, HttpStatus.CREATED);
    }

    @PutMapping("/books/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(summary = "Update book details")
    public ResponseEntity<LibraryBookDTO> updateBook(@PathVariable Long id, @Valid @RequestBody LibraryBook bookDetails) {
        LibraryBookDTO updatedBook = libraryService.updateBook(id, bookDetails);
        return new ResponseEntity<>(updatedBook, HttpStatus.OK);
    }

    @GetMapping("/books/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'LIBRARIAN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get book by ID")
    public ResponseEntity<LibraryBookDTO> getBookById(@PathVariable Long id) {
        LibraryBookDTO book = libraryService.getBookById(id);
        return new ResponseEntity<>(book, HttpStatus.OK);
    }

    @GetMapping("/books")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'LIBRARIAN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get all books",
            description = "Optional page/size query params paginate the result (0-indexed page); omit both to get the full list. Total count is returned in the X-Total-Count header when paginated.")
    public ResponseEntity<List<LibraryBookDTO>> getAllBooks(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        Pageable pageable = PaginationUtil.toPageable(page, size);
        if (pageable == null) {
            return new ResponseEntity<>(libraryService.getAllBooks(), HttpStatus.OK);
        }
        Page<LibraryBookDTO> result = libraryService.getAllBooks(pageable);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(result.getTotalElements()))
                .body(result.getContent());
    }

    @GetMapping("/books/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'LIBRARIAN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Search books by title")
    public ResponseEntity<List<LibraryBookDTO>> searchBooks(@RequestParam String title) {
        List<LibraryBookDTO> books = libraryService.searchBooks(title);
        return new ResponseEntity<>(books, HttpStatus.OK);
    }

    @GetMapping("/books/category/{category}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'LIBRARIAN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get books by category")
    public ResponseEntity<List<LibraryBookDTO>> getBooksByCategory(@PathVariable BookCategory category) {
        List<LibraryBookDTO> books = libraryService.getBooksByCategory(category);
        return new ResponseEntity<>(books, HttpStatus.OK);
    }

    @GetMapping("/books/author/{author}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'LIBRARIAN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get books by author")
    public ResponseEntity<List<LibraryBookDTO>> getBooksByAuthor(@PathVariable String author) {
        List<LibraryBookDTO> books = libraryService.getBooksByAuthor(author);
        return new ResponseEntity<>(books, HttpStatus.OK);
    }

    @GetMapping("/books/available")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'LIBRARIAN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get available books")
    public ResponseEntity<List<LibraryBookDTO>> getAvailableBooks() {
        List<LibraryBookDTO> books = libraryService.getAvailableBooks();
        return new ResponseEntity<>(books, HttpStatus.OK);
    }

    @PostMapping("/books/{bookId}/borrow")
    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT')")
    @Operation(summary = "Borrow a book")
    public ResponseEntity<String> borrowBook(
            @PathVariable Long bookId,
            @RequestParam(defaultValue = "14") int borrowDays,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        libraryService.borrowBook(bookId, user, borrowDays);
        return new ResponseEntity<>("Book borrowed successfully", HttpStatus.OK);
    }

    @PostMapping("/books/{bookId}/return")
    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT')")
    @Operation(summary = "Return a borrowed book")
    public ResponseEntity<String> returnBook(@PathVariable Long bookId, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        libraryService.returnBook(bookId, user);
        return new ResponseEntity<>("Book returned successfully", HttpStatus.OK);
    }

    // "Mượn/trả hộ" (H.2.3 / G.4 mục 5) - a LIBRARIAN records a borrow or
    // return for a student at the desk. Self-service borrow/return above
    // reads the borrower from the JWT; these take an explicit studentId
    // instead. LIBRARIAN-facing (plus ADMIN), unlike the self-service pair
    // which is TEACHER/STUDENT only.
    @PostMapping("/books/{bookId}/lend")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(summary = "Lend a book to a student (recorded by a librarian, not self-service)")
    public ResponseEntity<String> lendBookToStudent(
            @PathVariable Long bookId,
            @RequestParam Long studentId,
            @RequestParam(defaultValue = "14") int borrowDays) {
        libraryService.lendBookToStudent(bookId, studentId, borrowDays);
        return new ResponseEntity<>("Book lent to student successfully", HttpStatus.OK);
    }

    @PostMapping("/books/{bookId}/return-for")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(summary = "Return a book on a student's behalf (recorded by a librarian)")
    public ResponseEntity<String> returnBookForStudent(
            @PathVariable Long bookId,
            @RequestParam Long studentId) {
        libraryService.returnBookForStudent(bookId, studentId);
        return new ResponseEntity<>("Book returned for student successfully", HttpStatus.OK);
    }

    @DeleteMapping("/books/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(summary = "Delete book from library")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        libraryService.deleteBook(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // Added alongside the frontend Library Management page (Tuần 4 Ngày 1) -
    // borrowBook/returnBook above wrote BookTransaction rows from the start,
    // but nothing ever exposed them, so a caller (frontend or otherwise) had
    // no way to know which books a user currently has borrowed.
    @GetMapping("/transactions/me")
    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT')")
    @Operation(summary = "Get the current user's own borrow/return history")
    public ResponseEntity<List<BookTransactionDTO>> getMyTransactions(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return new ResponseEntity<>(libraryService.getMyTransactions(user), HttpStatus.OK);
    }

    @GetMapping("/transactions")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'LIBRARIAN')")
    @Operation(summary = "Get all currently outstanding (not yet returned) borrows")
    public ResponseEntity<List<BookTransactionDTO>> getActiveBorrows() {
        return new ResponseEntity<>(libraryService.getActiveBorrows(), HttpStatus.OK);
    }
}

