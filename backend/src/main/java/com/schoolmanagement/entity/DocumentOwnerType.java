package com.schoolmanagement.entity;

/**
 * What a DocumentAttachment's ownerId refers to, per IMPLEMENTATION_PLAN.md
 * 3.9 — same polymorphic-owner pattern as NotificationTargetType.
 */
public enum DocumentOwnerType {
    STUDENT,
    STAFF,
    ADMISSION_APPLICATION
}
