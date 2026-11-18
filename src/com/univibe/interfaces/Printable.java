package com.univibe.interfaces;

/**
 * Interface representing printable documents such as digital tickets and receipts.
 * Demonstrates: Interfaces, File Handling contracts.
 */
public interface Printable {
    String generatePrintableContent();
    String getDocumentIdentifier();
}
