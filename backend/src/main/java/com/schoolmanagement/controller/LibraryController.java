package com.schoolmanagement.controller;

import com.schoolmanagement.dto.LibraryBookDTO;
import com.schoolmanagement.entity.BookCategory;
import com.schoolmanagement.entity.LibraryBook;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.service.LibraryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
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
    public ResponseEntity<LibraryBookDTO> addBook(@RequestBody LibraryBook book) {
        LibraryBookDTO addedBook = libraryService.addBook(book);
        return new ResponseEntity<>(addedBook, HttpStatus.CREATED);
    }

    @PutMapping("/books/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(summary = "Update book details")
    public ResponseEntity<LibraryBookDTO> updateBook(@PathVariable Long id, @RequestBody LibraryBook bookDetails) {
        LibraryBookDTO updatedBook = libraryService.updateBook(id, bookDetails);
        return new ResponseEntity<>(updatedBook, HttpStatus.OK);
    }

    @GetMapping("/books/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get book by ID")
    public ResponseEntity<LibraryBookDTO> getBookById(@PathVariable Long id) {
        LibraryBookDTO book = libraryService.getBookById(id);
        return new ResponseEntity<>(book, HttpStatus.OK);
    }

    @GetMapping("/books")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get all books")
    public ResponseEntity<List<LibraryBookDTO>> getAllBooks() {
        List<LibraryBookDTO> books = libraryService.getAllBooks();
        return new ResponseEntity<>(books, HttpStatus.OK);
    }

    @GetMapping("/books/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Search books by title")
    public ResponseEntity<List<LibraryBookDTO>> searchBooks(@RequestParam String title) {
        List<LibraryBookDTO> books = libraryService.searchBooks(title);
        return new ResponseEntity<>(books, HttpStatus.OK);
    }

    @GetMapping("/books/category/{category}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get books by category")
    public ResponseEntity<List<LibraryBookDTO>> getBooksByCategory(@PathVariable BookCategory category) {
        List<LibraryBookDTO> books = libraryService.getBooksByCategory(category);
        return new ResponseEntity<>(books, HttpStatus.OK);
    }

    @GetMapping("/books/author/{author}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get books by author")
    public ResponseEntity<List<LibraryBookDTO>> getBooksByAuthor(@PathVariable String author) {
        List<LibraryBookDTO> books = libraryService.getBooksByAuthor(author);
        return new ResponseEntity<>(books, HttpStatus.OK);
    }

    @GetMapping("/books/available")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'TEACHER', 'STUDENT')")
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

    @DeleteMapping("/books/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    @Operation(summary = "Delete book from library")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        libraryService.deleteBook(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

