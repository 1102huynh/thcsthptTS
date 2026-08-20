package com.schoolmanagement.service;

import com.schoolmanagement.dto.AuthRequest;
import com.schoolmanagement.dto.AuthResponse;
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

@Service
@AllArgsConstructor
@Transactional
public class AuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private JwtTokenProvider jwtTokenProvider;

    public AuthResponse register(User user, String rawPassword) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new DuplicateResourceException("Username already exists");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }

        user.setPassword(passwordEncoder.encode(rawPassword));
        if (user.getRole() == null) {
            user.setRole(Role.STUDENT);
        }
        user.setEnabled(true);

        User savedUser = userRepository.save(user);

        return buildAuthResponse(savedUser, null);
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

    private AuthResponse buildAuthResponse(User user, String accessToken) {
        return buildAuthResponse(user, accessToken, jwtTokenProvider.generateRefreshToken(user));
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
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
                .issuedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();
    }
}

