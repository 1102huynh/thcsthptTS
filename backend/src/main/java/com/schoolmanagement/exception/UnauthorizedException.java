package com.schoolmanagement.exception;

/**
 * Exception thrown when a user attempts to perform an action
 * they are not authorized to perform.
 *
 * Example: A student trying to edit timetable, or a teacher
 * trying to edit another teacher's class timetable.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}

