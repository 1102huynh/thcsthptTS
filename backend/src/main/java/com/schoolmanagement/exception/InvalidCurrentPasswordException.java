package com.schoolmanagement.exception;

/**
 * Thrown when a user tries to change their own password but supplies the
 * wrong current password. Deliberately a separate type from
 * {@link org.springframework.security.authentication.BadCredentialsException}
 * (used for login failures) so {@link GlobalExceptionHandler} can return a
 * message that actually matches the "đổi mật khẩu" context instead of the
 * generic "Invalid username or password" login message.
 */
public class InvalidCurrentPasswordException extends RuntimeException {
    public InvalidCurrentPasswordException(String message) {
        super(message);
    }
}
