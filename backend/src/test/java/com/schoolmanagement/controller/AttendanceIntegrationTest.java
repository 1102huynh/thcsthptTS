package com.schoolmanagement.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.schoolmanagement.entity.Attendance;
import com.schoolmanagement.entity.AttendanceStatus;
import com.schoolmanagement.entity.EmploymentStatus;
import com.schoolmanagement.entity.Role;
import com.schoolmanagement.entity.SchoolClass;
import com.schoolmanagement.entity.Staff;
import com.schoolmanagement.entity.StaffPosition;
import com.schoolmanagement.entity.Student;
import com.schoolmanagement.entity.StudentStatus;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.repository.AttendanceRepository;
import com.schoolmanagement.repository.SchoolClassRepository;
import com.schoolmanagement.repository.StaffRepository;
import com.schoolmanagement.repository.StudentRepository;
import com.schoolmanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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
    private ObjectMapper objectMapper;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private StaffRepository staffRepository;
    @Autowired
    private SchoolClassRepository schoolClassRepository;
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
        // teacherUser is made GVCN of CLASS_NAME/SECTION below - H.3.1 scopes
        // every write here (mark/update/delete) to the caller's homeroom class.
        Staff teacherStaff = staffRepository.save(Staff.builder()
                .employeeId("ITEST-ATT-EMP").user(teacherUser)
                .position(StaffPosition.TEACHER).status(EmploymentStatus.ACTIVE).build());
        schoolClassRepository.save(SchoolClass.builder()
                .className(CLASS_NAME).section(SECTION).academicYear("2099-2100")
                .classTeacher(teacherStaff).build());

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

    // ---- H.3.1 - a TEACHER may only mark/update/delete attendance for a
    // student in a class they are GVCN (homeroom teacher) of ----

    private User nonHomeroomTeacher() {
        return userRepository.save(User.builder()
                .username("itest.att.other-teacher").email("itest.att.other-teacher@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Integration").lastName("OtherTeacher").role(Role.TEACHER).enabled(true).build());
        // No Staff profile at all - resolveOwnStaff throws for this user, same
        // 403 outcome as a TEACHER with a Staff profile but no homeroom class.
    }

    @Test
    void markAttendance_asHomeroomTeacher_returns201() throws Exception {
        Attendance attendance = Attendance.builder()
                .student(Student.builder().id(studentA.getId()).build())
                .attendanceDate(DATE).status(AttendanceStatus.PRESENT).build();

        mockMvc.perform(post("/v1/attendance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(attendance))
                        .with(asUser(teacherUser, "TEACHER")))
                .andExpect(status().isCreated());
    }

    @Test
    void markAttendance_asNonHomeroomTeacher_returns403() throws Exception {
        Attendance attendance = Attendance.builder()
                .student(Student.builder().id(studentA.getId()).build())
                .attendanceDate(DATE).status(AttendanceStatus.PRESENT).build();

        mockMvc.perform(post("/v1/attendance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(attendance))
                        .with(asUser(nonHomeroomTeacher(), "TEACHER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateAttendance_asNonHomeroomTeacher_returns403() throws Exception {
        Attendance existing = attendanceRepository.save(Attendance.builder()
                .student(studentA).attendanceDate(DATE).status(AttendanceStatus.PRESENT).build());
        Attendance update = Attendance.builder()
                .student(Student.builder().id(studentA.getId()).build())
                .attendanceDate(DATE).status(AttendanceStatus.ABSENT).remarks("sua").build();

        mockMvc.perform(put("/v1/attendance/{id}", existing.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update))
                        .with(asUser(nonHomeroomTeacher(), "TEACHER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteAttendance_asHomeroomTeacher_returns204() throws Exception {
        Attendance existing = attendanceRepository.save(Attendance.builder()
                .student(studentA).attendanceDate(DATE).status(AttendanceStatus.PRESENT).build());

        mockMvc.perform(delete("/v1/attendance/{id}", existing.getId())
                        .with(asUser(teacherUser, "TEACHER")))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteAttendance_asNonHomeroomTeacher_returns403() throws Exception {
        Attendance existing = attendanceRepository.save(Attendance.builder()
                .student(studentA).attendanceDate(DATE).status(AttendanceStatus.PRESENT).build());

        mockMvc.perform(delete("/v1/attendance/{id}", existing.getId())
                        .with(asUser(nonHomeroomTeacher(), "TEACHER")))
                .andExpect(status().isForbidden());
    }
}
