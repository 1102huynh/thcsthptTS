package com.schoolmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.schoolmanagement.entity.EmploymentStatus;
import com.schoolmanagement.entity.Role;
import com.schoolmanagement.entity.Staff;
import com.schoolmanagement.entity.StaffPosition;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.repository.StaffRepository;
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

import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for /v1/staff - real Spring context + local MySQL via
 * the "test" profile, matching AuditLogIntegrationTest's own pattern.
 *
 * Written after fixing a real bug found live while wiring the frontend's
 * Staff Management page to this endpoint (Tuần 3 Ngày 3-4, Track
 * Frontend): StaffService.mapToDTO() never populated StaffDTO.user at all
 * (UserDTO.builder() was called nowhere in the backend), so every staff
 * row's name/email came back null - the frontend table just showed blank
 * cells. These tests guard against that regressing.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class StaffIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private StaffRepository staffRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private User adminUser;

    @BeforeEach
    void setUp() {
        adminUser = userRepository.save(User.builder()
                .username("itest.staff.admin").email("itest.staff.admin@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Integration").lastName("Admin").role(Role.ADMIN).enabled(true).build());
    }

    private RequestPostProcessor asUser(User user, String role) {
        return authentication(new UsernamePasswordAuthenticationToken(
                user, null, List.of(new SimpleGrantedAuthority("ROLE_" + role))));
    }

    private User saveTeacherUser(String suffix) {
        return userRepository.save(User.builder()
                .username("itest.staff.teacher" + suffix).email("itest.staff.teacher" + suffix + "@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Trần").lastName("Thị B").role(Role.TEACHER).enabled(true).build());
    }

    @Test
    void createStaff_responseIncludesUser() throws Exception {
        User teacherUser = saveTeacherUser("1");

        Map<String, Object> body = Map.of(
                "employeeId", "ITEST-EMP-1",
                "user", Map.of("id", teacherUser.getId()),
                "position", "TEACHER",
                "status", "ACTIVE");

        mockMvc.perform(post("/v1/staff")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .with(asUser(adminUser, "ADMIN")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.employeeId").value("ITEST-EMP-1"))
                .andExpect(jsonPath("$.user").exists())
                .andExpect(jsonPath("$.user.id").value(teacherUser.getId()))
                .andExpect(jsonPath("$.user.firstName").value("Trần"))
                .andExpect(jsonPath("$.user.lastName").value("Thị B"))
                .andExpect(jsonPath("$.user.email").value(teacherUser.getEmail()))
                // password must never leak into the DTO
                .andExpect(jsonPath("$.user.password").doesNotExist());
    }

    @Test
    void getAllStaff_listIncludesUserForEachRow() throws Exception {
        User teacherUser = saveTeacherUser("2");
        staffRepository.save(Staff.builder()
                .employeeId("ITEST-EMP-2").user(teacherUser)
                .position(StaffPosition.TEACHER).status(EmploymentStatus.ACTIVE).build());

        mockMvc.perform(get("/v1/staff").with(asUser(adminUser, "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.employeeId == 'ITEST-EMP-2')].user.email").value(teacherUser.getEmail()));
    }

    @Test
    void updateStaff_responseStillIncludesUser() throws Exception {
        User teacherUser = saveTeacherUser("3");
        Staff staff = staffRepository.save(Staff.builder()
                .employeeId("ITEST-EMP-3").user(teacherUser)
                .position(StaffPosition.TEACHER).department("Toán").status(EmploymentStatus.ACTIVE).build());

        Map<String, Object> update = Map.of(
                "employeeId", "ITEST-EMP-3",
                "position", "TEACHER",
                "department", "Vật lý",
                "status", "ACTIVE");

        mockMvc.perform(put("/v1/staff/{id}", staff.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update))
                        .with(asUser(adminUser, "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.department").value("Vật lý"))
                .andExpect(jsonPath("$.user.email").value(teacherUser.getEmail()));
    }

    @Test
    void getStaffById_asTeacher_redactsSalaryAndPii() throws Exception {
        User teacherUser = saveTeacherUser("5");
        Staff staff = staffRepository.save(Staff.builder()
                .employeeId("ITEST-EMP-5").user(teacherUser)
                .position(StaffPosition.TEACHER).department("Toán").status(EmploymentStatus.ACTIVE)
                .salary(18_500_000.0).address("123 Lê Lợi").city("Đà Nẵng")
                .emergencyContactName("Nguyễn Văn X").emergencyContactPhone("0900000000").build());

        // ADMIN sees the full record
        mockMvc.perform(get("/v1/staff/{id}", staff.getId()).with(asUser(adminUser, "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.salary").value(18_500_000.0))
                .andExpect(jsonPath("$.address").value("123 Lê Lợi"))
                .andExpect(jsonPath("$.emergencyContactPhone").value("0900000000"));

        // TEACHER sees name/position/department but salary + PII are nulled
        mockMvc.perform(get("/v1/staff/{id}", staff.getId()).with(asUser(teacherUser, "TEACHER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.department").value("Toán"))
                .andExpect(jsonPath("$.salary").doesNotExist())
                .andExpect(jsonPath("$.address").doesNotExist())
                .andExpect(jsonPath("$.city").doesNotExist())
                .andExpect(jsonPath("$.emergencyContactName").doesNotExist())
                .andExpect(jsonPath("$.emergencyContactPhone").doesNotExist());
    }

    @Test
    void getAllStaff_asTeacher_redactsSalaryForEveryRow() throws Exception {
        User teacherUser = saveTeacherUser("6");
        staffRepository.save(Staff.builder()
                .employeeId("ITEST-EMP-6").user(teacherUser)
                .position(StaffPosition.TEACHER).status(EmploymentStatus.ACTIVE)
                .salary(20_000_000.0).address("456 Trần Phú").build());

        mockMvc.perform(get("/v1/staff").with(asUser(teacherUser, "TEACHER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.employeeId == 'ITEST-EMP-6')].salary").isEmpty())
                .andExpect(jsonPath("$[?(@.employeeId == 'ITEST-EMP-6')].address").isEmpty());
    }

    @Test
    void createStaff_asTeacher_returns403() throws Exception {
        User teacherUser = saveTeacherUser("4");

        Map<String, Object> body = Map.of(
                "employeeId", "ITEST-EMP-4",
                "user", Map.of("id", teacherUser.getId()),
                "position", "TEACHER",
                "status", "ACTIVE");

        mockMvc.perform(post("/v1/staff")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .with(asUser(teacherUser, "TEACHER")))
                .andExpect(status().isForbidden());
    }
}
