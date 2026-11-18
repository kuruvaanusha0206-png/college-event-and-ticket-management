package com.univibe.dao;

import com.univibe.database.DatabaseConnection;
import com.univibe.model.Event;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

/**
 * Data Access Object for Events.
 * Demonstrates:
 *  1. JDBC CRUD queries using PreparedStatement
 *  2. Date parsing and SQL Date conversion
 *  3. Collections (ArrayList, HashMap)
 *  4. Event popularity ranking computation
 *  5. Aggregation queries for Admin and Organizer dashboards
 */
public class EventDAO {

    private final DatabaseConnection db;

    public EventDAO() {
        this.db = DatabaseConnection.getInstance();
    }

    public boolean createEvent(Event event) throws SQLException {
        String sql = "INSERT INTO Events (title, description, category, venue, event_date, event_time, ticket_price, total_seats, available_seats, organizer_id, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, event.getTitle());
            ps.setString(2, event.getDescription());
            ps.setString(3, event.getCategory());
            ps.setString(4, event.getVenue());

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String dateStr = sdf.format(event.getEventDate());
            ps.setString(5, dateStr);

            ps.setString(6, event.getEventTime());
            ps.setDouble(7, event.getTicketPrice());
            ps.setInt(8, event.getTotalSeats());
            ps.setInt(9, event.getTotalSeats()); // initial available = total
            ps.setInt(10, event.getOrganizerId());
            ps.setString(11, event.getStatus());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        event.setEventId(keys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean updateEvent(Event event) throws SQLException {
        String sql = "UPDATE Events SET title = ?, description = ?, category = ?, venue = ?, event_date = ?, event_time = ?, ticket_price = ?, total_seats = ? WHERE event_id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, event.getTitle());
            ps.setString(2, event.getDescription());
            ps.setString(3, event.getCategory());
            ps.setString(4, event.getVenue());

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String dateStr = sdf.format(event.getEventDate());
            ps.setString(5, dateStr);

            ps.setString(6, event.getEventTime());
            ps.setDouble(7, event.getTicketPrice());
            ps.setInt(8, event.getTotalSeats());
            ps.setInt(9, event.getEventId());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteEvent(int eventId) throws SQLException {
        String sql = "DELETE FROM Events WHERE event_id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, eventId);
            return ps.executeUpdate() > 0;
        }
    }

    public Event findById(int eventId) throws SQLException {
        String sql = "SELECT e.*, u.full_name AS organizer_name FROM Events e " +
                "JOIN Organizers o ON e.organizer_id = o.organizer_id " +
                "JOIN Users u ON o.user_id = u.user_id WHERE e.event_id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToEvent(rs);
                }
            }
        }
        return null;
    }

    public List<Event> getAllEvents() throws SQLException {
        List<Event> list = new ArrayList<>();
        String sql = "SELECT e.*, u.full_name AS organizer_name FROM Events e " +
                "JOIN Organizers o ON e.organizer_id = o.organizer_id " +
                "JOIN Users u ON o.user_id = u.user_id ORDER BY e.event_date ASC";
        try (Statement stmt = db.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRowToEvent(rs));
            }
        }
        enrichEventMetrics(list);
        return list;
    }

    public List<Event> getApprovedUpcomingEvents() throws SQLException {
        List<Event> list = new ArrayList<>();
        String sql = "SELECT e.*, u.full_name AS organizer_name FROM Events e " +
                "JOIN Organizers o ON e.organizer_id = o.organizer_id " +
                "JOIN Users u ON o.user_id = u.user_id " +
                "WHERE e.status = 'APPROVED' ORDER BY e.event_date ASC";
        try (Statement stmt = db.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRowToEvent(rs));
            }
        }
        enrichEventMetrics(list);
        return list;
    }

    public List<Event> getEventsByOrganizer(int organizerId) throws SQLException {
        List<Event> list = new ArrayList<>();
        String sql = "SELECT e.*, u.full_name AS organizer_name FROM Events e " +
                "JOIN Organizers o ON e.organizer_id = o.organizer_id " +
                "JOIN Users u ON o.user_id = u.user_id " +
                "WHERE e.organizer_id = ? ORDER BY e.event_date DESC";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, organizerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToEvent(rs));
                }
            }
        }
        enrichEventMetrics(list);
        return list;
    }

    public boolean updateEventStatus(int eventId, String status) throws SQLException {
        String sql = "UPDATE Events SET status = ? WHERE event_id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, eventId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Atomically increments or decrements available seats during booking/cancellation.
     */
    public boolean updateAvailableSeats(Connection conn, int eventId, int delta) throws SQLException {
        String sql = "UPDATE Events SET available_seats = available_seats + ? WHERE event_id = ? AND available_seats + ? >= 0";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setInt(2, eventId);
            ps.setInt(3, delta);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Enriches events with real-time feedback ratings and total booking counts.
     */
    private void enrichEventMetrics(List<Event> events) {
        if (events == null || events.isEmpty()) return;
        try {
            Connection conn = db.getConnection();

            // Ratings Map
            String ratingSql = "SELECT event_id, AVG(rating) as avg_r, COUNT(*) as cnt FROM Feedback GROUP BY event_id";
            Map<Integer, double[]> ratingMap = new HashMap<>();
            try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(ratingSql)) {
                while (rs.next()) {
                    ratingMap.put(rs.getInt("event_id"), new double[]{rs.getDouble("avg_r"), rs.getInt("cnt")});
                }
            }

            // Bookings Count Map
            String bookSql = "SELECT event_id, SUM(number_of_seats) as total_booked FROM Bookings WHERE status = 'CONFIRMED' GROUP BY event_id";
            Map<Integer, Integer> bookMap = new HashMap<>();
            try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(bookSql)) {
                while (rs.next()) {
                    bookMap.put(rs.getInt("event_id"), rs.getInt("total_booked"));
                }
            }

            for (Event e : events) {
                if (ratingMap.containsKey(e.getEventId())) {
                    double[] r = ratingMap.get(e.getEventId());
                    e.setAverageRating(r[0]);
                    e.setFeedbackCount((int) r[1]);
                }
                if (bookMap.containsKey(e.getEventId())) {
                    e.setTotalBookings(bookMap.get(e.getEventId()));
                }
            }
        } catch (SQLException ex) {
            System.err.println("[EventDAO] Failed to enrich event metrics: " + ex.getMessage());
        }
    }

    private Event mapRowToEvent(ResultSet rs) throws SQLException {
        Event event = new Event();
        event.setEventId(rs.getInt("event_id"));
        event.setTitle(rs.getString("title"));
        event.setDescription(rs.getString("description"));
        event.setCategory(rs.getString("category"));
        event.setVenue(rs.getString("venue"));

        // Handle date
        String dateStr = rs.getString("event_date");
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            event.setEventDate(sdf.parse(dateStr));
        } catch (Exception e) {
            event.setEventDate(new Date());
        }

        event.setEventTime(rs.getString("event_time"));
        event.setTicketPrice(rs.getDouble("ticket_price"));
        event.setTotalSeats(rs.getInt("total_seats"));
        event.setAvailableSeats(rs.getInt("available_seats"));
        event.setOrganizerId(rs.getInt("organizer_id"));
        event.setStatus(rs.getString("status"));

        try {
            event.setOrganizerName(rs.getString("organizer_name"));
        } catch (SQLException ignored) {}

        try {
            event.setCreatedAt(rs.getTimestamp("created_at"));
        } catch (SQLException ignored) {}

        return event;
    }
}
