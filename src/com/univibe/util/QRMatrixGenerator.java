package com.univibe.util;

import java.security.MessageDigest;

/**
 * Procedural QR-Style 2D Code Matrix Generator.
 * Demonstrates:
 *  1. 2D Arrays (boolean[][])
 *  2. Bitwise operations & Hashing algorithms
 *  3. Procedural graphic pattern generation (Finder patterns, Timing patterns, Data payload)
 *  4. ASCII Art rendering for text console/files
 *  5. Standalone Core Java implementation without external QR dependencies.
 */
public final class QRMatrixGenerator {

    public static final int MATRIX_SIZE = 25; // 25x25 QR-style grid

    private QRMatrixGenerator() {}

    /**
     * Generates a deterministic 25x25 boolean matrix for a given ticket identifier.
     * true = black cell, false = white cell.
     */
    public static boolean[][] generateMatrix(String ticketData) {
        boolean[][] matrix = new boolean[MATRIX_SIZE][MATRIX_SIZE];

        // 1. Draw three standard Finder Patterns (7x7 squares) at corners
        drawFinderPattern(matrix, 0, 0);                               // Top-Left
        drawFinderPattern(matrix, MATRIX_SIZE - 7, 0);                 // Bottom-Left
        drawFinderPattern(matrix, 0, MATRIX_SIZE - 7);                 // Top-Right

        // 2. Draw Timing Patterns (row 6 and col 6)
        for (int i = 8; i < MATRIX_SIZE - 8; i++) {
            matrix[6][i] = (i % 2 == 0);
            matrix[i][6] = (i % 2 == 0);
        }

        // 3. Generate deterministic pseudo-random bits from the ticket string hash
        byte[] hash = computeHash(ticketData);

        int bitIndex = 0;
        for (int r = 0; r < MATRIX_SIZE; r++) {
            for (int c = 0; c < MATRIX_SIZE; c++) {
                // Skip finder patterns and timing lines
                if (isReservedArea(r, c)) continue;

                // Derive bit value from hash stream
                int bytePos = (bitIndex / 8) % hash.length;
                int bitPos = bitIndex % 8;
                boolean isBitSet = ((hash[bytePos] >> bitPos) & 1) == 1;

                // Add slight algorithmic dispersal for visual realism
                matrix[r][c] = isBitSet ^ ((r + c) % 3 == 0);
                bitIndex++;
            }
        }

        return matrix;
    }

    /**
     * Draws a 7x7 standard QR finder pattern (outer box, inner space, solid center).
     */
    private static void drawFinderPattern(boolean[][] matrix, int startRow, int startCol) {
        for (int r = 0; r < 7; r++) {
            for (int c = 0; c < 7; c++) {
                if (r == 0 || r == 6 || c == 0 || c == 6) {
                    matrix[startRow + r][startCol + c] = true; // Outer border
                } else if (r >= 2 && r <= 4 && c >= 2 && c <= 4) {
                    matrix[startRow + r][startCol + c] = true; // Inner solid square
                } else {
                    matrix[startRow + r][startCol + c] = false; // Separator space
                }
            }
        }
    }

    /**
     * Checks if a cell is part of the reserved QR functional areas.
     */
    private static boolean isReservedArea(int r, int c) {
        // Top-Left Finder + 1-cell separator
        if (r <= 7 && c <= 7) return true;
        // Bottom-Left Finder + 1-cell separator
        if (r >= MATRIX_SIZE - 8 && c <= 7) return true;
        // Top-Right Finder + 1-cell separator
        if (r <= 7 && c >= MATRIX_SIZE - 8) return true;
        // Timing lines
        if (r == 6 || c == 6) return true;
        return false;
    }

    /**
     * Computes SHA-256 or MD5 hash bytes for seeding matrix data cells.
     */
    private static byte[] computeHash(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest((data != null ? data : "UNIVIBE_PASS").getBytes());
        } catch (Exception e) {
            byte[] fallback = new byte[32];
            int code = (data != null ? data.hashCode() : 42);
            for (int i = 0; i < fallback.length; i++) {
                fallback[i] = (byte) ((code >> (i % 4 * 8)) ^ i);
            }
            return fallback;
        }
    }

    /**
     * Renders matrix to readable ASCII block characters for text receipts.
     */
    public static String toAsciiArt(boolean[][] matrix) {
        StringBuilder sb = new StringBuilder();
        int size = matrix.length;
        String border = "  " + "██".repeat(size + 2) + "\n";
        sb.append(border);
        for (int r = 0; r < size; r++) {
            sb.append("  ██");
            for (int c = 0; c < size; c++) {
                sb.append(matrix[r][c] ? "██" : "  ");
            }
            sb.append("██\n");
        }
        sb.append(border);
        return sb.toString();
    }
}
