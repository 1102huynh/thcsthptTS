package com.schoolmanagement.entity;

/**
 * Per IMPLEMENTATION_PLAN.md 3.6: APP and EMAIL are implemented now (see
 * AppNotificationSender/EmailNotificationSender). SMS and ZALO are
 * deliberately NOT implemented yet — sending a notification through them
 * throws {@link com.schoolmanagement.exception.NotificationChannelUnavailableException}
 * (mapped to 501) — pending an actual SMS provider (eSMS/FPT SMS) and Zalo
 * OA registration/budget decision, per the plan's explicit note that this
 * must be settled before building those two channels for real.
 */
public enum NotificationChannel {
    APP,
    EMAIL,
    SMS,
    ZALO
}
