package com.univibe.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Security Utility for Password Hashing and Verification.
 * Demonstrates:
 *  1. Cryptographic hashing using Core Java standard library (MessageDigest)
 *  2. Salt generation using SecureRandom
 *  3. Byte array manipulation and Hex encoding
 *  4. Exception handling for NoSuchAlgorithmException
 */
public final class PasswordHasher {

    public static final String DEFAULT_SALT = "univibe_salt_2026";
    private static final String HASH_ALGORITHM = "SHA-256";

    // Private constructor to prevent instantiation (Utility Class pattern)
    private PasswordHasher() {}

    /**
     * Hashes plain text password with a salt string.
     */
    public static String hashPassword(String password, String salt) {
        if (password == null) password = "";
        if (salt == null) salt = DEFAULT_SALT;

        try {
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            String input = password + salt;
            byte[] hashBytes = md.digest(input.getBytes());

            // Convert byte array to hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            System.err.println("CRITICAL: SHA-256 algorithm not available in current JVM: " + e.getMessage());
            // Fallback deterministic hash code representation if algorithm missing
            return Integer.toHexString((password + salt).hashCode());
        }
    }

    /**
     * Generates a random alphanumeric salt string.
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] saltBytes = new byte[16];
        random.nextBytes(saltBytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : saltBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Verifies whether entered plain text password matches stored hash.
     */
    public static boolean verifyPassword(String enteredPassword, String salt, String storedHash) {
        if (enteredPassword == null || storedHash == null) return false;
        String computedHash = hashPassword(enteredPassword, salt);
        return computedHash.equalsIgnoreCase(storedHash);
    }
}
