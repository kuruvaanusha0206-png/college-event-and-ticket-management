package com.univibe.service;

import com.univibe.dao.EventDAO;
import com.univibe.dao.FeedbackDAO;
import com.univibe.exception.EventNotFoundException;
import com.univibe.model.Event;
import com.univibe.model.Feedback;
import com.univibe.util.FileHandler;

import java.sql.SQLException;
import java.util.*;

/**
 * Event Management and Discovery Service.
 * Demonstrates:
 *  1. Method Overloading (searchEvents and filterEvents variations)
 *  2. Collections (ArrayList, HashMap, HashSet)
 *  3. Sorting and Comparator lambda / anonymous class usage
 *  4. Business logic for Popularity Rankings & Approval workflows
 */
public class EventManager {

    private final EventDAO eventDAO;
    private final FeedbackDAO feedbackDAO;

    public EventManager() {
        this.eventDAO = new EventDAO();
        this.feedbackDAO = new FeedbackDAO();
    }

    public boolean submitNewEvent(Event event) throws SQLException {
        boolean ok = eventDAO.createEvent(event);
        if (ok) {
            FileHandler.logAuditActivity("EVENT_CREATE", "ORGANIZER_" + event.getOrganizerId(), "Created event: " + event.getTitle());
        }
        return ok;
    }

    public boolean updateEvent(Event event) throws SQLException {
        boolean ok = eventDAO.updateEvent(event);
        if (ok) {
            FileHandler.logAuditActivity("EVENT_UPDATE", "ORGANIZER_" + event.getOrganizerId(), "Updated event ID: " + event.getEventId());
        }
        return ok;
    }

    public boolean approveEvent(int eventId) throws SQLException {
        boolean ok = eventDAO.updateEventStatus(eventId, Event.STATUS_APPROVED);
        if (ok) {
            FileHandler.logAuditActivity("EVENT_APPROVE", "ADMIN", "Approved event ID: " + eventId);
        }
        return ok;
    }

    public boolean rejectEvent(int eventId) throws SQLException {
        boolean ok = eventDAO.updateEventStatus(eventId, Event.STATUS_REJECTED);
        if (ok) {
            FileHandler.logAuditActivity("EVENT_REJECT", "ADMIN", "Rejected event ID: " + eventId);
        }
        return ok;
    }

    public Event getEventById(int eventId) throws SQLException, EventNotFoundException {
        Event event = eventDAO.findById(eventId);
        if (event == null) {
            throw new EventNotFoundException(eventId);
        }
        return event;
    }

    public List<Event> getAllEvents() throws SQLException {
        return eventDAO.getAllEvents();
    }

    public List<Event> getApprovedEvents() throws SQLException {
        return eventDAO.getApprovedUpcomingEvents();
    }

    public List<Event> getEventsByOrganizer(int organizerId) throws SQLException {
        return eventDAO.getEventsByOrganizer(organizerId);
    }

    // Method Overloading 1: Search by keyword alone
    public List<Event> searchEvents(String keyword) throws SQLException {
        return searchEvents(keyword, "All", null, null, "Date (Soonest)");
    }

    // Method Overloading 2: Search by keyword, category, and max price
    public List<Event> searchEvents(String keyword, String category, Double maxPrice) throws SQLException {
        return searchEvents(keyword, category, null, maxPrice, "Date (Soonest)");
    }

    // Method Overloading 3: Full multi-criteria search and sorting
    public List<Event> searchEvents(String keyword, String category, Double minPrice, Double maxPrice, String sortBy)
            throws SQLException {

        List<Event> all = eventDAO.getApprovedUpcomingEvents();
        List<Event> filtered = new ArrayList<>();

        for (Event e : all) {
            // Keyword match
            if (keyword != null && !keyword.trim().isEmpty() && !e.matchesKeyword(keyword)) {
                continue;
            }
            // Category filter
            if (category != null && !category.equalsIgnoreCase("All") && !e.getCategory().equalsIgnoreCase(category)) {
                continue;
            }
            // Price range filter
            if (minPrice != null && e.getTicketPrice() < minPrice) {
                continue;
            }
            if (maxPrice != null && e.getTicketPrice() > maxPrice) {
                continue;
            }
            filtered.add(e);
        }

        // Apply Sorting
        if ("Popularity".equalsIgnoreCase(sortBy)) {
            filtered.sort((a, b) -> Double.compare(b.getPopularityScore(), a.getPopularityScore()));
        } else if ("Price (Low to High)".equalsIgnoreCase(sortBy)) {
            filtered.sort(Comparator.comparingDouble(Event::getTicketPrice));
        } else if ("Price (High to Low)".equalsIgnoreCase(sortBy)) {
            filtered.sort((a, b) -> Double.compare(b.getTicketPrice(), a.getTicketPrice()));
        } else if ("Rating (Highest)".equalsIgnoreCase(sortBy)) {
            filtered.sort((a, b) -> Double.compare(b.getAverageRating(), a.getAverageRating()));
        } else {
            // Default: Soonest Date
            filtered.sort(Comparator.comparing(Event::getEventDate));
        }

        return filtered;
    }

    /**
     * Extracts unique venues across all events using HashSet.
     * Demonstrates: HashSet collection.
     */
    public Set<String> getUniqueVenues() throws SQLException {
        List<Event> events = eventDAO.getAllEvents();
        Set<String> venues = new HashSet<>();
        for (Event e : events) {
            venues.add(e.getVenue());
        }
        return venues;
    }

    /**
     * Feedback handling
     */
    public boolean submitFeedback(Feedback fb) throws SQLException {
        return feedbackDAO.submitFeedback(fb);
    }

    public List<Feedback> getEventFeedback(int eventId) throws SQLException {
        return feedbackDAO.getFeedbackForEvent(eventId);
    }

    public boolean hasStudentGivenFeedback(int studentId, int eventId) throws SQLException {
        return feedbackDAO.hasStudentGivenFeedback(studentId, eventId);
    }
}
