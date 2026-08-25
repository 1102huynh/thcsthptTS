package com.schoolmanagement.controller;

import com.schoolmanagement.entity.Attendance;
import com.schoolmanagement.entity.AttendanceStatus;
import com.schoolmanagement.entity.EmploymentStatus;
import com.schoolmanagement.entity.Fee;
import com.schoolmanagement.entity.FeeStatus;
import com.schoolmanagement.entity.Grade;
import com.schoolmanagement.entity.ParentRelationship;
import com.schoolmanagement.entity.ParentStudentRelation;
import com.schoolmanagement.entity.Role;
import com.schoolmanagement.entity.Staff;
import com.schoolmanagement.entity.StaffPosition;
import com.schoolmanagement.entity.Student;
import com.schoolmanagement.entity.StudentStatus;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.repository.AttendanceRepository;
import com.schoolmanagement.repository.FeeRepository;
import com.schoolmanagement.repository.GradeRepository;
import com.schoolmanagement.repository.ParentStudentRelationRepository;
import com.schoolmanagement.repository.StaffRepository;
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
 * Integration test for the 3.6 PARENT/STUDENT ownership retrofit on the
 * legacy (Phase 1-2) /v1/grades, /v1/fees, /v1/attendance controllers —
 * real Spring context + local MySQL via the "test" profile,
 * {@literal @}Transactional rollback per test.
 *
 * <p>These also double as a regression test for a real bug found live while
 * building this retrofit: getStudentGrades/getStudentFees/getStudentAttendance
 * (and their siblings) used to return the raw JPA entity, whose lazy
 * student/teacher/markedBy associations blew up with
 * LazyInitializationException on serialization (open-in-view is off) for
 * EVERY role, not just the new PARENT one — fixed by returning
 * GradeDTO/FeeDTO/AttendanceDTO instead. A 200 with real body content below
 * (not a 500) is the regression check.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class LegacyStudentAccessIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private StaffRepository staffRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ParentStudentRelationRepository parentStudentRelationRepository;
    @Autowired
    private GradeRepository gradeRepository;
    @Autowired
    private FeeRepository feeRepository;
    @Autowired
    private AttendanceRepository attendanceRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private Student student;
    private User studentUser;
    private User parentUser;
    private User otherParentUser;
    private Staff teacher;

    @BeforeEach
    void setUp() {
        studentUser = userRepository.save(User.builder()
                .username("itest.lsa.student").email("itest.lsa.student@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Integration").lastName("Student").role(Role.STUDENT).enabled(true).build());
        student = studentRepository.save(Student.builder()
                .rollNumber("ITEST-LSA-ROLL").admissionNumber("ITEST-LSA-ADM")
                .user(studentUser).status(StudentStatus.ACTIVE).build());

        User teacherUser = userRepository.save(User.builder()
                .username("itest.lsa.teacher").email("itest.lsa.teacher@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Integration").lastName("Teacher").role(Role.TEACHER).enabled(true).build());
        teacher = staffRepository.save(Staff.builder()
                .employeeId("ITEST-LSA-EMP").user(teacherUser)
                .position(StaffPosition.TEACHER).status(EmploymentStatus.ACTIVE).build());

        parentUser = userRepository.save(User.builder()
                .username("itest.lsa.parent").email("itest.lsa.parent@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Integration").lastName("Parent").role(Role.PARENT).enabled(true).build());
        parentStudentRelationRepository.save(ParentStudentRelation.builder()
                .parent(parentUser).student(student)
                .relationship(ParentRelationship.CHA).isPrimaryContact(true).build());

        otherParentUser = userRepository.save(User.builder()
                .username("itest.lsa.other-parent").email("itest.lsa.other-parent@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Integration").lastName("OtherParent").role(Role.PARENT).enabled(true).build());

        gradeRepository.save(Grade.builder()
                .student(student).subject("ITEST Subject").examType("Midterm")
                .marksObtained(85.0).totalMarks(100.0).percentage(85.0).grade("A")
                .teacher(teacher).academicYear("2099-2100").build());

        feeRepository.save(Fee.builder()
                .student(student).academicYear("2099-2100").feeType("Tuition")
                .amount(1000.0).remainingAmount(1000.0).status(FeeStatus.PENDING)
                .dueDate(LocalDate.of(2100, 1, 1)).build());

        attendanceRepository.save(Attendance.builder()
                .student(student).attendanceDate(LocalDate.of(2099, 9, 2))
                .status(AttendanceStatus.PRESENT).markedBy(teacherUser).build());
    }

    private RequestPostProcessor asUser(User user, String role) {
        return authentication(new UsernamePasswordAuthenticationToken(
                user, null, List.of(new SimpleGrantedAuthority("ROLE_" + role))));
    }

    // ---- Grades ----

    @Test
    void grades_studentReadsOwn_returns200WithRealBody() throws Exception {
        mockMvc.perform(get("/v1/grades/student/{id}", student.getId())
                        .with(asUser(studentUser, "STUDENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].studentName").value("Integration Student"))
                .andExpect(jsonPath("$[0].percentage").value(85.0));
    }

    @Test
    void grades_parentReadsOwnChild_returns200() throws Exception {
        mockMvc.perform(get("/v1/grades/student/{id}", student.getId())
                        .with(asUser(parentUser, "PARENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].percentage").value(85.0));
    }

    @Test
    void grades_parentReadsNonChild_returns403() throws Exception {
        mockMvc.perform(get("/v1/grades/student/{id}", student.getId())
                        .with(asUser(otherParentUser, "PARENT")))
                .andExpect(status().isForbidden());
    }

    @Test
    void grades_nonexistentStudentId_returns404NotForbidden() throws Exception {
        mockMvc.perform(get("/v1/grades/student/{id}", 9_999_999L)
                        .with(asUser(parentUser, "PARENT")))
                .andExpect(status().isNotFound());
    }

    // ---- Fees ----

    @Test
    void fees_studentReadsOwn_returns200WithRealBody() throws Exception {
        mockMvc.perform(get("/v1/fees/student/{id}", student.getId())
                        .with(asUser(studentUser, "STUDENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].feeType").value("Tuition"))
                .andExpect(jsonPath("$[0].remainingAmount").value(1000.0));
    }

    @Test
    void fees_parentReadsNonChild_returns403() throws Exception {
        mockMvc.perform(get("/v1/fees/student/{id}", student.getId())
                        .with(asUser(otherParentUser, "PARENT")))
                .andExpect(status().isForbidden());
    }

    // ---- Attendance ----

    @Test
    void attendance_parentReadsOwnChild_returns200WithRealBody() throws Exception {
        mockMvc.perform(get("/v1/attendance/student/{id}", student.getId())
                        .with(asUser(parentUser, "PARENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PRESENT"))
                .andExpect(jsonPath("$[0].markedByName").exists());
    }

    @Test
    void attendance_studentReadsAnother_returns403() throws Exception {
        User otherStudentUser = userRepository.save(User.builder()
                .username("itest.lsa.student2").email("itest.lsa.student2@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Integration").lastName("StudentTwo").role(Role.STUDENT).enabled(true).build());
        studentRepository.save(Student.builder()
                .rollNumber("ITEST-LSA-ROLL-2").admissionNumber("ITEST-LSA-ADM-2")
                .user(otherStudentUser).status(StudentStatus.ACTIVE).build());

        mockMvc.perform(get("/v1/attendance/student/{id}", student.getId())
                        .with(asUser(otherStudentUser, "STUDENT")))
                .andExpect(status().isForbidden());
    }
}
