package com.schoolmanagement.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.schoolmanagement.entity.AcademicYear;
import com.schoolmanagement.entity.AcademicYearStatus;
import com.schoolmanagement.entity.Attendance;
import com.schoolmanagement.entity.AttendanceStatus;
import com.schoolmanagement.entity.EmploymentStatus;
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
import com.schoolmanagement.entity.TeachingAssignment;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.repository.AcademicYearRepository;
import com.schoolmanagement.repository.AttendanceRepository;
import com.schoolmanagement.repository.SchoolClassRepository;
import com.schoolmanagement.repository.SemesterRepository;
import com.schoolmanagement.repository.StaffRepository;
import com.schoolmanagement.repository.StudentRepository;
import com.schoolmanagement.repository.SubjectRepository;
import com.schoolmanagement.repository.TeachingAssignmentRepository;
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
    private AcademicYearRepository academicYearRepository;
    @Autowired
    private SemesterRepository semesterRepository;
    @Autowired
    private SubjectRepository subjectRepository;
    @Autowired
    private TeachingAssignmentRepository teachingAssignmentRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private User teacherUser;
    private Student studentA;
    private Student studentB;
    private SchoolClass schoolClass;
    private Semester semester;
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
        // every write here (mark/update/delete) to the caller's homeroom class,
        // OR to a GVBM with a TeachingAssignment for it (see below) - matches
        // how sổ đầu bài attendance is actually recorded per period.
        Staff teacherStaff = staffRepository.save(Staff.builder()
                .employeeId("ITEST-ATT-EMP").user(teacherUser)
                .position(StaffPosition.TEACHER).status(EmploymentStatus.ACTIVE).build());
        schoolClass = schoolClassRepository.save(SchoolClass.builder()
                .className(CLASS_NAME).section(SECTION).academicYear("2099-2100")
                .classTeacher(teacherStaff).build());

        // AcademicYear name is the far-future "2099-2100" placeholder this
        // suite always uses (collision-proof against seed data), but the
        // Semester's own startDate/endDate are set to bracket the fixed
        // `DATE` constant (2025-03-10) instead - only those two fields are
        // ever compared against `DATE` by TeacherAssignmentGuard.hasAssignmentForClass.
        AcademicYear academicYear = academicYearRepository.save(AcademicYear.builder()
                .name("2099-2100")
                .startDate(LocalDate.of(2025, 1, 1)).endDate(LocalDate.of(2025, 12, 31))
                .status(AcademicYearStatus.ACTIVE).build());
        semester = semesterRepository.save(Semester.builder()
                .academicYear(academicYear).name(SemesterName.HK1)
                .startDate(LocalDate.of(2025, 1, 1)).endDate(LocalDate.of(2025, 6, 30))
                .build());

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

    // GVBM (subject teacher) - NOT GVCN of CLASS_NAME/SECTION, but holds a
    // TeachingAssignment for it in `semester` - the H.3.1 extension this
    // suite tests: sổ đầu bài attendance is recorded per period by whichever
    // teacher is teaching, not only GVCN.
    private User gvbmTeacher() {
        User user = userRepository.save(User.builder()
                .username("itest.att.gvbm").email("itest.att.gvbm@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Integration").lastName("Gvbm").role(Role.TEACHER).enabled(true).build());
        Staff staff = staffRepository.save(Staff.builder()
                .employeeId("ITEST-ATT-GVBM-EMP").user(user)
                .position(StaffPosition.TEACHER).status(EmploymentStatus.ACTIVE).build());
        Subject subject = subjectRepository.save(Subject.builder()
                .code("ITEST-ATT-SUBJ").name("ITEST Subject").category(SubjectCategory.BAT_BUOC).build());
        teachingAssignmentRepository.save(TeachingAssignment.builder()
                .schoolClass(schoolClass).subject(subject).teacher(staff).semester(semester).build());
        return user;
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

    // ---- H.3.1 mở rộng - a TEACHER may also mark/update/delete attendance
    // for a class they hold a TeachingAssignment for (GVBM), not only their
    // homeroom class (GVCN) - matches how sổ đầu bài attendance is actually
    // recorded per period ----

    @Test
    void markClassAttendance_asGvbmWithAssignmentButNotHomeroom_returns200() throws Exception {
        mockMvc.perform(post("/v1/attendance/class")
                        .param("className", CLASS_NAME).param("section", SECTION).param("date", DATE.toString())
                        .param("presentStudentIds", studentA.getId().toString())
                        .with(asUser(gvbmTeacher(), "TEACHER")))
                .andExpect(status().isOk());
    }

    @Test
    void markAttendance_asGvbmWithAssignmentButNotHomeroom_returns201() throws Exception {
        Attendance attendance = Attendance.builder()
                .student(Student.builder().id(studentA.getId()).build())
                .attendanceDate(DATE).status(AttendanceStatus.PRESENT).build();

        mockMvc.perform(post("/v1/attendance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(attendance))
                        .with(asUser(gvbmTeacher(), "TEACHER")))
                .andExpect(status().isCreated());
    }

}
