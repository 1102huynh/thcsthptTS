package com.schoolmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.schoolmanagement.entity.EmploymentStatus;
import com.schoolmanagement.entity.GradeComponentType;
import com.schoolmanagement.entity.GradeRecord;
import com.schoolmanagement.entity.Role;
import com.schoolmanagement.entity.Semester;
import com.schoolmanagement.entity.SemesterName;
import com.schoolmanagement.entity.AcademicYear;
import com.schoolmanagement.entity.AcademicYearStatus;
import com.schoolmanagement.entity.Staff;
import com.schoolmanagement.entity.StaffPosition;
import com.schoolmanagement.entity.Student;
import com.schoolmanagement.entity.StudentStatus;
import com.schoolmanagement.entity.Subject;
import com.schoolmanagement.entity.SubjectCategory;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.repository.AcademicYearRepository;
import com.schoolmanagement.repository.GradeRecordRepository;
import com.schoolmanagement.repository.SemesterRepository;
import com.schoolmanagement.repository.StaffRepository;
import com.schoolmanagement.repository.StudentRepository;
import com.schoolmanagement.repository.SubjectRepository;
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
 * Integration test for /v1/audit-logs and the manual audit call sites it
 * reads from — real Spring context + local MySQL via the "test" profile.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuditLogIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AcademicYearRepository academicYearRepository;
    @Autowired
    private SemesterRepository semesterRepository;
    @Autowired
    private SubjectRepository subjectRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private StaffRepository staffRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GradeRecordRepository gradeRecordRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private Semester hk1;
    private Subject subject;
    private Student student;
    private Staff staff;
    private User adminUser;

    @BeforeEach
    void setUp() {
        AcademicYear academicYear = academicYearRepository.save(AcademicYear.builder()
                .name("2099-2100").startDate(LocalDate.of(2099, 9, 1)).endDate(LocalDate.of(2100, 5, 31))
                .status(AcademicYearStatus.ACTIVE).build());
        hk1 = semesterRepository.save(Semester.builder()
                .academicYear(academicYear).name(SemesterName.HK1)
                .startDate(academicYear.getStartDate()).endDate(LocalDate.of(2100, 1, 15)).build());
        subject = subjectRepository.save(Subject.builder()
                .code("ITEST-AUDIT-SUBJ").name("ITEST Subject").category(SubjectCategory.BAT_BUOC).build());

        User teacherUser = userRepository.save(User.builder()
                .username("itest.audit.teacher").email("itest.audit.teacher@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Integration").lastName("Teacher").role(Role.TEACHER).enabled(true).build());
        staff = staffRepository.save(Staff.builder()
                .employeeId("ITEST-AUDIT-EMP").user(teacherUser)
                .position(StaffPosition.TEACHER).status(EmploymentStatus.ACTIVE).build());

        User studentUser = userRepository.save(User.builder()
                .username("itest.audit.student").email("itest.audit.student@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Integration").lastName("Student").role(Role.STUDENT).enabled(true).build());
        student = studentRepository.save(Student.builder()
                .rollNumber("ITEST-AUDIT-ROLL").admissionNumber("ITEST-AUDIT-ADM")
                .user(studentUser).status(StudentStatus.ACTIVE).build());

        adminUser = userRepository.save(User.builder()
                .username("itest.audit.admin").email("itest.audit.admin@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Integration").lastName("Admin").role(Role.ADMIN).enabled(true).build());
    }

    private RequestPostProcessor asUser(User user, String role) {
        return authentication(new UsernamePasswordAuthenticationToken(
                user, null, List.of(new SimpleGrantedAuthority("ROLE_" + role))));
    }

    @Test
    void updateGradeRecord_writesAuditLogEntry() throws Exception {
        GradeRecord record = gradeRecordRepository.save(GradeRecord.builder()
                .student(student).subject(subject).semester(hk1)
                .componentType(GradeComponentType.MIENG).score(7.0).teacher(staff).build());

        GradeRecord update = GradeRecord.builder()
                .student(Student.builder().id(student.getId()).build())
                .subject(Subject.builder().id(subject.getId()).build())
                .semester(Semester.builder().id(hk1.getId()).build())
                .componentType(GradeComponentType.MIENG).score(9.0)
                .teacher(Staff.builder().id(staff.getId()).build())
                .build();

        mockMvc.perform(put("/v1/grade-records/{id}", record.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update))
                        .with(asUser(adminUser, "ADMIN")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/v1/audit-logs")
                        .param("entityType", "GradeRecord")
                        .with(asUser(adminUser, "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.entityId == " + record.getId() + " && @.action == 'UPDATE')]").exists())
                .andExpect(jsonPath("$.content[0].actorName").value("Integration Admin"));
    }

    @Test
    void deleteGradeRecord_writesAuditLogEntry() throws Exception {
        GradeRecord record = gradeRecordRepository.save(GradeRecord.builder()
                .student(student).subject(subject).semester(hk1)
                .componentType(GradeComponentType.MIENG).score(7.0).teacher(staff).build());

        mockMvc.perform(delete("/v1/grade-records/{id}", record.getId())
                        .with(asUser(adminUser, "ADMIN")))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/v1/audit-logs")
                        .param("entityType", "GradeRecord")
                        .with(asUser(adminUser, "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.entityId == " + record.getId() + " && @.action == 'DELETE')]").exists());
    }

    @Test
    void deleteStudent_writesAuditLogEntry() throws Exception {
        mockMvc.perform(delete("/v1/students/{id}", student.getId())
                        .with(asUser(adminUser, "ADMIN")))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/v1/audit-logs")
                        .param("entityType", "Student")
                        .param("actorId", adminUser.getId().toString())
                        .with(asUser(adminUser, "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.entityId == " + student.getId() + " && @.action == 'DELETE')]").exists());
    }

    @Test
    void search_asNonAdmin_returns403() throws Exception {
        mockMvc.perform(get("/v1/audit-logs").with(asUser(staff.getUser(), "TEACHER")))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void search_sizeOverLimit_returns400() throws Exception {
        mockMvc.perform(get("/v1/audit-logs").param("size", "500"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void search_negativePage_returns400() throws Exception {
        mockMvc.perform(get("/v1/audit-logs").param("page", "-1"))
                .andExpect(status().isBadRequest());
    }
}
