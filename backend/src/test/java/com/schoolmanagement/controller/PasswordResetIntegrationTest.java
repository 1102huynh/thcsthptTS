package com.schoolmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.schoolmanagement.dto.ForgotPasswordRequest;
import com.schoolmanagement.dto.ResetPasswordRequest;
import com.schoolmanagement.entity.PasswordResetToken;
import com.schoolmanagement.entity.Role;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.repository.PasswordResetTokenRepository;
import com.schoolmanagement.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test for /v1/auth/forgot-password and /v1/auth/reset-password
 * — real Spring context + local MySQL via the "test" profile. Rate-limiting
 * itself is tested separately (ForgotPasswordRateLimitIntegrationTest), same
 * reasoning as AdmissionIntegrationTest/AdmissionRateLimitIntegrationTest.
 *
 * <p>reset-password tests insert a {@link PasswordResetToken} directly
 * (hashing a known raw token the same way {@code PasswordResetService} does)
 * rather than going through forgotPassword() first — the real raw token only
 * ever exists in the outgoing email body, which this test doesn't intercept
 * (see PasswordResetService's Javadoc on why only the hash is persisted).
 * This still exercises the real resetPassword() logic end-to-end via HTTP;
 * it just supplies the token forgotPassword() would otherwise have emailed.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PasswordResetIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private User user;

    @BeforeEach
    void setUp() {
        user = userRepository.save(User.builder()
                .username("itest.pwreset.user").email("itest.pwreset.user@school.com")
                .password(passwordEncoder.encode("OldPassw0rd!"))
                .firstName("Integration").lastName("User").role(Role.STUDENT).enabled(true).build());
    }

    // ---------------------------------------------------------------
    // forgot-password
    // ---------------------------------------------------------------

    @Test
    void forgotPassword_existingEmail_createsToken() throws Exception {
        mockMvc.perform(post("/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                ForgotPasswordRequest.builder().email(user.getEmail()).build())))
                .andExpect(status().isOk());

        List<PasswordResetToken> tokens = passwordResetTokenRepository.findByUserAndUsedAtIsNull(user);
        Assertions.assertEquals(1, tokens.size());
        Assertions.assertTrue(tokens.get(0).getExpiresAt().isAfter(LocalDateTime.now()));
        Assertions.assertTrue(tokens.get(0).getExpiresAt().isBefore(LocalDateTime.now().plusMinutes(16)));
    }

    @Test
    void forgotPassword_nonexistentEmail_returnsGenericResponseNoToken() throws Exception {
        // Same 200 + same generic message as the existing-email case above -
        // this is what actually prevents email enumeration; a differing
        // response (or a 404) would leak which addresses are registered.
        mockMvc.perform(post("/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                ForgotPasswordRequest.builder().email("itest.nonexistent@school.com").build())))
                .andExpect(status().isOk());

        Assertions.assertTrue(passwordResetTokenRepository.findAll().stream()
                .noneMatch(t -> t.getUser() != null && "itest.nonexistent@school.com".equals(t.getUser().getEmail())));
    }

    @Test
    void forgotPassword_secondRequestInvalidatesFirstToken() throws Exception {
        mockMvc.perform(post("/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                ForgotPasswordRequest.builder().email(user.getEmail()).build())))
                .andExpect(status().isOk());
        mockMvc.perform(post("/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                ForgotPasswordRequest.builder().email(user.getEmail()).build())))
                .andExpect(status().isOk());

        // The first token is superseded (usedAt set) the moment a second is
        // requested - only the newest should still be outstanding.
        Assertions.assertEquals(1, passwordResetTokenRepository.findByUserAndUsedAtIsNull(user).size());
    }

    // ---------------------------------------------------------------
    // reset-password
    // ---------------------------------------------------------------

    @Test
    void resetPassword_validToken_changesPasswordAndConsumesToken() throws Exception {
        String rawToken = saveTokenFor(user, LocalDateTime.now().plusMinutes(15));

        mockMvc.perform(post("/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                ResetPasswordRequest.builder().token(rawToken).newPassword("N3wPassw0rd!").build())))
                .andExpect(status().isOk());

        User reloaded = userRepository.findById(user.getId()).orElseThrow();
        Assertions.assertTrue(passwordEncoder.matches("N3wPassw0rd!", reloaded.getPassword()));

        PasswordResetToken token = passwordResetTokenRepository.findByTokenHash(hash(rawToken)).orElseThrow();
        Assertions.assertNotNull(token.getUsedAt());
    }

    @Test
    void resetPassword_alreadyUsedToken_returns400() throws Exception {
        String rawToken = saveTokenFor(user, LocalDateTime.now().plusMinutes(15));

        mockMvc.perform(post("/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                ResetPasswordRequest.builder().token(rawToken).newPassword("N3wPassw0rd!").build())))
                .andExpect(status().isOk());

        // Same token again - already consumed by the call above.
        mockMvc.perform(post("/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                ResetPasswordRequest.builder().token(rawToken).newPassword("Ano3therPassw0rd!").build())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resetPassword_expiredToken_returns400() throws Exception {
        String rawToken = saveTokenFor(user, LocalDateTime.now().minusMinutes(1));

        mockMvc.perform(post("/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                ResetPasswordRequest.builder().token(rawToken).newPassword("N3wPassw0rd!").build())))
                .andExpect(status().isBadRequest());

        User reloaded = userRepository.findById(user.getId()).orElseThrow();
        Assertions.assertTrue(passwordEncoder.matches("OldPassw0rd!", reloaded.getPassword()),
                "an expired token must not have changed the password");
    }

    @Test
    void resetPassword_unknownToken_returns400() throws Exception {
        mockMvc.perform(post("/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                ResetPasswordRequest.builder().token("not-a-real-token").newPassword("N3wPassw0rd!").build())))
                .andExpect(status().isBadRequest());
    }

    private String saveTokenFor(User forUser, LocalDateTime expiresAt) {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        passwordResetTokenRepository.save(PasswordResetToken.builder()
                .user(forUser)
                .tokenHash(hash(rawToken))
                .expiresAt(expiresAt)
                .build());
        return rawToken;
    }

    /** Mirrors PasswordResetService's own hashing exactly - see its Javadoc. */
    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
