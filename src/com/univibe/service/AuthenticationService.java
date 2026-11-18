package com.univibe.service;

import com.univibe.dao.UserDAO;
import com.univibe.exception.AuthenticationException;
import com.univibe.model.Organizer;
import com.univibe.model.Student;
import com.univibe.model.User;
import com.univibe.util.FileHandler;
import com.univibe.util.PasswordHasher;
import com.univibe.util.ValidationHelper;

import java.sql.SQLException;

/**
 * Authentication and User Session Service.
 * Demonstrates:
 *  1. Service Layer Pattern
 *  2. Custom Exception Handling (AuthenticationException)
 *  3. Password Hashing Verification
 *  4. Security audit logging
 */
public class AuthenticationService {

    private final UserDAO userDAO;
    private static User currentUser;

    public AuthenticationService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Authenticates user against stored salt and SHA-256 hash.
     */
    public User login(String username, String plainPassword) throws AuthenticationException, SQLException {
        if (!ValidationHelper.isNotEmpty(username) || !ValidationHelper.isNotEmpty(plainPassword)) {
            throw new AuthenticationException("Username and password must not be empty.", username);
        }

        User user = userDAO.findByUsername(username.trim());
        if (user == null) {
            FileHandler.logAuditActivity("LOGIN_FAILED", username, "Attempt with unknown username.");
            throw new AuthenticationException("Invalid username or password.", username);
        }

        if (User.STATUS_INACTIVE.equalsIgnoreCase(user.getStatus())) {
            FileHandler.logAuditActivity("LOGIN_BLOCKED", username, "Deactivated account attempted login.");
            throw new AuthenticationException("Your account is deactivated. Please contact campus admin.", username);
        }

        boolean match = PasswordHasher.verifyPassword(plainPassword, user.getSalt(), user.getPasswordHash());
        if (!match) {
            FileHandler.logAuditActivity("LOGIN_FAILED", username, "Incorrect password entered.");
            throw new AuthenticationException("Invalid username or password.", username);
        }

        currentUser = user;
        FileHandler.logAuditActivity("LOGIN_SUCCESS", username, "Logged in as " + user.getRole());
        return user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void logout() {
        if (currentUser != null) {
            FileHandler.logAuditActivity("LOGOUT", currentUser.getUsername(), "Logged out successfully.");
            currentUser = null;
        }
    }

    public boolean registerStudent(String username, String password, String fullName, String email,
                                   String phone, String rollNumber, String department, int semester)
            throws AuthenticationException, SQLException {

        if (!ValidationHelper.isValidEmail(email)) {
            throw new AuthenticationException("Invalid college email address format.");
        }
        if (!ValidationHelper.isValidPhone(phone)) {
            throw new AuthenticationException("Invalid mobile phone number format.");
        }
        if (!ValidationHelper.isValidRollNumber(rollNumber)) {
            throw new AuthenticationException("Invalid college roll number.");
        }

        Student student = new Student();
        student.setUsername(username.trim());
        student.setFullName(fullName.trim());
        student.setEmail(email.trim());
        student.setPhone(phone.trim());
        student.setRollNumber(rollNumber.trim().toUpperCase());
        student.setDepartment(department.trim());
        student.setSemester(semester);

        boolean ok = userDAO.registerStudent(student, password);
        if (ok) {
            FileHandler.logAuditActivity("STUDENT_REGISTER", username, "Registered student: " + rollNumber);
        }
        return ok;
    }

    public boolean registerOrganizer(String username, String password, String fullName, String email,
                                     String phone, String department, String designation, String unit)
            throws AuthenticationException, SQLException {

        if (!ValidationHelper.isValidEmail(email)) {
            throw new AuthenticationException("Invalid institutional email format.");
        }
        if (!ValidationHelper.isValidPhone(phone)) {
            throw new AuthenticationException("Invalid mobile phone number.");
        }

        Organizer organizer = new Organizer();
        organizer.setUsername(username.trim());
        organizer.setFullName(fullName.trim());
        organizer.setEmail(email.trim());
        organizer.setPhone(phone.trim());
        organizer.setDepartment(department.trim());
        organizer.setDesignation(designation.trim());
        organizer.setOrganizationUnit(unit.trim());

        boolean ok = userDAO.registerOrganizer(organizer, password);
        if (ok) {
            FileHandler.logAuditActivity("ORGANIZER_REGISTER", username, "Registered organizer: " + unit);
        }
        return ok;
    }
}
