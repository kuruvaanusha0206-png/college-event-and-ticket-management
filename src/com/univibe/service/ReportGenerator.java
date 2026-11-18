package com.univibe.service;

import com.univibe.dao.BookingDAO;
import com.univibe.dao.EventDAO;
import com.univibe.dao.PaymentDAO;
import com.univibe.dao.UserDAO;
import com.univibe.model.Booking;
import com.univibe.model.Event;
import com.univibe.model.Organizer;
import com.univibe.model.Student;
import com.univibe.util.FileHandler;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Multithreaded Background Report Generator.
 * Demonstrates:
 *  1. Multithreading (implements Runnable, separate Thread execution)
 *  2. Progress callback listeners (Observer / Callback pattern)
 *  3. Thread safety and non-blocking Swing UI execution
 *  4. Complex report aggregation using Streams and Collections
 *  5. File I/O via FileHandler
 */
public class ReportGenerator implements Runnable {

    public enum ReportType {
        SYSTEM_SUMMARY,
        EVENT_WISE_ANALYTICS,
        ORGANIZER_PERFORMANCE
    }

    public interface ReportProgressListener {
        void onProgress(int percentage, String message);
        void onComplete(File generatedFile);
        void onError(Exception error);
    }

    private final ReportType reportType;
    private final ReportProgressListener listener;

    private final EventDAO eventDAO;
    private final BookingDAO bookingDAO;
    private final PaymentDAO paymentDAO;
    private final UserDAO userDAO;

    public ReportGenerator(ReportType reportType, ReportProgressListener listener) {
        this.reportType = reportType;
        this.listener = listener;
        this.eventDAO = new EventDAO();
        this.bookingDAO = new BookingDAO();
        this.paymentDAO = new PaymentDAO();
        this.userDAO = new UserDAO();
    }

    @Override
    public void run() {
        try {
            notifyProgress(10, "Initializing report environment...");
            Thread.sleep(200); // Simulate background workload step

            StringBuilder report = new StringBuilder();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String timestamp = sdf.format(new Date());

            String fileName;

            if (reportType == ReportType.SYSTEM_SUMMARY) {
                fileName = "UniVibe_System_Summary_" + System.currentTimeMillis() + ".txt";
                buildSystemSummary(report, timestamp);
            } else if (reportType == ReportType.EVENT_WISE_ANALYTICS) {
                fileName = "UniVibe_Event_Analytics_" + System.currentTimeMillis() + ".csv";
                buildEventAnalytics(report, timestamp);
            } else {
                fileName = "UniVibe_Organizer_Performance_" + System.currentTimeMillis() + ".txt";
                buildOrganizerPerformance(report, timestamp);
            }

            notifyProgress(85, "Writing report file to disk...");
            File exportedFile = FileHandler.exportReportToFile(report.toString(), fileName);

            notifyProgress(100, "Report successfully generated!");
            if (listener != null) {
                listener.onComplete(exportedFile);
            }

        } catch (Exception ex) {
            System.err.println("[ReportGenerator] Exception during multithreaded report generation: " + ex.getMessage());
            if (listener != null) {
                listener.onError(ex);
            }
        }
    }

    private void notifyProgress(int percent, String message) {
        if (listener != null) {
            listener.onProgress(percent, message);
        }
    }

