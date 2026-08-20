package com.schoolmanagement.service;

import com.schoolmanagement.dto.CreateUserRequest;
import com.schoolmanagement.dto.RegisterRequest;
import com.schoolmanagement.entity.Role;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.exception.DuplicateResourceException;
import com.schoolmanagement.repository.UserRepository;
import com.schoolmanagement.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        authenticationService = new AuthenticationService(
                userRepository, passwordEncoder, authenticationManager, jwtTokenProvider);
    }

    @Test
    void register_alwaysAssignsStudentRole_evenThoughRequestHasNoRoleChannel() {
        // RegisterRequest intentionally has no `role` field — this is the actual fix
        // for the privilege-escalation bug. This test locks in that the saved account
        // always ends up as STUDENT.
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser")
                .email("newuser@school.com")
                .password("Str0ngPassw0rd!")
                .firstName("New")
                .lastName("User")
                .build();

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@school.com")).thenReturn(false);
        when(passwordEncoder.encode("Str0ngPassw0rd!")).thenReturn("encoded-hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });
        when(jwtTokenProvider.generateRefreshToken(any(User.class))).thenReturn("refresh-token");

        authenticationService.register(request);

        ArgumentCaptor<User> savedUserCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(savedUserCaptor.capture());
        User savedUser = savedUserCaptor.getValue();

        assertThat(savedUser.getRole()).isEqualTo(Role.STUDENT);
        assertThat(savedUser.getPassword()).isEqualTo("encoded-hash");
        assertThat(savedUser.isEnabled()).isTrue();
    }

    @Test
    void register_throwsWhenUsernameAlreadyExists() {
        RegisterRequest request = RegisterRequest.builder()
                .username("admin")
                .email("someone@school.com")
                .password("Str0ngPassw0rd!")
                .firstName("A")
                .lastName("B")
                .build();

        when(userRepository.existsByUsername("admin")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authenticationService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_throwsWhenEmailAlreadyExists() {
        RegisterRequest request = RegisterRequest.builder()
                .username("someone")
                .email("admin@school.com")
                .password("Str0ngPassw0rd!")
                .firstName("A")
                .lastName("B")
                .build();

        when(userRepository.existsByUsername("someone")).thenReturn(false);
        when(userRepository.existsByEmail("admin@school.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authenticationService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUserByAdmin_honoursTheRequestedRole() {
        CreateUserRequest request = CreateUserRequest.builder()
                .username("newteacher")
                .email("newteacher@school.com")
                .password("Str0ngPassw0rd!")
                .firstName("New")
                .lastName("Teacher")
                .role(Role.TEACHER)
                .build();

        when(userRepository.existsByUsername("newteacher")).thenReturn(false);
        when(userRepository.existsByEmail("newteacher@school.com")).thenReturn(false);
        when(passwordEncoder.encode("Str0ngPassw0rd!")).thenReturn("encoded-hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });
        when(jwtTokenProvider.generateRefreshToken(any(User.class))).thenReturn("refresh-token");

        authenticationService.createUserByAdmin(request);

        ArgumentCaptor<User> savedUserCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(savedUserCaptor.capture());
        assertThat(savedUserCaptor.getValue().getRole()).isEqualTo(Role.TEACHER);
    }
}
