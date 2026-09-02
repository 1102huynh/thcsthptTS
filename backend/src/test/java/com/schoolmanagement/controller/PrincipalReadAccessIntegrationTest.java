package com.schoolmanagement.controller;

import com.schoolmanagement.entity.Role;
import com.schoolmanagement.entity.Student;
import com.schoolmanagement.entity.StudentStatus;
import com.schoolmanagement.entity.User;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Mức 2.1 (KE_HOACH_NANG_CAP_V4.md, Phần H.2.1 / G.4 mục 2): PRINCIPAL was
 * "mù dữ liệu học tập" — every GET on attendance/grades/conduct/fees/reports
 * 403'd, so navigation.js couldn't route a PRINCIPAL to any of those pages.
 * PRINCIPAL now has read access to those modules; writes stay with
 * TEACHER (học tập) / ACCOUNTANT (học phí).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PrincipalReadAccessIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private User principal;
    private Student student;

    @BeforeEach
    void setUp() {
        principal = userRepository.save(User.builder()
                .username("itest.pra.principal").email("itest.pra.principal@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Integration").lastName("Principal").role(Role.PRINCIPAL).enabled(true).build());

        User studentUser = userRepository.save(User.builder()
                .username("itest.pra.student").email("itest.pra.student@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Học").lastName("Sinh").role(Role.STUDENT).enabled(true).build());
        student = studentRepository.save(Student.builder()
                .rollNumber("ITEST-PRA-ROLL").admissionNumber("ITEST-PRA-ADM")
                .user(studentUser).status(StudentStatus.ACTIVE).build());
    }

    private RequestPostProcessor asPrincipal() {
        return authentication(new UsernamePasswordAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority("ROLE_PRINCIPAL"))));
    }

    // ---- reads that used to 403 for PRINCIPAL ----

    @Test
    void principal_readsAttendanceByDate() throws Exception {
        mockMvc.perform(get("/v1/attendance/date/{d}", "2026-09-01").with(asPrincipal()))
                .andExpect(status().isOk());
    }

    @Test
    void principal_readsAttendanceBetween() throws Exception {
        mockMvc.perform(get("/v1/attendance/between")
                        .param("startDate", "2026-09-01").param("endDate", "2026-09-30")
                        .with(asPrincipal()))
                .andExpect(status().isOk());
    }

    @Test
    void principal_readsStudentGradeRecords() throws Exception {
        mockMvc.perform(get("/v1/grade-records/student/{id}/semester/{sid}", student.getId(), 1L)
                        .with(asPrincipal()))
                .andExpect(status().isOk());
    }

    @Test
    void principal_readsStudentConduct() throws Exception {
        mockMvc.perform(get("/v1/conduct/student/{id}", student.getId()).with(asPrincipal()))
                .andExpect(status().isOk());
    }

    @Test
    void principal_readsFeesByYearAndStatus() throws Exception {
        mockMvc.perform(get("/v1/fees/year/{y}", "2025-2026").with(asPrincipal()))
                .andExpect(status().isOk());
        mockMvc.perform(get("/v1/fees/status/{s}", "PENDING").with(asPrincipal()))
                .andExpect(status().isOk());
        mockMvc.perform(get("/v1/fees/student/{id}", student.getId()).with(asPrincipal()))
                .andExpect(status().isOk());
    }

    @Test
    void principal_readsLibraryCatalogAndActiveBorrows() throws Exception {
        mockMvc.perform(get("/v1/library/books").with(asPrincipal())).andExpect(status().isOk());
        mockMvc.perform(get("/v1/library/transactions").with(asPrincipal())).andExpect(status().isOk());
    }

    // ---- writes stay denied for PRINCIPAL ----
    //
    // DELETE endpoints (no @RequestBody, so no chance a 400 validation error
    // pre-empts the 403) — @PreAuthorize runs before the service is reached,
    // so a non-existent id still 403s rather than 404ing.

    @Test
    void principal_cannotWriteAttendance() throws Exception {
        mockMvc.perform(delete("/v1/attendance/{id}", 999_999L).with(asPrincipal()))
                .andExpect(status().isForbidden());
    }

    @Test
    void principal_cannotWriteGradeRecords() throws Exception {
        mockMvc.perform(delete("/v1/grade-records/{id}", 999_999L).with(asPrincipal()))
                .andExpect(status().isForbidden());
    }

    @Test
    void principal_cannotWriteFees() throws Exception {
        mockMvc.perform(delete("/v1/fees/{id}", 999_999L).with(asPrincipal()))
                .andExpect(status().isForbidden());
    }
}
