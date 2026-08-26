package com.schoolmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.schoolmanagement.dto.ApproveAndCreateRequest;
import com.schoolmanagement.dto.SubmitAdmissionRequest;
import com.schoolmanagement.dto.UpdateAdmissionStatusRequest;
import com.schoolmanagement.entity.AdmissionApplication;
import com.schoolmanagement.entity.AdmissionStatus;
import com.schoolmanagement.entity.Role;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.repository.AdmissionApplicationRepository;
import com.schoolmanagement.repository.StudentRepository;
import com.schoolmanagement.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for /v1/admissions — real Spring context + local MySQL
 * via the "test" profile, {@literal @}Transactional rollback per test.
 * Rate-limiting itself is tested separately (AdmissionRateLimitIntegrationTest)
 * since AdmissionRateLimitFilter's in-memory counters are a singleton shared
 * across every test in this context — application-test.yml raises the limit
 * high enough that these functional tests never trip it as a side effect.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdmissionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AdmissionApplicationRepository admissionApplicationRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private UserRepository userRepository;

    /**
     * @WithMockUser's SecurityContext lives on the test method's own thread
     * (ThreadLocal) — invisible to the pool threads the concurrency test
     * below dispatches MockMvc calls from. Attaching authentication directly
     * to each request like this works regardless of which thread executes it.
     * Transient (unsaved) principal — fine wherever the endpoint only checks
     * @PreAuthorize and never persists a reference to the caller (approve-and-
     * create takes no Authentication at all). Endpoints that do persist the
     * caller (e.g. updateStatus's reviewedBy) need asUser() with a real,
     * already-saved User instead — a transient one has no id to put in the FK.
     */
    private RequestPostProcessor asAdmin() {
        User admin = User.builder().role(Role.ADMIN).username("itest-admin-principal").build();
        return authentication(new UsernamePasswordAuthenticationToken(
                admin, null, java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
    }

    private RequestPostProcessor asUser(User user, String role) {
        return authentication(new UsernamePasswordAuthenticationToken(
                user, null, java.util.List.of(new SimpleGrantedAuthority("ROLE_" + role))));
    }

    private String submitPayload(String phone, int gradeLevel) throws Exception {
        SubmitAdmissionRequest request = SubmitAdmissionRequest.builder()
                .applicantName("Nguyen Van ITEST")
                .dateOfBirth(LocalDate.of(2014, 5, 20))
                .contactPhone(phone)
                .desiredGradeLevel(gradeLevel)
                .priorSchool("ITEST THCS")
                .build();
        return objectMapper.writeValueAsString(request);
    }

    @Test
    void submit_persistsAsPendingWithServerControlledFields() throws Exception {
        mockMvc.perform(post("/v1/admissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submitPayload("0912345671", 10)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.submittedAt").exists())
                .andExpect(jsonPath("$.reviewedById").doesNotExist());
    }

    @Test
    void submit_invalidPhone_returns400() throws Exception {
        mockMvc.perform(post("/v1/admissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submitPayload("not-a-phone", 10)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void submit_gradeLevelOutOfRange_returns400() throws Exception {
        mockMvc.perform(post("/v1/admissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submitPayload("0912345672", 13)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void submit_applicantNameTooLong_returns400() throws Exception {
        // varchar(255) on admission_applications.applicant_name - without
        // @Size(max=255) on SubmitAdmissionRequest this would instead hit the
        // DB column limit and surface as a masked 500.
        SubmitAdmissionRequest request = SubmitAdmissionRequest.builder()
                .applicantName("A".repeat(256))
                .dateOfBirth(LocalDate.of(2014, 5, 20))
                .contactPhone("0912345674")
                .desiredGradeLevel(10)
                .build();

        mockMvc.perform(post("/v1/admissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void submit_ignoresClientSuppliedStatus() throws Exception {
        // SubmitAdmissionRequest has no status field at all - a raw JSON body
        // trying to sneak one in has nothing to bind to.
        String json = "{\"applicantName\":\"Sneaky\",\"dateOfBirth\":\"2014-05-20\","
                + "\"contactPhone\":\"0912345673\",\"desiredGradeLevel\":10,\"status\":\"APPROVED\"}";

        mockMvc.perform(post("/v1/admissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getById_nonexistent_returns404() throws Exception {
        mockMvc.perform(get("/v1/admissions/{id}", 9_999_999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAll_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/v1/admissions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void statusFilter_returnsOnlyMatching() throws Exception {
        AdmissionApplication pending = admissionApplicationRepository.save(newApplication("ITEST Pending"));
        AdmissionApplication rejected = admissionApplicationRepository.save(newApplication("ITEST Rejected"));
        rejected.setStatus(AdmissionStatus.REJECTED);
        admissionApplicationRepository.save(rejected);

        mockMvc.perform(get("/v1/admissions").param("status", "REJECTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + pending.getId() + ")]").doesNotExist())
                .andExpect(jsonPath("$[?(@.id == " + rejected.getId() + ")]").exists());
    }

    @Test
    void updateStatus_setsReviewerAndNote() throws Exception {
        // AdmissionController.updateStatus casts authentication.getPrincipal()
        // to our own User (persisted as reviewedBy) - @WithMockUser's principal
        // is Spring Security's own User type, and a transient (unsaved) one has
        // no id for the FK - needs a real, already-saved User.
        User reviewer = userRepository.save(User.builder()
                .username("itest.admission.reviewer").email("itest.admission.reviewer@school.com")
                .password("{noop}Str0ngPassw0rd!")
                .firstName("Integration").lastName("Reviewer").role(Role.ADMIN).enabled(true).build());
        AdmissionApplication application = admissionApplicationRepository.save(newApplication("ITEST Review"));

        UpdateAdmissionStatusRequest request = UpdateAdmissionStatusRequest.builder()
                .status(AdmissionStatus.REVIEWING).note("Đang xem xét").build();

        mockMvc.perform(put("/v1/admissions/{id}/status", application.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(asUser(reviewer, "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REVIEWING"))
                .andExpect(jsonPath("$.reviewedByName").value("Integration Reviewer"))
                .andExpect(jsonPath("$.note").value("Đang xem xét"));
    }

    @Test
    void updateStatus_omittingNote_keepsExistingNote() throws Exception {
        // A later reviewer just changing the status (note left null on the
        // request) must not silently wipe out a previous reviewer's note -
        // only an explicit empty string should clear it.
        User reviewer = userRepository.save(User.builder()
                .username("itest.admission.reviewer2").email("itest.admission.reviewer2@school.com")
                .password("{noop}Str0ngPassw0rd!")
                .firstName("Integration").lastName("Reviewer2").role(Role.ADMIN).enabled(true).build());
        AdmissionApplication application = admissionApplicationRepository.save(newApplication("ITEST KeepNote"));

        UpdateAdmissionStatusRequest first = UpdateAdmissionStatusRequest.builder()
                .status(AdmissionStatus.REVIEWING).note("Cần bổ sung hồ sơ").build();
        mockMvc.perform(put("/v1/admissions/{id}/status", application.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(first))
                        .with(asUser(reviewer, "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.note").value("Cần bổ sung hồ sơ"));

        UpdateAdmissionStatusRequest second = UpdateAdmissionStatusRequest.builder()
                .status(AdmissionStatus.APPROVED).build(); // note omitted (null)
        mockMvc.perform(put("/v1/admissions/{id}/status", application.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(second))
                        .with(asUser(reviewer, "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.note").value("Cần bổ sung hồ sơ"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void approveAndCreate_beforeApproved_returns400() throws Exception {
        AdmissionApplication application = admissionApplicationRepository.save(newApplication("ITEST NotApproved"));

        mockMvc.perform(post("/v1/admissions/{id}/approve-and-create", application.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approveRequest("itest.notapproved"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void approveAndCreate_persistsUserAndStudentFromApplicationData() throws Exception {
        AdmissionApplication application = admissionApplicationRepository.save(approvedApplication("Tran Thi ITEST"));

        mockMvc.perform(post("/v1/admissions/{id}/approve-and-create", application.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approveRequest("itest.approved1"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("itest.approved1"))
                .andExpect(jsonPath("$.rollNumber").value("ITEST-ROLL-1"));

        var user = userRepository.findByUsername("itest.approved1").orElseThrow();
        Assertions.assertEquals("Tran", user.getFirstName());
        Assertions.assertEquals("Thi ITEST", user.getLastName());

        var student = studentRepository.findByRollNumber("ITEST-ROLL-1").orElseThrow();
        Assertions.assertEquals(application.getDateOfBirth(), student.getDateOfBirth());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void approveAndCreate_secondAttempt_returns409() throws Exception {
        AdmissionApplication application = admissionApplicationRepository.save(approvedApplication("ITEST Twice"));

        mockMvc.perform(post("/v1/admissions/{id}/approve-and-create", application.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approveRequest("itest.twice1"))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/v1/admissions/{id}/approve-and-create", application.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approveRequest("itest.twice2"))))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void approveAndCreate_duplicateUsername_returns409() throws Exception {
        AdmissionApplication application = admissionApplicationRepository.save(approvedApplication("ITEST DupUser"));
        ApproveAndCreateRequest request = approveRequest("admin"); // seeded username, already exists

        mockMvc.perform(post("/v1/admissions/{id}/approve-and-create", application.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    /**
     * Exercises the DataIntegrityViolationException catch added around the
     * User/Student saves in approveAndCreate() - a cross-application
     * rollNumber collision that the existsByRollNumber() pre-check can't
     * rule out under concurrent requests (TOCTOU), distinct from the
     * @Version guard below which only protects the SAME application against
     * a double-click. Two DIFFERENT already-APPROVED applications race to
     * claim the same rollNumber; sequential calls can't reproduce this
     * (the first save would already be visible to the second's
     * existsByRollNumber() pre-check within the same transaction), so this
     * needs the same NOT_SUPPORTED + real-thread setup as
     * approveAndCreate_concurrentCalls_onlyOneSucceeds.
     */
    @Test
    @org.springframework.transaction.annotation.Transactional(propagation = org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
    void approveAndCreate_duplicateRollNumberAcrossApplications_returns409() throws Exception {
        AdmissionApplication first = admissionApplicationRepository.save(approvedApplication("ITEST RollA"));
        AdmissionApplication second = admissionApplicationRepository.save(approvedApplication("ITEST RollB"));
        Long firstId = first.getId();
        Long secondId = second.getId();
        String sharedRollNumber = "ITEST-ROLL-RACE";

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch go = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger();
        java.util.List<String> usernamesToCleanUp = new java.util.concurrent.CopyOnWriteArrayList<>();

        java.util.function.Function<Long, Runnable> attempt = applicationId -> () -> {
            try {
                String suffix = "itest.rolldup." + Thread.currentThread().getId();
                usernamesToCleanUp.add(suffix);
                ApproveAndCreateRequest request = ApproveAndCreateRequest.builder()
                        .username(suffix)
                        .email(suffix + "@school.com")
                        .password("Str0ngPassw0rd!")
                        .rollNumber(sharedRollNumber) // same across both applications - the race
                        .admissionNumber("ADM-" + suffix)
                        .build();
                ready.countDown();
                go.await();
                int status = mockMvc.perform(post("/v1/admissions/{id}/approve-and-create", applicationId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(asAdmin()))
                        .andReturn().getResponse().getStatus();
                if (status == 201) {
                    successCount.incrementAndGet();
                }
            } catch (Exception ignored) {
                // A losing thread may also surface as an exception depending on
                // timing - the success-count assertion below is what matters.
            }
        };

        try {
            pool.submit(attempt.apply(firstId));
            pool.submit(attempt.apply(secondId));
            ready.await();
            go.countDown();
            pool.shutdown();
            pool.awaitTermination(10, TimeUnit.SECONDS);

            Assertions.assertEquals(1, successCount.get(),
                    "exactly one of two applications racing for the same rollNumber should succeed");
        } finally {
            admissionApplicationRepository.deleteById(firstId);
            admissionApplicationRepository.deleteById(secondId);
            studentRepository.findByRollNumber(sharedRollNumber).ifPresent(studentRepository::delete);
            usernamesToCleanUp.forEach(username ->
                    userRepository.findByUsername(username).ifPresent(userRepository::delete));
        }
    }

    /**
     * The concurrency guard this exercises (optimistic @Version lock on
     * AdmissionApplication) was a real review finding: without it, two
     * concurrent approve-and-create calls for the same application could
     * both pass the "no student created yet" check and each create their
     * own separate User+Student.
     *
     * <p>Deliberately NOT_SUPPORTED (opting out of the class's @Transactional):
     * two MockMvc calls sharing this test method's own transaction would
     * share one Hibernate session too, so the second call would see the
     * first call's in-memory-updated entity directly from the session cache
     * instead of genuinely racing against it in a separate transaction —
     * that would only re-prove the ordinary "already created" business
     * check (see approveAndCreate_secondAttempt_returns409 above), not the
     * @Version guard. Suspending the transaction lets each MockMvc call
     * commit for real on its own pooled thread, so the two really race —
     * which also means nothing here auto-rolls-back, hence the manual
     * cleanup at the end. No class-level @WithMockUser here either — its
     * SecurityContext has the same thread-visibility problem, so each
     * request attaches its own authentication directly (see asAdmin()).
     */
    @Test
    @org.springframework.transaction.annotation.Transactional(propagation = org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
    void approveAndCreate_concurrentCalls_onlyOneSucceeds() throws Exception {
        AdmissionApplication application = admissionApplicationRepository.save(approvedApplication("ITEST Concurrent"));
        Long applicationId = application.getId();

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch go = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger();
        java.util.List<String> usernamesToCleanUp = new java.util.concurrent.CopyOnWriteArrayList<>();
        java.util.List<String> rollNumbersToCleanUp = new java.util.concurrent.CopyOnWriteArrayList<>();

        Runnable attempt = () -> {
            try {
                // Distinct username/rollNumber/admissionNumber per thread so the
                // only possible conflict is "this application already has a
                // student" (the @Version guard under test) - not an incidental
                // duplicate-rollNumber unique-constraint race, which would
                // surface as an unrelated masked 500 instead of the 409 this
                // test is actually checking for.
                String suffix = "itest.concurrent." + Thread.currentThread().getId();
                usernamesToCleanUp.add(suffix);
                rollNumbersToCleanUp.add("ROLL-" + suffix);
                ApproveAndCreateRequest request = ApproveAndCreateRequest.builder()
                        .username(suffix)
                        .email(suffix + "@school.com")
                        .password("Str0ngPassw0rd!")
                        .rollNumber("ROLL-" + suffix)
                        .admissionNumber("ADM-" + suffix)
                        .build();
                ready.countDown();
                go.await();
                int status = mockMvc.perform(post("/v1/admissions/{id}/approve-and-create", applicationId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(asAdmin()))
                        .andReturn().getResponse().getStatus();
                if (status == 201) {
                    successCount.incrementAndGet();
                }
            } catch (Exception ignored) {
                // A losing thread may also surface as an exception depending on
                // timing - the success-count assertion below is what matters.
            }
        };

        try {
            pool.submit(attempt);
            pool.submit(attempt);
            ready.await();
            go.countDown();
            pool.shutdown();
            pool.awaitTermination(10, TimeUnit.SECONDS);

            Assertions.assertEquals(1, successCount.get(), "exactly one concurrent approve-and-create call should succeed");
        } finally {
            // The application's created_student_id FK must be cleared before the
            // winning Student it points to can be deleted.
            admissionApplicationRepository.deleteById(applicationId);
            rollNumbersToCleanUp.forEach(roll ->
                    studentRepository.findByRollNumber(roll).ifPresent(studentRepository::delete));
            usernamesToCleanUp.forEach(username ->
                    userRepository.findByUsername(username).ifPresent(userRepository::delete));
        }
    }

    private AdmissionApplication newApplication(String name) {
        return AdmissionApplication.builder()
                .applicantName(name)
                .dateOfBirth(LocalDate.of(2014, 5, 20))
                .contactPhone("0912345670")
                .desiredGradeLevel(10)
                .status(AdmissionStatus.PENDING)
                .submittedAt(java.time.LocalDateTime.now())
                .build();
    }

    private AdmissionApplication approvedApplication(String name) {
        AdmissionApplication application = newApplication(name);
        application.setStatus(AdmissionStatus.APPROVED);
        return application;
    }

    private ApproveAndCreateRequest approveRequest(String username) {
        return ApproveAndCreateRequest.builder()
                .username(username)
                .email(username + "@school.com")
                .password("Str0ngPassw0rd!")
                .rollNumber("ITEST-ROLL-1")
                .admissionNumber("ITEST-ADM-1")
                .build();
    }
}
