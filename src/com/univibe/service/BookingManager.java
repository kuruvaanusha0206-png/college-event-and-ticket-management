package com.univibe.service;

import com.univibe.dao.BookingDAO;
import com.univibe.exception.DuplicateBookingException;
import com.univibe.exception.InvalidBookingException;
import com.univibe.exception.SeatUnavailableException;
import com.univibe.model.Booking;
import com.univibe.model.Event;
import com.univibe.model.Payment;
import com.univibe.model.Student;
import com.univibe.model.Ticket;
import com.univibe.util.FileHandler;

import java.security.SecureRandom;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Booking Management Service.
 * Demonstrates:
 *  1. Business validation & Rule enforcement
 *  2. Unique Reference & Ticket Code generation algorithms
 *  3. Orchestrating transactional DAO persistence
 *  4. Exception propagation (User-Defined Exceptions)
 *  5. Collections & String manipulation
 */
public class BookingManager {

    private final BookingDAO bookingDAO;
    private final SecureRandom random;

    public BookingManager() {
        this.bookingDAO = new BookingDAO();
        this.random = new SecureRandom();
    }

    /**
     * Books tickets for a student with transaction safety.
     */
    public Booking bookTickets(Student student, Event event, int seatQuantity, String paymentMethod)
            throws InvalidBookingException, SeatUnavailableException, DuplicateBookingException, SQLException {

        if (student == null || event == null) {
            throw new InvalidBookingException("Student and Event must not be null.");
        }
        if (seatQuantity <= 0) {
            throw new InvalidBookingException("Seat quantity must be at least 1.");
        }
        if (seatQuantity > 10) {
            throw new InvalidBookingException("A maximum of 10 seats can be booked in a single transaction.");
        }
        if (event.getAvailableSeats() < seatQuantity) {
            throw new SeatUnavailableException("Insufficient seats available.", seatQuantity, event.getAvailableSeats());
        }

        // 1. Generate unique booking reference (BK-YYYYMMDD-XXXX)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String datePart = sdf.format(new Date());
        int randomSuffix = 1000 + random.nextInt(9000);
        String bookingRef = String.format("BK-%s-%d", datePart, randomSuffix);

        // 2. Calculate Total Amount
        double totalAmount = event.getTicketPrice() * seatQuantity;

        Booking booking = new Booking();
        booking.setBookingReference(bookingRef);
        booking.setStudentId(student.getStudentId());
        booking.setEventId(event.getEventId());
        booking.setNumberOfSeats(seatQuantity);
        booking.setTotalAmount(totalAmount);
        booking.setStatus(Booking.STATUS_CONFIRMED);
        booking.setBookingDate(new Date());

        // For display helpers
        booking.setEventTitle(event.getTitle());
        booking.setEventCategory(event.getCategory());
        booking.setVenue(event.getVenue());
        booking.setStudentName(student.getFullName());
        booking.setStudentRollNo(student.getRollNumber());

        // 3. Create Ticket items
        List<Ticket> tickets = new ArrayList<>();
        int currentOccupied = event.getTotalSeats() - event.getAvailableSeats();
        for (int i = 0; i < seatQuantity; i++) {
            int seatSeq = currentOccupied + i + 1;
            char rowChar = (char) ('A' + ((seatSeq - 1) / 20));
            int seatNum = ((seatSeq - 1) % 20) + 1;
            String seatLabel = String.format("%c-%02d", rowChar, seatNum);

            String ticketCode = String.format("TKT-%s-%04d", datePart.substring(2), random.nextInt(10000));
            String qrHash = String.format("UV#%d#%s#%s#%d", event.getEventId(), seatLabel, student.getRollNumber(), random.nextInt(100000));

            Ticket t = new Ticket();
            t.setTicketCode(ticketCode);
            t.setSeatNumber(seatLabel);
            t.setQrHash(qrHash);
            t.setAttendanceStatus(Ticket.ATTENDANCE_PENDING);
            t.setIssuedAt(new Date());

            t.setEventTitle(event.getTitle());
            t.setVenue(event.getVenue());
            t.setStudentName(student.getFullName());
            t.setRollNumber(student.getRollNumber());
            t.setTicketPrice(event.getTicketPrice());

            tickets.add(t);
            booking.addTicket(t);
        }

        // 4. Create Payment item
        Payment payment = new Payment();
        payment.setAmount(totalAmount);
        payment.setPaymentMethod(paymentMethod != null ? paymentMethod : Payment.METHOD_UPI);
        payment.setTransactionRef(String.format("TXN-%s-%d", payment.getPaymentMethod(), random.nextInt(900000) + 100000));
        payment.setPaymentStatus(Payment.STATUS_SUCCESS);
        payment.setPaymentDate(new Date());
        booking.setPayment(payment);

        // 5. Execute transaction in DAO
        bookingDAO.processBookingTransaction(booking, payment, tickets);

        FileHandler.logAuditActivity("BOOKING_SUCCESS", student.getUsername(),
                String.format("Booked %d seats for event '%s' [Ref: %s]", seatQuantity, event.getTitle(), bookingRef));

        return booking;
    }

    /**
     * Cancels an existing booking.
     */
    public boolean cancelBooking(int bookingId, String actorUsername) throws InvalidBookingException, SQLException {
        boolean ok = bookingDAO.cancelBookingTransaction(bookingId);
        if (ok) {
            FileHandler.logAuditActivity("BOOKING_CANCEL", actorUsername, "Cancelled booking ID: " + bookingId);
        }
        return ok;
    }

    public List<Booking> getStudentBookings(int studentId) throws SQLException {
        return bookingDAO.getBookingsByStudent(studentId);
    }

    public List<Booking> getEventBookings(int eventId) throws SQLException {
        return bookingDAO.getBookingsByEvent(eventId);
    }

    public List<Booking> getAllBookings() throws SQLException {
        return bookingDAO.getAllBookings();
    }

    public boolean updateAttendance(int ticketId, String status, String organizerUsername) throws SQLException {
        boolean ok = bookingDAO.updateTicketAttendance(ticketId, status);
        if (ok) {
            FileHandler.logAuditActivity("ATTENDANCE_UPDATE", organizerUsername,
                    String.format("Updated ticket ID %d status to %s", ticketId, status));
        }
        return ok;
    }
}
