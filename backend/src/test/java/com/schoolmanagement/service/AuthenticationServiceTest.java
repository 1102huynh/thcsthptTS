package com.schoolmanagement.service;

import com.schoolmanagement.dto.AuthResponse;
import com.schoolmanagement.dto.ChangePasswordRequest;
import com.schoolmanagement.dto.CreateUserRequest;
import com.schoolmanagement.dto.RegisterRequest;
import com.schoolmanagement.dto.SetUserEnabledRequest;
import com.schoolmanagement.dto.UpdateProfileRequest;
import com.schoolmanagement.dto.UserDTO;
import com.schoolmanagement.entity.Role;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.exception.DuplicateResourceException;
import com.schoolmanagement.exception.InvalidCurrentPasswordException;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.repository.UserRepository;
import com.schoolmanagement.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
    @Mock
    private AuditLogService auditLogService;

    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        authenticationService = new AuthenticationService(
                userRepository, passwordEncoder, authenticationManager, jwtTokenProvider, auditLogService);
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
        when(jwtTokenProvider.getTokenIssuedAt(anyString())).thenReturn(new Date());
        when(jwtTokenProvider.getTokenExpiration(anyString())).thenReturn(new Date());

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
        when(jwtTokenProvider.getTokenIssuedAt(anyString())).thenReturn(new Date());
        when(jwtTokenProvider.getTokenExpiration(anyString())).thenReturn(new Date());

        User actingAdmin = User.builder().id(1L).role(Role.ADMIN).build();
        authenticationService.createUserByAdmin(request, actingAdmin);

        ArgumentCaptor<User> savedUserCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(savedUserCaptor.capture());
        assertThat(savedUserCaptor.getValue().getRole()).isEqualTo(Role.TEACHER);
    }

    @Test
    void refreshToken_rejectsAnAccessTokenPresentedAsARefreshToken() {
        // An access token has no "type":"refresh" claim — isRefreshToken() must say so,
        // otherwise a leaked/expired-soon access token could be used to mint a fresh
        // pair, and (separately, guarded in JwtAuthenticationFilter) a refresh token
        // could be used as API credentials for its whole 7-day lifetime.
        when(jwtTokenProvider.isRefreshToken("some-access-token")).thenReturn(false);

        assertThrows(BadCredentialsException.class,
                () -> authenticationService.refreshToken("some-access-token"));
        verify(userRepository, never()).findByUsername(any());
    }

    @Test
    void refreshToken_returnsNewTokenPairForAValidRefreshToken() {
        User user = User.builder().id(1L).username("admin").role(Role.ADMIN).build();

        when(jwtTokenProvider.isRefreshToken("valid-refresh-token")).thenReturn(true);
        when(jwtTokenProvider.extractUsername("valid-refresh-token")).thenReturn("admin");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(jwtTokenProvider.isTokenValid("valid-refresh-token", user)).thenReturn(true);
        when(jwtTokenProvider.generateToken(user)).thenReturn("new-access-token");
        when(jwtTokenProvider.generateRefreshToken(user)).thenReturn("new-refresh-token");
        when(jwtTokenProvider.getTokenIssuedAt(anyString())).thenReturn(new Date());
        when(jwtTokenProvider.getTokenExpiration(anyString())).thenReturn(new Date());

        AuthResponse response = authenticationService.refreshToken("valid-refresh-token");

        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");
    }

    @Test
    void updateProfile_savesTheNewFieldsAndReturnsThem() {
        User principal = User.builder().id(1L).username("student1").email("old@school.com")
                .firstName("Old").lastName("Name").role(Role.STUDENT).build();
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .firstName("New").lastName("Name").email("new@school.com").phoneNumber("0900000000").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(principal));
        when(userRepository.existsByEmail("new@school.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDTO result = authenticationService.updateProfile(principal, request);

        assertThat(result.getFirstName()).isEqualTo("New");
        assertThat(result.getEmail()).isEqualTo("new@school.com");
        assertThat(result.getPhoneNumber()).isEqualTo("0900000000");
    }

    @Test
    void updateProfile_keepingTheSameEmailDoesNotTripTheDuplicateCheck() {
        // Regression guard: existsByEmail("same@school.com") would itself be true
        // (it's this user's own row), so the check must be skipped when the email
        // isn't actually changing - otherwise nobody could ever save their profile
        // without also changing their email.
        User principal = User.builder().id(1L).username("student1").email("same@school.com")
                .firstName("Old").lastName("Name").role(Role.STUDENT).build();
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .firstName("New").lastName("Name").email("same@school.com").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(principal));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authenticationService.updateProfile(principal, request);

        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    void updateProfile_throwsWhenTheNewEmailBelongsToSomeoneElse() {
        User principal = User.builder().id(1L).username("student1").email("old@school.com")
                .firstName("Old").lastName("Name").role(Role.STUDENT).build();
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .firstName("Old").lastName("Name").email("taken@school.com").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(principal));
        when(userRepository.existsByEmail("taken@school.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authenticationService.updateProfile(principal, request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_reencodesOnceTheCurrentPasswordMatches() {
        User principal = User.builder().id(1L).username("student1").password("old-hash").role(Role.STUDENT).build();
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword("OldPassw0rd!").newPassword("N3wPassw0rd!").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(principal));
        when(passwordEncoder.matches("OldPassw0rd!", "old-hash")).thenReturn(true);
        when(passwordEncoder.encode("N3wPassw0rd!")).thenReturn("new-hash");

        authenticationService.changePassword(principal, request);

        assertThat(principal.getPassword()).isEqualTo("new-hash");
        verify(userRepository).save(principal);
    }

    @Test
    void changePassword_throwsAndDoesNotSaveWhenTheCurrentPasswordIsWrong() {
        User principal = User.builder().id(1L).username("student1").password("old-hash").role(Role.STUDENT).build();
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword("WrongPassword!").newPassword("N3wPassw0rd!").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(principal));
        when(passwordEncoder.matches("WrongPassword!", "old-hash")).thenReturn(false);

        // InvalidCurrentPasswordException, NOT BadCredentialsException - the latter is
        // what login failures throw, and GlobalExceptionHandler hard-codes its response
        // to the generic "Invalid username or password", which would be a confusing
        // message on the "đổi mật khẩu" screen. See GlobalExceptionHandler's dedicated
        // handleInvalidCurrentPasswordException + UserControllerIntegrationTest for the
        // end-to-end HTTP-message assertion.
        assertThrows(InvalidCurrentPasswordException.class, () -> authenticationService.changePassword(principal, request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_throwsWhenNewPasswordIsSameAsCurrent() {
        User principal = User.builder().id(1L).username("student1").password("old-hash").role(Role.STUDENT).build();
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword("Str0ngPassw0rd!").newPassword("Str0ngPassw0rd!").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(principal));
        // Both matches() calls compare the plaintext candidate against the SAME stored
        // hash (current-password check, then new-password-differs check) - real
        // BCryptPasswordEncoder would naturally return true for both here since it's
        // the same plaintext, so stub it that way rather than relying on Mockito's
        // default `false` for an unstubbed call (which would make this test pass for
        // the wrong reason if the guard were ever removed).
        when(passwordEncoder.matches("Str0ngPassw0rd!", "old-hash")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> authenticationService.changePassword(principal, request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateProfile_throwsWhenPrincipalNoLongerExists() {
        User principal = User.builder().id(1L).username("student1").email("old@school.com").role(Role.STUDENT).build();
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .firstName("New").lastName("Name").email("old@school.com").build();

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> authenticationService.updateProfile(principal, request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_throwsWhenPrincipalNoLongerExists() {
        User principal = User.builder().id(1L).username("student1").password("old-hash").role(Role.STUDENT).build();
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword("Str0ngPassw0rd!").newPassword("N3wPassw0rd!").build();

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> authenticationService.changePassword(principal, request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void setUserEnabled_disablesTheTargetAccountAndLogsIt() {
        User admin = User.builder().id(1L).username("admin").role(Role.ADMIN).build();
        User target = User.builder().id(2L).username("teacher1").role(Role.TEACHER).enabled(true).build();
        SetUserEnabledRequest request = SetUserEnabledRequest.builder().enabled(false).build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(target));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDTO result = authenticationService.setUserEnabled(2L, request, admin);

        assertThat(result.getEnabled()).isFalse();
        verify(auditLogService).log(eq(admin), eq("DISABLE"), eq("User"), eq(2L), any());
    }

    @Test
    void setUserEnabled_throwsWhenAdminTriesToLockTheirOwnAccount() {
        User admin = User.builder().id(1L).username("admin").role(Role.ADMIN).build();
        SetUserEnabledRequest request = SetUserEnabledRequest.builder().enabled(false).build();

        assertThrows(IllegalArgumentException.class, () -> authenticationService.setUserEnabled(1L, request, admin));
        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void setUserEnabled_throwsWhenTargetDoesNotExist() {
        User admin = User.builder().id(1L).username("admin").role(Role.ADMIN).build();
        SetUserEnabledRequest request = SetUserEnabledRequest.builder().enabled(false).build();

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authenticationService.setUserEnabled(99L, request, admin));
        verify(userRepository, never()).save(any());
    }
}
