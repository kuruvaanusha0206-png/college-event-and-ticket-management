package com.univibe.database;

import com.univibe.util.PasswordHasher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Database Schema and Sample Data Initializer.
 * Demonstrates:
 *  1. JDBC Statement & PreparedStatement execution
 *  2. DDL Script generation and schema creation
 *  3. Cross-database compatibility (MySQL / SQLite syntax adaptation)
 *  4. Automatic data seeding with hashed passwords and rich academic demo records
 */
public class DatabaseInitializer {

    public static void initializeDatabase() {
        DatabaseConnection db = DatabaseConnection.getInstance();
        Connection conn = db.getConnection();
        if (conn == null) {
            System.err.println("[DatabaseInitializer] Connection is null. Schema initialization aborted.");
            return;
        }

        boolean isMySQL = db.isMySQL();

        try (Statement stmt = conn.createStatement()) {
            if (isMySQL) {
                createMySQLSchema(stmt);
            } else {
                createSQLiteSchema(stmt);
            }
            seedInitialDataIfEmpty(conn);
            System.out.println("[DatabaseInitializer] Database initialized successfully (" + db.getDatabaseType() + ").");
        } catch (Exception e) {
            System.err.println("[DatabaseInitializer] Schema setup exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void createMySQLSchema(Statement stmt) throws Exception {
        stmt.execute("CREATE TABLE IF NOT EXISTS Users (" +
                "user_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(50) NOT NULL UNIQUE, " +
                "password_hash VARCHAR(128) NOT NULL, " +
                "salt VARCHAR(64) NOT NULL, " +
                "full_name VARCHAR(100) NOT NULL, " +
                "email VARCHAR(100) NOT NULL UNIQUE, " +
                "phone VARCHAR(20) NOT NULL, " +
                "role VARCHAR(20) NOT NULL, " +
                "status VARCHAR(20) DEFAULT 'ACTIVE', " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

        stmt.execute("CREATE TABLE IF NOT EXISTS Students (" +
                "student_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "user_id INT NOT NULL UNIQUE, " +
                "roll_number VARCHAR(30) NOT NULL UNIQUE, " +
                "department VARCHAR(60) NOT NULL, " +
                "semester INT NOT NULL, " +
                "FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE)");

        stmt.execute("CREATE TABLE IF NOT EXISTS Organizers (" +
                "organizer_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "user_id INT NOT NULL UNIQUE, " +
                "department VARCHAR(60) NOT NULL, " +
                "designation VARCHAR(60) NOT NULL, " +
                "organization_unit VARCHAR(100) NOT NULL, " +
                "FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE)");

        stmt.execute("CREATE TABLE IF NOT EXISTS Events (" +
                "event_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "title VARCHAR(150) NOT NULL, " +
                "description TEXT NOT NULL, " +
                "category VARCHAR(60) NOT NULL, " +
                "venue VARCHAR(100) NOT NULL, " +
                "event_date DATE NOT NULL, " +
                "event_time VARCHAR(30) NOT NULL, " +
                "ticket_price DECIMAL(10,2) NOT NULL DEFAULT 0.00, " +
                "total_seats INT NOT NULL, " +
                "available_seats INT NOT NULL, " +
                "organizer_id INT NOT NULL, " +
                "status VARCHAR(20) DEFAULT 'PENDING', " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (organizer_id) REFERENCES Organizers(organizer_id) ON DELETE CASCADE)");

        stmt.execute("CREATE TABLE IF NOT EXISTS Bookings (" +
                "booking_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "booking_reference VARCHAR(50) NOT NULL UNIQUE, " +
                "student_id INT NOT NULL, " +
                "event_id INT NOT NULL, " +
                "number_of_seats INT NOT NULL DEFAULT 1, " +
                "total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00, " +
                "booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "status VARCHAR(20) DEFAULT 'CONFIRMED', " +
                "FOREIGN KEY (student_id) REFERENCES Students(student_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (event_id) REFERENCES Events(event_id) ON DELETE CASCADE)");

        stmt.execute("CREATE TABLE IF NOT EXISTS Tickets (" +
                "ticket_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "booking_id INT NOT NULL, " +
                "ticket_code VARCHAR(50) NOT NULL UNIQUE, " +
                "seat_number VARCHAR(20) NOT NULL, " +
                "qr_hash VARCHAR(100) NOT NULL, " +
                "attendance_status VARCHAR(20) DEFAULT 'PENDING', " +
                "issued_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (booking_id) REFERENCES Bookings(booking_id) ON DELETE CASCADE)");

        stmt.execute("CREATE TABLE IF NOT EXISTS Payments (" +
                "payment_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "booking_id INT NOT NULL UNIQUE, " +
                "amount DECIMAL(10,2) NOT NULL, " +
                "payment_method VARCHAR(50) NOT NULL, " +
                "transaction_ref VARCHAR(64) NOT NULL UNIQUE, " +
                "payment_status VARCHAR(20) DEFAULT 'SUCCESS', " +
                "payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (booking_id) REFERENCES Bookings(booking_id) ON DELETE CASCADE)");

        stmt.execute("CREATE TABLE IF NOT EXISTS Feedback (" +
                "feedback_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "student_id INT NOT NULL, " +
                "event_id INT NOT NULL, " +
                "rating INT NOT NULL, " +
                "comment TEXT, " +
                "submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (student_id) REFERENCES Students(student_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (event_id) REFERENCES Events(event_id) ON DELETE CASCADE)");
    }

    private static void createSQLiteSchema(Statement stmt) throws Exception {
        stmt.execute("CREATE TABLE IF NOT EXISTS Users (" +
                "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT NOT NULL UNIQUE, " +
                "password_hash TEXT NOT NULL, " +
                "salt TEXT NOT NULL, " +
                "full_name TEXT NOT NULL, " +
                "email TEXT NOT NULL UNIQUE, " +
                "phone TEXT NOT NULL, " +
                "role TEXT NOT NULL, " +
                "status TEXT DEFAULT 'ACTIVE', " +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP)");

        stmt.execute("CREATE TABLE IF NOT EXISTS Students (" +
                "student_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL UNIQUE, " +
                "roll_number TEXT NOT NULL UNIQUE, " +
                "department TEXT NOT NULL, " +
                "semester INTEGER NOT NULL, " +
                "FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE)");

        stmt.execute("CREATE TABLE IF NOT EXISTS Organizers (" +
                "organizer_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL UNIQUE, " +
                "department TEXT NOT NULL, " +
                "designation TEXT NOT NULL, " +
                "organization_unit TEXT NOT NULL, " +
                "FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE)");

        stmt.execute("CREATE TABLE IF NOT EXISTS Events (" +
                "event_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT NOT NULL, " +
                "description TEXT NOT NULL, " +
                "category TEXT NOT NULL, " +
                "venue TEXT NOT NULL, " +
                "event_date TEXT NOT NULL, " +
                "event_time TEXT NOT NULL, " +
                "ticket_price REAL NOT NULL DEFAULT 0.00, " +
                "total_seats INTEGER NOT NULL, " +
                "available_seats INTEGER NOT NULL, " +
                "organizer_id INTEGER NOT NULL, " +
                "status TEXT DEFAULT 'PENDING', " +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (organizer_id) REFERENCES Organizers(organizer_id) ON DELETE CASCADE)");

        stmt.execute("CREATE TABLE IF NOT EXISTS Bookings (" +
                "booking_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "booking_reference TEXT NOT NULL UNIQUE, " +
                "student_id INTEGER NOT NULL, " +
                "event_id INTEGER NOT NULL, " +
                "number_of_seats INTEGER NOT NULL DEFAULT 1, " +
                "total_amount REAL NOT NULL DEFAULT 0.00, " +
                "booking_date DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "status TEXT DEFAULT 'CONFIRMED', " +
                "FOREIGN KEY (student_id) REFERENCES Students(student_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (event_id) REFERENCES Events(event_id) ON DELETE CASCADE)");

        stmt.execute("CREATE TABLE IF NOT EXISTS Tickets (" +
                "ticket_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "booking_id INTEGER NOT NULL, " +
                "ticket_code TEXT NOT NULL UNIQUE, " +
                "seat_number TEXT NOT NULL, " +
                "qr_hash TEXT NOT NULL, " +
                "attendance_status TEXT DEFAULT 'PENDING', " +
                "issued_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (booking_id) REFERENCES Bookings(booking_id) ON DELETE CASCADE)");

        stmt.execute("CREATE TABLE IF NOT EXISTS Payments (" +
                "payment_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "booking_id INTEGER NOT NULL UNIQUE, " +
                "amount REAL NOT NULL, " +
                "payment_method TEXT NOT NULL, " +
                "transaction_ref TEXT NOT NULL UNIQUE, " +
                "payment_status TEXT DEFAULT 'SUCCESS', " +
                "payment_date DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (booking_id) REFERENCES Bookings(booking_id) ON DELETE CASCADE)");

        stmt.execute("CREATE TABLE IF NOT EXISTS Feedback (" +
                "feedback_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "student_id INTEGER NOT NULL, " +
                "event_id INTEGER NOT NULL, " +
                "rating INTEGER NOT NULL, " +
                "comment TEXT, " +
                "submitted_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (student_id) REFERENCES Students(student_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (event_id) REFERENCES Events(event_id) ON DELETE CASCADE)");
    }

    private static void seedInitialDataIfEmpty(Connection conn) {
        try {
            // Check if users exist
            Statement checkStmt = conn.createStatement();
            ResultSet rs = checkStmt.executeQuery("SELECT COUNT(*) AS cnt FROM Users");
            if (rs.next() && rs.getInt("cnt") > 0) {
                return; // Data already exists
            }
            rs.close();
            checkStmt.close();

            System.out.println("[DatabaseInitializer] Seeding comprehensive academic dataset...");

            String salt = PasswordHasher.DEFAULT_SALT;
            String adminHash = PasswordHasher.hashPassword("admin123", salt);
            String orgHash = PasswordHasher.hashPassword("org123", salt);
            String studentHash = PasswordHasher.hashPassword("student123", salt);

            // 1. Insert Users
            insertUser(conn, 1, "admin", adminHash, salt, "System Administrator", "admin@univibe.edu", "9876543210", "ADMIN");
            insertUser(conn, 2, "org_sarah", orgHash, salt, "Prof. Sarah Jenkins", "s.jenkins@univibe.edu", "9876543211", "ORGANIZER");
            insertUser(conn, 3, "org_rahul", orgHash, salt, "Dr. Rahul Sharma", "r.sharma@univibe.edu", "9876543212", "ORGANIZER");
            insertUser(conn, 4, "student_alex", studentHash, salt, "Alex Mercer", "alex.mercer@univibe.edu", "9876543213", "STUDENT");
            insertUser(conn, 5, "student_priya", studentHash, salt, "Priya Patel", "priya.patel@univibe.edu", "9876543214", "STUDENT");
            insertUser(conn, 6, "student_david", studentHash, salt, "David Kim", "david.kim@univibe.edu", "9876543215", "STUDENT");

            // 2. Insert Organizers
            insertOrganizer(conn, 1, 2, "Computer Science & Engineering", "Associate Professor", "ACM Student Chapter");
            insertOrganizer(conn, 2, 3, "Electronics & Communication", "Head of Department", "Robotics & Cultural Club");

            // 3. Insert Students
            insertStudent(conn, 1, 4, "CS2024-001", "Computer Science", 5);
            insertStudent(conn, 2, 5, "IT2024-042", "Information Technology", 5);
            insertStudent(conn, 3, 6, "EC2024-019", "Electronics & Comm.", 3);

            // 4. Insert Events across all 8 Categories
            insertEvent(conn, 1, "HackVibe 2026: 36-Hour National Hackathon",
                    "Build innovative AI, Cloud, and Web3 solutions. Mentorship from top industry tech leads with cash prizes up to ₹50,000.",
                    "Hackathons", "Tech Park Auditorium & Labs", "2026-12-10", "09:00 AM", 150.00, 100, 85, 1, "APPROVED");

            insertEvent(conn, 2, "VibeSync Annual Cultural Carnival",
                    "Three days of vibrant music, drama, fashion parades, street plays, and celebrity night performances.",
                    "College Festivals", "Main Campus Ground", "2026-12-18", "05:00 PM", 250.00, 500, 420, 2, "APPROVED");

            insertEvent(conn, 3, "Generative AI & LLM Engineering Masterclass",
                    "Hands-on practical workshop exploring RAG architectures, prompt engineering, and building local agent pipelines.",
                    "Workshops", "Seminar Hall 3", "2026-11-28", "10:00 AM", 100.00, 60, 14, 1, "APPROVED");

            insertEvent(conn, 4, "Inter-College Battle of the Bands",
                    "Electric musical showdown between college rock, pop, and fusion music bands. Live crowd voting!",
                    "Music and Dance Competitions", "Open Air Amphitheatre", "2026-12-05", "06:00 PM", 120.00, 200, 180, 2, "APPROVED");

            insertEvent(conn, 5, "CyberShield: Cloud Security & Penetration Testing",
                    "Technical seminar on cloud infrastructure defense, zero-day mitigation, and ethical hacking fundamentals.",
                    "Technical Events", "CS Audio-Visual Lab", "2026-12-02", "02:00 PM", 50.00, 80, 75, 1, "APPROVED");

            insertEvent(conn, 6, "UniCup Inter-Department Football Championship",
                    "Annual 7-a-side football tournament. Cheer for your department champions under the floodlights!",
                    "Sports Events", "Campus Sports Complex", "2026-12-12", "04:00 PM", 0.00, 300, 280, 2, "APPROVED");

            insertEvent(conn, 7, "Quantum Computing: Future Horizon Seminar",
                    "Distinguished lecture series exploring quantum gates, Qiskit simulation, and quantum cryptography.",
                    "Seminars", "Science Block Auditorium", "2026-12-22", "11:00 AM", 0.00, 120, 115, 1, "APPROVED");

            insertEvent(conn, 8, "Rhythmics: Solo & Group Western Dance Clash",
                    "Showcase your choreography and moves in hip-hop, contemporary, and freestyle dance categories.",
                    "Music and Dance Competitions", "Silver Jubilee Auditorium", "2026-12-15", "03:30 PM", 80.00, 150, 150, 2, "PENDING");

            // 5. Insert Bookings
            insertBooking(conn, 1, "BK-20261118-9102", 1, 1, 1, 150.00, "CONFIRMED");
            insertBooking(conn, 2, "BK-20261118-8421", 2, 3, 2, 200.00, "CONFIRMED");
            insertBooking(conn, 3, "BK-20261118-3719", 3, 2, 1, 250.00, "CONFIRMED");

            // 6. Insert Tickets
            insertTicket(conn, 1, 1, "TKT-HV26-001", "A-12", "UV#HACK2026#001#ALEX", "CHECKED_IN");
            insertTicket(conn, 2, 2, "TKT-AI26-014", "C-04", "UV#AIWORK2026#014#PRIYA", "PENDING");
            insertTicket(conn, 3, 2, "TKT-AI26-015", "C-05", "UV#AIWORK2026#015#PRIYA", "PENDING");
            insertTicket(conn, 4, 3, "TKT-VS26-088", "GA-88", "UV#VIBE2026#088#DAVID", "PENDING");

            // 7. Insert Payments
            insertPayment(conn, 1, 1, 150.00, "UPI", "TXN-UPI-98421045", "SUCCESS");
            insertPayment(conn, 2, 2, 200.00, "CAMPUS_CARD", "TXN-CARD-44218765", "SUCCESS");
            insertPayment(conn, 3, 3, 250.00, "NET_BANKING", "TXN-NET-77123901", "SUCCESS");

            // 8. Insert Feedback
            insertFeedback(conn, 1, 1, 1, 5, "Outstanding organization and world-class mentors! Learned tons in 36 hours.");
            insertFeedback(conn, 2, 2, 3, 4, "Very engaging hands-on workshop on LLMs. Loved the practical notebooks.");

            System.out.println("[DatabaseInitializer] Initial seed data successfully inserted.");
        } catch (Exception e) {
            System.err.println("[DatabaseInitializer] Error seeding data: " + e.getMessage());
        }
    }

    private static void insertUser(Connection conn, int id, String uname, String hash, String salt, String name, String email, String phone, String role) throws Exception {
        String sql = "INSERT INTO Users (user_id, username, password_hash, salt, full_name, email, phone, role, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'ACTIVE')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, uname);
            ps.setString(3, hash);
            ps.setString(4, salt);
            ps.setString(5, name);
            ps.setString(6, email);
            ps.setString(7, phone);
            ps.setString(8, role);
            ps.executeUpdate();
        }
    }

    private static void insertOrganizer(Connection conn, int id, int userId, String dept, String desig, String unit) throws Exception {
        String sql = "INSERT INTO Organizers (organizer_id, user_id, department, designation, organization_unit) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, userId);
            ps.setString(3, dept);
            ps.setString(4, desig);
            ps.setString(5, unit);
            ps.executeUpdate();
        }
    }

    private static void insertStudent(Connection conn, int id, int userId, String roll, String dept, int sem) throws Exception {
        String sql = "INSERT INTO Students (student_id, user_id, roll_number, department, semester) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, userId);
            ps.setString(3, roll);
            ps.setString(4, dept);
            ps.setInt(5, sem);
            ps.executeUpdate();
        }
    }

