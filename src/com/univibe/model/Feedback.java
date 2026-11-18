package com.univibe.model;

import java.util.Date;

/**
 * Event Feedback and Rating Entity.
 * Demonstrates:
 *  1. Encapsulation with validation
 *  2. String manipulation (star ratings)
 */
public class Feedback {
    private int feedbackId;
    private int studentId;
    private int eventId;
    private int rating; // 1 to 5
    private String comment;
    private Date submittedAt;

    // Display helpers
    private String studentName;
    private String eventTitle;

    public Feedback() {
        this.feedbackId = 0;
        this.studentId = 0;
        this.eventId = 0;
        this.rating = 5;
        this.comment = "";
        this.submittedAt = new Date();
    }

    public Feedback(int feedbackId, int studentId, int eventId, int rating, String comment, Date submittedAt) {
        this.feedbackId = feedbackId;
        this.studentId = studentId;
        this.eventId = eventId;
        setRating(rating); // validated setter
        this.comment = comment;
        this.submittedAt = (submittedAt != null) ? submittedAt : new Date();
    }

    public String getStarsDisplay() {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            if (i <= rating) {
                sb.append("★");
            } else {
                sb.append("☆");
            }
        }
        return sb.toString();
    }

    // Getters and Setters
    public int getFeedbackId() {
        return feedbackId;
    }

    public void setFeedbackId(int feedbackId) {
        this.feedbackId = feedbackId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        if (rating < 1) this.rating = 1;
        else if (rating > 5) this.rating = 5;
        else this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Date submittedAt) {
        this.submittedAt = submittedAt;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    @Override
    public String toString() {
        return String.format("[%s] Rating: %d/5 by %s: \"%s\"", eventTitle, rating, studentName, comment);
    }
}
