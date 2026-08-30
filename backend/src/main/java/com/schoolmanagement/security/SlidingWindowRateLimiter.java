package com.schoolmanagement.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A simple in-memory per-key sliding-window rate limiter — no Redis/
 * distributed store, matching this project's single-instance, self-hosted
 * deployment model (would need a shared store if this app is ever scaled to
 * multiple instances behind a load balancer). Extracted from
 * {@link AdmissionRateLimitFilter} (3.7) when a second consumer
 * ({@code ForgotPasswordRateLimitFilter}, 3.9) needed the identical logic
 * with different limits — {@link AdmissionRateLimitFilter} itself is left
 * on its own already-shipped, already-reviewed inline copy rather than
 * migrated onto this, to avoid re-risking stable code for reuse's own sake.
 *
 * <p>Not a Spring bean — each caller owns its own instance (constructed with
 * its own max-requests/window) and its own {@code @Scheduled} eviction
 * method, since scheduling only applies to Spring-managed bean methods, not
 * plain objects.
 *
 * <p>Every mutation of the underlying map — recording a new event and
 * evicting stale entries alike — goes through a {@code compute}-family call
 * for the same key, never a plain get-then-synchronized-mutate: {@link
 * ConcurrentHashMap}'s compute-family methods are mutually exclusive per
 * key, so a concurrent eviction sweep can never remove a key's bucket in the
 * narrow window between a request reading the map and that same request
 * recording its own event — which would otherwise silently lose that event
 * from tracking right at an eviction boundary, quietly loosening the limit.
 */
public class SlidingWindowRateLimiter {

    private static final Logger log = LoggerFactory.getLogger(SlidingWindowRateLimiter.class);

    private final int maxEvents;
    private final Duration window;
    private final ConcurrentHashMap<String, Deque<Instant>> eventsByKey = new ConcurrentHashMap<>();

    public SlidingWindowRateLimiter(int maxEvents, Duration window) {
        this.maxEvents = maxEvents;
        this.window = window;
    }

    /** @return true if this event is allowed (and is now recorded); false if the key is over its limit. */
    public boolean tryConsume(String key) {
        Instant windowStart = Instant.now().minus(window);
        AtomicBoolean allowed = new AtomicBoolean(true);

        eventsByKey.compute(key, (k, existing) -> {
            Deque<Instant> events = existing != null ? existing : new ConcurrentLinkedDeque<>();
            pruneOlderThan(events, windowStart);
            if (events.size() >= maxEvents) {
                allowed.set(false);
            } else {
                events.addLast(Instant.now());
            }
            return events;
        });

        return allowed.get();
    }

    /** Sweeps every tracked key's deque so a one-time caller's entry doesn't live in memory forever. */
    public void evictStale() {
        Instant windowStart = Instant.now().minus(window);
        AtomicInteger removed = new AtomicInteger();

        for (String key : eventsByKey.keySet()) {
            eventsByKey.computeIfPresent(key, (k, events) -> {
                pruneOlderThan(events, windowStart);
                if (events.isEmpty()) {
                    removed.incrementAndGet();
                    return null; // removes the entry
                }
                return events;
            });
        }

        if (removed.get() > 0) {
            log.debug("SlidingWindowRateLimiter: evicted {} stale entries", removed.get());
        }
    }

    private void pruneOlderThan(Deque<Instant> events, Instant windowStart) {
        while (!events.isEmpty() && events.peekFirst().isBefore(windowStart)) {
            events.pollFirst();
        }
    }
}
