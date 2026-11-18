package com.univibe.model;

import java.util.Date;

/**
 * Admin User Entity.
 * Demonstrates:
 *  1. Inheritance (extends User)
 *  2. Polymorphism (implements abstract methods from User)
 *  3. Method Overriding (getRole, getDashboardTitle)
 *  4. Constructor chaining with super()
 */
public class Admin extends User {

    // Default Constructor
    public Admin() {
        super();
    }

    // Parameterized Constructor
    public Admin(int userId, String username, String fullName, String email, String phone, String status) {
        super(userId, username, fullName, email, phone, status);
    }

    // Full Parameterized Constructor
    public Admin(int userId, String username, String passwordHash, String salt,
                 String fullName, String email, String phone, String status, Date createdAt) {
        super(userId, username, passwordHash, salt, fullName, email, phone, status, createdAt);
    }

    @Override
    public String getRole() {
        return "ADMIN";
    }

    @Override
    public String getDashboardTitle() {
        return "UniVibe Administrator Operations Console";
    }

    public boolean canApproveEvents() {
        return true;
    }

    public boolean canManageAllUsers() {
        return true;
    }

    public boolean canGenerateSystemReports() {
        return true;
    }
}
