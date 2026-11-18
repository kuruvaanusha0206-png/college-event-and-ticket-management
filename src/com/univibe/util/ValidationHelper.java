package com.univibe.util;

import java.util.regex.Pattern;

/**
 * Input Validation Utility.
 * Demonstrates:
 *  1. Regular Expressions (Pattern & Matcher)
 *  2. String validation algorithms
 *  3. Static utility methods
 */
public final class ValidationHelper {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[6-9]\\d{9}$"); // Indian 10-digit mobile or standard 10-digit
    private static final Pattern ROLL_NUMBER_PATTERN = Pattern.compile("^[A-Za-z0-9-]{4,20}$");

    private ValidationHelper() {}

    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null) return false;
        String cleaned = phone.replaceAll("[\\s-]", "");
        return PHONE_PATTERN.matcher(cleaned).matches() || (cleaned.length() >= 10 && cleaned.length() <= 15 && cleaned.matches("\\d+"));
    }

    public static boolean isValidRollNumber(String roll) {
        if (roll == null) return false;
        return ROLL_NUMBER_PATTERN.matcher(roll.trim()).matches();
    }

    public static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }

    public static boolean isPositiveNumber(int value) {
        return value > 0;
    }

    public static boolean isNonNegativeDouble(double value) {
        return value >= 0.0;
    }
}
