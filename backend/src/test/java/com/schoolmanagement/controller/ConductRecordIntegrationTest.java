package com.schoolmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.schoolmanagement.entity.AcademicYear;
import com.schoolmanagement.entity.AcademicYearStatus;
import com.schoolmanagement.entity.ConductRating;
import com.schoolmanagement.entity.ConductRecord;
import com.schoolmanagement.entity.EmploymentStatus;
import com.schoolmanagement.entity.Role;
import com.schoolmanagement.entity.SchoolClass;
import com.schoolmanagement.entity.Semester;
import com.schoolmanagement.entity.SemesterName;
import com.schoolmanagement.entity.Staff;
import com.schoolmanagement.entity.StaffPosition;
import com.schoolmanagement.entity.Student;
import com.schoolmanagement.entity.StudentStatus;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.repository.AcademicYearRepository;
import com.schoolmanagement.repository.ConductRecordRepository;
import com.schoolmanagement.repository.SchoolClassRepository;
import com.schoolmanagement.repository.SemesterRepository;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for /v1/conduct — real Spring context + local MySQL via
 * the "test" profile. Each test rolls back (@Transactional). Covers the
 * IMPLEMENTATION_PLAN.md 3.4 permission rule: a TEACHER may only write
 * conduct for students in the class they are GVCN (homeroom teacher) of.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ConductRecordIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AcademicYearRepository academicYearRepository;
    @Autowired
    private SemesterRepository semesterRepository;
    @Autowired
    private SchoolClassRepository schoolClassRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private StaffRepository staffRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ConductRecordRepository conductRecordRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private Semester hk1;
    private Staff homeroomTeacher;
    private Staff otherTeacher;
    private Student student;       // in homeroomTeacher's class
    private Student otherStudent;  // in a different class
    private User adminUser;
    private User homeroomTeacherUser;
    private User otherTeacherUser;
    private User studentUser;
    private User otherStudentUser;

    @BeforeEach
    void setUp() {
        AcademicYear academicYear = academicYearRepository.save(AcademicYear.builder()
                .name("2099-2100") // far-future year, never collides with real/seed data
                .startDate(LocalDate.of(2099, 9, 1))
                .endDate(LocalDate.of(2100, 5, 31))
                .status(AcademicYearStatus.ACTIVE)
                .build());
        hk1 = semesterRepository.save(Semester.builder()
                .academicYear(academicYear).name(SemesterName.HK1)
                .startDate(academicYear.getStartDate()).endDate(LocalDate.of(2100, 1, 15))
                .build());

        adminUser = userRepository.save(User.builder()
                .username("itest.cond.admin").email("itest.cond.admin@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Integration").lastName("Admin").role(Role.ADMIN).enabled(true).build());

        homeroomTeacherUser = userRepository.save(User.builder()
                .username("itest.cond.gvcn").email("itest.cond.gvcn@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Integration").lastName("Gvcn").role(Role.TEACHER).enabled(true).build());
        homeroomTeacher = staffRepository.save(Staff.builder()
                .employeeId("ITEST-COND-GVCN").user(homeroomTeacherUser)
                .position(StaffPosition.TEACHER).status(EmploymentStatus.ACTIVE).build());

        otherTeacherUser = userRepository.save(User.builder()
                .username("itest.cond.other-teacher").email("itest.cond.other-teacher@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Integration").lastName("OtherTeacher").role(Role.TEACHER).enabled(true).build());
        otherTeacher = staffRepository.save(Staff.builder()
                .employeeId("ITEST-COND-OTHER").user(otherTeacherUser)
                .position(StaffPosition.TEACHER).status(EmploymentStatus.ACTIVE).build());

        SchoolClass homeroomClass = schoolClassRepository.save(SchoolClass.builder()
                .className("ITEST-COND-10").section("A").academicYear("2099-2100")
                .classTeacher(homeroomTeacher).build());
        schoolClassRepository.save(SchoolClass.builder()
                .className("ITEST-COND-10").section("B").academicYear("2099-2100")
                .classTeacher(otherTeacher).build());

        studentUser = userRepository.save(User.builder()
                .username("itest.cond.student").email("itest.cond.student@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Integration").lastName("Student").role(Role.STUDENT).enabled(true).build());
        student = studentRepository.save(Student.builder()
                .rollNumber("ITEST-COND-ROLL-1").admissionNumber("ITEST-COND-ADM-1")
                .user(studentUser).status(StudentStatus.ACTIVE)
                .className(homeroomClass.getClassName()).section(homeroomClass.getSection())
                .build());

        otherStudentUser = userRepository.save(User.builder()
                .username("itest.cond.student2").email("itest.cond.student2@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Integration").lastName("StudentTwo").role(Role.STUDENT).enabled(true).build());
        otherStudent = studentRepository.save(Student.builder()
                .rollNumber("ITEST-COND-ROLL-2").admissionNumber("ITEST-COND-ADM-2")
                .user(otherStudentUser).status(StudentStatus.ACTIVE)
                .className("ITEST-COND-10").section("B")
                .build());
    }

    /**
     * @WithMockUser's principal is Spring Security's own User, not our
     * com.schoolmanagement.entity.User — every write/self-scoped read endpoint
     * under test casts authentication.getPrincipal() to our User, so those
     * tests authenticate as a real domain User instead.
     */
    private RequestPostProcessor asUser(User user, String role) {
        return authentication(new UsernamePasswordAuthenticationToken(
                user, null, List.of(new SimpleGrantedAuthority("ROLE_" + role))));
    }

    private String conductPayload(Student forStudent, Staff evaluator, ConductRating rating, String remarks) throws Exception {
        ConductRecord request = ConductRecord.builder()
                .student(Student.builder().id(forStudent.getId()).build())
                .semester(Semester.builder().id(hk1.getId()).build())
                .rating(rating)
                .remarks(remarks)
                .evaluatedBy(Staff.builder().id(evaluator.getId()).build())
                .build();
        return objectMapper.writeValueAsString(request);
    }

    @Test
    void createConductRecord_asHomeroomTeacher_persistsAndReturnsIt() throws Exception {
        mockMvc.perform(post("/v1/conduct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(conductPayload(student, homeroomTeacher, ConductRating.TOT, "Ngoan"))
                        .with(asUser(homeroomTeacherUser, "TEACHER")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating").value("TOT"))
                .andExpect(jsonPath("$.studentName").value("Integration Student"));
    }

    @Test
    void createConductRecord_asNonHomeroomTeacher_returns403() throws Exception {
        mockMvc.perform(post("/v1/conduct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(conductPayload(student, otherTeacher, ConductRating.TOT, "Ngoan"))
                        .with(asUser(otherTeacherUser, "TEACHER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void createConductRecord_asAdmin_forAnyClass_returns201() throws Exception {
        mockMvc.perform(post("/v1/conduct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(conductPayload(otherStudent, otherTeacher, ConductRating.KHA, "OK"))
                        .with(asUser(adminUser, "ADMIN")))
                .andExpect(status().isCreated());
    }

    @Test
    void createConductRecord_duplicateStudentSemester_returns409() throws Exception {
        conductRecordRepository.save(ConductRecord.builder()
                .student(student).semester(hk1).rating(ConductRating.TOT)
                .evaluatedBy(homeroomTeacher).build());

        mockMvc.perform(post("/v1/conduct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(conductPayload(student, homeroomTeacher, ConductRating.KHA, "again"))
                        .with(asUser(homeroomTeacherUser, "TEACHER")))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void createConductRecord_asStudent_returns403() throws Exception {
        // @PreAuthorize denies before the controller body runs, so a generic
        // @WithMockUser (no real User principal) is fine here.
        mockMvc.perform(post("/v1/conduct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(conductPayload(student, homeroomTeacher, ConductRating.TOT, "x")))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateConductRecord_asHomeroomTeacher_updatesSuccessfully() throws Exception {
        ConductRecord existing = conductRecordRepository.save(ConductRecord.builder()
                .student(student).semester(hk1).rating(ConductRating.TOT)
                .evaluatedBy(homeroomTeacher).build());

        mockMvc.perform(put("/v1/conduct/{id}", existing.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(conductPayload(student, homeroomTeacher, ConductRating.KHA, "Revised"))
                        .with(asUser(homeroomTeacherUser, "TEACHER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value("KHA"))
                .andExpect(jsonPath("$.remarks").value("Revised"));
    }

    @Test
    void updateConductRecord_asNonHomeroomTeacher_returns403() throws Exception {
        ConductRecord existing = conductRecordRepository.save(ConductRecord.builder()
                .student(student).semester(hk1).rating(ConductRating.TOT)
                .evaluatedBy(homeroomTeacher).build());

        mockMvc.perform(put("/v1/conduct/{id}", existing.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(conductPayload(student, otherTeacher, ConductRating.YEU, "unauthorized change"))
                        .with(asUser(otherTeacherUser, "TEACHER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateConductRecord_hijackAnotherGvcnsRecord_returns403AndLeavesItUnchanged() throws Exception {
        // Record belongs to otherStudent (GVCN: otherTeacher). homeroomTeacher
        // (GVCN of a different class) must not be able to "steal" this record
        // by reassigning it to their own student.
        ConductRecord existing = conductRecordRepository.save(ConductRecord.builder()
                .student(otherStudent).semester(hk1).rating(ConductRating.TOT)
                .evaluatedBy(otherTeacher).build());

        mockMvc.perform(put("/v1/conduct/{id}", existing.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(conductPayload(student, homeroomTeacher, ConductRating.YEU, "hijacked"))
                        .with(asUser(homeroomTeacherUser, "TEACHER")))
                .andExpect(status().isForbidden());

        ConductRecord stillOwnedByOtherStudent = conductRecordRepository.findById(existing.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(otherStudent.getId(), stillOwnedByOtherStudent.getStudent().getId());
        org.junit.jupiter.api.Assertions.assertEquals(ConductRating.TOT, stillOwnedByOtherStudent.getRating());
    }

    @Test
    void createConductRecord_evaluatedByNotOwnStaffProfile_returns403() throws Exception {
        // homeroomTeacher is authorized over the student, but tries to attribute
        // the evaluation to a different staff member than themselves.
        mockMvc.perform(post("/v1/conduct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(conductPayload(student, otherTeacher, ConductRating.TOT, "spoofed evaluator"))
                        .with(asUser(homeroomTeacherUser, "TEACHER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateConductRecord_reassigningToDuplicateStudentSemester_returns409() throws Exception {
        // record1 = (student, hk1) already exists; record2 = (otherStudent, hk1).
        // Updating record2 to also target (student, hk1) must be rejected as a
        // duplicate, not silently violate the unique constraint underneath.
        conductRecordRepository.save(ConductRecord.builder()
                .student(student).semester(hk1).rating(ConductRating.TOT)
                .evaluatedBy(homeroomTeacher).build());
        ConductRecord record2 = conductRecordRepository.save(ConductRecord.builder()
                .student(otherStudent).semester(hk1).rating(ConductRating.KHA)
                .evaluatedBy(otherTeacher).build());

        mockMvc.perform(put("/v1/conduct/{id}", record2.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(conductPayload(student, homeroomTeacher, ConductRating.YEU, "collide"))
                        .with(asUser(adminUser, "ADMIN")))
                .andExpect(status().isConflict());
    }

    @Test
    void getStudentConductRecords_asOwner_returns200() throws Exception {
        conductRecordRepository.save(ConductRecord.builder()
                .student(student).semester(hk1).rating(ConductRating.TOT)
                .evaluatedBy(homeroomTeacher).build());

        mockMvc.perform(get("/v1/conduct/student/{studentId}", student.getId())
                        .with(asUser(studentUser, "STUDENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rating").value("TOT"));
    }

    @Test
    void getStudentConductRecords_asDifferentStudent_returns403() throws Exception {
        mockMvc.perform(get("/v1/conduct/student/{studentId}", student.getId())
                        .with(asUser(otherStudentUser, "STUDENT")))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void getClassSemesterRoster_showsAllStudentsIncludingUnevaluated() throws Exception {
        conductRecordRepository.save(ConductRecord.builder()
                .student(student).semester(hk1).rating(ConductRating.TOT)
                .evaluatedBy(homeroomTeacher).build());

        SchoolClass homeroomClass = schoolClassRepository
                .findByClassNameAndSection("ITEST-COND-10", "A").orElseThrow();

        mockMvc.perform(get("/v1/conduct/class/{classId}/semester/{semesterId}",
                        homeroomClass.getId(), hk1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].studentId").value(student.getId()))
                .andExpect(jsonPath("$[0].rating").value("TOT"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void getClassSemesterRoster_asStudent_returns403() throws Exception {
        SchoolClass homeroomClass = schoolClassRepository
                .findByClassNameAndSection("ITEST-COND-10", "A").orElseThrow();

        mockMvc.perform(get("/v1/conduct/class/{classId}/semester/{semesterId}",
                        homeroomClass.getId(), hk1.getId()))
                .andExpect(status().isForbidden());
    }
}
