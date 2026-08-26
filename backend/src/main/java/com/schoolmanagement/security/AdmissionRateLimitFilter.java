package com.schoolmanagement.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Anti-spam rate limit for the public POST /v1/admissions endpoint — per
 * IMPLEMENTATION_PLAN.md 3.7 ("có rate-limit chống spam"). A simple in-memory
 * sliding window per client IP: no Redis/distributed store, matching this
 * project's single-instance, self-hosted deployment model — would need to
 * move to a shared store (e.g. Redis) if this app is ever scaled to multiple
 * instances behind a load balancer.
 *
 * <p>Every mutation of {@link #submissionsByIp} — recording a new submission
 * and evicting stale entries alike — goes through a {@code compute}-family
 * call for the same key, never a plain get-then-synchronized-mutate. That's
 * deliberate: {@link ConcurrentHashMap}'s compute-family methods are mutually
 * exclusive per key, so a scheduled eviction can never remove an IP's bucket
 * in the narrow window between a request reading the map and that same
 * request recording its own submission — which would otherwise silently
 * lose that submission from tracking (a request "vanishing" from the
 * limiter's memory right at an eviction boundary, quietly loosening the
 * limit for that IP).
 */
@Component
public class AdmissionRateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AdmissionRateLimitFilter.class);
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Value("${app.admissions.rate-limit.max-requests:5}")
    private int maxRequests;

    @Value("${app.admissions.rate-limit.window-minutes:60}")
    private int windowMinutes;

    /**
     * Off by default: request.getRemoteAddr() (the actual TCP socket peer,
     * not spoofable by the caller) is what's rate-limited unless an operator
     * explicitly confirms this instance sits behind a reverse proxy that
     * sets/overwrites X-Forwarded-For itself — otherwise any caller could
     * send a different X-Forwarded-For value on every request and get a
     * fresh bucket each time, defeating the whole limit.
     */
    @Value("${app.admissions.rate-limit.trust-forwarded-for:false}")
    private boolean trustForwardedFor;

    private final ConcurrentHashMap<String, Deque<Instant>> submissionsByIp = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!isAdmissionSubmission(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = resolveClientIp(request);
        Instant windowStart = Instant.now().minus(Duration.ofMinutes(windowMinutes));
        AtomicBoolean rejected = new AtomicBoolean(false);

        submissionsByIp.compute(clientIp, (ip, existing) -> {
            Deque<Instant> submissions = existing != null ? existing : new ConcurrentLinkedDeque<>();
            pruneOlderThan(submissions, windowStart);
            if (submissions.size() >= maxRequests) {
                rejected.set(true);
            } else {
                submissions.addLast(Instant.now());
            }
            return submissions;
        });

        if (rejected.get()) {
            respondTooManyRequests(response, request);
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Sweeps every tracked IP's deque on a fixed schedule (independent of
     * request traffic) so a one-time visitor's entry doesn't live in memory
     * forever. computeIfPresent, not a plain get+remove, for the same
     * per-key-atomicity reason explained on the class Javadoc.
     */
    @Scheduled(fixedRateString = "${app.admissions.rate-limit.window-minutes:60}", timeUnit = TimeUnit.MINUTES)
    void evictStaleEntries() {
        Instant windowStart = Instant.now().minus(Duration.ofMinutes(windowMinutes));
        AtomicInteger removed = new AtomicInteger();

        for (String ip : submissionsByIp.keySet()) {
            submissionsByIp.computeIfPresent(ip, (key, submissions) -> {
                pruneOlderThan(submissions, windowStart);
                if (submissions.isEmpty()) {
                    removed.incrementAndGet();
                    return null; // removes the entry
                }
                return submissions;
            });
        }

        if (removed.get() > 0) {
            log.debug("Admission rate limiter: evicted {} stale IP entries", removed.get());
        }
    }

    private void pruneOlderThan(Deque<Instant> submissions, Instant windowStart) {
        while (!submissions.isEmpty() && submissions.peekFirst().isBefore(windowStart)) {
            submissions.pollFirst();
        }
    }

    private boolean isAdmissionSubmission(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod()) && request.getRequestURI().endsWith("/v1/admissions");
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
                + "\"message\":\"Bạn đã gửi quá nhiều hồ sơ, vui lòng thử lại sau.\","
                + "\"code\":429,"
                + "\"path\":\"" + request.getRequestURI() + "\","
                + "\"timestamp\":\"" + TIMESTAMP_FORMAT.format(LocalDateTime.now()) + "\""
                + "}");
    }
}
