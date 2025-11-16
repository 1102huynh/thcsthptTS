package com.schoolmanagement.service;

import com.schoolmanagement.dto.AuthRequest;
import com.schoolmanagement.dto.AuthResponse;
import com.schoolmanagement.entity.Role;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.exception.DuplicateResourceException;
import com.schoolmanagement.repository.UserRepository;
import com.schoolmanagement.security.JwtTokenProvider;
import lombok.AllArgsConstructor;
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
        System.out.println("=== LOGIN ATTEMPT ===");
        System.out.println("Username: " + authRequest.getUsername());

        try {
            // Check if user exists
            User user = userRepository.findByUsername(authRequest.getUsername())
                    .orElseThrow(() -> {
                        System.out.println("ERROR: User not found: " + authRequest.getUsername());
                        return new BadCredentialsException("Invalid credentials");
                    });

            System.out.println("User found: " + user.getUsername());
            System.out.println("User enabled: " + user.isEnabled());
            System.out.println("User role: " + user.getRole());
            System.out.println("Password hash from DB (FULL): " + user.getPassword());
            System.out.println("Password hash length: " + user.getPassword().length());

            // Generate a fresh hash for comparison
            String freshHash = passwordEncoder.encode(authRequest.getPassword());
            System.out.println("Fresh hash for 'Test@123': " + freshHash);

            // Test password manually
            boolean passwordMatches = passwordEncoder.matches(authRequest.getPassword(), user.getPassword());
            System.out.println("Password matches: " + passwordMatches);
            System.out.println("Input password: " + authRequest.getPassword());

            if (!passwordMatches) {
                System.out.println("ERROR: Password does not match!");
                throw new BadCredentialsException("Invalid username or password");
            }

            // Authenticate
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getUsername(),
                            authRequest.getPassword()
                    )
            );

            System.out.println("Authentication successful!");

            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            String accessToken = jwtTokenProvider.generateToken(user);
            String refreshToken = jwtTokenProvider.generateRefreshToken(user);

            return buildAuthResponse(user, accessToken, refreshToken);

        } catch (BadCredentialsException ex) {
            System.out.println("ERROR: BadCredentialsException - " + ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            System.out.println("ERROR: Unexpected exception - " + ex.getMessage());
            ex.printStackTrace();
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

