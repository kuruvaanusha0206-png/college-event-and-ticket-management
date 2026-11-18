package com.univibe.exception;

/**
 * Custom Exception thrown when an event ID cannot be found in the system.
 * Demonstrates: User-Defined Exceptions.
 */
public class EventNotFoundException extends Exception {
    private final int eventId;

    public EventNotFoundException(int eventId) {
        super(String.format("Event with ID %d was not found in the UniVibe catalog.", eventId));
        this.eventId = eventId;
    }

    public EventNotFoundException(String message) {
        super(message);
        this.eventId = -1;
    }

    public int getEventId() {
        return eventId;
    }
}
