package com.schoolmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.schoolmanagement.entity.EmploymentStatus;
import com.schoolmanagement.entity.Notification;
import com.schoolmanagement.entity.NotificationChannel;
import com.schoolmanagement.entity.NotificationTargetType;
import com.schoolmanagement.entity.ParentRelationship;
import com.schoolmanagement.entity.ParentStudentRelation;
import com.schoolmanagement.entity.Role;
import com.schoolmanagement.entity.SchoolClass;
import com.schoolmanagement.entity.Staff;
import com.schoolmanagement.entity.StaffPosition;
import com.schoolmanagement.entity.Student;
import com.schoolmanagement.entity.StudentStatus;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.repository.NotificationRecipientRepository;
import com.schoolmanagement.repository.ParentStudentRelationRepository;
import com.schoolmanagement.repository.SchoolClassRepository;
import com.schoolmanagement.repository.StaffRepository;
import com.schoolmanagement.repository.StudentRepository;
import com.schoolmanagement.repository.UserRepository;
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

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for /v1/parents and /v1/notifications — real Spring
 * context + local MySQL via the "test" profile. Each test rolls back
 * (@Transactional). No real SMTP is configured in the test profile, so
 * EMAIL sends are expected to fail gracefully (status FAILED, not a crash)
 * — that IS the behavior under test, matching the real local-dev state.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ParentNotificationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private SchoolClassRepository schoolClassRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private StaffRepository staffRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ParentStudentRelationRepository parentStudentRelationRepository;
    @Autowired
    private NotificationRecipientRepository notificationRecipientRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private Student student1;
    private Student student2;
    private Student student3; // no linked parent
    private User parentUser1;
    private User parentUser2;
    private User adminUser;
    private User teacherUser;

    @BeforeEach
    void setUp() {
        SchoolClass schoolClass = schoolClassRepository.save(SchoolClass.builder()
                .className("ITEST-PN-10").section("A").academicYear("2099-2100").build());

        adminUser = userRepository.save(User.builder()
                .username("itest.pn.admin").email("itest.pn.admin@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Integration").lastName("Admin").role(Role.ADMIN).enabled(true).build());

        teacherUser = userRepository.save(User.builder()
                .username("itest.pn.teacher").email("itest.pn.teacher@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Integration").lastName("Teacher").role(Role.TEACHER).enabled(true).build());
        staffRepository.save(Staff.builder()
                .employeeId("ITEST-PN-EMP").user(teacherUser)
                .position(StaffPosition.TEACHER).status(EmploymentStatus.ACTIVE).build());

        User studentUser1 = userRepository.save(User.builder()
                .username("itest.pn.student1").email("itest.pn.student1@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Integration").lastName("Student1").role(Role.STUDENT).enabled(true).build());
        student1 = studentRepository.save(Student.builder()
                .rollNumber("ITEST-PN-ROLL-1").admissionNumber("ITEST-PN-ADM-1")
                .user(studentUser1).status(StudentStatus.ACTIVE)
                .className(schoolClass.getClassName()).section(schoolClass.getSection()).build());

        User studentUser2 = userRepository.save(User.builder()
                .username("itest.pn.student2").email("itest.pn.student2@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Integration").lastName("Student2").role(Role.STUDENT).enabled(true).build());
        student2 = studentRepository.save(Student.builder()
                .rollNumber("ITEST-PN-ROLL-2").admissionNumber("ITEST-PN-ADM-2")
                .user(studentUser2).status(StudentStatus.ACTIVE)
                .className(schoolClass.getClassName()).section(schoolClass.getSection()).build());

        User studentUser3 = userRepository.save(User.builder()
                .username("itest.pn.student3").email("itest.pn.student3@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Integration").lastName("Student3").role(Role.STUDENT).enabled(true).build());
        student3 = studentRepository.save(Student.builder()
                .rollNumber("ITEST-PN-ROLL-3").admissionNumber("ITEST-PN-ADM-3")
                .user(studentUser3).status(StudentStatus.ACTIVE)
                .className(schoolClass.getClassName()).section(schoolClass.getSection()).build());

        parentUser1 = userRepository.save(User.builder()
                .username("itest.pn.parent1").email("itest.pn.parent1@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Integration").lastName("Parent1").role(Role.PARENT).enabled(true).build());
        parentStudentRelationRepository.save(ParentStudentRelation.builder()
                .parent(parentUser1).student(student1)
                .relationship(ParentRelationship.CHA).isPrimaryContact(true).build());

        parentUser2 = userRepository.save(User.builder()
                .username("itest.pn.parent2").email("itest.pn.parent2@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Integration").lastName("Parent2").role(Role.PARENT).enabled(true).build());
        parentStudentRelationRepository.save(ParentStudentRelation.builder()
                .parent(parentUser2).student(student2)
                .relationship(ParentRelationship.ME).isPrimaryContact(true).build());
    }

    private RequestPostProcessor asUser(User user, String role) {
        return authentication(new UsernamePasswordAuthenticationToken(
                user, null, List.of(new SimpleGrantedAuthority("ROLE_" + role))));
    }

    // ---- /v1/parents ----

    @Test
    @WithMockUser(roles = "ADMIN")
    void linkChild_duplicate_returns409() throws Exception {
        mockMvc.perform(post("/v1/parents/{parentId}/children/{studentId}", parentUser1.getId(), student1.getId())
                        .param("relationship", "CHA"))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void linkChild_nonParentUser_returns400() throws Exception {
        mockMvc.perform(post("/v1/parents/{parentId}/children/{studentId}", teacherUser.getId(), student1.getId())
                        .param("relationship", "CHA"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void linkChild_nonexistentStudent_returns404() throws Exception {
        mockMvc.perform(post("/v1/parents/{parentId}/children/{studentId}", parentUser1.getId(), 9_999_999L)
                        .param("relationship", "CHA"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void unlinkChild_removesRelation() throws Exception {
        mockMvc.perform(delete("/v1/parents/{parentId}/children/{studentId}", parentUser1.getId(), student1.getId()))
                .andExpect(status().isNoContent());

        Assertions.assertTrue(parentStudentRelationRepository.findByParent(parentUser1).isEmpty());
    }

    @Test
    void getChildren_asOwnParent_returns200() throws Exception {
        mockMvc.perform(get("/v1/parents/{parentId}/children", parentUser1.getId())
                        .with(asUser(parentUser1, "PARENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].studentId").value(student1.getId()));
    }

    @Test
    void getChildren_asDifferentParent_returns403() throws Exception {
        mockMvc.perform(get("/v1/parents/{parentId}/children", parentUser1.getId())
                        .with(asUser(parentUser2, "PARENT")))
                .andExpect(status().isForbidden());
    }

    // ---- /v1/notifications ----

    private String notificationPayload(NotificationTargetType targetType, Long targetId, NotificationChannel channel) throws Exception {
        Notification request = Notification.builder()
                .title("ITEST title").content("ITEST content")
                .targetType(targetType).targetId(targetId).channel(channel)
                .build();
        return objectMapper.writeValueAsString(request);
    }

    @Test
    void createAndSend_appChannelToStudent_deliversToLinkedParentOnly() throws Exception {
        mockMvc.perform(post("/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(notificationPayload(NotificationTargetType.STUDENT, student1.getId(), NotificationChannel.APP))
                        .with(asUser(adminUser, "ADMIN")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SENT"))
                .andExpect(jsonPath("$.recipientCount").value(1))
                .andExpect(jsonPath("$.deliveredCount").value(1));

        List<com.schoolmanagement.entity.NotificationRecipient> rows =
                notificationRecipientRepository.findByRecipientOrderByCreatedAtDesc(parentUser1);
        Assertions.assertEquals(1, rows.size());
        Assertions.assertNotNull(rows.get(0).getDeliveredAt());
    }

    @Test
    void createAndSend_emailChannel_reportsFailedWithoutCrashing() throws Exception {
        // No SMTP server is configured in the test profile - this asserts
        // graceful degradation (status FAILED), not a 500.
        mockMvc.perform(post("/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(notificationPayload(NotificationTargetType.STUDENT, student1.getId(), NotificationChannel.EMAIL))
                        .with(asUser(adminUser, "ADMIN")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.deliveredCount").value(0));
    }

    @Test
    void createAndSend_smsChannel_returns501() throws Exception {
        // The rollback-nothing-persisted behavior (NotificationChannelUnavailableException
        // propagates uncaught -> @Transactional rolls back the whole call) was
        // live-verified against a real standalone request/transaction (curl,
        // then a direct MySQL count) - not re-asserted here via
        // notificationRepository.count(), since this test method's own
        // @Transactional wraps the MockMvc call in the SAME not-yet-rolled-back
        // transaction, so the just-inserted row is still visible to a read in
        // that same transaction regardless of the eventual rollback at
        // teardown. The 501 here confirms the exception actually propagated.
        mockMvc.perform(post("/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(notificationPayload(NotificationTargetType.STUDENT, student1.getId(), NotificationChannel.SMS))
                        .with(asUser(adminUser, "ADMIN")))
                .andExpect(status().is(501));
    }

    @Test
    void createAndSend_classTarget_resolvesBothLinkedParentsNotUnlinkedStudent() throws Exception {
        SchoolClass schoolClass = schoolClassRepository
                .findByClassNameAndSection("ITEST-PN-10", "A").orElseThrow();

        mockMvc.perform(post("/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(notificationPayload(NotificationTargetType.CLASS, schoolClass.getId(), NotificationChannel.APP))
                        .with(asUser(adminUser, "ADMIN")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.recipientCount").value(2)) // student1's + student2's parent; student3 has none
                .andExpect(jsonPath("$.deliveredCount").value(2));
    }

    @Test
    void createAndSend_allParentsTarget_resolvesEveryParentAccount() throws Exception {
        mockMvc.perform(post("/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(notificationPayload(NotificationTargetType.ALL_PARENTS, null, NotificationChannel.APP))
                        .with(asUser(adminUser, "ADMIN")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.recipientCount").value(org.hamcrest.Matchers.greaterThanOrEqualTo(2)));
    }

    @Test
    void createAndSend_studentWithNoLinkedParent_returns404() throws Exception {
        mockMvc.perform(post("/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(notificationPayload(NotificationTargetType.STUDENT, student3.getId(), NotificationChannel.APP))
                        .with(asUser(adminUser, "ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void createAndSend_asStudent_returns403() throws Exception {
        mockMvc.perform(post("/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(notificationPayload(NotificationTargetType.ALL_PARENTS, null, NotificationChannel.APP)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMyNotifications_returnsOwnRecipientRowsOnly() throws Exception {
        mockMvc.perform(post("/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(notificationPayload(NotificationTargetType.STUDENT, student1.getId(), NotificationChannel.APP))
                        .with(asUser(adminUser, "ADMIN")))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/v1/notifications/my").with(asUser(parentUser1, "PARENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("ITEST title"));

        mockMvc.perform(get("/v1/notifications/my").with(asUser(parentUser2, "PARENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void markAsRead_own_succeedsAndSetsReadAt() throws Exception {
        String response = mockMvc.perform(post("/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(notificationPayload(NotificationTargetType.STUDENT, student1.getId(), NotificationChannel.APP))
                        .with(asUser(adminUser, "ADMIN")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long notificationId = objectMapper.readTree(response).get("id").asLong();

        Long recipientRowId = notificationRecipientRepository.findByRecipientOrderByCreatedAtDesc(parentUser1)
                .stream()
                .filter(row -> row.getNotification().getId().equals(notificationId))
                .findFirst().orElseThrow().getId();

        mockMvc.perform(put("/v1/notifications/{recipientId}/read", recipientRowId)
                        .with(asUser(parentUser1, "PARENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.readAt").exists());
    }

    @Test
    void markAsRead_notOwn_returns403() throws Exception {
        String response = mockMvc.perform(post("/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(notificationPayload(NotificationTargetType.STUDENT, student1.getId(), NotificationChannel.APP))
                        .with(asUser(adminUser, "ADMIN")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long notificationId = objectMapper.readTree(response).get("id").asLong();

        Long recipientRowId = notificationRecipientRepository.findByRecipientOrderByCreatedAtDesc(parentUser1)
                .stream()
                .filter(row -> row.getNotification().getId().equals(notificationId))
                .findFirst().orElseThrow().getId();

        mockMvc.perform(put("/v1/notifications/{recipientId}/read", recipientRowId)
                        .with(asUser(parentUser2, "PARENT")))
                .andExpect(status().isForbidden());
    }
}
