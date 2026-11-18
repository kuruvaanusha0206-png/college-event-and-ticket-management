package com.univibe.exception;

/**
 * Custom Exception thrown when a user attempts a duplicate booking that violates rules.
 * Demonstrates: User-Defined Exceptions.
 */
public class DuplicateBookingException extends Exception {
    private final String bookingReference;

    public DuplicateBookingException(String message) {
        super(message);
        this.bookingReference = "";
    }

    public DuplicateBookingException(String message, String bookingReference) {
        super(String.format("%s [Reference: %s]", message, bookingReference));
        this.bookingReference = bookingReference;
    }

    public String getBookingReference() {
        return bookingReference;
    }
}
