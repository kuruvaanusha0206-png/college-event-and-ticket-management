package com.univibe.exception;

/**
 * Custom Exception thrown when an event has no remaining seats for the requested quantity.
 * Demonstrates: User-Defined Exceptions, Exception Hierarchy.
 */
public class SeatUnavailableException extends Exception {
    private final int requestedSeats;
    private final int availableSeats;

    public SeatUnavailableException(String message) {
        super(message);
        this.requestedSeats = 0;
        this.availableSeats = 0;
    }

    public SeatUnavailableException(String message, int requestedSeats, int availableSeats) {
        super(String.format("%s (Requested: %d, Available: %d)", message, requestedSeats, availableSeats));
        this.requestedSeats = requestedSeats;
        this.availableSeats = availableSeats;
    }

    public int getRequestedSeats() {
        return requestedSeats;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }
}
