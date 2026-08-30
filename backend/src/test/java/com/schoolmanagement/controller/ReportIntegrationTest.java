package com.schoolmanagement.controller;

import com.schoolmanagement.entity.AcademicYear;
import com.schoolmanagement.entity.AcademicYearStatus;
import com.schoolmanagement.entity.ConductRating;
import com.schoolmanagement.entity.ConductRecord;
import com.schoolmanagement.entity.EmploymentStatus;
import com.schoolmanagement.entity.Fee;
import com.schoolmanagement.entity.FeeStatus;
import com.schoolmanagement.entity.GradeComponentConfig;
import com.schoolmanagement.entity.GradeComponentType;
import com.schoolmanagement.entity.GradeRecord;
import com.schoolmanagement.entity.PromotionDecision;
import com.schoolmanagement.entity.PromotionRecord;
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
import com.schoolmanagement.entity.Attendance;
import com.schoolmanagement.entity.AttendanceStatus;
import com.schoolmanagement.repository.AcademicYearRepository;
import com.schoolmanagement.repository.AttendanceRepository;
import com.schoolmanagement.repository.ConductRecordRepository;
import com.schoolmanagement.repository.FeeRepository;
import com.schoolmanagement.repository.GradeComponentConfigRepository;
import com.schoolmanagement.repository.GradeRecordRepository;
import com.schoolmanagement.repository.PromotionRecordRepository;
import com.schoolmanagement.repository.SchoolClassRepository;
import com.schoolmanagement.repository.SemesterRepository;
import com.schoolmanagement.repository.StaffRepository;
import com.schoolmanagement.repository.StudentRepository;
import com.schoolmanagement.repository.SubjectRepository;
import com.schoolmanagement.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test for /v1/reports — real Spring context + local MySQL via
 * the "test" profile. Each test rolls back ({@literal @}Transactional).
 * Response bodies are verified by magic bytes ("%PDF-" / "PK\3\4") rather
 * than parsing the PDF/xlsx content — good enough to prove OpenPDF/POI
 * actually produced a well-formed document (and, for the PDF paths, that the
 * embedded Vietnamese font didn't blow up mid-render) without pulling in a
 * PDF/xlsx-reading library just for tests.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ReportIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
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
    private PromotionRecordRepository promotionRecordRepository;
    @Autowired
    private AttendanceRepository attendanceRepository;
    @Autowired
    private FeeRepository feeRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private AcademicYear academicYear;
    private Semester hk1;
    private Semester hk2;
    private Subject subject;
    private SchoolClass schoolClass;
    private Student student;
    private Staff staff;
    private User studentUser;
    private User otherStudentUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        academicYear = academicYearRepository.save(AcademicYear.builder()
                .name("2099-2100")
                .startDate(LocalDate.of(2099, 9, 1)).endDate(LocalDate.of(2100, 5, 31))
                .status(AcademicYearStatus.ACTIVE).build());

        hk1 = semesterRepository.save(Semester.builder()
                .academicYear(academicYear).name(SemesterName.HK1)
                .startDate(academicYear.getStartDate()).endDate(LocalDate.of(2100, 1, 15)).build());
        hk2 = semesterRepository.save(Semester.builder()
                .academicYear(academicYear).name(SemesterName.HK2)
                .startDate(LocalDate.of(2100, 1, 16)).endDate(academicYear.getEndDate()).build());

        subject = subjectRepository.save(Subject.builder()
                .code("ITEST-RPT-SUBJ").name("ITEST Subject").category(SubjectCategory.BAT_BUOC).build());

        schoolClass = schoolClassRepository.save(SchoolClass.builder()
                .className("ITEST-RPT-9").section("A").academicYear("2099-2100").gradeLevel(9).build());

        User teacherUser = userRepository.save(User.builder()
                .username("itest.rpt.teacher").email("itest.rpt.teacher@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Integration").lastName("Teacher").role(Role.TEACHER).enabled(true).build());
        staff = staffRepository.save(Staff.builder()
                .employeeId("ITEST-RPT-EMP").user(teacherUser)
                .position(StaffPosition.TEACHER).status(EmploymentStatus.ACTIVE).build());

        studentUser = userRepository.save(User.builder()
                .username("itest.rpt.student").email("itest.rpt.student@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Nguyễn Văn").lastName("Ítest").role(Role.STUDENT).enabled(true).build());
        student = studentRepository.save(Student.builder()
                .rollNumber("ITEST-RPT-ROLL").admissionNumber("ITEST-RPT-ADM")
                .user(studentUser).status(StudentStatus.ACTIVE)
                .className(schoolClass.getClassName()).section(schoolClass.getSection())
                .dateOfBirth(LocalDate.of(2009, 3, 15))
                .build());

        otherStudentUser = userRepository.save(User.builder()
                .username("itest.rpt.student2").email("itest.rpt.student2@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Integration").lastName("StudentTwo").role(Role.STUDENT).enabled(true).build());
        studentRepository.save(Student.builder()
                .rollNumber("ITEST-RPT-ROLL-2").admissionNumber("ITEST-RPT-ADM-2")
                .user(otherStudentUser).status(StudentStatus.ACTIVE)
                .className(schoolClass.getClassName()).section(schoolClass.getSection())
                .build());

        gradeComponentConfigRepository.save(GradeComponentConfig.builder()
                .componentType(GradeComponentType.MIENG).weight(1).appliesFrom("2099-2100").build());

        // ReportController casts authentication.getPrincipal() to the domain
        // User (needed for STUDENT/PARENT self-access checks) - @WithMockUser's
        // principal is Spring Security's own User type, which ClassCastExceptions
        // on that cast (masked as a 500). Needs a real, already-saved User + the
        // asUser() RequestPostProcessor instead, same as every other controller
        // in this codebase that reads the caller's identity.
        adminUser = userRepository.save(User.builder()
                .username("itest.rpt.admin").email("itest.rpt.admin@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Integration").lastName("Admin").role(Role.ADMIN).enabled(true).build());
    }

    private RequestPostProcessor asUser(User user, String role) {
        return authentication(new UsernamePasswordAuthenticationToken(
                user, null, List.of(new SimpleGrantedAuthority("ROLE_" + role))));
    }

    // ---------------------------------------------------------------
    // Transcript
    // ---------------------------------------------------------------

    @Test
    void studentTranscript_asOwner_returnsPdf() throws Exception {
        gradeRecordRepository.save(GradeRecord.builder()
                .student(student).subject(subject).semester(hk1)
                .componentType(GradeComponentType.MIENG).score(8.0).teacher(staff).build());
        gradeRecordRepository.save(GradeRecord.builder()
                .student(student).subject(subject).semester(hk2)
                .componentType(GradeComponentType.MIENG).score(9.0).teacher(staff).build());
        conductRecordRepository.save(ConductRecord.builder()
                .student(student).semester(hk1).rating(ConductRating.TOT).evaluatedBy(staff).build());
        conductRecordRepository.save(ConductRecord.builder()
                .student(student).semester(hk2).rating(ConductRating.KHA).evaluatedBy(staff).build());
        promotionRecordRepository.save(PromotionRecord.builder()
                .student(student).academicYear(academicYear)
                .decision(PromotionDecision.LEN_LOP).decisionDate(LocalDate.now())
                .decidedBy(staff).remarks("itest").build());

        byte[] pdf = mockMvc.perform(get("/v1/reports/student/{id}/transcript", student.getId())
                        .param("academicYearId", academicYear.getId().toString())
                        .with(asUser(studentUser, "STUDENT")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();

        assertIsPdf(pdf);
    }

    @Test
    void studentTranscript_asDifferentStudent_returns403() throws Exception {
        mockMvc.perform(get("/v1/reports/student/{id}/transcript", student.getId())
                        .param("academicYearId", academicYear.getId().toString())
                        .with(asUser(otherStudentUser, "STUDENT")))
                .andExpect(status().isForbidden());
    }

    @Test
    void studentTranscript_nonexistentAcademicYear_returns404() throws Exception {
        mockMvc.perform(get("/v1/reports/student/{id}/transcript", student.getId())
                        .param("academicYearId", "9999999")
                        .with(asUser(adminUser, "ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    void studentTranscript_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/v1/reports/student/{id}/transcript", student.getId())
                        .param("academicYearId", academicYear.getId().toString()))
                .andExpect(status().isUnauthorized());
    }

    // ---------------------------------------------------------------
    // Class attendance
    // ---------------------------------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void classAttendance_returnsExcel() throws Exception {
        LocalDate from = LocalDate.of(2099, 9, 1);
        LocalDate to = LocalDate.of(2099, 9, 5);
        attendanceRepository.save(Attendance.builder()
                .student(student).attendanceDate(from).status(AttendanceStatus.PRESENT).build());
        attendanceRepository.save(Attendance.builder()
                .student(student).attendanceDate(from.plusDays(1)).status(AttendanceStatus.ABSENT).build());

        byte[] excel = mockMvc.perform(get("/v1/reports/class/{id}/attendance", schoolClass.getId())
                        .param("from", from.toString())
                        .param("to", to.toString()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();

        assertIsXlsx(excel);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void classAttendance_leavePendingExcludedFromRecordedAndPercentage() throws Exception {
        // A pending (unresolved) leave request is neither a confirmed absence
        // nor a confirmed excused day - it must not count toward "recorded" or
        // move the chuyên cần % in either direction until it's actually decided.
        LocalDate day1 = LocalDate.of(2099, 9, 1);
        LocalDate day2 = LocalDate.of(2099, 9, 2);
        attendanceRepository.save(Attendance.builder()
                .student(student).attendanceDate(day1).status(AttendanceStatus.PRESENT).build());
        attendanceRepository.save(Attendance.builder()
                .student(student).attendanceDate(day2).status(AttendanceStatus.LEAVE_PENDING).build());

        byte[] excel = mockMvc.perform(get("/v1/reports/class/{id}/attendance", schoolClass.getId())
                        .param("from", day1.toString())
                        .param("to", day2.toString()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();

        try (org.apache.poi.xssf.usermodel.XSSFWorkbook workbook =
                     new org.apache.poi.xssf.usermodel.XSSFWorkbook(new java.io.ByteArrayInputStream(excel))) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(0);
            // row 0 = legend, row 1 = blank separator, row 2 = header, row 3 = this student.
            org.apache.poi.ss.usermodel.Row dataRow = sheet.getRow(3);
            // columns: 0=Mã HS, 1=Họ tên, 2-3=the two dates, 4=Có mặt, 5=Vắng, 6=Phép/Ốm, 7=Tổng ghi nhận, 8=Chuyên cần(%)
            Assertions.assertEquals("CM", dataRow.getCell(2).getStringCellValue());
            Assertions.assertEquals("CD", dataRow.getCell(3).getStringCellValue());
            Assertions.assertEquals(1.0, dataRow.getCell(4).getNumericCellValue(), "Có mặt");
            Assertions.assertEquals(0.0, dataRow.getCell(5).getNumericCellValue(), "Vắng");
            Assertions.assertEquals(0.0, dataRow.getCell(6).getNumericCellValue(), "Phép/Ốm - LEAVE_PENDING must not count here");
            Assertions.assertEquals(1.0, dataRow.getCell(7).getNumericCellValue(), "Tổng ghi nhận - LEAVE_PENDING excluded");
            Assertions.assertEquals(100.0, dataRow.getCell(8).getNumericCellValue(), "Chuyên cần % - based only on the 1 resolved day");
        }
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void classAttendance_toBeforeFrom_returns400() throws Exception {
        mockMvc.perform(get("/v1/reports/class/{id}/attendance", schoolClass.getId())
                        .param("from", "2099-09-05")
                        .param("to", "2099-09-01"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void classAttendance_asStudent_returns403() throws Exception {
        mockMvc.perform(get("/v1/reports/class/{id}/attendance", schoolClass.getId())
                        .param("from", "2099-09-01")
                        .param("to", "2099-09-05")
                        .with(asUser(studentUser, "STUDENT")))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void classAttendance_nonexistentClass_returns404() throws Exception {
        mockMvc.perform(get("/v1/reports/class/{id}/attendance", 9_999_999L)
                        .param("from", "2099-09-01")
                        .param("to", "2099-09-05"))
                .andExpect(status().isNotFound());
    }

    // ---------------------------------------------------------------
    // Fee receipt
    // ---------------------------------------------------------------

    @Test
    void feeReceipt_withPayment_returnsPdf() throws Exception {
        Fee fee = feeRepository.save(Fee.builder()
                .student(student).academicYear("2099-2100").feeType("Học phí học kỳ 1")
                .amount(500000.0).paidAmount(500000.0).remainingAmount(0.0)
                .paidDate(LocalDate.now()).paymentMethod("Tiền mặt").transactionId("ITEST-TXN-1")
                .status(FeeStatus.PAID).build());

        byte[] pdf = mockMvc.perform(get("/v1/reports/fees/receipt/{feeId}", fee.getId())
                        .with(asUser(studentUser, "STUDENT")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();

        assertIsPdf(pdf);
    }

    @Test
    void feeReceipt_withoutPayment_returns400() throws Exception {
        Fee fee = feeRepository.save(Fee.builder()
                .student(student).academicYear("2099-2100").feeType("Học phí học kỳ 1")
                .amount(500000.0).remainingAmount(500000.0)
                .status(FeeStatus.PENDING).build());

        mockMvc.perform(get("/v1/reports/fees/receipt/{feeId}", fee.getId())
                        .with(asUser(adminUser, "ADMIN")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void feeReceipt_asDifferentStudent_returns403() throws Exception {
        Fee fee = feeRepository.save(Fee.builder()
                .student(student).academicYear("2099-2100").feeType("Học phí học kỳ 1")
                .amount(500000.0).paidAmount(500000.0).remainingAmount(0.0)
                .status(FeeStatus.PAID).build());

        mockMvc.perform(get("/v1/reports/fees/receipt/{feeId}", fee.getId())
                        .with(asUser(otherStudentUser, "STUDENT")))
                .andExpect(status().isForbidden());
    }

    private void assertIsPdf(byte[] content) {
        Assertions.assertTrue(content.length > 4, "PDF body should not be empty");
        String header = new String(content, 0, 5, java.nio.charset.StandardCharsets.US_ASCII);
        Assertions.assertEquals("%PDF-", header, "response body should be a well-formed PDF");
    }

    private void assertIsXlsx(byte[] content) {
        Assertions.assertTrue(content.length > 4, "xlsx body should not be empty");
        // xlsx is a zip archive - "PK\3\4" is the local-file-header magic every zip starts with.
        Assertions.assertEquals(0x50, content[0] & 0xFF);
        Assertions.assertEquals(0x4B, content[1] & 0xFF);
    }
}
