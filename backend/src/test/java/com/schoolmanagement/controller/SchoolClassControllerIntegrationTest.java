package com.schoolmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.schoolmanagement.entity.EmploymentStatus;
import com.schoolmanagement.entity.Role;
import com.schoolmanagement.entity.SchoolClass;
import com.schoolmanagement.entity.Staff;
import com.schoolmanagement.entity.StaffPosition;
import com.schoolmanagement.entity.Student;
import com.schoolmanagement.entity.StudentStatus;
import com.schoolmanagement.entity.User;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for /v1/classes — runs the real Spring context (MockMvc,
 * method security, JPA) against local MySQL via the "test" profile. Every
 * test method rolls back at the end (@Transactional), so it never depends on
 * — or pollutes — TEST_DATA_CORRECTED.sql's seeded rows.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SchoolClassControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private SchoolClassRepository schoolClassRepository;
    @Autowired
    private StaffRepository staffRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private Staff teacher;

    @BeforeEach
    void setUp() {
        User teacherUser = userRepository.save(User.builder()
                .username("itest.teacher")
                .email("itest.teacher@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Integration")
                .lastName("Teacher")
                .role(Role.TEACHER)
                .enabled(true)
                .build());

        teacher = staffRepository.save(Staff.builder()
                .employeeId("ITEST-EMP-1")
                .user(teacherUser)
                .position(StaffPosition.TEACHER)
                .status(EmploymentStatus.ACTIVE)
                .build());
    }

    private RequestPostProcessor asUser(User user, String role) {
        return authentication(new UsernamePasswordAuthenticationToken(
                user, null, List.of(new SimpleGrantedAuthority("ROLE_" + role))));
    }

    private SchoolClass newClassPayload(String className, String section) {
        return SchoolClass.builder()
                .className(className)
                .section(section)
                .academicYear("2099-2100") // far-future year, never collides with seed data
                .capacity(30)
                .roomNumber("P.999")
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createClass_persistsAndReturnsIt() throws Exception {
        mockMvc.perform(post("/v1/classes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newClassPayload("ITEST-10", "A"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.className").value("ITEST-10"))
                .andExpect(jsonPath("$.section").value("A"))
                .andExpect(jsonPath("$.academicYear").value("2099-2100"))
                .andExpect(jsonPath("$.studentCount").value(0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createClass_duplicateNameSectionYear_returns409() throws Exception {
        schoolClassRepository.save(newClassPayload("ITEST-11", "A"));

        mockMvc.perform(post("/v1/classes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newClassPayload("ITEST-11", "A"))))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createClass_missingRequiredFields_returns400() throws Exception {
        mockMvc.perform(post("/v1/classes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void createClass_asTeacher_returns403() throws Exception {
        mockMvc.perform(post("/v1/classes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newClassPayload("ITEST-12", "A"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateClass_changesFields() throws Exception {
        SchoolClass saved = schoolClassRepository.save(newClassPayload("ITEST-13", "A"));

        SchoolClass update = newClassPayload("ITEST-13", "B");
        update.setCapacity(45);
        update.setRoomNumber("P.101");

        mockMvc.perform(put("/v1/classes/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.section").value("B"))
                .andExpect(jsonPath("$.capacity").value(45))
                .andExpect(jsonPath("$.roomNumber").value("P.101"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void assignClassTeacher_setsTeacherIdAndName() throws Exception {
        SchoolClass saved = schoolClassRepository.save(newClassPayload("ITEST-14", "A"));

        mockMvc.perform(put("/v1/classes/{id}/teacher/{staffId}", saved.getId(), teacher.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.classTeacherId").value(teacher.getId()))
                .andExpect(jsonPath("$.classTeacherName").value("Integration Teacher"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void assignClassTeacher_unknownStaffId_returns404() throws Exception {
        SchoolClass saved = schoolClassRepository.save(newClassPayload("ITEST-15", "A"));

        mockMvc.perform(put("/v1/classes/{id}/teacher/{staffId}", saved.getId(), 999_999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteClass_withStudentsAssigned_returns409() throws Exception {
        SchoolClass saved = schoolClassRepository.save(newClassPayload("ITEST-16", "A"));

        User studentUser = userRepository.save(User.builder()
                .username("itest.student")
                .email("itest.student@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Integration")
                .lastName("Student")
                .role(Role.STUDENT)
                .enabled(true)
                .build());

        studentRepository.save(Student.builder()
                .rollNumber("ITEST-ROLL-1")
                .admissionNumber("ITEST-ADM-1")
                .user(studentUser)
                .className(saved.getClassName())
                .section(saved.getSection())
                .status(StudentStatus.ACTIVE)
                .dateOfAdmission(LocalDate.now())
                .build());

        mockMvc.perform(delete("/v1/classes/{id}", saved.getId()))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteClass_empty_returns204() throws Exception {
        SchoolClass saved = schoolClassRepository.save(newClassPayload("ITEST-17", "A"));

        mockMvc.perform(delete("/v1/classes/{id}", saved.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void getAllClasses_asTeacher_isAllowedToRead() throws Exception {
        schoolClassRepository.save(newClassPayload("ITEST-18", "A"));

        mockMvc.perform(get("/v1/classes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(greaterThanOrEqualTo(1))));
    }

    // H.3.1 - GET /{id}/students is the roster view used by ClassManagement;
    // a TEACHER may only fetch it for a class they are GVCN (homeroom
    // teacher) of.
    @Test
    void getStudentsInClass_asHomeroomTeacher_returns200() throws Exception {
        SchoolClass saved = schoolClassRepository.save(SchoolClass.builder()
                .className("ITEST-19").section("A").academicYear("2099-2100")
                .classTeacher(teacher).build());
        User studentUser = userRepository.save(User.builder()
                .username("itest.cls19.student").email("itest.cls19.student@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Integration").lastName("Student").role(Role.STUDENT).enabled(true).build());
        studentRepository.save(Student.builder()
                .rollNumber("ITEST-CLS19-ROLL").admissionNumber("ITEST-CLS19-ADM")
                .user(studentUser).className(saved.getClassName()).section(saved.getSection())
                .status(StudentStatus.ACTIVE).dateOfAdmission(LocalDate.now()).build());

        mockMvc.perform(get("/v1/classes/{id}/students", saved.getId())
                        .with(asUser(teacher.getUser(), "TEACHER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rollNumber").value("ITEST-CLS19-ROLL"));
    }

    @Test
    void getStudentsInClass_asNonHomeroomTeacher_returns403() throws Exception {
        SchoolClass saved = schoolClassRepository.save(newClassPayload("ITEST-20", "A"));
        // `saved` has no classTeacher set - `teacher` is GVCN of nothing.

        mockMvc.perform(get("/v1/classes/{id}/students", saved.getId())
                        .with(asUser(teacher.getUser(), "TEACHER")))
                .andExpect(status().isForbidden());
    }
}
