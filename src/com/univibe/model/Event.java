package com.univibe.model;

import com.univibe.interfaces.Auditable;
import com.univibe.interfaces.Reportable;
import com.univibe.interfaces.Searchable;

import java.util.Date;
import java.util.Objects;

/**
 * Event Entity.
 * Demonstrates:
 *  1. Encapsulation (private attributes, validated getters/setters)
 *  2. Interface implementation (Searchable, Reportable, Auditable)
 *  3. Constructor Overloading
 *  4. Business logic methods (seat calculations, low-seat notification, popularity score)
 */
public class Event implements Searchable, Reportable, Auditable {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    public static final int LOW_SEAT_THRESHOLD = 15; // Low-seat badge threshold

    private int eventId;
    private String title;
    private String description;
    private String category;
    private String venue;
    private Date eventDate;
    private String eventTime;
    private double ticketPrice;
    private int totalSeats;
    private int availableSeats;
    private int organizerId;
    private String organizerName; // Helper for display
    private String status;
    private Date createdAt;

    // Derived / Aggregated attributes
    private double averageRating;
    private int feedbackCount;
    private int totalBookings;

    // Default Constructor
    public Event() {
        this.eventId = 0;
        this.title = "";
        this.description = "";
        this.category = EventCategory.TECHNICAL_EVENTS.getDisplayName();
        this.venue = "";
        this.eventDate = new Date();
        this.eventTime = "10:00 AM";
        this.ticketPrice = 0.0;
        this.totalSeats = 100;
        this.availableSeats = 100;
        this.organizerId = 0;
        this.organizerName = "Campus Faculty";
        this.status = STATUS_PENDING;
        this.createdAt = new Date();
        this.averageRating = 0.0;
        this.feedbackCount = 0;
        this.totalBookings = 0;
    }

    // Parameterized Constructor 1: Basic creation
    public Event(String title, String description, String category, String venue,
                 Date eventDate, String eventTime, double ticketPrice, int totalSeats, int organizerId) {
        this.eventId = 0;
        this.title = title;
        this.description = description;
        this.category = category;
        this.venue = venue;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.ticketPrice = ticketPrice;
        this.totalSeats = totalSeats;
        this.availableSeats = totalSeats;
        this.organizerId = organizerId;
        this.organizerName = "";
        this.status = STATUS_PENDING;
        this.createdAt = new Date();
        this.averageRating = 0.0;
        this.feedbackCount = 0;
        this.totalBookings = 0;
    }

    // Parameterized Constructor 2: Full Entity (Overloading)
    public Event(int eventId, String title, String description, String category, String venue,
                 Date eventDate, String eventTime, double ticketPrice, int totalSeats,
                 int availableSeats, int organizerId, String status, Date createdAt) {
        this.eventId = eventId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.venue = venue;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.ticketPrice = ticketPrice;
        this.totalSeats = totalSeats;
        this.availableSeats = availableSeats;
        this.organizerId = organizerId;
        this.status = status;
        this.createdAt = createdAt;
        this.averageRating = 0.0;
        this.feedbackCount = 0;
        this.totalBookings = 0;
    }

    // Business Logic Methods
    public boolean isLowSeatAvailability() {
        return availableSeats > 0 && (availableSeats <= LOW_SEAT_THRESHOLD || ((double) availableSeats / totalSeats) <= 0.15);
    }

    public boolean isHousefull() {
        return availableSeats <= 0;
    }

    public int getBookedSeats() {
        return totalSeats - availableSeats;
    }

    public double getOccupancyRate() {
        if (totalSeats == 0) return 0.0;
        return ((double) (totalSeats - availableSeats) / totalSeats) * 100.0;
    }

    public double getPopularityScore() {
        // Weighted formula: (Occupancy % * 0.7) + ((Average Rating / 5.0) * 100 * 0.3)
        double occupancyPart = getOccupancyRate() * 0.7;
        double ratingPart = (averageRating / 5.0) * 100.0 * 0.3;
        return occupancyPart + ratingPart;
    }

    // Interfaces Implementation
    @Override
    public boolean matchesKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return true;
        String kw = keyword.trim().toLowerCase();
        return title.toLowerCase().contains(kw)
                || description.toLowerCase().contains(kw)
                || venue.toLowerCase().contains(kw)
                || category.toLowerCase().contains(kw);
    }

    @Override
    public String getSearchCategory() {
        return category;
    }

    @Override
    public String toReportRow() {
        return String.format("%d,%s,%s,%s,%s,%.2f,%d,%d,%s",
                eventId,
                title.replace(",", ";"),
                category,
                venue.replace(",", ";"),
                eventDate.toString(),
                ticketPrice,
                totalSeats,
                availableSeats,
                status);
    }

    @Override
    public String[] getReportHeaders() {
        return new String[]{"Event ID", "Title", "Category", "Venue", "Date", "Price", "Total Seats", "Available", "Status"};
    }

    @Override
    public Date getCreationTimestamp() {
        return createdAt;
    }

    @Override
    public String getAuditSummary() {
        return String.format("Event '%s' (ID: %d) organized by ID %d on %s", title, eventId, organizerId, createdAt);
    }

    // Getters and Setters
    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    public double getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(double ticketPrice) {
        this.ticketPrice = ticketPrice;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    public int getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(int organizerId) {
        this.organizerId = organizerId;
    }

    public String getOrganizerName() {
        return organizerName;
    }

    public void setOrganizerName(String organizerName) {
        this.organizerName = organizerName;
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

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public int getFeedbackCount() {
        return feedbackCount;
    }

    public void setFeedbackCount(int feedbackCount) {
        this.feedbackCount = feedbackCount;
    }

    public int getTotalBookings() {
        return totalBookings;
    }

    public void setTotalBookings(int totalBookings) {
        this.totalBookings = totalBookings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Event)) return false;
        Event event = (Event) o;
        return eventId == event.eventId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (Seats: %d/%d, ₹%.2f)", category, title, availableSeats, totalSeats, ticketPrice);
    }
}
