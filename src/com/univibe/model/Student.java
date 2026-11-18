package com.univibe.model;

import java.util.Date;

/**
 * Student User Entity.
 * Demonstrates:
 *  1. Inheritance (extends User)
 *  2. Encapsulation (student-specific attributes rollNumber, department, semester)
 *  3. Polymorphism & Method Overriding
 *  4. Super constructor invocation
 */
public class Student extends User {
    private int studentId;
    private String rollNumber;
    private String department;
    private int semester;

    // Default Constructor
    public Student() {
        super();
        this.studentId = 0;
        this.rollNumber = "";
        this.department = "General";
        this.semester = 1;
    }

    // Constructor with basic user info
    public Student(int userId, String username, String fullName, String email, String phone, String status) {
        super(userId, username, fullName, email, phone, status);
        this.studentId = 0;
        this.rollNumber = "";
        this.department = "General";
        this.semester = 1;
    }

    // Full Parameterized Constructor (Overloading)
    public Student(int userId, String username, String passwordHash, String salt,
                   String fullName, String email, String phone, String status, Date createdAt,
                   int studentId, String rollNumber, String department, int semester) {
        super(userId, username, passwordHash, salt, fullName, email, phone, status, createdAt);
        this.studentId = studentId;
        this.rollNumber = rollNumber;
        this.department = department;
        this.semester = semester;
    }

    @Override
    public String getRole() {
        return "STUDENT";
    }

    @Override
    public String getDashboardTitle() {
        return "UniVibe Student Hub - Discover & Book Events";
    }

    // Getters and Setters
    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    @Override
    public String displayProfile(boolean showSensitive) {
        String baseProfile = super.displayProfile(showSensitive);
        return baseProfile + String.format("Roll No: %s | Dept: %s | Semester: %d\n", rollNumber, department, semester);
    }

    @Override
    public String toString() {
        return String.format("Student: %s (%s) - Dept: %s", getFullName(), rollNumber, department);
    }
}
