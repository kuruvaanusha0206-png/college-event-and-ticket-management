package com.univibe.model;

import java.util.Date;

/**
 * Organizer User Entity.
 * Demonstrates:
 *  1. Inheritance (extends User)
 *  2. Encapsulation (department, designation, organizationUnit)
 *  3. Polymorphism & Method Overriding
 */
public class Organizer extends User {
    private int organizerId;
    private String department;
    private String designation;
    private String organizationUnit;

    // Default Constructor
    public Organizer() {
        super();
        this.organizerId = 0;
        this.department = "";
        this.designation = "";
        this.organizationUnit = "";
    }

    // Constructor with basic user info
    public Organizer(int userId, String username, String fullName, String email, String phone, String status) {
        super(userId, username, fullName, email, phone, status);
        this.organizerId = 0;
        this.department = "";
        this.designation = "";
        this.organizationUnit = "";
    }

    // Full Parameterized Constructor (Overloading)
    public Organizer(int userId, String username, String passwordHash, String salt,
                     String fullName, String email, String phone, String status, Date createdAt,
                     int organizerId, String department, String designation, String organizationUnit) {
        super(userId, username, passwordHash, salt, fullName, email, phone, status, createdAt);
        this.organizerId = organizerId;
        this.department = department;
        this.designation = designation;
        this.organizationUnit = organizationUnit;
    }

    @Override
    public String getRole() {
        return "ORGANIZER";
    }

    @Override
    public String getDashboardTitle() {
        return "UniVibe Organizer Management Studio";
    }

    // Getters and Setters
    public int getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(int organizerId) {
        this.organizerId = organizerId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getOrganizationUnit() {
        return organizationUnit;
    }

    public void setOrganizationUnit(String organizationUnit) {
        this.organizationUnit = organizationUnit;
    }

    @Override
    public String displayProfile(boolean showSensitive) {
        String baseProfile = super.displayProfile(showSensitive);
        return baseProfile + String.format("Designation: %s | Dept: %s | Unit: %s\n", designation, department, organizationUnit);
    }

    @Override
    public String toString() {
        return String.format("Organizer: %s (%s, %s)", getFullName(), designation, department);
    }
}
