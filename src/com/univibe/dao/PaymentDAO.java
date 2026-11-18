package com.univibe.dao;

import com.univibe.database.DatabaseConnection;
import com.univibe.model.Payment;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for Payment Transactions.
 * Demonstrates:
 *  1. JDBC aggregate functions (SUM, COUNT)
 *  2. Collections: HashMap for revenue categorization
 *  3. Encapsulation and Data Transfer
 */
public class PaymentDAO {

    private final DatabaseConnection db;

    public PaymentDAO() {
        this.db = DatabaseConnection.getInstance();
    }

    public Payment getPaymentByBookingId(int bookingId) throws SQLException {
        String sql = "SELECT * FROM Payments WHERE booking_id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Payment(
                            rs.getInt("payment_id"),
                            rs.getInt("booking_id"),
                            rs.getDouble("amount"),
                            rs.getString("payment_method"),
                            rs.getString("transaction_ref"),
                            rs.getString("payment_status"),
                            rs.getTimestamp("payment_date")
                    );
                }
            }
        }
        return null;
    }

    public double getTotalNetRevenue() throws SQLException {
        String sql = "SELECT SUM(amount) AS net_revenue FROM Payments WHERE payment_status = 'SUCCESS'";
        try (Statement stmt = db.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble("net_revenue");
            }
        }
        return 0.0;
    }

    public Map<String, Double> getRevenueByCategory() throws SQLException {
        Map<String, Double> map = new HashMap<>();
        String sql = "SELECT e.category, SUM(p.amount) AS category_revenue " +
                "FROM Payments p " +
                "JOIN Bookings b ON p.booking_id = b.booking_id " +
                "JOIN Events e ON b.event_id = e.event_id " +
                "WHERE p.payment_status = 'SUCCESS' " +
                "GROUP BY e.category";
        try (Statement stmt = db.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getString("category"), rs.getDouble("category_revenue"));
            }
        }
        return map;
    }

    public List<Payment> getAllPayments() throws SQLException {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT * FROM Payments ORDER BY payment_date DESC";
        try (Statement stmt = db.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Payment(
                        rs.getInt("payment_id"),
                        rs.getInt("booking_id"),
                        rs.getDouble("amount"),
                        rs.getString("payment_method"),
                        rs.getString("transaction_ref"),
                        rs.getString("payment_status"),
                        rs.getTimestamp("payment_date")
                ));
            }
        }
        return list;
    }
}
