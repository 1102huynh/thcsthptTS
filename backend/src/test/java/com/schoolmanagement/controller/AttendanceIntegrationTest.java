package com.schoolmanagement.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.schoolmanagement.entity.Role;
import com.schoolmanagement.entity.Student;
import com.schoolmanagement.entity.StudentStatus;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.repository.AttendanceRepository;
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

import java.time.LocalDate;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for POST /v1/attendance/class - added alongside a fix
 * for the same endpoint (Tuần 4 Ngày 2, Track Frontend): it never recorded
 * who marked attendance (markedBy always null), and re-marking the same
 * class+date just inserted a second batch of rows on top of the first (no
 * unique constraint on student+date), silently duplicating records and
 * skewing every %-based calculation that counts rows.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AttendanceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AttendanceRepository attendanceRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private User teacherUser;
    private Student studentA;
    private Student studentB;
    private static final String CLASS_NAME = "ITEST-CLS";
    private static final String SECTION = "X";
    private static final LocalDate DATE = LocalDate.of(2025, 3, 10);

    @BeforeEach
    void setUp() {
        teacherUser = userRepository.save(User.builder()
                .username("itest.att.teacher").email("itest.att.teacher@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Integration").lastName("Teacher").role(Role.TEACHER).enabled(true).build());

        User userA = userRepository.save(User.builder()
                .username("itest.att.studentA").email("itest.att.studentA@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Student").lastName("A").role(Role.STUDENT).enabled(true).build());
        studentA = studentRepository.save(Student.builder()
                .rollNumber("ITEST-ATT-A").admissionNumber("ITEST-ATT-ADM-A")
                .user(userA).className(CLASS_NAME).section(SECTION).status(StudentStatus.ACTIVE).build());

        User userB = userRepository.save(User.builder()
                .username("itest.att.studentB").email("itest.att.studentB@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Student").lastName("B").role(Role.STUDENT).enabled(true).build());
        studentB = studentRepository.save(Student.builder()
                .rollNumber("ITEST-ATT-B").admissionNumber("ITEST-ATT-ADM-B")
                .user(userB).className(CLASS_NAME).section(SECTION).status(StudentStatus.ACTIVE).build());
    }

    private RequestPostProcessor asUser(User user, String role) {
        return authentication(new UsernamePasswordAuthenticationToken(
                user, null, List.of(new SimpleGrantedAuthority("ROLE_" + role))));
    }

    @Test
    void markClassAttendance_setsStatusAndMarkedByForEveryStudent() throws Exception {
        mockMvc.perform(post("/v1/attendance/class")
                        .param("className", CLASS_NAME)
                        .param("section", SECTION)
                        .param("date", DATE.toString())
                        .param("presentStudentIds", studentA.getId().toString())
                        .with(asUser(teacherUser, "TEACHER")))
                .andExpect(status().isOk());

        var rows = attendanceRepository.findByStudentInAndAttendanceDate(List.of(studentA, studentB), DATE);
        assertEquals(2, rows.size());
        rows.forEach(r -> {
            var expectedStatus = r.getStudent().getId().equals(studentA.getId()) ? "PRESENT" : "ABSENT";
            assertEquals(expectedStatus, r.getStatus().name());
            assertEquals(teacherUser.getId(), r.getMarkedBy().getId());
        });
    }

    @Test
    void markClassAttendance_reSubmitting_replacesRowsInsteadOfDuplicating() throws Exception {
        // First pass: only A present.
        mockMvc.perform(post("/v1/attendance/class")
                        .param("className", CLASS_NAME).param("section", SECTION).param("date", DATE.toString())
                        .param("presentStudentIds", studentA.getId().toString())
                        .with(asUser(teacherUser, "TEACHER")))
                .andExpect(status().isOk());

        // Re-submit: both present now (e.g. a correction).
        mockMvc.perform(post("/v1/attendance/class")
                        .param("className", CLASS_NAME).param("section", SECTION).param("date", DATE.toString())
                        .param("presentStudentIds", studentA.getId().toString(), studentB.getId().toString())
                        .with(asUser(teacherUser, "TEACHER")))
                .andExpect(status().isOk());

        var rows = attendanceRepository.findByStudentInAndAttendanceDate(List.of(studentA, studentB), DATE);
        // Still exactly 2 rows (one per student), not 4 - the second
        // submit replaced the first instead of stacking on top of it.
        assertEquals(2, rows.size());
        rows.forEach(r -> assertEquals("PRESENT", r.getStatus().name()));
    }

    @Test
    void markClassAttendance_asStudent_returns403() throws Exception {
        mockMvc.perform(post("/v1/attendance/class")
                        .param("className", CLASS_NAME).param("section", SECTION).param("date", DATE.toString())
                        .param("presentStudentIds", studentA.getId().toString())
                        .with(asUser(studentA.getUser(), "STUDENT")))
                .andExpect(status().isForbidden());
    }
}
