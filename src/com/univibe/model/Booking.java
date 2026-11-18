package com.univibe.model;

import com.univibe.interfaces.Payable;
import com.univibe.interfaces.Printable;
import com.univibe.interfaces.Reportable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Booking Entity.
 * Demonstrates:
 *  1. Implementation of multiple interfaces (Payable, Printable, Reportable)
 *  2. ArrayList collection for holding Ticket entities
 *  3. Encapsulation & Constructor Overloading
 *  4. Business logic for cancellation eligibility and receipt formatting
 */
public class Booking implements Payable, Printable, Reportable {

    public static final String STATUS_CONFIRMED = "CONFIRMED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    private int bookingId;
    private String bookingReference;
    private int studentId;
    private int eventId;
    private int numberOfSeats;
    private double totalAmount;
    private Date bookingDate;
    private String status;

    // Associated Entities and Collections
    private List<Ticket> tickets; // ArrayList of tickets
    private Payment payment;

    // Display metadata
    private String eventTitle;
    private String eventCategory;
    private String venue;
    private String eventDateStr;
    private String studentName;
    private String studentRollNo;

    // Default Constructor
    public Booking() {
        this.bookingId = 0;
        this.bookingReference = "";
        this.studentId = 0;
        this.eventId = 0;
        this.numberOfSeats = 1;
        this.totalAmount = 0.0;
        this.bookingDate = new Date();
        this.status = STATUS_CONFIRMED;
        this.tickets = new ArrayList<>();
    }

    // Parameterized Constructor
    public Booking(int bookingId, String bookingReference, int studentId, int eventId,
                   int numberOfSeats, double totalAmount, Date bookingDate, String status) {
        this.bookingId = bookingId;
        this.bookingReference = bookingReference;
        this.studentId = studentId;
        this.eventId = eventId;
        this.numberOfSeats = numberOfSeats;
        this.totalAmount = totalAmount;
        this.bookingDate = (bookingDate != null) ? bookingDate : new Date();
        this.status = (status != null) ? status : STATUS_CONFIRMED;
        this.tickets = new ArrayList<>();
    }

    // Business Logic: Can cancel only if status is CONFIRMED
    public boolean isEligibleForCancellation() {
        return STATUS_CONFIRMED.equalsIgnoreCase(status);
    }

    // Payable Interface Implementation
    @Override
    public double calculateTotalAmount() {
        return totalAmount;
    }

    @Override
    public String getPaymentReference() {
        return (payment != null) ? payment.getTransactionRef() : ("REF-" + bookingReference);
    }

    @Override
    public boolean isPaymentSuccessful() {
        return payment != null && Payment.STATUS_SUCCESS.equalsIgnoreCase(payment.getPaymentStatus());
    }

    // Printable Interface Implementation
    @Override
    public String generatePrintableContent() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StringBuilder sb = new StringBuilder();
        sb.append("======================================================================\n");
        sb.append("                    UNIVIBE - BOOKING CONFIRMATION                    \n");
        sb.append("======================================================================\n");
        sb.append(String.format(" Booking Reference : %s\n", bookingReference));
        sb.append(String.format(" Booking Date      : %s\n", sdf.format(bookingDate)));
        sb.append(String.format(" Booking Status    : %s\n", status));
        sb.append("----------------------------------------------------------------------\n");
        sb.append(String.format(" Event             : %s (%s)\n", (eventTitle != null ? eventTitle : "Event ID: " + eventId), (eventCategory != null ? eventCategory : "")));
        sb.append(String.format(" Venue             : %s\n", (venue != null ? venue : "Main Auditorium")));
        sb.append(String.format(" Event Date & Time : %s\n", (eventDateStr != null ? eventDateStr : "TBD")));
        sb.append(String.format(" Student Name      : %s (%s)\n", (studentName != null ? studentName : "Student ID: " + studentId), (studentRollNo != null ? studentRollNo : "")));
        sb.append(String.format(" Number of Seats   : %d\n", numberOfSeats));
        sb.append(String.format(" Total Amount Paid : ₹%.2f\n", totalAmount));
        sb.append("----------------------------------------------------------------------\n");
        sb.append(" ISSUED DIGITAL TICKETS:\n");
        if (tickets != null && !tickets.isEmpty()) {
            for (int i = 0; i < tickets.size(); i++) {
                Ticket t = tickets.get(i);
                sb.append(String.format("   [%d] Ticket Code: %-16s | Seat: %-6s | Check-in: %s\n",
                        (i + 1), t.getTicketCode(), t.getSeatNumber(), t.getAttendanceStatus()));
            }
        } else {
            sb.append("   (Tickets assigned upon confirmation)\n");
        }
        sb.append("======================================================================\n");
        sb.append(" Keep this booking confirmation safe. Valid student ID card required.\n");
        sb.append("======================================================================\n");
        return sb.toString();
    }

    @Override
    public String getDocumentIdentifier() {
        return bookingReference;
    }

    // Reportable Interface Implementation
    @Override
    public String toReportRow() {
        return String.format("%d,%s,%d,%d,%d,%.2f,%s,%s",
                bookingId,
                bookingReference,
                studentId,
                eventId,
                numberOfSeats,
                totalAmount,
                status,
                bookingDate.toString());
    }

    @Override
    public String[] getReportHeaders() {
        return new String[]{"Booking ID", "Reference", "Student ID", "Event ID", "Seats", "Total Amount", "Status", "Date"};
    }

    // Getters and Setters
    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public String getBookingReference() {
        return bookingReference;
    }

    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public int getNumberOfSeats() {
        return numberOfSeats;
    }

    public void setNumberOfSeats(int numberOfSeats) {
        this.numberOfSeats = numberOfSeats;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Date getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(Date bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
    }

    public void addTicket(Ticket ticket) {
        if (this.tickets == null) {
            this.tickets = new ArrayList<>();
        }
        this.tickets.add(ticket);
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public String getEventCategory() {
        return eventCategory;
    }

    public void setEventCategory(String eventCategory) {
        this.eventCategory = eventCategory;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getEventDateStr() {
        return eventDateStr;
    }

    public void setEventDateStr(String eventDateStr) {
        this.eventDateStr = eventDateStr;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentRollNo() {
        return studentRollNo;
    }

    public void setStudentRollNo(String studentRollNo) {
        this.studentRollNo = studentRollNo;
    }

    @Override
    public String toString() {
        return String.format("Booking %s: %s (%d seats, ₹%.2f, %s)", bookingReference, eventTitle, numberOfSeats, totalAmount, status);
    }
}
