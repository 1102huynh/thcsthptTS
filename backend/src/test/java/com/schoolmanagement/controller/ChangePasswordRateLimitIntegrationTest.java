package com.schoolmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.schoolmanagement.entity.Role;
import com.schoolmanagement.entity.User;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Dedicated test for ChangePasswordRateLimitFilter's 429 behavior — isolated
 * in its own class (like ForgotPasswordRateLimitIntegrationTest /
 * AdmissionRateLimitIntegrationTest) so a low max-requests override doesn't
 * starve other tests that legitimately call this endpoint more than twice.
 *
 * Without this filter, a caller holding a valid-but-stolen JWT could brute
 * force `currentPassword` with unlimited attempts (see
 * ChangePasswordRateLimitFilter's Javadoc) — every request below
 * deliberately sends the WRONG current password, so a 401 on each of the
 * first two confirms the filter isn't just coincidentally blocking on the
 * password check.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "app.auth.change-password-rate-limit.max-requests=2")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
// Unlike ForgotPasswordRateLimitIntegrationTest (which never writes a row),
// setUp() here persists a User — @Transactional rolls that back per test so
// reruns don't collide on the unique username/email constraint.
@Transactional
class ChangePasswordRateLimitIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private User self;

    @BeforeEach
    void setUp() {
        self = userRepository.save(User.builder()
                .username("itest.pwd.rl").email("itest.pwd.rl@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Rate").lastName("Limit").role(Role.STUDENT).enabled(true).build());
    }

    private RequestPostProcessor asSelf() {
        return authentication(new UsernamePasswordAuthenticationToken(
                self, null, List.of(new SimpleGrantedAuthority("ROLE_" + self.getRole().name()))));
    }

    private String wrongPasswordPayload() throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "currentPassword", "TotallyWrongPassword!",
                "newPassword", "N3wStr0ngPassw0rd!"));
    }

    @Test
    void thirdAttemptFromSameUserWithinWindow_returns429() throws Exception {
        mockMvc.perform(post("/v1/users/me/change-password")
                        .contentType(MediaType.APPLICATION_JSON).content(wrongPasswordPayload()).with(asSelf()))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/v1/users/me/change-password")
                        .contentType(MediaType.APPLICATION_JSON).content(wrongPasswordPayload()).with(asSelf()))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/v1/users/me/change-password")
                        .contentType(MediaType.APPLICATION_JSON).content(wrongPasswordPayload()).with(asSelf()))
                .andExpect(status().is(429));
    }
}