    private void buildSystemSummary(StringBuilder sb, String timestamp) throws Exception {
        notifyProgress(25, "Fetching campus user and event metrics...");
        List<Student> students = userDAO.getAllStudents();
        List<Organizer> organizers = userDAO.getAllOrganizers();
        List<Event> events = eventDAO.getAllEvents();

        notifyProgress(50, "Calculating financial revenue summaries...");
        double netRevenue = paymentDAO.getTotalNetRevenue();
        Map<String, Double> categoryRevenue = paymentDAO.getRevenueByCategory();
        List<Booking> bookings = bookingDAO.getAllBookings();

        long confirmedCount = bookings.stream().filter(b -> Booking.STATUS_CONFIRMED.equalsIgnoreCase(b.getStatus())).count();
        long cancelledCount = bookings.stream().filter(b -> Booking.STATUS_CANCELLED.equalsIgnoreCase(b.getStatus())).count();

        notifyProgress(75, "Formatting executive summary layout...");

        sb.append("========================================================================================\n");
        sb.append("                UNIVIBE COLLEGE EVENT & TICKET MANAGEMENT SYSTEM                       \n");
        sb.append("                          EXECUTIVE AUDIT & SYSTEM SUMMARY                              \n");
        sb.append("========================================================================================\n");
        sb.append(" Generated Timestamp : ").append(timestamp).append("\n");
        sb.append(" System Version      : 2.0.0 (Core Java Standard Edition)\n");
        sb.append("----------------------------------------------------------------------------------------\n");
        sb.append(" 1. CAMPUS ENGAGEMENT OVERVIEW:\n");
        sb.append(String.format("    - Total Registered Students  : %d\n", students.size()));
        sb.append(String.format("    - Total Event Organizers     : %d\n", organizers.size()));
        sb.append(String.format("    - Total College Events       : %d\n", events.size()));
        sb.append(String.format("    - Confirmed Bookings         : %d\n", confirmedCount));
        sb.append(String.format("    - Cancelled Bookings         : %d\n", cancelledCount));
        sb.append(String.format("    - Total Net Revenue          : ₹%.2f\n", netRevenue));
        sb.append("----------------------------------------------------------------------------------------\n");
        sb.append(" 2. REVENUE DISTRIBUTION BY CATEGORY:\n");
        if (categoryRevenue.isEmpty()) {
            sb.append("    (No revenue recorded yet)\n");
        } else {
            for (Map.Entry<String, Double> entry : categoryRevenue.entrySet()) {
                sb.append(String.format("    - %-32s : ₹%.2f\n", entry.getKey(), entry.getValue()));
            }
        }
        sb.append("----------------------------------------------------------------------------------------\n");
        sb.append(" 3. ACTIVE EVENTS SUMMARY:\n");
        for (Event e : events) {
            sb.append(String.format("    [ID: %02d] %-40s | %-16s | ₹%-6.2f | Seats: %d/%d | %s\n",
                    e.getEventId(),
                    (e.getTitle().length() > 38 ? e.getTitle().substring(0, 35) + "..." : e.getTitle()),
                    e.getCategory(),
                    e.getTicketPrice(),
                    e.getAvailableSeats(),
                    e.getTotalSeats(),
                    e.getStatus()));
        }
        sb.append("========================================================================================\n");
        sb.append("                   END OF REPORT - CONFIDENTIAL CAMPUS DOCUMENT                         \n");
        sb.append("========================================================================================\n");
    }

    private void buildEventAnalytics(StringBuilder sb, String timestamp) throws Exception {
        notifyProgress(30, "Extracting event attendance and occupancy logs...");
        List<Event> events = eventDAO.getAllEvents();

        notifyProgress(60, "Generating CSV tabular dataset...");
        sb.append("Event ID,Title,Category,Venue,Date,Price (INR),Total Seats,Available Seats,Occupancy Rate (%),Status,Avg Rating\n");

        for (Event e : events) {
            sb.append(String.format("%d,\"%s\",\"%s\",\"%s\",%s,%.2f,%d,%d,%.1f,%s,%.1f\n",
                    e.getEventId(),
                    e.getTitle().replace("\"", "\"\""),
                    e.getCategory(),
                    e.getVenue().replace("\"", "\"\""),
                    new SimpleDateFormat("yyyy-MM-dd").format(e.getEventDate()),
                    e.getTicketPrice(),
                    e.getTotalSeats(),
                    e.getAvailableSeats(),
                    e.getOccupancyRate(),
                    e.getStatus(),
                    e.getAverageRating()));
        }
    }

    private void buildOrganizerPerformance(StringBuilder sb, String timestamp) throws Exception {
        notifyProgress(30, "Analyzing organizer portfolios...");
        List<Organizer> organizers = userDAO.getAllOrganizers();

        sb.append("========================================================================================\n");
        sb.append("                    UNIVIBE - ORGANIZER PERFORMANCE REPORT                             \n");
        sb.append("========================================================================================\n");
        sb.append(" Generated Timestamp : ").append(timestamp).append("\n");
        sb.append("----------------------------------------------------------------------------------------\n");

        for (Organizer org : organizers) {
            List<Event> orgEvents = eventDAO.getEventsByOrganizer(org.getOrganizerId());
            double totalRevenue = 0.0;
            int totalSeatsBooked = 0;
            double ratingSum = 0.0;
            int ratedEvents = 0;

            for (Event e : orgEvents) {
                totalSeatsBooked += e.getBookedSeats();
                totalRevenue += (e.getBookedSeats() * e.getTicketPrice());
                if (e.getAverageRating() > 0) {
                    ratingSum += e.getAverageRating();
                    ratedEvents++;
                }
            }
            double avgRating = ratedEvents > 0 ? (ratingSum / ratedEvents) : 0.0;

            sb.append(String.format(" Organizer : %s (%s, %s)\n", org.getFullName(), org.getDesignation(), org.getDepartment()));
            sb.append(String.format(" Unit      : %s | Email: %s\n", org.getOrganizationUnit(), org.getEmail()));
            sb.append(String.format(" Portfolio : Events Hosted: %d | Total Tickets Sold: %d | Revenue: ₹%.2f | Avg Rating: %.1f/5.0\n",
                    orgEvents.size(), totalSeatsBooked, totalRevenue, avgRating));
            sb.append("----------------------------------------------------------------------------------------\n");
        }
        sb.append("========================================================================================\n");
    }
}
