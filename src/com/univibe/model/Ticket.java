package com.univibe.model;

import com.univibe.interfaces.Printable;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Individual Digital Ticket Entity.
 * Demonstrates:
 *  1. Printable Interface Implementation
 *  2. Encapsulation
 *  3. String Building & Date Formatting
 *  4. Attendance Management (PENDING, CHECKED_IN, ABSENT)
 */
public class Ticket implements Printable {

    public static final String ATTENDANCE_PENDING = "PENDING";
    public static final String ATTENDANCE_CHECKED_IN = "CHECKED_IN";
    public static final String ATTENDANCE_ABSENT = "ABSENT";

    private int ticketId;
    private int bookingId;
    private String ticketCode;
    private String seatNumber;
    private String qrHash;
    private String attendanceStatus;
    private Date issuedAt;

    // Associated metadata for display and printing
    private String eventTitle;
    private String studentName;
    private String rollNumber;
    private String venue;
    private String eventDateStr;
    private String eventTimeStr;
    private double ticketPrice;

    public Ticket() {
        this.ticketId = 0;
        this.bookingId = 0;
        this.ticketCode = "";
        this.seatNumber = "GA-01";
        this.qrHash = "";
        this.attendanceStatus = ATTENDANCE_PENDING;
        this.issuedAt = new Date();
    }

    public Ticket(int ticketId, int bookingId, String ticketCode, String seatNumber,
                  String qrHash, String attendanceStatus, Date issuedAt) {
        this.ticketId = ticketId;
        this.bookingId = bookingId;
        this.ticketCode = ticketCode;
        this.seatNumber = seatNumber;
        this.qrHash = qrHash;
        this.attendanceStatus = (attendanceStatus != null) ? attendanceStatus : ATTENDANCE_PENDING;
        this.issuedAt = (issuedAt != null) ? issuedAt : new Date();
    }

    @Override
    public String generatePrintableContent() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StringBuilder sb = new StringBuilder();
        sb.append("======================================================================\n");
        sb.append("                    UNIVIBE - OFFICIAL ADMIT PASS                     \n");
        sb.append("======================================================================\n");
        sb.append(String.format(" Ticket ID        : %s\n", ticketCode));
        sb.append(String.format(" Event Name       : %s\n", (eventTitle != null ? eventTitle : "Campus Event")));
        sb.append(String.format(" Attendee Name    : %s\n", (studentName != null ? studentName : "Student Attendee")));
        sb.append(String.format(" Roll Number      : %s\n", (rollNumber != null ? rollNumber : "N/A")));
        sb.append(String.format(" Venue            : %s\n", (venue != null ? venue : "College Auditorium")));
        sb.append(String.format(" Date & Time      : %s at %s\n", (eventDateStr != null ? eventDateStr : "TBD"), (eventTimeStr != null ? eventTimeStr : "TBD")));
        sb.append(String.format(" Seat Allocated   : %s\n", seatNumber));
        sb.append(String.format(" Price Paid       : ₹%.2f\n", ticketPrice));
        sb.append(String.format(" Attendance Status: %s\n", attendanceStatus));
        sb.append(String.format(" Issued Timestamp : %s\n", sdf.format(issuedAt)));
        sb.append("----------------------------------------------------------------------\n");
        sb.append(String.format(" DIGITAL VERIFICATION HASH: %s\n", qrHash));
        sb.append(" [Present this pass or scan the digital QR at the entry gate]\n");
        sb.append("======================================================================\n");
        return sb.toString();
    }

    @Override
    public String getDocumentIdentifier() {
        return ticketCode;
    }

    // Getters and Setters
    public int getTicketId() {
        return ticketId;
    }

    public void setTicketId(int ticketId) {
        this.ticketId = ticketId;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public String getTicketCode() {
        return ticketCode;
    }

    public void setTicketCode(String ticketCode) {
        this.ticketCode = ticketCode;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getQrHash() {
        return qrHash;
    }

    public void setQrHash(String qrHash) {
        this.qrHash = qrHash;
    }

    public String getAttendanceStatus() {
        return attendanceStatus;
    }

    public void setAttendanceStatus(String attendanceStatus) {
        this.attendanceStatus = attendanceStatus;
    }

    public Date getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Date issuedAt) {
        this.issuedAt = issuedAt;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
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

    public String getEventTimeStr() {
        return eventTimeStr;
    }

    public void setEventTimeStr(String eventTimeStr) {
        this.eventTimeStr = eventTimeStr;
    }

    public double getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(double ticketPrice) {
        this.ticketPrice = ticketPrice;
    }

    @Override
    public String toString() {
        return String.format("Ticket %s (Seat %s, Status: %s)", ticketCode, seatNumber, attendanceStatus);
    }
}
