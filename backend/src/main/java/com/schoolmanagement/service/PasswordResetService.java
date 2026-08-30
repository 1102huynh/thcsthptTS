package com.schoolmanagement.service;

import com.schoolmanagement.entity.PasswordResetToken;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.repository.PasswordResetTokenRepository;
import com.schoolmanagement.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

/**
 * Forgot/reset password, per IMPLEMENTATION_PLAN.md 3.9. The raw token
 * (256 bits of {@link SecureRandom}, URL-safe base64) is emailed once and
 * never persisted — only its SHA-256 hash is stored (see
 * {@link com.schoolmanagement.entity.PasswordResetToken}'s Javadoc) — so a
 * database leak alone can't be used to reset anyone's password. Tokens
 * expire after {@value #TOKEN_EXPIRY_MINUTES} minutes and are single-use.
 */
@Service
@Transactional
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private static final int TOKEN_EXPIRY_MINUTES = 15;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailNotificationSender emailNotificationSender;
    private final AuditLogService auditLogService;
    private final String resetPasswordUrl;

    // Not @AllArgsConstructor: Lombok's generated all-args constructor doesn't
    // copy the @Value annotation onto its parameter, so Spring's constructor
    // injection (the only injection mode when there's exactly one
    // constructor) would try to autowire resetPasswordUrl as a `String` bean
    // and fail to start the whole application context. @Value only works
    // here because it's on an explicit constructor parameter.
    public PasswordResetService(UserRepository userRepository,
                                 PasswordResetTokenRepository passwordResetTokenRepository,
                                 PasswordEncoder passwordEncoder,
                                 EmailNotificationSender emailNotificationSender,
                                 AuditLogService auditLogService,
                                 @Value("${app.frontend.reset-password-url}") String resetPasswordUrl) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailNotificationSender = emailNotificationSender;
        this.auditLogService = auditLogService;
        this.resetPasswordUrl = resetPasswordUrl;
    }

    /**
     * Always "succeeds" from the caller's point of view regardless of
     * whether {@code email} matches an account — silently no-ops on a miss
     * instead of a 404, so this endpoint can't be used to enumerate which
     * emails are registered. See AuthController for the (identical either
     * way) response it returns.
     */
    public void forgotPassword(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return;
        }
        User user = userOpt.get();

        // A newer request supersedes any still-outstanding one - an old email
        // sitting in an inbox (or a compromised earlier link) shouldn't stay
        // valid once the user has asked for a fresh one.
        LocalDateTime now = LocalDateTime.now();
        List<PasswordResetToken> outstanding = passwordResetTokenRepository.findByUserAndUsedAtIsNull(user);
        outstanding.forEach(t -> t.setUsedAt(now));
        passwordResetTokenRepository.saveAll(outstanding);

        String rawToken = generateRawToken();
        passwordResetTokenRepository.save(PasswordResetToken.builder()
                .user(user)
                .tokenHash(hash(rawToken))
                .expiresAt(now.plusMinutes(TOKEN_EXPIRY_MINUTES))
                .build());

        String link = resetPasswordUrl + "?token=" + rawToken;
        String content = "Xin chào " + user.getFirstName() + ",\n\n"
                + "Nhấn vào liên kết sau để đặt lại mật khẩu (hết hạn sau " + TOKEN_EXPIRY_MINUTES + " phút):\n"
                + link + "\n\n"
                + "Nếu bạn không yêu cầu đặt lại mật khẩu, hãy bỏ qua email này — mật khẩu hiện tại vẫn giữ nguyên.";

        // EmailNotificationSender.send() already catches MailException and
        // returns false on failure - deliberately not treated as an error
        // here either, for the same no-enumeration reason: whether the SMTP
        // server is reachable is not something to leak to the caller.
        boolean sent = emailNotificationSender.send(user.getEmail(), "Đặt lại mật khẩu", content);
        if (!sent) {
            log.warn("Failed to send password reset email to user {}", user.getId());
        }
    }

    public void resetPassword(String rawToken, String newPassword) {
        PasswordResetToken token = passwordResetTokenRepository.findByTokenHash(hash(rawToken))
                .filter(t -> t.getUsedAt() == null)
                .filter(t -> t.getExpiresAt().isAfter(LocalDateTime.now()))
                // Same message whether the token is unknown, already used, or
                // expired - distinguishing them would tell an attacker which
                // guess was "closer".
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset token"));

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        token.setUsedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(token);

        auditLogService.log(user, "PASSWORD_RESET", "User", user.getId(), null);
    }

    private String generateRawToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            // SHA-256 is a JDK-guaranteed algorithm (JLS/JCA spec) - this is
            // unreachable in practice, wrapped only so the method signature
            // doesn't force every caller to handle a checked exception that
            // can't actually occur.
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}
