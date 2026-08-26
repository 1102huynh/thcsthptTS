package com.schoolmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.schoolmanagement.dto.SubmitAdmissionRequest;
import com.schoolmanagement.repository.AdmissionApplicationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Dedicated test for AdmissionRateLimitFilter's 429 behavior, isolated from
 * AdmissionIntegrationTest's Spring context: the filter's IP->timestamps map
 * is a singleton bean, so a low rate limit here would otherwise poison every
 * other test posting to /v1/admissions in the same shared context.
 * {@code @DirtiesContext} forces a fresh context (and thus a fresh filter
 * instance) for whatever runs after this class, so the low override doesn't
 * leak anywhere else either.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "app.admissions.rate-limit.max-requests=2")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AdmissionRateLimitIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AdmissionApplicationRepository admissionApplicationRepository;

    // No @Transactional here (would defeat the point — the rate limiter
    // needs to see genuinely separate requests) - clean up manually instead.
    @AfterEach
    void cleanUp() {
        admissionApplicationRepository.findAll().stream()
                .filter(a -> "ITEST RateLimit".equals(a.getApplicantName()))
                .forEach(admissionApplicationRepository::delete);
    }

    private String payload(String phone) throws Exception {
        SubmitAdmissionRequest request = SubmitAdmissionRequest.builder()
                .applicantName("ITEST RateLimit")
                .dateOfBirth(LocalDate.of(2014, 5, 20))
                .contactPhone(phone)
                .desiredGradeLevel(10)
                .build();
        return objectMapper.writeValueAsString(request);
    }

    @Test
    void thirdSubmissionFromSameIpWithinWindow_returns429() throws Exception {
        // MockMvc's default client IP is constant across calls in one test -
        // exactly what's needed to exercise the per-IP bucket deterministically.
        mockMvc.perform(post("/v1/admissions").contentType(MediaType.APPLICATION_JSON).content(payload("0912340001")))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/v1/admissions").contentType(MediaType.APPLICATION_JSON).content(payload("0912340002")))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/v1/admissions").contentType(MediaType.APPLICATION_JSON).content(payload("0912340003")))
                .andExpect(status().is(429));
    }
}
