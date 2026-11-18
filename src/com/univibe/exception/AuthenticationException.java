package com.univibe.exception;

/**
 * Custom Exception thrown when authentication or authorization fails.
 * Demonstrates: User-Defined Exceptions, Security validation.
 */
public class AuthenticationException extends Exception {
    private final String username;

    public AuthenticationException(String message) {
        super(message);
        this.username = "Unknown";
    }

    public AuthenticationException(String message, String username) {
        super(String.format("%s [Username: %s]", message, username));
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
