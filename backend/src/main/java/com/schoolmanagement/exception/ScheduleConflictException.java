package com.schoolmanagement.exception;

/**
 * Thrown when a timetable slot would double-book a teacher, room, or class —
 * same semester, day of week, and period already taken by something else.
 */
public class ScheduleConflictException extends RuntimeException {
    public ScheduleConflictException(String message) {
        super(message);
    }
}
