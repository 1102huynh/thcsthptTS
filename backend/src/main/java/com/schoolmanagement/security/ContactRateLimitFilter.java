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
 * Anti-spam rate limit for the public {@code POST /v1/public/contact}
 * endpoint (KE_HOACH_TRANG_TIN_TUC_CONG_KHAI.md §8). Same
 * {@link SlidingWindowRateLimiter} mechanism as
 * {@link ForgotPasswordRateLimitFilter} / {@link AdmissionRateLimitFilter}.
 */
@Component
public class ContactRateLimitFilter extends OncePerRequestFilter {

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final boolean trustForwardedFor;
    private final SlidingWindowRateLimiter limiter;

    public ContactRateLimitFilter(
            @Value("${app.public.contact-rate-limit.max-requests:5}") int maxRequests,
            @Value("${app.public.contact-rate-limit.window-minutes:60}") int windowMinutes,
            @Value("${app.public.contact-rate-limit.trust-forwarded-for:false}") boolean trustForwardedFor) {
        this.trustForwardedFor = trustForwardedFor;
        this.limiter = new SlidingWindowRateLimiter(maxRequests, Duration.ofMinutes(windowMinutes));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!isContactRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!limiter.tryConsume(resolveClientIp(request))) {
            respondTooManyRequests(response, request);
            return;
        }

        filterChain.doFilter(request, response);
    }

    @Scheduled(fixedRateString = "${app.public.contact-rate-limit.window-minutes:60}", timeUnit = TimeUnit.MINUTES)
    void evictStaleEntries() {
        limiter.evictStale();
    }

    private boolean isContactRequest(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod()) && request.getRequestURI().endsWith("/v1/public/contact");
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
                + "\"message\":\"Bạn đã gửi liên hệ quá nhiều lần, vui lòng thử lại sau.\","
                + "\"code\":429,"
                + "\"path\":\"" + request.getRequestURI() + "\","
                + "\"timestamp\":\"" + TIMESTAMP_FORMAT.format(LocalDateTime.now()) + "\""
                + "}");
    }
}
