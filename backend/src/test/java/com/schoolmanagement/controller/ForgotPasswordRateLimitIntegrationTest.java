package com.schoolmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.schoolmanagement.dto.ForgotPasswordRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Dedicated test for ForgotPasswordRateLimitFilter's 429 behavior, isolated
 * from other tests hitting /v1/auth/forgot-password for the same reason
 * AdmissionRateLimitIntegrationTest is isolated from AdmissionIntegrationTest
 * — see that class's Javadoc.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "app.auth.forgot-password-rate-limit.max-requests=2")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ForgotPasswordRateLimitIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private String payload(String email) throws Exception {
        return objectMapper.writeValueAsString(ForgotPasswordRequest.builder().email(email).build());
    }

    @Test
    void thirdRequestFromSameIpWithinWindow_returns429() throws Exception {
        // Doesn't matter that none of these emails exist - the rate limiter
        // runs before the (also-generic-either-way) service logic.
        mockMvc.perform(post("/v1/auth/forgot-password").contentType(MediaType.APPLICATION_JSON).content(payload("itest.rl1@school.com")))
                .andExpect(status().isOk());
        mockMvc.perform(post("/v1/auth/forgot-password").contentType(MediaType.APPLICATION_JSON).content(payload("itest.rl2@school.com")))
                .andExpect(status().isOk());
        mockMvc.perform(post("/v1/auth/forgot-password").contentType(MediaType.APPLICATION_JSON).content(payload("itest.rl3@school.com")))
                .andExpect(status().is(429));
    }
}
