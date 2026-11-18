package com.univibe.ui;

import com.univibe.exception.InvalidBookingException;
import com.univibe.model.*;
import com.univibe.model.Event;
import com.univibe.service.AuthenticationService;
import com.univibe.service.BookingManager;
import com.univibe.service.EventManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Student Campus Hub & Event Discovery Dashboard.
 * Demonstrates:
 *  1. Live Search & Multi-criteria Filtering (Keyword, Category, Sorting)
 *  2. Low-seat badge visualization
 *  3. Ticket Booking and Confirmation flows
 *  4. Booking history with cancellation and seat restoration
 *  5. Event Review and Star Rating dialog integration
 *  6. Printable Digital Pass launch
 */
public class StudentDashboard extends JFrame {

    private final Student student;
    private final EventManager eventManager;
    private final BookingManager bookingManager;

    // Discover Events Components
    private JTextField searchField;
    private JComboBox<String> categoryFilterCombo;
    private JComboBox<String> sortCombo;
    private JTable eventsTable;
    private DefaultTableModel eventsModel;
    private List<Event> currentEventsList;

    // My Bookings Components
    private JTable bookingsTable;
    private DefaultTableModel bookingsModel;
    private List<Booking> currentBookingsList;

    public StudentDashboard(Student student) {
        super("UniVibe - Student Campus Hub");
        this.student = student;
        this.eventManager = new EventManager();
        this.bookingManager = new BookingManager();
        initUI();
        performSearch();
        refreshBookings();
    }

    private void initUI() {
        setSize(1100, 750);
        setMinimumSize(new Dimension(950, 650));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.BACKGROUND);

        // Header Panel
        JPanel header = UITheme.createHeaderPanel(
                "UniVibe Student Campus Hub",
                String.format("Welcome, %s • Roll No: %s • %s (Sem %d)",
                        student.getFullName(), student.getRollNumber(), student.getDepartment(), student.getSemester()),
                student,
                this::handleLogout
        );
        add(header, BorderLayout.NORTH);

        // Center Content Tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_BOLD);
        tabbedPane.setBorder(new EmptyBorder(10, 14, 10, 14));

