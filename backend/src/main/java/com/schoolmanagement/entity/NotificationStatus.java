package com.schoolmanagement.entity;

/** Outcome of attempting to deliver a Notification to all its resolved recipients. */
public enum NotificationStatus {
    SENT,
    PARTIALLY_SENT,
    FAILED
}
