package com.univibe.interfaces;

/**
 * Interface representing entities that require payment processing.
 * Demonstrates: Interfaces, Polymorphism, Abstraction.
 */
public interface Payable {
    double calculateTotalAmount();
    String getPaymentReference();
    boolean isPaymentSuccessful();
}