        tabbedPane.addTab("Discover College Events", createDiscoverTab());
        tabbedPane.addTab("My Bookings & Digital Passes", createBookingsTab());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createDiscoverTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UITheme.BACKGROUND);

        // Top Filter Bar
        JPanel filterCard = UITheme.createCardPanel();
        filterCard.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 6));

        filterCard.add(new JLabel("Search Event / Venue:"));
        searchField = new JTextField(14);
        searchField.setFont(UITheme.FONT_REGULAR);
        searchField.addActionListener(e -> performSearch());
        filterCard.add(searchField);

        filterCard.add(new JLabel("Category:"));
        String[] categories = new String[EventCategory.values().length + 1];
        categories[0] = "All";
        for (int i = 0; i < EventCategory.values().length; i++) {
            categories[i + 1] = EventCategory.values()[i].getDisplayName();
        }
        categoryFilterCombo = new JComboBox<>(categories);
        categoryFilterCombo.setFont(UITheme.FONT_REGULAR);
        categoryFilterCombo.addActionListener(e -> performSearch());
        filterCard.add(categoryFilterCombo);

        filterCard.add(new JLabel("Sort By:"));
        String[] sorts = {"Date (Soonest)", "Popularity", "Price (Low to High)", "Price (High to Low)", "Rating (Highest)"};
        sortCombo = new JComboBox<>(sorts);
        sortCombo.setFont(UITheme.FONT_REGULAR);
        sortCombo.addActionListener(e -> performSearch());
        filterCard.add(sortCombo);

        JButton searchBtn = UITheme.createPrimaryButton("Search");
        searchBtn.addActionListener(e -> performSearch());
        filterCard.add(searchBtn);

        panel.add(filterCard, BorderLayout.NORTH);

        // Center Events Table
        JPanel tableCard = UITheme.createCardPanel();
        tableCard.setLayout(new BorderLayout(8, 8));

        String[] cols = {"ID", "Event Title", "Category", "Venue", "Date & Time", "Ticket Price", "Seats Left", "Status", "Rating"};
        eventsModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        eventsTable = new JTable(eventsModel);
        UITheme.styleTable(eventsTable);
        tableCard.add(new JScrollPane(eventsTable), BorderLayout.CENTER);

        // Bottom Action Buttons
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        actionRow.setOpaque(false);

        JButton viewDetailsBtn = UITheme.createOutlineButton("View Details & Description");
        viewDetailsBtn.addActionListener(e -> showEventDetails());

        JButton bookBtn = UITheme.createSuccessButton("Book Tickets Now");
        bookBtn.addActionListener(e -> bookTicketAction());

        actionRow.add(viewDetailsBtn);
        actionRow.add(bookBtn);
        tableCard.add(actionRow, BorderLayout.SOUTH);

        panel.add(tableCard, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createBookingsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UITheme.BACKGROUND);

        JPanel tableCard = UITheme.createCardPanel();
        tableCard.setLayout(new BorderLayout(8, 8));

        JLabel titleLbl = new JLabel("My Confirmed & Past Event Reservations:");
        titleLbl.setFont(UITheme.FONT_BOLD);
        tableCard.add(titleLbl, BorderLayout.NORTH);

        String[] cols = {"Booking Ref", "Event Name", "Category", "Venue", "Date & Time", "Seats", "Total Paid", "Status"};
        bookingsModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        bookingsTable = new JTable(bookingsModel);
        UITheme.styleTable(bookingsTable);
        tableCard.add(new JScrollPane(bookingsTable), BorderLayout.CENTER);

        // Action Buttons Row
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        actionRow.setOpaque(false);

        JButton viewTicketBtn = UITheme.createPrimaryButton("View & Print Digital Pass (QR)");
        viewTicketBtn.addActionListener(e -> viewDigitalPassAction());

        JButton cancelBtn = UITheme.createDangerButton("Cancel Booking");
        cancelBtn.addActionListener(e -> cancelBookingAction());

        JButton feedbackBtn = UITheme.createSuccessButton("Rate & Review Event");
        feedbackBtn.addActionListener(e -> rateEventAction());

        JButton refreshBtn = UITheme.createOutlineButton("Refresh");
        refreshBtn.addActionListener(e -> refreshBookings());

        actionRow.add(viewTicketBtn);
        actionRow.add(cancelBtn);
        actionRow.add(feedbackBtn);
        actionRow.add(refreshBtn);

        tableCard.add(actionRow, BorderLayout.SOUTH);
        panel.add(tableCard, BorderLayout.CENTER);
        return panel;
    }

    private void performSearch() {
        String keyword = searchField.getText().trim();
        String category = (String) categoryFilterCombo.getSelectedItem();
        String sort = (String) sortCombo.getSelectedItem();

        try {
            currentEventsList = eventManager.searchEvents(keyword, category, null, null, sort);
            eventsModel.setRowCount(0);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            for (Event e : currentEventsList) {
                String seatInfo = String.valueOf(e.getAvailableSeats());
                if (e.isLowSeatAvailability()) {
                    seatInfo += " [LOW SEATS!]";
                } else if (e.isHousefull()) {
                    seatInfo = "HOUSEFULL";
                }

                String ratingStr = (e.getAverageRating() > 0)
                        ? String.format("%.1f ★ (%d)", e.getAverageRating(), e.getFeedbackCount())
                        : "No reviews";

                eventsModel.addRow(new Object[]{
                        e.getEventId(),
                        e.getTitle(),
                        e.getCategory(),
                        e.getVenue(),
                        sdf.format(e.getEventDate()) + " " + e.getEventTime(),
                        String.format("₹%.2f", e.getTicketPrice()),
                        seatInfo,
                        e.getStatus(),
                        ratingStr
                });
            }
        } catch (Exception ex) {
            System.err.println("[StudentDashboard] Error searching events: " + ex.getMessage());
        }
    }

    private void showEventDetails() {
        int row = eventsTable.getSelectedRow();
        if (row < 0 || currentEventsList == null || row >= currentEventsList.size()) {
            JOptionPane.showMessageDialog(this, "Please select an event row to view details.", "Select Event", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Event e = currentEventsList.get(row);
        StringBuilder sb = new StringBuilder();
        sb.append(e.getTitle()).append("\n\n");
        sb.append("Category    : ").append(e.getCategory()).append("\n");
        sb.append("Venue       : ").append(e.getVenue()).append("\n");
        sb.append("Date & Time : ").append(new SimpleDateFormat("yyyy-MM-dd").format(e.getEventDate())).append(" at ").append(e.getEventTime()).append("\n");
        sb.append("Price       : ₹").append(String.format("%.2f", e.getTicketPrice())).append("\n");
        sb.append("Capacity    : ").append(e.getAvailableSeats()).append(" available out of ").append(e.getTotalSeats()).append(" total\n");
        sb.append("Rating      : ").append(e.getAverageRating() > 0 ? String.format("%.1f / 5.0", e.getAverageRating()) : "Unrated").append("\n\n");
        sb.append("Description:\n").append(e.getDescription());

        JOptionPane.showMessageDialog(this, sb.toString(), "Event Information", JOptionPane.INFORMATION_MESSAGE);
    }

    private void bookTicketAction() {
        int row = eventsTable.getSelectedRow();
        if (row < 0 || currentEventsList == null || row >= currentEventsList.size()) {
            JOptionPane.showMessageDialog(this, "Please select an event from the list to book tickets.", "Select Event", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Event selectedEvent = currentEventsList.get(row);
        if (selectedEvent.isHousefull()) {
            JOptionPane.showMessageDialog(this, "Sorry, this event is completely housefull!", "Sold Out", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BookingDialog dialog = new BookingDialog(this, student, selectedEvent, bookingManager);
        dialog.setVisible(true);

        if (dialog.isBookingSuccessful()) {
            performSearch();
            refreshBookings();
        }
    }

    private void refreshBookings() {
        try {
            currentBookingsList = bookingManager.getStudentBookings(student.getStudentId());
            bookingsModel.setRowCount(0);

            for (Booking b : currentBookingsList) {
                bookingsModel.addRow(new Object[]{
                        b.getBookingReference(),
                        b.getEventTitle(),
                        b.getEventCategory(),
                        b.getVenue(),
                        b.getEventDateStr(),
                        b.getNumberOfSeats(),
                        String.format("₹%.2f", b.getTotalAmount()),
                        b.getStatus()
                });
            }
        } catch (Exception ex) {
            System.err.println("[StudentDashboard] Error refreshing bookings: " + ex.getMessage());
        }
    }

    private void viewDigitalPassAction() {
        int row = bookingsTable.getSelectedRow();
        if (row < 0 || currentBookingsList == null || row >= currentBookingsList.size()) {
            JOptionPane.showMessageDialog(this, "Please select a booking row from the table.", "Select Booking", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Booking b = currentBookingsList.get(row);
        if (b.getTickets() == null || b.getTickets().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No digital tickets found for this booking.", "Pass Unavailable", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Ticket firstTicket = b.getTickets().get(0);
        TicketReceiptDialog dialog = new TicketReceiptDialog(this, firstTicket);
        dialog.setVisible(true);
    }

    private void cancelBookingAction() {
        int row = bookingsTable.getSelectedRow();
        if (row < 0 || currentBookingsList == null || row >= currentBookingsList.size()) {
            JOptionPane.showMessageDialog(this, "Please select a booking to cancel.", "Select Booking", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Booking b = currentBookingsList.get(row);
        if (!b.isEligibleForCancellation()) {
            JOptionPane.showMessageDialog(this, "This booking has already been cancelled or is ineligible.", "Cancellation Ineligible", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                String.format("Are you sure you want to cancel booking '%s'?\n%d seat(s) will be restored to the event.",
                        b.getBookingReference(), b.getNumberOfSeats()),
                "Confirm Cancellation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean ok = bookingManager.cancelBooking(b.getBookingId(), student.getUsername());
                if (ok) {
                    JOptionPane.showMessageDialog(this,
                            "Booking successfully cancelled.\nSeats have been restored and payment marked refunded.",
                            "Cancelled",
                            JOptionPane.INFORMATION_MESSAGE);
                    refreshBookings();
                    performSearch();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error cancelling booking: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void rateEventAction() {
        int row = bookingsTable.getSelectedRow();
        if (row < 0 || currentBookingsList == null || row >= currentBookingsList.size()) {
            JOptionPane.showMessageDialog(this, "Please select an event booking to rate.", "Select Booking", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Booking b = currentBookingsList.get(row);
        try {
            boolean alreadyGiven = eventManager.hasStudentGivenFeedback(student.getStudentId(), b.getEventId());
            if (alreadyGiven) {
                JOptionPane.showMessageDialog(this, "You have already submitted a review for this event. Thank you!", "Already Reviewed", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            Event event = eventManager.getEventById(b.getEventId());
            FeedbackDialog dialog = new FeedbackDialog(this, student, event, eventManager);
            dialog.setVisible(true);
            performSearch();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?", "Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            AuthenticationService.logout();
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
}
