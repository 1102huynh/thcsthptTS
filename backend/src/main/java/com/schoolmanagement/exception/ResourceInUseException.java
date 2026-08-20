package com.schoolmanagement.exception;

/**
 * Thrown when a delete is refused because other records still depend on the
 * resource (e.g. deleting a class that still has students assigned to it).
 */
public class ResourceInUseException extends RuntimeException {
    public ResourceInUseException(String message) {
        super(message);
    }
}
