package com.schoolmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Dedicated test for LoginRateLimitFilter's 429 behavior, isolated in its
 * own class (like ForgotPasswordRateLimitIntegrationTest /
 * AdmissionRateLimitIntegrationTest) so a low max-requests override doesn't
 * starve other tests that legitimately call /v1/auth/login. Every request
 * below deliberately uses a wrong password - a 401 on each of the first two
 * confirms the filter isn't just coincidentally blocking on the credentials
 * check itself.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "app.auth.login-rate-limit.max-requests=2")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class LoginRateLimitIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private String wrongCredentialsPayload() throws Exception {
        return objectMapper.writeValueAsString(Map.of("username", "no-such-user", "password", "WrongPassword!"));
    }

    @Test
    void thirdLoginAttemptFromSameIpWithinWindow_returns429() throws Exception {
        mockMvc.perform(post("/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(wrongCredentialsPayload()))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(wrongCredentialsPayload()))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(wrongCredentialsPayload()))
                .andExpect(status().is(429));
    }
}
