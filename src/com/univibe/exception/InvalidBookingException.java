package com.univibe.exception;

/**
 * Custom Exception thrown when booking input is invalid (e.g. quantity <= 0 or past event).
 * Demonstrates: User-Defined Exceptions.
 */
public class InvalidBookingException extends Exception {
    public InvalidBookingException(String message) {
        super(message);
    }

    public InvalidBookingException(String message, Throwable cause) {
        super(message, cause);
    }
}
