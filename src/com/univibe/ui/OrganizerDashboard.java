package com.univibe.ui;

import com.univibe.dao.BookingDAO;
import com.univibe.dao.EventDAO;
import com.univibe.model.Booking;
import com.univibe.model.Event;
import com.univibe.model.Organizer;
import com.univibe.model.Ticket;
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
 * Event Organizer Studio and Attendance Management Dashboard.
 * Demonstrates:
 *  1. Organizer workflows (Event creation, modification)
 *  2. Attendee tracking & Attendance Check-in management (CHECKED_IN, ABSENT)
 *  3. Revenue and Seat Occupancy monitoring
 *  4. Event status tracking
 */
public class OrganizerDashboard extends JFrame {

    private final Organizer organizer;
    private final EventDAO eventDAO;
    private final BookingDAO bookingDAO;
    private final EventManager eventManager;
    private final BookingManager bookingManager;

    private JLabel totalEventsLbl;
    private JLabel totalSoldLbl;
    private JLabel totalRevenueLbl;

    private JTable eventsTable;
    private DefaultTableModel eventsModel;

    private JTable attendeesTable;
    private DefaultTableModel attendeesModel;
    private JLabel selectedEventHeaderLbl;

    public OrganizerDashboard(Organizer organizer) {
        super("UniVibe - Organizer Event Studio");
        this.organizer = organizer;
        this.eventDAO = new EventDAO();
        this.bookingDAO = new BookingDAO();
        this.eventManager = new EventManager();
        this.bookingManager = new BookingManager();
        initUI();
        refreshEvents();
    }

    private void initUI() {
        setSize(1050, 720);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.BACKGROUND);

        // Header
        JPanel header = UITheme.createHeaderPanel(
                "UniVibe Organizer Event Studio",
                String.format("%s • %s (%s)", organizer.getFullName(), organizer.getDesignation(), organizer.getOrganizationUnit()),
                organizer,
                this::handleLogout
        );
        add(header, BorderLayout.NORTH);

