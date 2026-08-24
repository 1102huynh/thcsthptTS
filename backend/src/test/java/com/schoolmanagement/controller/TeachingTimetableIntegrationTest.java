package com.schoolmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.schoolmanagement.entity.AcademicYear;
import com.schoolmanagement.entity.AcademicYearStatus;
import com.schoolmanagement.entity.EmploymentStatus;
import com.schoolmanagement.entity.Role;
import com.schoolmanagement.entity.SchoolClass;
import com.schoolmanagement.entity.Semester;
import com.schoolmanagement.entity.SemesterName;
import com.schoolmanagement.entity.Staff;
import com.schoolmanagement.entity.StaffPosition;
import com.schoolmanagement.entity.Subject;
import com.schoolmanagement.entity.SubjectCategory;
import com.schoolmanagement.entity.TeachingAssignment;
import com.schoolmanagement.entity.TimetableSlot;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.repository.AcademicYearRepository;
import com.schoolmanagement.repository.SchoolClassRepository;
import com.schoolmanagement.repository.SemesterRepository;
import com.schoolmanagement.repository.StaffRepository;
import com.schoolmanagement.repository.SubjectRepository;
import com.schoolmanagement.repository.TeachingAssignmentRepository;
import com.schoolmanagement.repository.TimetableSlotRepository;
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
 * Integration test for /v1/teaching-assignments and /v1/timetable — real
 * Spring context + local MySQL via the "test" profile. Each test rolls back
 * (@Transactional), so it never depends on — or pollutes — seeded data.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TeachingTimetableIntegrationTest {

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
    private StaffRepository staffRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TeachingAssignmentRepository teachingAssignmentRepository;
    @Autowired
    private TimetableSlotRepository timetableSlotRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private Semester semester;
    private SchoolClass classA;
    private SchoolClass classB;
    private Subject subject;
    private Staff teacher1;
    private Staff teacher2;

    @BeforeEach
    void setUp() {
        AcademicYear year = academicYearRepository.save(AcademicYear.builder()
                .name("ITEST-TT-2099-2100")
                .startDate(LocalDate.of(2099, 9, 1))
                .endDate(LocalDate.of(2100, 5, 31))
                .status(AcademicYearStatus.ACTIVE)
                .build());

        semester = semesterRepository.save(Semester.builder()
                .academicYear(year)
                .name(SemesterName.HK1)
                .startDate(year.getStartDate())
                .endDate(year.getEndDate())
                .build());

        classA = schoolClassRepository.save(SchoolClass.builder()
                .className("ITEST-TT-10").section("A").academicYear("2099-2100").build());
        classB = schoolClassRepository.save(SchoolClass.builder()
                .className("ITEST-TT-10").section("B").academicYear("2099-2100").build());

        subject = subjectRepository.save(Subject.builder()
                .code("ITEST-TT-SUBJ").name("ITEST Subject").category(SubjectCategory.BAT_BUOC).build());

        teacher1 = staffRepository.save(Staff.builder()
                .employeeId("ITEST-TT-EMP-1")
                .user(newUser("itest.tt.teacher1"))
                .position(StaffPosition.TEACHER)
                .status(EmploymentStatus.ACTIVE)
                .build());
        teacher2 = staffRepository.save(Staff.builder()
                .employeeId("ITEST-TT-EMP-2")
                .user(newUser("itest.tt.teacher2"))
                .position(StaffPosition.TEACHER)
                .status(EmploymentStatus.ACTIVE)
                .build());
    }

    private User newUser(String username) {
        return userRepository.save(User.builder()
                .username(username)
                .email(username + "@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Integration")
                .lastName("Teacher")
                .role(Role.TEACHER)
                .enabled(true)
                .build());
    }

    private TeachingAssignment newAssignment(SchoolClass schoolClass, Staff teacher) {
        return TeachingAssignment.builder()
                .schoolClass(SchoolClass.builder().id(schoolClass.getId()).build())
                .subject(Subject.builder().id(subject.getId()).build())
                .teacher(Staff.builder().id(teacher.getId()).build())
                .semester(Semester.builder().id(semester.getId()).build())
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createTeachingAssignment_persistsAndReturnsIt() throws Exception {
        mockMvc.perform(post("/v1/teaching-assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newAssignment(classA, teacher1))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.schoolClassLabel").value("ITEST-TT-10-A"))
                .andExpect(jsonPath("$.subjectCode").value("ITEST-TT-SUBJ"))
                .andExpect(jsonPath("$.teacherName").value("Integration Teacher"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createTeachingAssignment_duplicateClassSubjectSemester_returns409() throws Exception {
        teachingAssignmentRepository.save(resolvedAssignment(classA, teacher1));

        mockMvc.perform(post("/v1/teaching-assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newAssignment(classA, teacher2))))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void createTeachingAssignment_asTeacher_returns403() throws Exception {
        mockMvc.perform(post("/v1/teaching-assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newAssignment(classA, teacher1))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteTeachingAssignment_withSlots_returns409() throws Exception {
        TeachingAssignment saved = teachingAssignmentRepository.save(resolvedAssignment(classA, teacher1));
        timetableSlotRepository.save(TimetableSlot.builder()
                .teachingAssignment(saved).dayOfWeek(2).period(1).room("ITEST-ROOM").build());

        mockMvc.perform(delete("/v1/teaching-assignments/{id}", saved.getId()))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createSlot_persistsAndReturnsIt() throws Exception {
        TeachingAssignment assignment = teachingAssignmentRepository.save(resolvedAssignment(classA, teacher1));

        TimetableSlot slot = TimetableSlot.builder()
                .teachingAssignment(TeachingAssignment.builder().id(assignment.getId()).build())
                .dayOfWeek(2).period(1).room("ITEST-P101")
                .build();

        mockMvc.perform(post("/v1/timetable/slots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(slot)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dayOfWeek").value(2))
                .andExpect(jsonPath("$.period").value(1))
                .andExpect(jsonPath("$.room").value("ITEST-P101"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createSlot_teacherDoubleBooked_returns409() throws Exception {
        TeachingAssignment assignment1 = teachingAssignmentRepository.save(resolvedAssignment(classA, teacher1));
        TeachingAssignment assignment2 = teachingAssignmentRepository.save(resolvedAssignment(classB, teacher1));
        timetableSlotRepository.save(TimetableSlot.builder()
                .teachingAssignment(assignment1).dayOfWeek(2).period(1).room("ITEST-P101").build());

        TimetableSlot conflicting = TimetableSlot.builder()
                .teachingAssignment(TeachingAssignment.builder().id(assignment2.getId()).build())
                .dayOfWeek(2).period(1).room("ITEST-P102")
                .build();

        mockMvc.perform(post("/v1/timetable/slots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(conflicting)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("teacher")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createSlot_roomDoubleBooked_returns409() throws Exception {
        TeachingAssignment assignment1 = teachingAssignmentRepository.save(resolvedAssignment(classA, teacher1));
        TeachingAssignment assignment2 = teachingAssignmentRepository.save(resolvedAssignment(classB, teacher2));
        timetableSlotRepository.save(TimetableSlot.builder()
                .teachingAssignment(assignment1).dayOfWeek(2).period(1).room("ITEST-SHARED-ROOM").build());

        TimetableSlot conflicting = TimetableSlot.builder()
                .teachingAssignment(TeachingAssignment.builder().id(assignment2.getId()).build())
                .dayOfWeek(2).period(1).room("ITEST-SHARED-ROOM")
                .build();

        mockMvc.perform(post("/v1/timetable/slots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(conflicting)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("room")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createSlot_noConflictDifferentRoom_returns201() throws Exception {
        TeachingAssignment assignment1 = teachingAssignmentRepository.save(resolvedAssignment(classA, teacher1));
        TeachingAssignment assignment2 = teachingAssignmentRepository.save(resolvedAssignment(classB, teacher2));
        timetableSlotRepository.save(TimetableSlot.builder()
                .teachingAssignment(assignment1).dayOfWeek(2).period(1).room("ITEST-P101").build());

        TimetableSlot noConflict = TimetableSlot.builder()
                .teachingAssignment(TeachingAssignment.builder().id(assignment2.getId()).build())
                .dayOfWeek(2).period(1).room("ITEST-P999")
                .build();

        mockMvc.perform(post("/v1/timetable/slots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(noConflict)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createSlot_invalidDayOfWeek_returns400() throws Exception {
        TeachingAssignment assignment = teachingAssignmentRepository.save(resolvedAssignment(classA, teacher1));

        TimetableSlot invalid = TimetableSlot.builder()
                .teachingAssignment(TeachingAssignment.builder().id(assignment.getId()).build())
                .dayOfWeek(8).period(1).room("ITEST-P101")
                .build();

        mockMvc.perform(post("/v1/timetable/slots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getClassTimetable_returnsScheduledSlots() throws Exception {
        TeachingAssignment assignment = teachingAssignmentRepository.save(resolvedAssignment(classA, teacher1));
        timetableSlotRepository.save(TimetableSlot.builder()
                .teachingAssignment(assignment).dayOfWeek(3).period(2).room("ITEST-P101").build());

        mockMvc.perform(get("/v1/timetable/class/{classId}", classA.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].schoolClassLabel").value("ITEST-TT-10-A"))
                .andExpect(jsonPath("$[0].dayOfWeek").value(3));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getTeacherTimetable_returnsScheduledSlots() throws Exception {
        TeachingAssignment assignment = teachingAssignmentRepository.save(resolvedAssignment(classA, teacher1));
        timetableSlotRepository.save(TimetableSlot.builder()
                .teachingAssignment(assignment).dayOfWeek(4).period(3).room("ITEST-P101").build());

        mockMvc.perform(get("/v1/timetable/teacher/{teacherId}", teacher1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].teacherName").value("Integration Teacher"))
                .andExpect(jsonPath("$[0].dayOfWeek").value(4));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteSlot_succeeds() throws Exception {
        TeachingAssignment assignment = teachingAssignmentRepository.save(resolvedAssignment(classA, teacher1));
        TimetableSlot slot = timetableSlotRepository.save(TimetableSlot.builder()
                .teachingAssignment(assignment).dayOfWeek(5).period(4).room("ITEST-P101").build());

        mockMvc.perform(delete("/v1/timetable/slots/{id}", slot.getId()))
                .andExpect(status().isNoContent());
    }

    private TeachingAssignment resolvedAssignment(SchoolClass schoolClass, Staff teacher) {
        return TeachingAssignment.builder()
                .schoolClass(schoolClass)
                .subject(subject)
                .teacher(teacher)
                .semester(semester)
                .build();
    }
}
