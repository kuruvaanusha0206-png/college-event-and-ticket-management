package com.univibe.model;

import java.util.Date;
import java.util.Objects;

/**
 * Abstract Base User Class.
 * Demonstrates:
 *  1. Abstraction (abstract class and abstract methods)
 *  2. Encapsulation (private fields, accessors, mutators)
 *  3. Static and Final keywords (APP_NAME, static user counters)
 *  4. Constructor Overloading (default vs parameterized constructors)
 *  5. Method Overloading (displayProfile methods)
 *  6. Method Overriding (toString, equals, hashCode)
 */
public abstract class User {
    // Static and Final Constants
    public static final String APP_NAME = "UniVibe";
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";

    // Encapsulated Fields
    private int userId;
    private String username;
    private String passwordHash;
    private String salt;
    private String fullName;
    private String email;
    private String phone;
    private String status;
    private Date createdAt;

    // Static counter for demonstration
    private static int totalUsersInstantiated = 0;

    // Constructor 1: Default Constructor
    public User() {
        this.userId = 0;
        this.username = "";
        this.passwordHash = "";
        this.salt = "";
        this.fullName = "Guest User";
        this.email = "";
        this.phone = "";
        this.status = STATUS_ACTIVE;
        this.createdAt = new Date();
        totalUsersInstantiated++;
    }

    // Constructor 2: Parameterized Constructor (Overloading)
    public User(int userId, String username, String fullName, String email, String phone, String status) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = "";
        this.salt = "";
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.status = (status != null) ? status : STATUS_ACTIVE;
        this.createdAt = new Date();
        totalUsersInstantiated++;
    }

    // Constructor 3: Full Parameterized Constructor (Overloading)
    public User(int userId, String username, String passwordHash, String salt,
                String fullName, String email, String phone, String status, Date createdAt) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.status = (status != null) ? status : STATUS_ACTIVE;
        this.createdAt = (createdAt != null) ? createdAt : new Date();
        totalUsersInstantiated++;
    }

    // Abstract Methods (Mandatory for Subclasses to Implement)
    public abstract String getRole();
    public abstract String getDashboardTitle();

    // Method Overloading Demonstration
    public String displayProfile() {
        return displayProfile(false);
    }

    public String displayProfile(boolean showSensitive) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("User ID: %d | Name: %s | Role: %s\n", userId, fullName, getRole()));
        sb.append(String.format("Username: %s | Email: %s | Phone: %s\n", username, email, phone));
        sb.append(String.format("Status: %s\n", status));
        if (showSensitive) {
            sb.append(String.format("Created At: %s | Salt: %s\n", createdAt, salt));
        }
        return sb.toString();
    }

    // Static Utility Method
    public static int getTotalUsersInstantiated() {
        return totalUsersInstantiated;
    }

    // Getters and Setters (Encapsulation)
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    // Method Overriding
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return userId == user.userId && Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, username);
    }

    @Override
    public String toString() {
        return String.format("%s (%s - %s)", fullName, username, getRole());
    }
}