        // Center Content
        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(12, 16, 12, 16));

        // 1. KPI Cards
        JPanel kpis = new JPanel(new GridLayout(1, 3, 12, 12));
        kpis.setOpaque(false);

        JPanel card1 = UITheme.createKPICard("My Events", "0", UITheme.PRIMARY, "Hosted");
        totalEventsLbl = (JLabel) ((BorderLayout) card1.getLayout()).getLayoutComponent(BorderLayout.CENTER);

        JPanel card2 = UITheme.createKPICard("Tickets Sold", "0", UITheme.SUCCESS, "Attendees");
        totalSoldLbl = (JLabel) ((BorderLayout) card2.getLayout()).getLayoutComponent(BorderLayout.CENTER);

        JPanel card3 = UITheme.createKPICard("Event Revenue", "₹0.00", UITheme.WARNING, "Gross Value");
        totalRevenueLbl = (JLabel) ((BorderLayout) card3.getLayout()).getLayoutComponent(BorderLayout.CENTER);

        kpis.add(card1);
        kpis.add(card2);
        kpis.add(card3);
        center.add(kpis, BorderLayout.NORTH);

        // 2. Split Panel: Top = My Events, Bottom = Attendees & Attendance Check-in
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.5);
        splitPane.setBorder(null);

        // Top Panel: My Events
        JPanel myEventsPanel = UITheme.createCardPanel();
        myEventsPanel.setLayout(new BorderLayout(8, 8));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        JLabel evtTitle = new JLabel("My Organized College Events (Select to View Attendees):");
        evtTitle.setFont(UITheme.FONT_BOLD);
        topBar.add(evtTitle, BorderLayout.WEST);

        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionBtns.setOpaque(false);

        JButton createBtn = UITheme.createSuccessButton("+ Create New Event");
        createBtn.addActionListener(e -> openCreateEventDialog());

        JButton refreshBtn = UITheme.createOutlineButton("Refresh");
        refreshBtn.addActionListener(e -> refreshEvents());

        actionBtns.add(createBtn);
        actionBtns.add(refreshBtn);
        topBar.add(actionBtns, BorderLayout.EAST);
        myEventsPanel.add(topBar, BorderLayout.NORTH);

        String[] cols = {"ID", "Event Title", "Category", "Venue", "Date", "Price", "Remaining / Total", "Status", "Avg Rating"};
        eventsModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        eventsTable = new JTable(eventsModel);
        UITheme.styleTable(eventsTable);
        eventsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadAttendeesForSelectedEvent();
            }
        });
        myEventsPanel.add(new JScrollPane(eventsTable), BorderLayout.CENTER);
        splitPane.setTopComponent(myEventsPanel);

        // Bottom Panel: Attendees & Attendance Check-in
        JPanel attendeesPanel = UITheme.createCardPanel();
        attendeesPanel.setLayout(new BorderLayout(8, 8));

        JPanel attendeeHeader = new JPanel(new BorderLayout());
        attendeeHeader.setOpaque(false);

        selectedEventHeaderLbl = new JLabel("Registered Attendees & Attendance Check-in:");
        selectedEventHeaderLbl.setFont(UITheme.FONT_BOLD);
        attendeeHeader.add(selectedEventHeaderLbl, BorderLayout.WEST);

        JPanel checkinBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        checkinBtns.setOpaque(false);

        JButton checkinBtn = UITheme.createSuccessButton("Mark Checked-In");
        checkinBtn.addActionListener(e -> updateAttendanceStatus(Ticket.ATTENDANCE_CHECKED_IN));

        JButton absentBtn = UITheme.createDangerButton("Mark Absent");
        absentBtn.addActionListener(e -> updateAttendanceStatus(Ticket.ATTENDANCE_ABSENT));

        checkinBtns.add(checkinBtn);
        checkinBtns.add(absentBtn);
        attendeeHeader.add(checkinBtns, BorderLayout.EAST);
        attendeesPanel.add(attendeeHeader, BorderLayout.NORTH);

        String[] attCols = {"Ticket ID", "Student Name", "Roll No", "Seat No", "Ticket Code", "Check-in Status"};
        attendeesModel = new DefaultTableModel(attCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        attendeesTable = new JTable(attendeesModel);
        UITheme.styleTable(attendeesTable);
        attendeesPanel.add(new JScrollPane(attendeesTable), BorderLayout.CENTER);
        splitPane.setBottomComponent(attendeesPanel);

        center.add(splitPane, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
    }

    private void openCreateEventDialog() {
        CreateEventDialog dialog = new CreateEventDialog(this, organizer, eventManager);
        dialog.setVisible(true);
        if (dialog.isEventCreated()) {
            refreshEvents();
        }
    }

    private void refreshEvents() {
        try {
            List<Event> myEvents = eventDAO.getEventsByOrganizer(organizer.getOrganizerId());

            int totalSold = 0;
            double totalRevenue = 0.0;

            eventsModel.setRowCount(0);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            for (Event e : myEvents) {
                int sold = e.getBookedSeats();
                totalSold += sold;
                totalRevenue += (sold * e.getTicketPrice());

                eventsModel.addRow(new Object[]{
                        e.getEventId(),
                        e.getTitle(),
                        e.getCategory(),
                        e.getVenue(),
                        sdf.format(e.getEventDate()),
                        String.format("₹%.2f", e.getTicketPrice()),
                        String.format("%d / %d", e.getAvailableSeats(), e.getTotalSeats()),
                        e.getStatus(),
                        (e.getAverageRating() > 0 ? String.format("%.1f ★", e.getAverageRating()) : "Unrated")
                });
            }

            totalEventsLbl.setText(String.valueOf(myEvents.size()));
            totalSoldLbl.setText(String.valueOf(totalSold));
            totalRevenueLbl.setText(String.format("₹%.2f", totalRevenue));

            if (!myEvents.isEmpty() && eventsTable.getSelectedRow() < 0) {
                eventsTable.setRowSelectionInterval(0, 0);
            } else if (myEvents.isEmpty()) {
                attendeesModel.setRowCount(0);
                selectedEventHeaderLbl.setText("No events created yet. Click '+ Create New Event' above.");
            }

        } catch (Exception ex) {
            System.err.println("[OrganizerDashboard] Error refreshing events: " + ex.getMessage());
        }
    }

    private void loadAttendeesForSelectedEvent() {
        int row = eventsTable.getSelectedRow();
        if (row < 0) return;

        int eventId = (Integer) eventsModel.getValueAt(row, 0);
        String eventTitle = (String) eventsModel.getValueAt(row, 1);
        selectedEventHeaderLbl.setText("Attendees for: " + eventTitle);

        try {
            List<Booking> bookings = bookingDAO.getBookingsByEvent(eventId);
            attendeesModel.setRowCount(0);

            for (Booking b : bookings) {
                if (Booking.STATUS_CONFIRMED.equalsIgnoreCase(b.getStatus()) && b.getTickets() != null) {
                    for (Ticket t : b.getTickets()) {
                        attendeesModel.addRow(new Object[]{
                                t.getTicketId(),
                                t.getStudentName(),
                                t.getRollNumber(),
                                t.getSeatNumber(),
                                t.getTicketCode(),
                                t.getAttendanceStatus()
                        });
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println("[OrganizerDashboard] Error loading attendees: " + ex.getMessage());
        }
    }

    private void updateAttendanceStatus(String newStatus) {
        int row = attendeesTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an attendee from the table.", "Select Attendee", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int ticketId = (Integer) attendeesModel.getValueAt(row, 0);
        String studentName = (String) attendeesModel.getValueAt(row, 1);

        try {
            boolean ok = bookingManager.updateAttendance(ticketId, newStatus, organizer.getUsername());
            if (ok) {
                attendeesModel.setValueAt(newStatus, row, 5);
                JOptionPane.showMessageDialog(this,
                        String.format("Attendance for %s updated to: %s", studentName, newStatus),
                        "Attendance Updated",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error updating attendance: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
