package com.univibe.dao;

import com.univibe.database.DatabaseConnection;
import com.univibe.model.Feedback;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Event Reviews and Star Ratings.
 * Demonstrates:
 *  1. JDBC PreparedStatement execution
 *  2. Collections: List & ArrayList
 *  3. Duplicate check and rating aggregation
 */
public class FeedbackDAO {

    private final DatabaseConnection db;

    public FeedbackDAO() {
        this.db = DatabaseConnection.getInstance();
    }

    public boolean submitFeedback(Feedback feedback) throws SQLException {
        // Upsert feedback
        String sql = "INSERT INTO Feedback (student_id, event_id, rating, comment) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, feedback.getStudentId());
            ps.setInt(2, feedback.getEventId());
            ps.setInt(3, feedback.getRating());
            ps.setString(4, feedback.getComment());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        feedback.setFeedbackId(keys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean hasStudentGivenFeedback(int studentId, int eventId) throws SQLException {
        String sql = "SELECT feedback_id FROM Feedback WHERE student_id = ? AND event_id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public List<Feedback> getFeedbackForEvent(int eventId) throws SQLException {
        List<Feedback> list = new ArrayList<>();
        String sql = "SELECT f.*, u.full_name AS student_name, e.title AS event_title " +
                "FROM Feedback f " +
                "JOIN Students s ON f.student_id = s.student_id " +
                "JOIN Users u ON s.user_id = u.user_id " +
                "JOIN Events e ON f.event_id = e.event_id " +
                "WHERE f.event_id = ? ORDER BY f.submitted_at DESC";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Feedback fb = new Feedback(
                            rs.getInt("feedback_id"),
                            rs.getInt("student_id"),
                            rs.getInt("event_id"),
                            rs.getInt("rating"),
                            rs.getString("comment"),
                            rs.getTimestamp("submitted_at")
                    );
                    fb.setStudentName(rs.getString("student_name"));
                    fb.setEventTitle(rs.getString("event_title"));
                    list.add(fb);
                }
            }
        }
        return list;
    }
}
