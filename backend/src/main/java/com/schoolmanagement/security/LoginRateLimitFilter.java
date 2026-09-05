package com.schoolmanagement.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * Anti brute-force rate limit for the public POST /v1/auth/login endpoint
 * (KE_HOACH_NANG_CAP_V4.md v4.16, G.4 mục 8) — every other public,
 * sensitive endpoint here already has one (ForgotPasswordRateLimitFilter,
 * AdmissionRateLimitFilter, ContactRateLimitFilter), but login itself, the
 * single most obvious password-guessing target in the app, had none until
 * now. Same {@link SlidingWindowRateLimiter} mechanism, keyed by IP like
 * the others (login has no other stable identity to key on before the
 * password is verified).
 *
 * The default is deliberately much more generous than
 * ForgotPasswordRateLimitFilter's (3/60min) or AdmissionRateLimitFilter's
 * (5/60min): those protect rare one-off actions, but login is something
 * every legitimate user does constantly, often from a school's shared
 * NAT'd public IP (a whole classroom or staff room can share one address).
 * A strict per-IP limit here would risk locking out an entire school
 * instead of just a would-be attacker. 20 attempts / 5 minutes still stops
 * a scripted attempt at hundreds-to-thousands of passwords while leaving
 * generous headroom for real concurrent logins.
 */
@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /** Same reasoning as AdmissionRateLimitFilter.trustForwardedFor - see its Javadoc. */
    private final boolean trustForwardedFor;
    private final SlidingWindowRateLimiter limiter;

    public LoginRateLimitFilter(
            @Value("${app.auth.login-rate-limit.max-requests:20}") int maxRequests,
            @Value("${app.auth.login-rate-limit.window-minutes:5}") int windowMinutes,
            @Value("${app.auth.login-rate-limit.trust-forwarded-for:false}") boolean trustForwardedFor) {
        this.trustForwardedFor = trustForwardedFor;
        this.limiter = new SlidingWindowRateLimiter(maxRequests, Duration.ofMinutes(windowMinutes));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!isLoginRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = resolveClientIp(request);
        if (!limiter.tryConsume(clientIp)) {
            respondTooManyRequests(response, request);
            return;
        }

        filterChain.doFilter(request, response);
    }

    @Scheduled(fixedRateString = "${app.auth.login-rate-limit.window-minutes:5}", timeUnit = TimeUnit.MINUTES)
    void evictStaleEntries() {
        limiter.evictStale();
    }

    private boolean isLoginRequest(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod()) && request.getRequestURI().endsWith("/v1/auth/login");
    }

    private String resolveClientIp(HttpServletRequest request) {
        if (trustForwardedFor) {
            String forwarded = request.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                return forwarded.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }

    private void respondTooManyRequests(HttpServletResponse response, HttpServletRequest request) throws IOException {
        response.setStatus(429);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{"
                + "\"status\":\"TOO_MANY_REQUESTS\","
                + "\"message\":\"Bạn đã đăng nhập sai quá nhiều lần, vui lòng thử lại sau ít phút.\","
                + "\"code\":429,"
                + "\"path\":\"" + request.getRequestURI() + "\","
                + "\"timestamp\":\"" + TIMESTAMP_FORMAT.format(LocalDateTime.now()) + "\""
                + "}");
    }
}
