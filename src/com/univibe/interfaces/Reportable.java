package com.univibe.interfaces;

/**
 * Interface representing entities capable of generating tabular report lines.
 * Demonstrates: Interfaces, Collections, and Formatting.
 */
public interface Reportable {
    String toReportRow();
    String[] getReportHeaders();
}
