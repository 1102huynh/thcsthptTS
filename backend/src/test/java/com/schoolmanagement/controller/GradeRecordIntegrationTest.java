package com.schoolmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.schoolmanagement.entity.AcademicYear;
import com.schoolmanagement.entity.AcademicYearStatus;
import com.schoolmanagement.entity.EmploymentStatus;
import com.schoolmanagement.entity.GradeComponentConfig;
import com.schoolmanagement.entity.GradeComponentType;
import com.schoolmanagement.entity.GradeRecord;
import com.schoolmanagement.entity.Role;
import com.schoolmanagement.entity.Semester;
import com.schoolmanagement.entity.SemesterName;
import com.schoolmanagement.entity.Staff;
import com.schoolmanagement.entity.StaffPosition;
import com.schoolmanagement.entity.Student;
import com.schoolmanagement.entity.StudentStatus;
import com.schoolmanagement.entity.Subject;
import com.schoolmanagement.entity.SubjectCategory;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.repository.AcademicYearRepository;
import com.schoolmanagement.repository.GradeComponentConfigRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for /v1/grade-records and /v1/grade-config — real Spring
 * context + local MySQL via the "test" profile. Each test rolls back
 * (@Transactional). The summary tests hand-check the two formulas from
 * IMPLEMENTATION_PLAN.md 3.3 against a worked example.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class GradeRecordIntegrationTest {

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
    private GradeComponentConfigRepository gradeComponentConfigRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private AcademicYear academicYear;
    private Semester hk1;
    private Semester hk2;
    private Subject subject;
    private Student student;
    private Staff teacher;

    @BeforeEach
    void setUp() {
        academicYear = academicYearRepository.save(AcademicYear.builder()
                // Deliberately not "ITEST-2099-2100": GradeRecordService.extractStartYear()
                // parses the year out of the leading "-"-delimited token of the academic-year
                // name (matches the real "2024-2025" naming convention), so a text prefix would
                // break it. A far-future year alone is already collision-proof against real/seed data.
                .name("2099-2100")
                .startDate(LocalDate.of(2099, 9, 1))
                .endDate(LocalDate.of(2100, 5, 31))
                .status(AcademicYearStatus.ACTIVE)
                .build());
        hk1 = semesterRepository.save(Semester.builder()
                .academicYear(academicYear).name(SemesterName.HK1)
                .startDate(academicYear.getStartDate()).endDate(LocalDate.of(2100, 1, 15))
                .build());
        hk2 = semesterRepository.save(Semester.builder()
                .academicYear(academicYear).name(SemesterName.HK2)
                .startDate(LocalDate.of(2100, 1, 16)).endDate(academicYear.getEndDate())
                .build());

        subject = subjectRepository.save(Subject.builder()
                .code("ITEST-GR-SUBJ").name("ITEST Subject").category(SubjectCategory.BAT_BUOC).build());

        User studentUser = userRepository.save(User.builder()
                .username("itest.gr.student").email("itest.gr.student@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Integration").lastName("Student").role(Role.STUDENT).enabled(true).build());
        student = studentRepository.save(Student.builder()
                .rollNumber("ITEST-GR-ROLL").admissionNumber("ITEST-GR-ADM")
                .user(studentUser).status(StudentStatus.ACTIVE).build());

        User teacherUser = userRepository.save(User.builder()
                .username("itest.gr.teacher").email("itest.gr.teacher@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Integration").lastName("Teacher").role(Role.TEACHER).enabled(true).build());
        teacher = staffRepository.save(Staff.builder()
                .employeeId("ITEST-GR-EMP").user(teacherUser)
                .position(StaffPosition.TEACHER).status(EmploymentStatus.ACTIVE).build());

        gradeComponentConfigRepository.save(GradeComponentConfig.builder()
                .componentType(GradeComponentType.MIENG).weight(1).appliesFrom("2099-2100").build());
        gradeComponentConfigRepository.save(GradeComponentConfig.builder()
                .componentType(GradeComponentType.MOT_TIET).weight(2).appliesFrom("2099-2100").build());
        gradeComponentConfigRepository.save(GradeComponentConfig.builder()
                .componentType(GradeComponentType.CUOI_KY).weight(3).appliesFrom("2099-2100").build());
    }

    private void recordGrade(Semester semester, GradeComponentType type, double score) {
        gradeRecordRepository.save(GradeRecord.builder()
                .student(student).subject(subject).semester(semester)
                .componentType(type).score(score).teacher(teacher).build());
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void createGradeRecord_persistsAndReturnsIt() throws Exception {
        GradeRecord request = GradeRecord.builder()
                .student(Student.builder().id(student.getId()).build())
                .subject(Subject.builder().id(subject.getId()).build())
                .semester(Semester.builder().id(hk1.getId()).build())
                .componentType(GradeComponentType.MIENG)
                .score(8.5)
                .teacher(Staff.builder().id(teacher.getId()).build())
                .build();

        mockMvc.perform(post("/v1/grade-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.score").value(8.5))
                .andExpect(jsonPath("$.studentName").value("Integration Student"));
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void createGradeRecord_scoreAboveTen_returns400() throws Exception {
        GradeRecord request = GradeRecord.builder()
                .student(Student.builder().id(student.getId()).build())
                .subject(Subject.builder().id(subject.getId()).build())
                .semester(Semester.builder().id(hk1.getId()).build())
                .componentType(GradeComponentType.MIENG)
                .score(10.5)
                .teacher(Staff.builder().id(teacher.getId()).build())
                .build();

        mockMvc.perform(post("/v1/grade-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void createGradeRecord_asStudent_returns403() throws Exception {
        GradeRecord request = GradeRecord.builder()
                .student(Student.builder().id(student.getId()).build())
                .subject(Subject.builder().id(subject.getId()).build())
                .semester(Semester.builder().id(hk1.getId()).build())
                .componentType(GradeComponentType.MIENG)
                .score(8.0)
                .teacher(Staff.builder().id(teacher.getId()).build())
                .build();

        mockMvc.perform(post("/v1/grade-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void semesterSummary_matchesHandCalculatedWeightedAverage() throws Exception {
        // Điểm TB môn học kỳ = Σ(score × weight) / Σ(weight).
        // MIENG(w1)=8, MOT_TIET(w2)=6, CUOI_KY(w3)=9
        // = (8*1 + 6*2 + 9*3) / (1+2+3) = (8+12+27)/6 = 47/6 = 7.8333.. -> 7.83
        recordGrade(hk1, GradeComponentType.MIENG, 8.0);
        recordGrade(hk1, GradeComponentType.MOT_TIET, 6.0);
        recordGrade(hk1, GradeComponentType.CUOI_KY, 9.0);

        mockMvc.perform(get("/v1/grade-records/student/{studentId}/summary", student.getId())
                        .param("semesterId", hk1.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].subjectId").value(subject.getId()))
                .andExpect(jsonPath("$[0].average").value(7.83))
                .andExpect(jsonPath("$[0].classification").doesNotExist());
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void semesterSummary_noWeightConfigured_returns404() throws Exception {
        recordGrade(hk1, GradeComponentType.GIUA_KY, 8.0); // no config seeded for GIUA_KY in setUp

        mockMvc.perform(get("/v1/grade-records/student/{studentId}/summary", student.getId())
                        .param("semesterId", hk1.getId().toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void yearSummary_combinesBothSemesters_matchesHandCalculatedFormula() throws Exception {
        // HK1: MIENG(w1)=8, CUOI_KY(w3)=8 -> (8+24)/4 = 8.0
        recordGrade(hk1, GradeComponentType.MIENG, 8.0);
        recordGrade(hk1, GradeComponentType.CUOI_KY, 8.0);
        // HK2: MIENG(w1)=6, CUOI_KY(w3)=6 -> (6+18)/4 = 6.0
        recordGrade(hk2, GradeComponentType.MIENG, 6.0);
        recordGrade(hk2, GradeComponentType.CUOI_KY, 6.0);

        // Điểm TB cả năm = (HK1 + HK2 × 2) / 3 = (8.0 + 6.0*2) / 3 = 20/3 = 6.6666.. -> 6.67
        mockMvc.perform(get("/v1/grade-records/student/{studentId}/year-summary", student.getId())
                        .param("academicYearId", academicYear.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].semester1Average").value(8.0))
                .andExpect(jsonPath("$[0].semester2Average").value(6.0))
                .andExpect(jsonPath("$[0].yearAverage").value(6.67));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createGradeConfig_duplicateTypeAndYear_returns409() throws Exception {
        GradeComponentConfig duplicate = GradeComponentConfig.builder()
                .componentType(GradeComponentType.MIENG).weight(5).appliesFrom("2099-2100").build();

        mockMvc.perform(post("/v1/grade-config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void createGradeConfig_asTeacher_returns403() throws Exception {
        GradeComponentConfig config = GradeComponentConfig.builder()
                .componentType(GradeComponentType.GIUA_KY).weight(3).appliesFrom("2100-2101").build();

        mockMvc.perform(post("/v1/grade-config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(config)))
                .andExpect(status().isForbidden());
    }
}
