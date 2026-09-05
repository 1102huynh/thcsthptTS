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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test for the self-service "/v1/users/me*" endpoints added by
 * the Profile & Account Settings feature (GET/PUT /v1/users/me, POST
 * /v1/users/me/change-password). Complements the pure-unit
 * AuthenticationServiceTest with: (1) end-to-end @Valid → 400 wiring, which a
 * service-layer unit test can't see, and (2) the actual HTTP-visible error
 * message for a wrong current password, guarding the fix that stopped it
 * from showing the generic login message "Invalid username or password"
 * (see GlobalExceptionHandler.handleInvalidCurrentPasswordException).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserControllerIntegrationTest {

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
                .username("itest.profile.self").email("itest.profile.self@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Nguyễn").lastName("Văn A").role(Role.STUDENT).enabled(true).build());
    }

    private RequestPostProcessor asSelf() {
        return authentication(new UsernamePasswordAuthenticationToken(
                self, null, List.of(new SimpleGrantedAuthority("ROLE_" + self.getRole().name()))));
    }

    private RequestPostProcessor asUser(User user) {
        return authentication(new UsernamePasswordAuthenticationToken(
                user, null, List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))));
    }

    @Test
    void updateMe_withBlankFirstName_returns400() throws Exception {
        Map<String, Object> body = Map.of(
                "firstName", "",
                "lastName", "Văn A",
                "email", self.getEmail());

        mockMvc.perform(put("/v1/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .with(asSelf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changePassword_withTooShortNewPassword_returns400() throws Exception {
        Map<String, Object> body = Map.of(
                "currentPassword", "Str0ngPassw0rd!",
                "newPassword", "short");

        mockMvc.perform(post("/v1/users/me/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .with(asSelf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changePassword_withWrongCurrentPassword_returnsContextualMessageNotGenericLoginMessage() throws Exception {
        Map<String, Object> body = Map.of(
                "currentPassword", "TotallyWrongPassword!",
                "newPassword", "N3wStr0ngPassw0rd!");

        mockMvc.perform(post("/v1/users/me/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .with(asSelf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Mật khẩu hiện tại không đúng"));
    }

    @Test
    void changePassword_withNewPasswordSameAsCurrent_returns400() throws Exception {
        Map<String, Object> body = Map.of(
                "currentPassword", "Str0ngPassw0rd!",
                "newPassword", "Str0ngPassw0rd!");

        mockMvc.perform(post("/v1/users/me/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .with(asSelf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Mật khẩu mới phải khác mật khẩu hiện tại"));
    }

    // ---- D6: GET /v1/users/search + PUT /v1/users/{id}/enabled (trang quản trị tài khoản)

    @Test
    void searchUsers_asNonAdmin_returns403() throws Exception {
        mockMvc.perform(get("/v1/users/search").with(asSelf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void searchUsers_asAdmin_findsByRoleAndKeyword() throws Exception {
        User admin = userRepository.save(User.builder()
                .username("itest.accounts.admin").email("itest.accounts.admin@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Admin").lastName("Root").role(Role.ADMIN).enabled(true).build());

        mockMvc.perform(get("/v1/users/search").param("role", "STUDENT").param("q", "itest.profile.self").with(asUser(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.username == 'itest.profile.self')]").exists());
    }

    @Test
    void setEnabled_disablingAnAccount_blocksItsNextLogin() throws Exception {
        User admin = userRepository.save(User.builder()
                .username("itest.accounts.admin2").email("itest.accounts.admin2@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Admin").lastName("Root").role(Role.ADMIN).enabled(true).build());

        mockMvc.perform(put("/v1/users/{id}/enabled", self.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("enabled", false)))
                        .with(asUser(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));

        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("username", self.getUsername(), "password", "Str0ngPassw0rd!"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void setEnabled_adminCannotLockTheirOwnAccount() throws Exception {
        User admin = userRepository.save(User.builder()
                .username("itest.accounts.admin3").email("itest.accounts.admin3@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Admin").lastName("Root").role(Role.ADMIN).enabled(true).build());

        mockMvc.perform(put("/v1/users/{id}/enabled", admin.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("enabled", false)))
                        .with(asUser(admin)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Không thể tự khoá tài khoản của chính mình"));
    }
}
