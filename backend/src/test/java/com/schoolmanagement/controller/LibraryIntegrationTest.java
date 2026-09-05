package com.schoolmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.schoolmanagement.entity.BookCategory;
import com.schoolmanagement.entity.BookStatus;
import com.schoolmanagement.entity.LibraryBook;
import com.schoolmanagement.entity.Role;
import com.schoolmanagement.entity.Student;
import com.schoolmanagement.entity.StudentStatus;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.repository.LibraryBookRepository;
import com.schoolmanagement.repository.StudentRepository;
import com.schoolmanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for /v1/library/transactions{,/me} - added alongside
 * those endpoints (Tuần 4 Ngày 1, Track Frontend): borrowBook/returnBook
 * always wrote BookTransaction rows, but nothing ever read them back, so
 * this is the first coverage for that data actually being retrievable.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class LibraryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private LibraryBookRepository bookRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private User adminUser;
    private User studentUser;
    private User librarianUser;
    private Student student;
    private LibraryBook book;

    @BeforeEach
    void setUp() {
        adminUser = userRepository.save(User.builder()
                .username("itest.lib.admin").email("itest.lib.admin@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Integration").lastName("Admin").role(Role.ADMIN).enabled(true).build());
        studentUser = userRepository.save(User.builder()
                .username("itest.lib.student").email("itest.lib.student@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Integration").lastName("Student").role(Role.STUDENT).enabled(true).build());
        librarianUser = userRepository.save(User.builder()
                .username("itest.lib.librarian").email("itest.lib.librarian@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Integration").lastName("Librarian").role(Role.LIBRARIAN).enabled(true).build());
        student = studentRepository.save(Student.builder()
                .rollNumber("ITEST-LIB-ROLL").admissionNumber("ITEST-LIB-ADM")
                .user(studentUser).status(StudentStatus.ACTIVE).build());
        book = bookRepository.save(LibraryBook.builder()
                .isbn("ITEST-ISBN-1").title("ITEST Book").author("ITEST Author")
                .category(BookCategory.FICTION).totalCopies(2).availableCopies(2)
                .status(BookStatus.AVAILABLE).build());
    }

    private RequestPostProcessor asUser(User user, String role) {
        return authentication(new UsernamePasswordAuthenticationToken(
                user, null, List.of(new SimpleGrantedAuthority("ROLE_" + role))));
    }

    @Test
    void borrowThenReturn_showsUpInMyTransactionsAndActiveBorrows() throws Exception {
        mockMvc.perform(post("/v1/library/books/{id}/borrow", book.getId())
                        .with(asUser(studentUser, "STUDENT")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/v1/library/transactions/me").with(asUser(studentUser, "STUDENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookTitle").value("ITEST Book"))
                .andExpect(jsonPath("$[0].transactionType").value("BORROW"))
                .andExpect(jsonPath("$[0].returnDate").doesNotExist());

        mockMvc.perform(get("/v1/library/transactions").with(asUser(adminUser, "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.bookId == " + book.getId() + " && @.userId == " + studentUser.getId() + ")]").exists());

        mockMvc.perform(post("/v1/library/books/{id}/return", book.getId())
                        .with(asUser(studentUser, "STUDENT")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/v1/library/transactions/me").with(asUser(studentUser, "STUDENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].returnDate").exists());

        mockMvc.perform(get("/v1/library/transactions").with(asUser(adminUser, "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.bookId == " + book.getId() + ")]").doesNotExist());
    }

    @Test
    void getActiveBorrows_asStudent_returns403() throws Exception {
        mockMvc.perform(get("/v1/library/transactions").with(asUser(studentUser, "STUDENT")))
                .andExpect(status().isForbidden());
    }

    @Test
    void borrowBook_asAdmin_returns403() throws Exception {
        mockMvc.perform(post("/v1/library/books/{id}/borrow", book.getId())
                        .with(asUser(adminUser, "ADMIN")))
                .andExpect(status().isForbidden());
    }

    // ---- "mượn/trả hộ" (H.2.3) ----

    @Test
    void librarian_lendsAndReturnsForStudent() throws Exception {
        mockMvc.perform(post("/v1/library/books/{id}/lend", book.getId())
                        .param("studentId", String.valueOf(student.getId()))
                        .with(asUser(librarianUser, "LIBRARIAN")))
                .andExpect(status().isOk());

        // The borrow lands on the student's own account, visible in their history.
        // (Not "$[?(@.returnDate == null)]": the DTO omits null fields from
        // JSON entirely rather than serializing them as `null`, so a JsonPath
        // filter comparing against == null never matches - same reasoning as
        // the doesNotExist() check right below, for the same field.)
        mockMvc.perform(get("/v1/library/transactions/me").with(asUser(studentUser, "STUDENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookId").value(book.getId()))
                .andExpect(jsonPath("$[0].returnDate").doesNotExist());

        mockMvc.perform(post("/v1/library/books/{id}/return-for", book.getId())
                        .param("studentId", String.valueOf(student.getId()))
                        .with(asUser(librarianUser, "LIBRARIAN")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/v1/library/transactions").with(asUser(adminUser, "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.bookId == " + book.getId() + ")]").doesNotExist());
    }

    @Test
    void lendOnBehalf_asStudent_returns403() throws Exception {
        mockMvc.perform(post("/v1/library/books/{id}/lend", book.getId())
                        .param("studentId", String.valueOf(student.getId()))
                        .with(asUser(studentUser, "STUDENT")))
                .andExpect(status().isForbidden());
    }
}
