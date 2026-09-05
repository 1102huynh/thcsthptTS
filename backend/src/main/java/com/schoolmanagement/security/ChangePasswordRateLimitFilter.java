package com.schoolmanagement.security;

import com.schoolmanagement.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * Anti brute-force rate limit for POST /v1/users/me/change-password.
 *
 * Unlike {@link ForgotPasswordRateLimitFilter} (a public, unauthenticated
 * endpoint keyed by IP), this one protects an *authenticated* endpoint: a
 * caller who holds a valid-but-stolen JWT (leaked/XSS'd access token) but not
 * the account's actual password could otherwise brute-force
 * `currentPassword` with unlimited attempts, since AuthenticationService
 * .changePassword() only checks the password, never throttles. So this
 * filter is keyed by user id, not IP, and — because that requires knowing
 * *who* is calling — it must run AFTER {@link JwtAuthenticationFilter} has
 * populated the SecurityContext (see SecurityConfig's addFilterAfter, unlike
 * every other rate-limit filter here which runs addFilterBefore it).
 */
@Component
public class ChangePasswordRateLimitFilter extends OncePerRequestFilter {

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final SlidingWindowRateLimiter limiter;

    public ChangePasswordRateLimitFilter(
            @Value("${app.auth.change-password-rate-limit.max-requests:5}") int maxRequests,
            @Value("${app.auth.change-password-rate-limit.window-minutes:15}") int windowMinutes) {
        this.limiter = new SlidingWindowRateLimiter(maxRequests, Duration.ofMinutes(windowMinutes));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!isChangePasswordRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = resolveRateLimitKey(request);
        if (key != null && !limiter.tryConsume(key)) {
            respondTooManyRequests(response, request);
            return;
        }

        filterChain.doFilter(request, response);
    }

    @Scheduled(fixedRateString = "${app.auth.change-password-rate-limit.window-minutes:15}", timeUnit = TimeUnit.MINUTES)
    void evictStaleEntries() {
        limiter.evictStale();
    }

    private boolean isChangePasswordRequest(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod())
                && request.getRequestURI().endsWith("/v1/users/me/change-password");
    }

    /**
     * Keyed by authenticated user id. Returns null (no limiting — the
     * endpoint's own @PreAuthorize/authenticated() rule will 401 it anyway)
     * only if, unexpectedly, no authentication has been set by this point.
     */
    private String resolveRateLimitKey(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            return null;
        }
        return "user:" + user.getId();
    }

    private void respondTooManyRequests(HttpServletResponse response, HttpServletRequest request) throws IOException {
        response.setStatus(429);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{"
                + "\"status\":\"TOO_MANY_REQUESTS\","
                + "\"message\":\"Bạn đã thử đổi mật khẩu quá nhiều lần, vui lòng thử lại sau.\","
                + "\"code\":429,"
                + "\"path\":\"" + request.getRequestURI() + "\","
                + "\"timestamp\":\"" + TIMESTAMP_FORMAT.format(LocalDateTime.now()) + "\""
                + "}");
    }
}
