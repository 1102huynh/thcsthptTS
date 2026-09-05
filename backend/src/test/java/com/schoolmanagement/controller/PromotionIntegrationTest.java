package com.schoolmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.schoolmanagement.entity.AcademicYear;
import com.schoolmanagement.entity.AcademicYearStatus;
import com.schoolmanagement.entity.ConductRating;
import com.schoolmanagement.entity.ConductRecord;
import com.schoolmanagement.entity.EmploymentStatus;
import com.schoolmanagement.entity.GradeComponentConfig;
import com.schoolmanagement.entity.GradeComponentType;
import com.schoolmanagement.entity.GradeRecord;
import com.schoolmanagement.entity.PromotionDecision;
import com.schoolmanagement.entity.PromotionRecord;
import com.schoolmanagement.entity.PromotionThresholdConfig;
import com.schoolmanagement.entity.Role;
import com.schoolmanagement.entity.SchoolClass;
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
import com.schoolmanagement.repository.AttendanceRepository;
import com.schoolmanagement.repository.ConductRecordRepository;
import com.schoolmanagement.repository.GradeComponentConfigRepository;
import com.schoolmanagement.repository.GradeRecordRepository;
import com.schoolmanagement.repository.PromotionRecordRepository;
import com.schoolmanagement.repository.PromotionThresholdConfigRepository;
import com.schoolmanagement.repository.SchoolClassRepository;
import com.schoolmanagement.repository.SemesterRepository;
import com.schoolmanagement.repository.StaffRepository;
import com.schoolmanagement.repository.StudentRepository;
import com.schoolmanagement.repository.SubjectRepository;
import com.schoolmanagement.repository.UserRepository;
import com.schoolmanagement.entity.Attendance;
import com.schoolmanagement.entity.AttendanceStatus;
import org.junit.jupiter.api.Assertions;
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
 * Integration test for /v1/promotions and /v1/promotion-thresholds — real
 * Spring context + local MySQL via the "test" profile. Each test rolls back
 * (@Transactional). Covers IMPLEMENTATION_PLAN.md 3.5: the preview's
 * suggestedDecision is a configurable approximation (lowest per-subject
 * điểm TB năm + HK2 hạnh kiểm + attendance rate vs. an ADMIN-configured
 * PromotionThresholdConfig), never the official TT22/58 xếp loại.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PromotionIntegrationTest {

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
    private SubjectRepository subjectRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private StaffRepository staffRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GradeComponentConfigRepository gradeComponentConfigRepository;
    @Autowired
    private GradeRecordRepository gradeRecordRepository;
    @Autowired
    private ConductRecordRepository conductRecordRepository;
    @Autowired
    private PromotionThresholdConfigRepository promotionThresholdConfigRepository;
    @Autowired
    private PromotionRecordRepository promotionRecordRepository;
    @Autowired
    private AttendanceRepository attendanceRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private AcademicYear academicYear;
    private AcademicYear otherAcademicYear;
    private Semester hk1;
    private Semester hk2;
    private Subject subject;
    private SchoolClass schoolClass;
    private Student student;
    private Staff staff;
    private User adminUser;
    private User studentUser;
    private User otherStudentUser;

    @BeforeEach
    void setUp() {
        academicYear = academicYearRepository.save(AcademicYear.builder()
                .name("2099-2100")
                .startDate(LocalDate.of(2099, 9, 1)).endDate(LocalDate.of(2100, 5, 31))
                .status(AcademicYearStatus.ACTIVE).build());
        otherAcademicYear = academicYearRepository.save(AcademicYear.builder()
                .name("2098-2099")
                .startDate(LocalDate.of(2098, 9, 1)).endDate(LocalDate.of(2099, 5, 31))
                .status(AcademicYearStatus.ACTIVE).build());

        hk1 = semesterRepository.save(Semester.builder()
                .academicYear(academicYear).name(SemesterName.HK1)
                .startDate(academicYear.getStartDate()).endDate(LocalDate.of(2100, 1, 15)).build());
        hk2 = semesterRepository.save(Semester.builder()
                .academicYear(academicYear).name(SemesterName.HK2)
                .startDate(LocalDate.of(2100, 1, 16)).endDate(academicYear.getEndDate()).build());

        subject = subjectRepository.save(Subject.builder()
                .code("ITEST-PROMO-SUBJ").name("ITEST Subject").category(SubjectCategory.BAT_BUOC).build());

        adminUser = userRepository.save(User.builder()
                .username("itest.promo.admin").email("itest.promo.admin@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Integration").lastName("Admin").role(Role.ADMIN).enabled(true).build());

        // staff is made this class's GVCN below (classTeacher(staff)) so the
        // TEACHER-role tests exercise the H.3.1 homeroom-preview scoping.
        User teacherUser = userRepository.save(User.builder()
                .username("itest.promo.teacher").email("itest.promo.teacher@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Integration").lastName("Teacher").role(Role.TEACHER).enabled(true).build());
        staff = staffRepository.save(Staff.builder()
                .employeeId("ITEST-PROMO-EMP").user(teacherUser)
                .position(StaffPosition.TEACHER).status(EmploymentStatus.ACTIVE).build());

        // gradeLevel 9 (a graduating grade) so the TOT_NGHIEP-suggestion branch is exercised.
        schoolClass = schoolClassRepository.save(SchoolClass.builder()
                .className("ITEST-PROMO-9").section("A").academicYear("2099-2100").gradeLevel(9)
                .classTeacher(staff).build());

        studentUser = userRepository.save(User.builder()
                .username("itest.promo.student").email("itest.promo.student@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Integration").lastName("Student").role(Role.STUDENT).enabled(true).build());
        student = studentRepository.save(Student.builder()
                .rollNumber("ITEST-PROMO-ROLL").admissionNumber("ITEST-PROMO-ADM")
                .user(studentUser).status(StudentStatus.ACTIVE)
                .className(schoolClass.getClassName()).section(schoolClass.getSection())
                .build());

        otherStudentUser = userRepository.save(User.builder()
                .username("itest.promo.student2").email("itest.promo.student2@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Integration").lastName("StudentTwo").role(Role.STUDENT).enabled(true).build());
        studentRepository.save(Student.builder()
                .rollNumber("ITEST-PROMO-ROLL-2").admissionNumber("ITEST-PROMO-ADM-2")
                .user(otherStudentUser).status(StudentStatus.ACTIVE)
                .className(schoolClass.getClassName()).section(schoolClass.getSection())
                .build());

        gradeComponentConfigRepository.save(GradeComponentConfig.builder()
                .componentType(GradeComponentType.MIENG).weight(1).appliesFrom("2099-2100").build());
    }

    private RequestPostProcessor asUser(User user, String role) {
        return authentication(new UsernamePasswordAuthenticationToken(
                user, null, List.of(new SimpleGrantedAuthority("ROLE_" + role))));
    }

    private void giveStudentQualifyingRecord() {
        gradeRecordRepository.save(GradeRecord.builder()
                .student(student).subject(subject).semester(hk1)
                .componentType(GradeComponentType.MIENG).score(9.0).teacher(staff).build());
        gradeRecordRepository.save(GradeRecord.builder()
                .student(student).subject(subject).semester(hk2)
                .componentType(GradeComponentType.MIENG).score(9.0).teacher(staff).build());
        conductRecordRepository.save(ConductRecord.builder()
                .student(student).semester(hk2).rating(ConductRating.TOT).evaluatedBy(staff).build());
        // No attendance rows -> attendanceRate stays null -> won't meet thresholds
        // on its own; individual tests add attendance rows when they need a full pass.
    }

    private void saveThresholdConfig() {
        promotionThresholdConfigRepository.save(PromotionThresholdConfig.builder()
                .appliesFrom("2099-2100").minSubjectAverage(5.0)
                .minConduct(ConductRating.TRUNG_BINH).maxAbsenceRate(20.0).build());
    }

    @Test
    void previewClassPromotions_noThresholdConfig_noSuggestion() throws Exception {
        mockMvc.perform(get("/v1/promotions/class/{classId}/preview", schoolClass.getId())
                        .param("academicYearId", academicYear.getId().toString())
                        .with(asUser(staff.getUser(), "TEACHER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].meetsThresholds").doesNotExist())
                .andExpect(jsonPath("$[0].suggestedDecision").doesNotExist())
                .andExpect(jsonPath("$[0].reasons[0]").value(org.hamcrest.Matchers.containsString("Chưa cấu hình")));
    }

    @Test
    void previewClassPromotions_classAcademicYearMismatch_returns400() throws Exception {
        mockMvc.perform(get("/v1/promotions/class/{classId}/preview", schoolClass.getId())
                        .param("academicYearId", otherAcademicYear.getId().toString())
                        .with(asUser(adminUser, "ADMIN")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void previewClassPromotions_graduatingGradeMeetsThresholds_suggestsTotNghiep() throws Exception {
        saveThresholdConfig();
        giveStudentQualifyingRecord();
        // 4 present, 1 absent this year -> 80% attendance, exactly at the 20% max-absence threshold.
        markAttendance(4, 1);

        mockMvc.perform(get("/v1/promotions/class/{classId}/preview", schoolClass.getId())
                        .param("academicYearId", academicYear.getId().toString())
                        .with(asUser(adminUser, "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].studentId").value(student.getId()))
                .andExpect(jsonPath("$[0].lowestSubjectAverage").value(9.0))
                .andExpect(jsonPath("$[0].conduct").value("TOT"))
                .andExpect(jsonPath("$[0].attendanceRate").value(80.0))
                .andExpect(jsonPath("$[0].meetsThresholds").value(true))
                .andExpect(jsonPath("$[0].suggestedDecision").value("TOT_NGHIEP"))
                .andExpect(jsonPath("$[0].reasons").isEmpty());
    }

    @Test
    void previewClassPromotions_belowThreshold_suggestsOLaiWithReasons() throws Exception {
        saveThresholdConfig();
        // No grades/conduct/attendance at all for this student.
        mockMvc.perform(get("/v1/promotions/class/{classId}/preview", schoolClass.getId())
                        .param("academicYearId", academicYear.getId().toString())
                        .with(asUser(adminUser, "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].meetsThresholds").value(false))
                .andExpect(jsonPath("$[0].suggestedDecision").value("O_LAI"))
                .andExpect(jsonPath("$[0].reasons", org.hamcrest.Matchers.hasSize(3)));
    }

    @Test
    void previewClassPromotions_asNonHomeroomTeacher_returns403() throws Exception {
        User otherTeacherUser = userRepository.save(User.builder()
                .username("itest.promo.other-teacher").email("itest.promo.other-teacher@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Integration").lastName("OtherTeacher").role(Role.TEACHER).enabled(true).build());
        staffRepository.save(Staff.builder()
                .employeeId("ITEST-PROMO-OTHER").user(otherTeacherUser)
                .position(StaffPosition.TEACHER).status(EmploymentStatus.ACTIVE).build());
        // otherTeacherUser has a Staff profile but is not GVCN of any class.

        mockMvc.perform(get("/v1/promotions/class/{classId}/preview", schoolClass.getId())
                        .param("academicYearId", academicYear.getId().toString())
                        .with(asUser(otherTeacherUser, "TEACHER")))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void confirmPromotions_asTeacher_returns403() throws Exception {
        mockMvc.perform(post("/v1/promotions/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(confirmRequest(PromotionDecision.LEN_LOP)))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void confirmPromotions_persistsSnapshotAndDecision() throws Exception {
        saveThresholdConfig();
        giveStudentQualifyingRecord();
        markAttendance(4, 1);

        mockMvc.perform(post("/v1/promotions/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(confirmRequest(PromotionDecision.TOT_NGHIEP)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].studentId").value(student.getId()))
                .andExpect(jsonPath("$[0].decision").value("TOT_NGHIEP"))
                .andExpect(jsonPath("$[0].lowestSubjectAverageSnapshot").value(9.0))
                .andExpect(jsonPath("$[0].conductSnapshot").value("TOT"))
                .andExpect(jsonPath("$[0].attendanceRateSnapshot").value(80.0))
                .andExpect(jsonPath("$[0].decidedById").value(staff.getId()));

        Assertions.assertEquals(1, promotionRecordRepository.findByStudent(student).size());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void confirmPromotions_overwritesExistingDecision_updatesInPlaceNotDuplicated() throws Exception {
        PromotionRecord existing = promotionRecordRepository.save(PromotionRecord.builder()
                .student(student).academicYear(academicYear)
                .decision(PromotionDecision.LEN_LOP).decisionDate(LocalDate.now())
                .decidedBy(staff).build());

        mockMvc.perform(post("/v1/promotions/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(confirmRequest(PromotionDecision.O_LAI)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(existing.getId()))
                .andExpect(jsonPath("$[0].decision").value("O_LAI"));

        List<PromotionRecord> allForStudent = promotionRecordRepository.findByStudent(student);
        Assertions.assertEquals(1, allForStudent.size());
        Assertions.assertEquals(PromotionDecision.O_LAI, allForStudent.get(0).getDecision());
    }

    @Test
    void getStudentPromotionHistory_asOwner_returns200() throws Exception {
        promotionRecordRepository.save(PromotionRecord.builder()
                .student(student).academicYear(academicYear)
                .decision(PromotionDecision.LEN_LOP).decisionDate(LocalDate.now())
                .decidedBy(staff).build());

        mockMvc.perform(get("/v1/promotions/student/{studentId}", student.getId())
                        .with(asUser(studentUser, "STUDENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].decision").value("LEN_LOP"));
    }

    @Test
    void getStudentPromotionHistory_asDifferentStudent_returns403() throws Exception {
        mockMvc.perform(get("/v1/promotions/student/{studentId}", student.getId())
                        .with(asUser(otherStudentUser, "STUDENT")))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createThresholdConfig_duplicateAppliesFrom_returns409() throws Exception {
        saveThresholdConfig();
        PromotionThresholdConfig duplicate = PromotionThresholdConfig.builder()
                .appliesFrom("2099-2100").minSubjectAverage(6.0)
                .minConduct(ConductRating.KHA).maxAbsenceRate(10.0).build();

        mockMvc.perform(post("/v1/promotion-thresholds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createThresholdConfig_malformedAppliesFrom_returns400() throws Exception {
        PromotionThresholdConfig bad = PromotionThresholdConfig.builder()
                .appliesFrom("AY2099").minSubjectAverage(5.0)
                .minConduct(ConductRating.TRUNG_BINH).maxAbsenceRate(20.0).build();

        mockMvc.perform(post("/v1/promotion-thresholds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void createThresholdConfig_asTeacher_returns403() throws Exception {
        PromotionThresholdConfig config = PromotionThresholdConfig.builder()
                .appliesFrom("2100-2101").minSubjectAverage(5.0)
                .minConduct(ConductRating.TRUNG_BINH).maxAbsenceRate(20.0).build();

        mockMvc.perform(post("/v1/promotion-thresholds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(config)))
                .andExpect(status().isForbidden());
    }

    private PromotionRecord confirmRequest(PromotionDecision decision) {
        return PromotionRecord.builder()
                .student(Student.builder().id(student.getId()).build())
                .academicYear(AcademicYear.builder().id(academicYear.getId()).build())
                .decision(decision)
                .decidedBy(Staff.builder().id(staff.getId()).build())
                .remarks("itest")
                .build();
    }

    private void markAttendance(int presentDays, int absentDays) {
        LocalDate day = academicYear.getStartDate().plusDays(1);
        for (int i = 0; i < presentDays; i++) {
            attendanceRepository.save(Attendance.builder()
                    .student(student).attendanceDate(day.plusDays(i))
                    .status(AttendanceStatus.PRESENT).build());
        }
        for (int i = 0; i < absentDays; i++) {
            attendanceRepository.save(Attendance.builder()
                    .student(student).attendanceDate(day.plusDays(presentDays + i))
                    .status(AttendanceStatus.ABSENT).build());
        }
    }
}
