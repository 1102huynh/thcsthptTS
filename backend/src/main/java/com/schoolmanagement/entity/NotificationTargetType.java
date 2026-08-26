package com.schoolmanagement.entity;

/**
 * Who a Notification's recipients are resolved from, per
 * IMPLEMENTATION_PLAN.md 3.6. targetId (on Notification) is the id relevant
 * to the type: a SchoolClass id for CLASS, a Student id for STUDENT, a
 * Staff id for STAFF; ALL_PARENTS ignores targetId (broadcasts to every
 * PARENT-role user).
 */
public enum NotificationTargetType {
    CLASS,
    STUDENT,
    ALL_PARENTS,
    STAFF
}
