package com.schoolmanagement.exception;

/**
 * Thrown when a Notification is sent through a channel that exists in the
 * API surface (NotificationChannel) but has no working sender yet — SMS and
 * ZALO, per IMPLEMENTATION_PLAN.md 3.6, pending a vendor/budget decision.
 * Mapped to 501 Not Implemented (see GlobalExceptionHandler) — not a client
 * error and not a server bug, so neither 400 nor a masked 500 fits.
 */
public class NotificationChannelUnavailableException extends RuntimeException {
    public NotificationChannelUnavailableException(String message) {
        super(message);
    }
}
