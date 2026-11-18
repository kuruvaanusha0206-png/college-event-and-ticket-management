package com.univibe.dao;

import com.univibe.database.DatabaseConnection;
import com.univibe.exception.DuplicateBookingException;
import com.univibe.exception.SeatUnavailableException;
import com.univibe.model.Booking;
import com.univibe.model.Payment;
import com.univibe.model.Ticket;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Data Access Object for Ticket Bookings, Payments, and Digital Passes.
 * Demonstrates:
 *  1. Database Transactions (setAutoCommit(false), commit(), rollback())
 *  2. Safe concurrency and atomic seat reservation
 *  3. Generated Keys retrieval
 *  4. Collections (List, ArrayList)
 *  5. Custom Exceptions handling (SeatUnavailableException, DuplicateBookingException)
 */
public class BookingDAO {

    private final DatabaseConnection db;

    public BookingDAO() {
        this.db = DatabaseConnection.getInstance();
    }

    /**
     * Executes an atomic database transaction to book tickets:
     *  Step 1: Check available seats and lock
     *  Step 2: Decrement available_seats in Events
     *  Step 3: Insert record into Bookings
     *  Step 4: Insert records into Tickets
     *  Step 5: Insert record into Payments
     *  Step 6: Commit transaction
     */
    public boolean processBookingTransaction(Booking booking, Payment payment, List<Ticket> tickets)
            throws SQLException, SeatUnavailableException, DuplicateBookingException {

        Connection conn = db.getConnection();
        boolean previousAutoCommit = conn.getAutoCommit();

        try {
            conn.setAutoCommit(false); // Begin Transaction

            // Step 1: Check seat availability with locking check
            int availableSeats = 0;
            String checkSeatsSql = "SELECT available_seats FROM Events WHERE event_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(checkSeatsSql)) {
                ps.setInt(1, booking.getEventId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        availableSeats = rs.getInt("available_seats");
                    } else {
                        throw new SQLException("Event not found with ID: " + booking.getEventId());
                    }
                }
            }

            if (availableSeats < booking.getNumberOfSeats()) {
                throw new SeatUnavailableException("Not enough seats available for this event.",
                        booking.getNumberOfSeats(), availableSeats);
            }

