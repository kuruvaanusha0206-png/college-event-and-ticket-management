-- ============================================================================
-- PROJECT: UniVibe - College Event and Ticket Management System
-- DATABASE: univibe_db
-- TARGET: MySQL 8.0+ / MariaDB
-- ============================================================================

CREATE DATABASE IF NOT EXISTS univibe_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE univibe_db;

-- ----------------------------------------------------------------------------
-- 1. Table: Users (Base credentials and common user attributes)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS Users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(128) NOT NULL,
    salt VARCHAR(64) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL,
    role ENUM('ADMIN', 'ORGANIZER', 'STUDENT') NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------------------------
-- 2. Table: Students (Subclass entity linked to Users)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS Students (
    student_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    roll_number VARCHAR(30) NOT NULL UNIQUE,
    department VARCHAR(60) NOT NULL,
    semester INT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------------------------
-- 3. Table: Organizers (Subclass entity linked to Users)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS Organizers (
    organizer_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    department VARCHAR(60) NOT NULL,
    designation VARCHAR(60) NOT NULL,
    organization_unit VARCHAR(100) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------------------------
-- 4. Table: Events (College Events created by organizers or admin)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS Events (
    event_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(60) NOT NULL,
    venue VARCHAR(100) NOT NULL,
    event_date DATE NOT NULL,
    event_time VARCHAR(30) NOT NULL,
    ticket_price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total_seats INT NOT NULL,
    available_seats INT NOT NULL,
    organizer_id INT NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'COMPLETED', 'CANCELLED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (organizer_id) REFERENCES Organizers(organizer_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------------------------
-- 5. Table: Bookings (Student Ticket Bookings)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS Bookings (
    booking_id INT AUTO_INCREMENT PRIMARY KEY,
    booking_reference VARCHAR(50) NOT NULL UNIQUE,
    student_id INT NOT NULL,
    event_id INT NOT NULL,
    number_of_seats INT NOT NULL DEFAULT 1,
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('CONFIRMED', 'CANCELLED') DEFAULT 'CONFIRMED',
    FOREIGN KEY (student_id) REFERENCES Students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (event_id) REFERENCES Events(event_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------------------------
-- 6. Table: Tickets (Individual Digital Tickets issued per booking)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS Tickets (
    ticket_id INT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT NOT NULL,
    ticket_code VARCHAR(50) NOT NULL UNIQUE,
    seat_number VARCHAR(20) NOT NULL,
    qr_hash VARCHAR(100) NOT NULL,
    attendance_status ENUM('PENDING', 'CHECKED_IN', 'ABSENT') DEFAULT 'PENDING',
    issued_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES Bookings(booking_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------------------------
-- 7. Table: Payments (Payment transaction records)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS Payments (
    payment_id INT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT NOT NULL UNIQUE,
    amount DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    transaction_ref VARCHAR(64) NOT NULL UNIQUE,
    payment_status ENUM('SUCCESS', 'REFUNDED', 'FAILED') DEFAULT 'SUCCESS',
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES Bookings(booking_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------------------------
-- 8. Table: Feedback (Student Reviews and Ratings for Events)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS Feedback (
    feedback_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    event_id INT NOT NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES Students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (event_id) REFERENCES Events(event_id) ON DELETE CASCADE,
    UNIQUE KEY uq_student_event_feedback (student_id, event_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================================
-- INDEXES FOR HIGH QUERY PERFORMANCE
-- ============================================================================
CREATE INDEX idx_events_date_category ON Events(event_date, category);
CREATE INDEX idx_events_status ON Events(status);
CREATE INDEX idx_bookings_student ON Bookings(student_id);
CREATE INDEX idx_tickets_booking ON Tickets(booking_id);
CREATE INDEX idx_feedback_event ON Feedback(event_id);

-- ============================================================================
-- SEED DATA (Default Admin, Organizers, Students, and Events)
-- Passwords are hashed using SHA-256 with salt 'univibe_salt_2026'
-- Raw passwords:
--   admin / admin123
--   org_sarah / org123
--   org_rahul / org123
--   student_alex / student123
--   student_priya / student123
--   student_david / student123
-- ============================================================================

-- Insert Users (Password: SHA256(password + 'univibe_salt_2026'))
-- admin123 + salt -> 7d9a19d59265b45ee10f6067db8f1d7d0a273295964f434b9d036e6b72a44415
-- org123 + salt   -> 2b11e29cbe29d724773c22b9b74305dfc29c87d4c82b3d87dbbe331a1fe5635f
-- student123+salt -> de290cf096324d5ba75709971bcba9d66cf7d853e390c58e80718d0421e434f0

INSERT INTO Users (user_id, username, password_hash, salt, full_name, email, phone, role, status) VALUES
(1, 'admin', '7d9a19d59265b45ee10f6067db8f1d7d0a273295964f434b9d036e6b72a44415', 'univibe_salt_2026', 'System Administrator', 'admin@univibe.edu', '9876543210', 'ADMIN', 'ACTIVE'),
(2, 'org_sarah', '2b11e29cbe29d724773c22b9b74305dfc29c87d4c82b3d87dbbe331a1fe5635f', 'univibe_salt_2026', 'Prof. Sarah Jenkins', 's.jenkins@univibe.edu', '9876543211', 'ORGANIZER', 'ACTIVE'),
(3, 'org_rahul', '2b11e29cbe29d724773c22b9b74305dfc29c87d4c82b3d87dbbe331a1fe5635f', 'univibe_salt_2026', 'Dr. Rahul Sharma', 'r.sharma@univibe.edu', '9876543212', 'ORGANIZER', 'ACTIVE'),
(4, 'student_alex', 'de290cf096324d5ba75709971bcba9d66cf7d853e390c58e80718d0421e434f0', 'univibe_salt_2026', 'Alex Mercer', 'alex.mercer@univibe.edu', '9876543213', 'STUDENT', 'ACTIVE'),
(5, 'student_priya', 'de290cf096324d5ba75709971bcba9d66cf7d853e390c58e80718d0421e434f0', 'univibe_salt_2026', 'Priya Patel', 'priya.patel@univibe.edu', '9876543214', 'STUDENT', 'ACTIVE'),
(6, 'student_david', 'de290cf096324d5ba75709971bcba9d66cf7d853e390c58e80718d0421e434f0', 'univibe_salt_2026', 'David Kim', 'david.kim@univibe.edu', '9876543215', 'STUDENT', 'ACTIVE')
ON DUPLICATE KEY UPDATE username=username;

-- Insert Organizers
INSERT INTO Organizers (organizer_id, user_id, department, designation, organization_unit) VALUES
(1, 2, 'Computer Science & Engineering', 'Associate Professor', 'ACM Student Chapter'),
(2, 3, 'Electronics & Communication', 'Head of Department', 'Robotics & Cultural Club')
ON DUPLICATE KEY UPDATE user_id=user_id;

-- Insert Students
INSERT INTO Students (student_id, user_id, roll_number, department, semester) VALUES
(1, 4, 'CS2024-001', 'Computer Science', 5),
(2, 5, 'IT2024-042', 'Information Technology', 5),
(3, 6, 'EC2024-019', 'Electronics & Comm.', 3)
ON DUPLICATE KEY UPDATE user_id=user_id;

-- Insert Diverse Events across Categories
INSERT INTO Events (event_id, title, description, category, venue, event_date, event_time, ticket_price, total_seats, available_seats, organizer_id, status) VALUES
(1, 'HackVibe 2026: 36-Hour National Hackathon', 'Build innovative AI, Cloud, and Web3 solutions. Mentorship from top industry tech leads with cash prizes up to $5,000.', 'Hackathons', 'Tech Park Auditorium & Labs', '2026-12-10', '09:00 AM', 150.00, 100, 85, 1, 'APPROVED'),
(2, 'VibeSync Annual Cultural Carnival', 'Three days of vibrant music, drama, fashion parades, street plays, and celebrity night performances.', 'College Festivals', 'Main Campus Ground', '2026-12-18', '05:00 PM', 250.00, 500, 420, 2, 'APPROVED'),
(3, 'Generative AI & LLM Engineering Masterclass', 'Hands-on practical workshop exploring RAG architectures, prompt engineering, and building local agent pipelines.', 'Workshops', 'Seminar Hall 3', '2026-11-28', '10:00 AM', 100.00, 60, 14, 1, 'APPROVED'),
(4, 'Inter-College Battle of the Bands', 'Electric musical showdown between college rock, pop, and fusion music bands. Live crowd voting!', 'Music and Dance Competitions', 'Open Air Amphitheatre', '2026-12-05', '06:00 PM', 120.00, 200, 180, 2, 'APPROVED'),
(5, 'CyberShield: Cloud Security & Penetration Testing', 'Technical seminar on cloud infrastructure defense, zero-day mitigation, and ethical hacking fundamentals.', 'Technical Events', 'CS Audio-Visual Lab', '2026-12-02', '02:00 PM', 50.00, 80, 75, 1, 'APPROVED'),
(6, 'UniCup Inter-Department Football Championship', 'Annual 7-a-side football tournament. Cheer for your department champions under the floodlights!', 'Sports Events', 'Campus Sports Complex', '2026-12-12', '04:00 PM', 0.00, 300, 280, 2, 'APPROVED'),
(7, 'Quantum Computing: Future Horizon Seminar', 'Distinguished lecture series exploring quantum gates, Qiskit simulation, and quantum cryptography.', 'Seminars', 'Science Block Auditorium', '2026-12-22', '11:00 AM', 0.00, 120, 115, 1, 'APPROVED'),
(8, 'Rhythmics: Solo & Group Western Dance Clash', 'Showcase your choreography and moves in hip-hop, contemporary, and freestyle dance categories.', 'Music and Dance Competitions', 'Silver Jubilee Auditorium', '2026-12-15', '03:30 PM', 80.00, 150, 150, 2, 'PENDING')
ON DUPLICATE KEY UPDATE title=title;

-- Sample Bookings
INSERT INTO Bookings (booking_id, booking_reference, student_id, event_id, number_of_seats, total_amount, status) VALUES
(1, 'BK-20261118-9102', 1, 1, 1, 150.00, 'CONFIRMED'),
(2, 'BK-20261118-8421', 2, 3, 2, 200.00, 'CONFIRMED'),
(3, 'BK-20261118-3719', 3, 2, 1, 250.00, 'CONFIRMED')
ON DUPLICATE KEY UPDATE booking_reference=booking_reference;

-- Sample Tickets
INSERT INTO Tickets (ticket_id, booking_id, ticket_code, seat_number, qr_hash, attendance_status) VALUES
(1, 1, 'TKT-HV26-001', 'A-12', 'UV#HACK2026#001#ALEX', 'CHECKED_IN'),
(2, 2, 'TKT-AI26-014', 'C-04', 'UV#AIWORK2026#014#PRIYA', 'PENDING'),
(3, 2, 'TKT-AI26-015', 'C-05', 'UV#AIWORK2026#015#PRIYA', 'PENDING'),
(4, 3, 'TKT-VS26-088', 'GA-88', 'UV#VIBE2026#088#DAVID', 'PENDING')
ON DUPLICATE KEY UPDATE ticket_code=ticket_code;

-- Sample Payments
INSERT INTO Payments (payment_id, booking_id, amount, payment_method, transaction_ref, payment_status) VALUES
(1, 1, 150.00, 'UPI', 'TXN-UPI-98421045', 'SUCCESS'),
(2, 2, 200.00, 'CAMPUS_CARD', 'TXN-CARD-44218765', 'SUCCESS'),
(3, 3, 250.00, 'NET_BANKING', 'TXN-NET-77123901', 'SUCCESS')
ON DUPLICATE KEY UPDATE transaction_ref=transaction_ref;

-- Sample Feedback
INSERT INTO Feedback (feedback_id, student_id, event_id, rating, comment) VALUES
(1, 1, 1, 5, 'Outstanding organization and world-class mentors! Learned tons in 36 hours.'),
(2, 2, 3, 4, 'Very engaging hands-on workshop on LLMs. Loved the practical notebooks.')
ON DUPLICATE KEY UPDATE rating=rating;
