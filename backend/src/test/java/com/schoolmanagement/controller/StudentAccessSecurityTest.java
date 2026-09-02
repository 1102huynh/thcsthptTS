package com.schoolmanagement.controller;

import com.schoolmanagement.entity.EmploymentStatus;
import com.schoolmanagement.entity.ParentRelationship;
import com.schoolmanagement.entity.ParentStudentRelation;
import com.schoolmanagement.entity.Role;
import com.schoolmanagement.entity.Staff;
import com.schoolmanagement.entity.StaffPosition;
import com.schoolmanagement.entity.Student;
import com.schoolmanagement.entity.StudentStatus;
import com.schoolmanagement.entity.User;
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

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Regression tests for two real IDOR/PII-leak bugs found in a security
 * review (KE_HOACH_NANG_CAP_V4.md, Phần G.4 mục 1 & 3), fixed the same day:
 *
 * <ol>
 *   <li>{@code GET /v1/students/{id}} and {@code /roll/{rollNumber}} allowed
 *       STUDENT but never called {@link com.schoolmanagement.security.StudentAccessGuard}
 *       - unlike every other per-student endpoint (grades/fees/attendance/
 *       conduct/promotions), a STUDENT could read any other student's full
 *       profile just by guessing an id or roll number. PARENT was also
 *       missing from these two endpoints entirely (403 even for their own
 *       child), inconsistent with every other per-student endpoint.</li>
 *   <li>{@code GET /v1/staff}, {@code /{id}}, {@code /employee/{employeeId}}
 *       allowed STUDENT - {@code StaffDTO} carries salary and home-address/
 *       emergency-contact PII with no redaction, so any student could browse
 *       the entire staff directory including every teacher's salary.</li>
 * </ol>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class StudentAccessSecurityTest {

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
    private PasswordEncoder passwordEncoder;

    private Student student;
    private User studentUser;
    private User otherStudentUser;
    private User parentUser;
    private User otherParentUser;
    private User teacherUser;

    @BeforeEach
    void setUp() {
        studentUser = userRepository.save(User.builder()
                .username("itest.sas.student").email("itest.sas.student@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Integration").lastName("Student").role(Role.STUDENT).enabled(true).build());
        student = studentRepository.save(Student.builder()
                .rollNumber("ITEST-SAS-ROLL").admissionNumber("ITEST-SAS-ADM")
                .user(studentUser).status(StudentStatus.ACTIVE).build());

        otherStudentUser = userRepository.save(User.builder()
                .username("itest.sas.student2").email("itest.sas.student2@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Integration").lastName("StudentTwo").role(Role.STUDENT).enabled(true).build());
        studentRepository.save(Student.builder()
                .rollNumber("ITEST-SAS-ROLL-2").admissionNumber("ITEST-SAS-ADM-2")
                .user(otherStudentUser).status(StudentStatus.ACTIVE).build());

        parentUser = userRepository.save(User.builder()
                .username("itest.sas.parent").email("itest.sas.parent@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Integration").lastName("Parent").role(Role.PARENT).enabled(true).build());
        parentStudentRelationRepository.save(ParentStudentRelation.builder()
                .parent(parentUser).student(student)
                .relationship(ParentRelationship.CHA).isPrimaryContact(true).build());

        otherParentUser = userRepository.save(User.builder()
                .username("itest.sas.other-parent").email("itest.sas.other-parent@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Integration").lastName("OtherParent").role(Role.PARENT).enabled(true).build());

        teacherUser = userRepository.save(User.builder()
                .username("itest.sas.teacher").email("itest.sas.teacher@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Integration").lastName("Teacher").role(Role.TEACHER).enabled(true).build());
        staffRepository.save(Staff.builder()
                .employeeId("ITEST-SAS-EMP").user(teacherUser)
                .position(StaffPosition.TEACHER).status(EmploymentStatus.ACTIVE).build());
    }

    private RequestPostProcessor asUser(User user, String role) {
        return authentication(new UsernamePasswordAuthenticationToken(
                user, null, List.of(new SimpleGrantedAuthority("ROLE_" + role))));
    }

    // ---- GET /v1/students/me (C3 self-service portal) ----

    @Test
    void student_readsOwnRecordViaMe_returns200() throws Exception {
        mockMvc.perform(get("/v1/students/me").with(asUser(studentUser, "STUDENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(student.getId()))
                .andExpect(jsonPath("$.rollNumber").value("ITEST-SAS-ROLL"));
    }

    @Test
    void nonStudent_callsStudentsMe_returns403() throws Exception {
        mockMvc.perform(get("/v1/students/me").with(asUser(teacherUser, "TEACHER")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/v1/students/me").with(asUser(parentUser, "PARENT")))
                .andExpect(status().isForbidden());
    }

    // ---- GET /v1/students/{id} and /roll/{rollNumber} ----

    @Test
    void student_readsOwnRecordById_returns200() throws Exception {
        mockMvc.perform(get("/v1/students/{id}", student.getId())
                        .with(asUser(studentUser, "STUDENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rollNumber").value("ITEST-SAS-ROLL"));
    }

    @Test
    void student_readsAnotherStudentById_returns403() throws Exception {
        mockMvc.perform(get("/v1/students/{id}", student.getId())
                        .with(asUser(otherStudentUser, "STUDENT")))
                .andExpect(status().isForbidden());
    }

    @Test
    void student_readsAnotherStudentByRollNumber_returns403() throws Exception {
        mockMvc.perform(get("/v1/students/roll/{rollNumber}", student.getRollNumber())
                        .with(asUser(otherStudentUser, "STUDENT")))
                .andExpect(status().isForbidden());
    }

    @Test
    void parent_readsOwnChildById_returns200() throws Exception {
        mockMvc.perform(get("/v1/students/{id}", student.getId())
                        .with(asUser(parentUser, "PARENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rollNumber").value("ITEST-SAS-ROLL"));
    }

    @Test
    void parent_readsNonChildById_returns403() throws Exception {
        mockMvc.perform(get("/v1/students/{id}", student.getId())
                        .with(asUser(otherParentUser, "PARENT")))
                .andExpect(status().isForbidden());
    }

    @Test
    void teacher_readsAnyStudentById_returns200() throws Exception {
        mockMvc.perform(get("/v1/students/{id}", student.getId())
                        .with(asUser(teacherUser, "TEACHER")))
                .andExpect(status().isOk());
    }

    @Test
    void accountant_readsStudentDirectoryAndById_returns200() throws Exception {
        // Mức 2.2: an accountant needs the student list to record whose fee
        // it is (the create-fee form's student picker calls GET /v1/students).
        User accountantUser = userRepository.save(User.builder()
                .username("itest.sas.accountant").email("itest.sas.accountant@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Integration").lastName("Accountant").role(Role.ACCOUNTANT).enabled(true).build());

        mockMvc.perform(get("/v1/students").with(asUser(accountantUser, "ACCOUNTANT")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/v1/students/{id}", student.getId()).with(asUser(accountantUser, "ACCOUNTANT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rollNumber").value("ITEST-SAS-ROLL"));
    }

    // ---- GET /v1/staff* - STUDENT must no longer see the directory ----

    @Test
    void student_readsStaffDirectory_returns403() throws Exception {
        mockMvc.perform(get("/v1/staff").with(asUser(studentUser, "STUDENT")))
                .andExpect(status().isForbidden());
    }

    @Test
    void student_readsStaffById_returns403() throws Exception {
        mockMvc.perform(get("/v1/staff/{id}", 1L).with(asUser(studentUser, "STUDENT")))
                .andExpect(status().isForbidden());
    }

    @Test
    void teacher_readsStaffDirectory_stillReturns200() throws Exception {
        mockMvc.perform(get("/v1/staff").with(asUser(teacherUser, "TEACHER")))
                .andExpect(status().isOk());
    }
}
