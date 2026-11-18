package com.univibe.model;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Payment Entity.
 * Demonstrates:
 *  1. Encapsulation
 *  2. Method Overloading (formatReceipt)
 *  3. Static constants
 */
public class Payment {
    public static final String METHOD_UPI = "UPI";
    public static final String METHOD_CAMPUS_CARD = "CAMPUS_CARD";
    public static final String METHOD_NET_BANKING = "NET_BANKING";
    public static final String METHOD_FREE = "FREE";

    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_REFUNDED = "REFUNDED";
    public static final String STATUS_FAILED = "FAILED";

    private int paymentId;
    private int bookingId;
    private double amount;
    private String paymentMethod;
    private String transactionRef;
    private String paymentStatus;
    private Date paymentDate;

    public Payment() {
        this.paymentId = 0;
        this.bookingId = 0;
        this.amount = 0.0;
        this.paymentMethod = METHOD_UPI;
        this.transactionRef = "";
        this.paymentStatus = STATUS_SUCCESS;
        this.paymentDate = new Date();
    }

    public Payment(int paymentId, int bookingId, double amount, String paymentMethod,
                   String transactionRef, String paymentStatus, Date paymentDate) {
        this.paymentId = paymentId;
        this.bookingId = bookingId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.transactionRef = transactionRef;
        this.paymentStatus = (paymentStatus != null) ? paymentStatus : STATUS_SUCCESS;
        this.paymentDate = (paymentDate != null) ? paymentDate : new Date();
    }

    // Method Overloading Demonstration 1: Standard Receipt
    public String formatReceipt() {
        return formatReceipt("General College Event");
    }

    // Method Overloading Demonstration 2: Detailed Receipt with Event Name
    public String formatReceipt(String eventTitle) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StringBuilder sb = new StringBuilder();
        sb.append("========================================================\n");
        sb.append("               UNIVIBE PAYMENT RECEIPT                  \n");
        sb.append("========================================================\n");
        sb.append(String.format(" Transaction Ref : %s\n", transactionRef));
        sb.append(String.format(" Event           : %s\n", eventTitle));
        sb.append(String.format(" Payment Method  : %s\n", paymentMethod));
        sb.append(String.format(" Amount Paid     : ₹%.2f\n", amount));
        sb.append(String.format(" Status          : %s\n", paymentStatus));
        sb.append(String.format(" Timestamp       : %s\n", sdf.format(paymentDate)));
        sb.append("========================================================\n");
        return sb.toString();
    }

    // Getters and Setters
    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getTransactionRef() {
        return transactionRef;
    }

    public void setTransactionRef(String transactionRef) {
        this.transactionRef = transactionRef;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public Date getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Date paymentDate) {
        this.paymentDate = paymentDate;
    }

    @Override
    public String toString() {
        return String.format("Payment %s: ₹%.2f via %s (%s)", transactionRef, amount, paymentMethod, paymentStatus);
    }
}
