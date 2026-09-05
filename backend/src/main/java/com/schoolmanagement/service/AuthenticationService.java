package com.schoolmanagement.service;

import com.schoolmanagement.dto.AuthRequest;
import com.schoolmanagement.dto.AuthResponse;
import com.schoolmanagement.dto.ChangePasswordRequest;
import com.schoolmanagement.dto.CreateUserRequest;
import com.schoolmanagement.dto.RegisterRequest;
import com.schoolmanagement.dto.UpdateProfileRequest;
import com.schoolmanagement.dto.UserDTO;
import com.schoolmanagement.entity.Role;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.dto.SetUserEnabledRequest;
import com.schoolmanagement.exception.DuplicateResourceException;
import com.schoolmanagement.exception.InvalidCurrentPasswordException;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.repository.UserRepository;
import com.schoolmanagement.security.JwtTokenProvider;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class AuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private JwtTokenProvider jwtTokenProvider;
    private AuditLogService auditLogService;

    /**
     * Self-service registration. Always creates a STUDENT account — RegisterRequest
     * has no `role` field, so a client has no channel to request a privileged role.
     * ADMIN must use {@link #createUserByAdmin(CreateUserRequest, User)} to grant other roles.
     */
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .role(Role.STUDENT)
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(savedUser);

        return buildAuthResponse(savedUser, null, refreshToken);
    }

    /**
     * ADMIN-only account creation with an explicit role. Only reachable via
     * POST /v1/users, which requires ROLE_ADMIN (see UserController).
     */
    public AuthResponse createUserByAdmin(CreateUserRequest request, User actor) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .role(request.getRole())
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(savedUser);

        auditLogService.log(actor, "CREATE", "User", savedUser.getId(),
                java.util.Map.of("username", savedUser.getUsername(), "role", savedUser.getRole().name()));

        return buildAuthResponse(savedUser, null, refreshToken);
    }

    /**
     * ADMIN-only account lookup by role (GET /v1/users?role=...) — added for
     * ParentManagement.jsx (Giai đoạn 3.6), which needs to list existing
     * PARENT accounts to link a child to one instead of only ever creating a
     * brand-new parent account per link (most parents have more than one
     * child at the school). UserRepository.findByRole already existed
     * (NotificationService uses it internally to resolve ALL_PARENTS
     * recipients) but was never exposed over HTTP before this.
     */
    public List<UserDTO> getUsersByRole(Role role) {
        return userRepository.findByRole(role).stream().map(this::mapToUserDTO).collect(Collectors.toList());
    }

    /**
     * ADMIN account browser (D6 — GET /v1/users/search) — deliberately a
     * separate endpoint/method from {@link #getUsersByRole}, which
     * ParentManagement.jsx already calls expecting a flat unpaginated
     * {@code List<UserDTO>}; changing that shape would break it. Both
     * filters are optional so ADMIN can browse every account, one role, a
     * name/username/email search, or a combination.
     */
    public Page<UserDTO> searchUsers(Role role, String q, Pageable pageable) {
        String likePattern = (q == null || q.isBlank()) ? null : "%" + q.trim().toLowerCase() + "%";
        return userRepository.search(role, likePattern, pageable).map(this::mapToUserDTO);
    }

    /**
     * ADMIN lock/unlock (D6 — PUT /v1/users/{id}/enabled). `enabled=false`
     * blocks login going forward via the existing UserDetails.isEnabled()
     * wiring (see SetUserEnabledRequest's Javadoc) — no filter/security
     * config changes needed. `actor` is always the caller resolved from the
     * JWT (see UserController), never a client-supplied value, so the
     * self-lockout guard below can't be bypassed by lying about who's asking.
     */
    public UserDTO setUserEnabled(Long targetId, SetUserEnabledRequest request, User actor) {
        if (targetId.equals(actor.getId())) {
            throw new IllegalArgumentException("Không thể tự khoá tài khoản của chính mình");
        }
        User target = userRepository.findById(targetId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản"));

        target.setEnabled(request.getEnabled());
        User saved = userRepository.save(target);
        auditLogService.log(actor, request.getEnabled() ? "ENABLE" : "DISABLE", "User", saved.getId(),
                java.util.Map.of("username", saved.getUsername()));
        return mapToUserDTO(saved);
    }

    /**
     * Self-service "my profile" lookup — GET /v1/users/me. `principal` is
     * whichever User the JWT resolved to (see UserController), so this can
     * never be used to read anyone else's profile.
     */
    public UserDTO getCurrentUserProfile(User principal) {
        return mapToUserDTO(principal);
    }

    /**
     * Self-service profile edit — PUT /v1/users/me. Only firstName/lastName/
     * email/phoneNumber are settable (see UpdateProfileRequest); username and
     * role stay immutable through this endpoint — an ADMIN wanting to change
     * either still goes through POST /v1/users-style tooling, not this one.
     */
    public UserDTO updateProfile(User principal, UpdateProfileRequest request) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        if (!user.getEmail().equalsIgnoreCase(request.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());

        User saved = userRepository.save(user);
        auditLogService.log(saved, "UPDATE", "User", saved.getId(),
                java.util.Map.of("action", "self_profile_update"));
        return mapToUserDTO(saved);
    }

    /**
     * Self-service password change for a still-logged-in user (POST
     * /v1/users/me/change-password) — distinct from PasswordResetService's
     * forgot/reset flow, which is for someone who can't log in at all. This
     * one requires proving the current password rather than an emailed
     * token.
     */
    public void changePassword(User principal, ChangePasswordRequest request) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            // Deliberately NOT BadCredentialsException here — GlobalExceptionHandler maps
            // that to the generic login message "Invalid username or password", which is
            // confusing on the "đổi mật khẩu" screen (this is an authenticated user
            // re-proving their password, not a login attempt).
            throw new InvalidCurrentPasswordException("Mật khẩu hiện tại không đúng");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu mới phải khác mật khẩu hiện tại");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        auditLogService.log(user, "PASSWORD_CHANGE", "User", user.getId(), null);
    }

    private UserDTO mapToUserDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .enabled(user.getEnabled())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLogin(user.getLastLogin())
                .build();
    }

    public AuthResponse login(AuthRequest authRequest) {
        log.debug("Login attempt for username: {}", authRequest.getUsername());

        try {
            // Check if user exists
            User user = userRepository.findByUsername(authRequest.getUsername())
                    .orElseThrow(() -> {
                        log.debug("Login failed - user not found: {}", authRequest.getUsername());
                        return new BadCredentialsException("Invalid credentials");
                    });

            boolean passwordMatches = passwordEncoder.matches(authRequest.getPassword(), user.getPassword());
            if (!passwordMatches) {
                log.debug("Login failed - password mismatch for username: {}", authRequest.getUsername());
                throw new BadCredentialsException("Invalid username or password");
            }

            // Authenticate
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getUsername(),
                            authRequest.getPassword()
                    )
            );

            log.debug("Login successful for username: {}", authRequest.getUsername());

            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            String accessToken = jwtTokenProvider.generateToken(user);
            String refreshToken = jwtTokenProvider.generateRefreshToken(user);

            return buildAuthResponse(user, accessToken, refreshToken);

        } catch (BadCredentialsException ex) {
            log.debug("Login failed for username: {} - {}", authRequest.getUsername(), ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error during login for username: {}", authRequest.getUsername(), ex);
            throw new BadCredentialsException("Invalid username or password", ex);
        }
    }

    public AuthResponse refreshToken(String refreshToken) {
        try {
            if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
                // Also rejects an access token presented here — access tokens carry no
                // "type":"refresh" claim, so they can't be used to mint a fresh pair.
                throw new BadCredentialsException("Invalid refresh token");
            }

            String username = jwtTokenProvider.extractUsername(refreshToken);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new BadCredentialsException("User not found"));

            if (jwtTokenProvider.isTokenValid(refreshToken, user)) {
                String newAccessToken = jwtTokenProvider.generateToken(user);
                String newRefreshToken = jwtTokenProvider.generateRefreshToken(user);
                return buildAuthResponse(user, newAccessToken, newRefreshToken);
            }

            throw new BadCredentialsException("Invalid refresh token");
        } catch (Exception ex) {
            throw new BadCredentialsException("Invalid refresh token", ex);
        }
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        // issuedAt/expiresAt describe whichever token the client will actually use for
        // auth: the access token when there is one (login/refresh), otherwise the
        // refresh token (register/admin-create, which issue no access token yet).
        String tokenForTimestamps = accessToken != null ? accessToken : refreshToken;

        return AuthResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .issuedAt(toLocalDateTime(jwtTokenProvider.getTokenIssuedAt(tokenForTimestamps)))
                .expiresAt(toLocalDateTime(jwtTokenProvider.getTokenExpiration(tokenForTimestamps)))
                .build();
    }

    private LocalDateTime toLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }
}

