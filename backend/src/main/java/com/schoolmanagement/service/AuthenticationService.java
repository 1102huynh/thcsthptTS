package com.schoolmanagement.service;

import com.schoolmanagement.dto.AuthRequest;
import com.schoolmanagement.dto.AuthResponse;
import com.schoolmanagement.dto.CreateUserRequest;
import com.schoolmanagement.dto.RegisterRequest;
import com.schoolmanagement.dto.UserDTO;
import com.schoolmanagement.entity.Role;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.exception.DuplicateResourceException;
import com.schoolmanagement.repository.UserRepository;
import com.schoolmanagement.security.JwtTokenProvider;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

