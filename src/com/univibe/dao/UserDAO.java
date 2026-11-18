package com.univibe.dao;

import com.univibe.database.DatabaseConnection;
import com.univibe.model.Admin;
import com.univibe.model.Organizer;
import com.univibe.model.Student;
import com.univibe.model.User;
import com.univibe.util.PasswordHasher;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Users, Students, and Organizers.
 * Demonstrates:
 *  1. JDBC PreparedStatement and ResultSet usage
 *  2. Polymorphism (instantiating Admin, Student, Organizer as User)
 *  3. Collections (List, ArrayList)
 *  4. Exception Handling
 */
public class UserDAO {

    private final DatabaseConnection db;

    public UserDAO() {
        this.db = DatabaseConnection.getInstance();
    }

    /**
     * Authenticates or fetches a user by their unique username.
     */
    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM Users WHERE username = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        }
        return null;
    }

    /**
     * Fetches a user by user_id.
     */
    public User findById(int userId) throws SQLException {
        String sql = "SELECT * FROM Users WHERE user_id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        }
        return null;
    }

    /**
     * Maps ResultSet row to specific User subclass polymorphically.
     */
    private User mapRowToUser(ResultSet rs) throws SQLException {
        int userId = rs.getInt("user_id");
        String username = rs.getString("username");
        String passwordHash = rs.getString("password_hash");
        String salt = rs.getString("salt");
        String fullName = rs.getString("full_name");
        String email = rs.getString("email");
        String phone = rs.getString("phone");
        String role = rs.getString("role");
        String status = rs.getString("status");
        Timestamp createdAt = rs.getTimestamp("created_at");

        if ("ADMIN".equalsIgnoreCase(role)) {
            return new Admin(userId, username, passwordHash, salt, fullName, email, phone, status, createdAt);
        } else if ("STUDENT".equalsIgnoreCase(role)) {
            Student student = new Student(userId, username, passwordHash, salt, fullName, email, phone, status, createdAt, 0, "", "General", 1);
            loadStudentDetails(student);
            return student;
        } else if ("ORGANIZER".equalsIgnoreCase(role)) {
            Organizer org = new Organizer(userId, username, passwordHash, salt, fullName, email, phone, status, createdAt, 0, "", "", "");
            loadOrganizerDetails(org);
            return org;
        }
        return null;
    }

    private void loadStudentDetails(Student student) {
        String sql = "SELECT * FROM Students WHERE user_id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, student.getUserId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    student.setStudentId(rs.getInt("student_id"));
                    student.setRollNumber(rs.getString("roll_number"));
                    student.setDepartment(rs.getString("department"));
                    student.setSemester(rs.getInt("semester"));
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] Failed to load student details: " + e.getMessage());
        }
    }

    private void loadOrganizerDetails(Organizer organizer) {
        String sql = "SELECT * FROM Organizers WHERE user_id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, organizer.getUserId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    organizer.setOrganizerId(rs.getInt("organizer_id"));
                    organizer.setDepartment(rs.getString("department"));
                    organizer.setDesignation(rs.getString("designation"));
                    organizer.setOrganizationUnit(rs.getString("organization_unit"));
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] Failed to load organizer details: " + e.getMessage());
        }
    }

    /**
     * Registers a new Student in database.
     */
    public boolean registerStudent(Student student, String plainPassword) throws SQLException {
        Connection conn = db.getConnection();
        boolean autoCommit = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);

            String salt = PasswordHasher.generateSalt();
            String hash = PasswordHasher.hashPassword(plainPassword, salt);

            String userSql = "INSERT INTO Users (username, password_hash, salt, full_name, email, phone, role, status) VALUES (?, ?, ?, ?, ?, ?, 'STUDENT', 'ACTIVE')";
            int generatedUserId;
            try (PreparedStatement ps = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, student.getUsername());
                ps.setString(2, hash);
                ps.setString(3, salt);
                ps.setString(4, student.getFullName());
                ps.setString(5, student.getEmail());
                ps.setString(6, student.getPhone());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        generatedUserId = keys.getInt(1);
                    } else {
                        throw new SQLException("Failed to retrieve generated user_id");
                    }
                }
            }

            String stuSql = "INSERT INTO Students (user_id, roll_number, department, semester) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(stuSql)) {
                ps.setInt(1, generatedUserId);
                ps.setString(2, student.getRollNumber());
                ps.setString(3, student.getDepartment());
                ps.setInt(4, student.getSemester());
                ps.executeUpdate();
            }

            conn.commit();
            student.setUserId(generatedUserId);
            return true;
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(autoCommit);
        }
    }

    /**
     * Registers a new Event Organizer in database.
     */
    public boolean registerOrganizer(Organizer organizer, String plainPassword) throws SQLException {
        Connection conn = db.getConnection();
        boolean autoCommit = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);

            String salt = PasswordHasher.generateSalt();
            String hash = PasswordHasher.hashPassword(plainPassword, salt);

            String userSql = "INSERT INTO Users (username, password_hash, salt, full_name, email, phone, role, status) VALUES (?, ?, ?, ?, ?, ?, 'ORGANIZER', 'ACTIVE')";
            int generatedUserId;
            try (PreparedStatement ps = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, organizer.getUsername());
                ps.setString(2, hash);
                ps.setString(3, salt);
                ps.setString(4, organizer.getFullName());
                ps.setString(5, organizer.getEmail());
                ps.setString(6, organizer.getPhone());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        generatedUserId = keys.getInt(1);
                    } else {
                        throw new SQLException("Failed to retrieve generated user_id");
                    }
                }
            }

            String orgSql = "INSERT INTO Organizers (user_id, department, designation, organization_unit) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(orgSql)) {
                ps.setInt(1, generatedUserId);
                ps.setString(2, organizer.getDepartment());
                ps.setString(3, organizer.getDesignation());
                ps.setString(4, organizer.getOrganizationUnit());
                ps.executeUpdate();
            }

            conn.commit();
            organizer.setUserId(generatedUserId);
            return true;
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(autoCommit);
        }
    }

    public List<Student> getAllStudents() throws SQLException {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT u.*, s.student_id, s.roll_number, s.department, s.semester " +
                "FROM Users u JOIN Students s ON u.user_id = s.user_id ORDER BY u.full_name";
        try (Statement stmt = db.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Student s = new Student(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("salt"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("status"),
                        rs.getTimestamp("created_at"),
                        rs.getInt("student_id"),
                        rs.getString("roll_number"),
                        rs.getString("department"),
                        rs.getInt("semester")
                );
                list.add(s);
            }
        }
        return list;
    }

    public List<Organizer> getAllOrganizers() throws SQLException {
        List<Organizer> list = new ArrayList<>();
        String sql = "SELECT u.*, o.organizer_id, o.department, o.designation, o.organization_unit " +
                "FROM Users u JOIN Organizers o ON u.user_id = o.user_id ORDER BY u.full_name";
        try (Statement stmt = db.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Organizer o = new Organizer(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("salt"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("status"),
                        rs.getTimestamp("created_at"),
                        rs.getInt("organizer_id"),
                        rs.getString("department"),
                        rs.getString("designation"),
                        rs.getString("organization_unit")
                );
                list.add(o);
            }
        }
        return list;
    }

    public boolean updateUserStatus(int userId, String status) throws SQLException {
        String sql = "UPDATE Users SET status = ? WHERE user_id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteUser(int userId) throws SQLException {
        String sql = "DELETE FROM Users WHERE user_id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        }
    }
}
