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
 * Anti-spam rate limit for the public POST /v1/auth/forgot-password endpoint
 * — per IMPLEMENTATION_PLAN.md 3.9. Without this, the endpoint is a free
 * email bomb against any registered address (every call sends one email) and
 * a way to churn through SMTP quota. Same {@link SlidingWindowRateLimiter}
 * mechanism {@link AdmissionRateLimitFilter} uses, tuned tighter by default
 * (this endpoint has no legitimate reason to be called more than a couple
 * times an hour by the same caller).
 */
@Component
public class ForgotPasswordRateLimitFilter extends OncePerRequestFilter {

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /** Same reasoning as AdmissionRateLimitFilter.trustForwardedFor - see its Javadoc. */
    private final boolean trustForwardedFor;
    private final SlidingWindowRateLimiter limiter;

    // Built eagerly here, not lazily on first request: a lazy `if (limiter ==
    // null) limiter = new ...` is not thread-safe - concurrent requests could
    // each see null and build their own separate limiter with an empty
    // counter, multiplying the effective rate limit by however many threads
    // raced (defeating the anti-spam/email-enumeration protection this filter
    // exists for). @Value works on a constructor parameter (unlike a
    // Lombok-generated all-args constructor's parameters, which don't carry
    // field annotations - see PasswordResetService's Javadoc for the same gotcha).
    public ForgotPasswordRateLimitFilter(
            @Value("${app.auth.forgot-password-rate-limit.max-requests:3}") int maxRequests,
            @Value("${app.auth.forgot-password-rate-limit.window-minutes:60}") int windowMinutes,
            @Value("${app.auth.forgot-password-rate-limit.trust-forwarded-for:false}") boolean trustForwardedFor) {
        this.trustForwardedFor = trustForwardedFor;
        this.limiter = new SlidingWindowRateLimiter(maxRequests, Duration.ofMinutes(windowMinutes));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!isForgotPasswordRequest(request)) {
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

    @Scheduled(fixedRateString = "${app.auth.forgot-password-rate-limit.window-minutes:60}", timeUnit = TimeUnit.MINUTES)
    void evictStaleEntries() {
        limiter.evictStale();
    }

    private boolean isForgotPasswordRequest(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod()) && request.getRequestURI().endsWith("/v1/auth/forgot-password");
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
                + "\"message\":\"Bạn đã yêu cầu đặt lại mật khẩu quá nhiều lần, vui lòng thử lại sau.\","
                + "\"code\":429,"
                + "\"path\":\"" + request.getRequestURI() + "\","
                + "\"timestamp\":\"" + TIMESTAMP_FORMAT.format(LocalDateTime.now()) + "\""
                + "}");
    }
}