    private static void insertEvent(Connection conn, int id, String title, String desc, String cat, String venue,
                                    String date, String time, double price, int total, int avail, int orgId, String status) throws Exception {
        String sql = "INSERT INTO Events (event_id, title, description, category, venue, event_date, event_time, ticket_price, total_seats, available_seats, organizer_id, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, title);
            ps.setString(3, desc);
            ps.setString(4, cat);
            ps.setString(5, venue);
            ps.setString(6, date);
            ps.setString(7, time);
            ps.setDouble(8, price);
            ps.setInt(9, total);
            ps.setInt(10, avail);
            ps.setInt(11, orgId);
            ps.setString(12, status);
            ps.executeUpdate();
        }
    }

    private static void insertBooking(Connection conn, int id, String ref, int stuId, int eventId, int seats, double total, String status) throws Exception {
        String sql = "INSERT INTO Bookings (booking_id, booking_reference, student_id, event_id, number_of_seats, total_amount, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, ref);
            ps.setInt(3, stuId);
            ps.setInt(4, eventId);
            ps.setInt(5, seats);
            ps.setDouble(6, total);
            ps.setString(7, status);
            ps.executeUpdate();
        }
    }

    private static void insertTicket(Connection conn, int id, int bookingId, String code, String seat, String qr, String status) throws Exception {
        String sql = "INSERT INTO Tickets (ticket_id, booking_id, ticket_code, seat_number, qr_hash, attendance_status) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, bookingId);
            ps.setString(3, code);
            ps.setString(4, seat);
            ps.setString(5, qr);
            ps.setString(6, status);
            ps.executeUpdate();
        }
    }

    private static void insertPayment(Connection conn, int id, int bookingId, double amt, String method, String ref, String status) throws Exception {
        String sql = "INSERT INTO Payments (payment_id, booking_id, amount, payment_method, transaction_ref, payment_status) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, bookingId);
            ps.setDouble(3, amt);
            ps.setString(4, method);
            ps.setString(5, ref);
            ps.setString(6, status);
            ps.executeUpdate();
        }
    }

    private static void insertFeedback(Connection conn, int id, int stuId, int eventId, int rating, String comment) throws Exception {
        String sql = "INSERT INTO Feedback (feedback_id, student_id, event_id, rating, comment) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, stuId);
            ps.setInt(3, eventId);
            ps.setInt(4, rating);
            ps.setString(5, comment);
            ps.executeUpdate();
        }
    }
}