            // Step 2: Prevent duplicate active booking by same student for same event
            String checkDupSql = "SELECT booking_id FROM Bookings WHERE student_id = ? AND event_id = ? AND status = 'CONFIRMED'";
            try (PreparedStatement ps = conn.prepareStatement(checkDupSql)) {
                ps.setInt(1, booking.getStudentId());
                ps.setInt(2, booking.getEventId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        throw new DuplicateBookingException("You already have an active confirmed booking for this event.");
                    }
                }
            }

            // Step 3: Decrement available seats in Events table
            String updateSeatsSql = "UPDATE Events SET available_seats = available_seats - ? WHERE event_id = ? AND available_seats >= ?";
            try (PreparedStatement ps = conn.prepareStatement(updateSeatsSql)) {
                ps.setInt(1, booking.getNumberOfSeats());
                ps.setInt(2, booking.getEventId());
                ps.setInt(3, booking.getNumberOfSeats());
                int rows = ps.executeUpdate();
                if (rows == 0) {
                    throw new SeatUnavailableException("Concurrent seat contention: seats sold out.",
                            booking.getNumberOfSeats(), availableSeats);
                }
            }

            // Step 4: Insert Booking record
            String insertBookingSql = "INSERT INTO Bookings (booking_reference, student_id, event_id, number_of_seats, total_amount, status) VALUES (?, ?, ?, ?, ?, 'CONFIRMED')";
            int generatedBookingId;
            try (PreparedStatement ps = conn.prepareStatement(insertBookingSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, booking.getBookingReference());
                ps.setInt(2, booking.getStudentId());
                ps.setInt(3, booking.getEventId());
                ps.setInt(4, booking.getNumberOfSeats());
                ps.setDouble(5, booking.getTotalAmount());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        generatedBookingId = keys.getInt(1);
                        booking.setBookingId(generatedBookingId);
                    } else {
                        throw new SQLException("Failed to retrieve generated booking_id");
                    }
                }
            }

            // Step 5: Insert Tickets records
            String insertTicketSql = "INSERT INTO Tickets (booking_id, ticket_code, seat_number, qr_hash, attendance_status) VALUES (?, ?, ?, ?, 'PENDING')";
            try (PreparedStatement ps = conn.prepareStatement(insertTicketSql, Statement.RETURN_GENERATED_KEYS)) {
                for (Ticket t : tickets) {
                    ps.setInt(1, generatedBookingId);
                    ps.setString(2, t.getTicketCode());
                    ps.setString(3, t.getSeatNumber());
                    ps.setString(4, t.getQrHash());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            t.setTicketId(keys.getInt(1));
                        }
                    }
                }
            }

            // Step 6: Insert Payment record
            if (payment != null) {
                String insertPaymentSql = "INSERT INTO Payments (booking_id, amount, payment_method, transaction_ref, payment_status) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertPaymentSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, generatedBookingId);
                    ps.setDouble(2, payment.getAmount());
                    ps.setString(3, payment.getPaymentMethod());
                    ps.setString(4, payment.getTransactionRef());
                    ps.setString(5, payment.getPaymentStatus());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            payment.setPaymentId(keys.getInt(1));
                        }
                    }
                }
            }

            // Commit Transaction
            conn.commit();
            return true;

        } catch (SQLException | SeatUnavailableException | DuplicateBookingException ex) {
            conn.rollback(); // Rollback all changes upon error
            throw ex;
        } finally {
            conn.setAutoCommit(previousAutoCommit);
        }
    }

    /**
     * Cancels a booking safely using a database transaction:
     * Restores seats to the event and marks booking CANCELLED and payment REFUNDED.
     */
    public boolean cancelBookingTransaction(int bookingId) throws SQLException {
        Connection conn = db.getConnection();
        boolean previousAutoCommit = conn.getAutoCommit();

        try {
            conn.setAutoCommit(false);

            // Fetch booking info
            int eventId = 0;
            int seatsToRestore = 0;
            String status = "";
            String fetchSql = "SELECT event_id, number_of_seats, status FROM Bookings WHERE booking_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(fetchSql)) {
                ps.setInt(1, bookingId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        eventId = rs.getInt("event_id");
                        seatsToRestore = rs.getInt("number_of_seats");
                        status = rs.getString("status");
                    } else {
                        throw new SQLException("Booking not found: " + bookingId);
                    }
                }
            }

            if (!Booking.STATUS_CONFIRMED.equalsIgnoreCase(status)) {
                throw new SQLException("Only CONFIRMED bookings can be cancelled.");
            }

            // Step 1: Mark Booking CANCELLED
            String updateBookingSql = "UPDATE Bookings SET status = 'CANCELLED' WHERE booking_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateBookingSql)) {
                ps.setInt(1, bookingId);
                ps.executeUpdate();
            }

            // Step 2: Restore available seats in Events table
            String restoreSeatsSql = "UPDATE Events SET available_seats = available_seats + ? WHERE event_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(restoreSeatsSql)) {
                ps.setInt(1, seatsToRestore);
                ps.setInt(2, eventId);
                ps.executeUpdate();
            }

            // Step 3: Update Payment status to REFUNDED
            String updatePaymentSql = "UPDATE Payments SET payment_status = 'REFUNDED' WHERE booking_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updatePaymentSql)) {
                ps.setInt(1, bookingId);
                ps.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException ex) {
            conn.rollback();
            throw ex;
        } finally {
            conn.setAutoCommit(previousAutoCommit);
        }
    }

    public List<Booking> getBookingsByStudent(int studentId) throws SQLException {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT b.*, e.title as event_title, e.category as event_category, e.venue, e.event_date, e.event_time, " +
                "u.full_name as student_name, s.roll_number " +
                "FROM Bookings b " +
                "JOIN Events e ON b.event_id = e.event_id " +
                "JOIN Students s ON b.student_id = s.student_id " +
                "JOIN Users u ON s.user_id = u.user_id " +
                "WHERE b.student_id = ? ORDER BY b.booking_date DESC";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Booking b = mapRowToBooking(rs);
                    b.setTickets(getTicketsForBooking(b.getBookingId()));
                    list.add(b);
                }
            }
        }
        return list;
    }

    public List<Booking> getBookingsByEvent(int eventId) throws SQLException {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT b.*, e.title as event_title, e.category as event_category, e.venue, e.event_date, e.event_time, " +
                "u.full_name as student_name, s.roll_number " +
                "FROM Bookings b " +
                "JOIN Events e ON b.event_id = e.event_id " +
                "JOIN Students s ON b.student_id = s.student_id " +
                "JOIN Users u ON s.user_id = u.user_id " +
                "WHERE b.event_id = ? ORDER BY b.booking_date DESC";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Booking b = mapRowToBooking(rs);
                    b.setTickets(getTicketsForBooking(b.getBookingId()));
                    list.add(b);
                }
            }
        }
        return list;
    }

    public List<Booking> getAllBookings() throws SQLException {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT b.*, e.title as event_title, e.category as event_category, e.venue, e.event_date, e.event_time, " +
                "u.full_name as student_name, s.roll_number " +
                "FROM Bookings b " +
                "JOIN Events e ON b.event_id = e.event_id " +
                "JOIN Students s ON b.student_id = s.student_id " +
                "JOIN Users u ON s.user_id = u.user_id " +
                "ORDER BY b.booking_date DESC";
        try (Statement stmt = db.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Booking b = mapRowToBooking(rs);
                b.setTickets(getTicketsForBooking(b.getBookingId()));
                list.add(b);
            }
        }
        return list;
    }

    public List<Ticket> getTicketsForBooking(int bookingId) throws SQLException {
        List<Ticket> tickets = new ArrayList<>();
        String sql = "SELECT t.*, e.title as event_title, e.venue, e.event_date, e.event_time, e.ticket_price, " +
                "u.full_name as student_name, s.roll_number " +
                "FROM Tickets t " +
                "JOIN Bookings b ON t.booking_id = b.booking_id " +
                "JOIN Events e ON b.event_id = e.event_id " +
                "JOIN Students s ON b.student_id = s.student_id " +
                "JOIN Users u ON s.user_id = u.user_id " +
                "WHERE t.booking_id = ? ORDER BY t.ticket_id ASC";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Ticket t = new Ticket(
                            rs.getInt("ticket_id"),
                            rs.getInt("booking_id"),
                            rs.getString("ticket_code"),
                            rs.getString("seat_number"),
                            rs.getString("qr_hash"),
                            rs.getString("attendance_status"),
                            rs.getTimestamp("issued_at")
                    );
                    t.setEventTitle(rs.getString("event_title"));
                    t.setVenue(rs.getString("venue"));
                    t.setEventDateStr(rs.getString("event_date"));
                    t.setEventTimeStr(rs.getString("event_time"));
                    t.setTicketPrice(rs.getDouble("ticket_price"));
                    t.setStudentName(rs.getString("student_name"));
                    t.setRollNumber(rs.getString("roll_number"));
                    tickets.add(t);
                }
            }
        }
        return tickets;
    }

    public boolean updateTicketAttendance(int ticketId, String status) throws SQLException {
        String sql = "UPDATE Tickets SET attendance_status = ? WHERE ticket_id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, ticketId);
            return ps.executeUpdate() > 0;
        }
    }

    private Booking mapRowToBooking(ResultSet rs) throws SQLException {
        Booking b = new Booking(
                rs.getInt("booking_id"),
                rs.getString("booking_reference"),
                rs.getInt("student_id"),
                rs.getInt("event_id"),
                rs.getInt("number_of_seats"),
                rs.getDouble("total_amount"),
                rs.getTimestamp("booking_date"),
                rs.getString("status")
        );
        b.setEventTitle(rs.getString("event_title"));
        b.setEventCategory(rs.getString("event_category"));
        b.setVenue(rs.getString("venue"));
        b.setEventDateStr(rs.getString("event_date") + " " + rs.getString("event_time"));
        b.setStudentName(rs.getString("student_name"));
        b.setStudentRollNo(rs.getString("roll_number"));
        return b;
    }
}
