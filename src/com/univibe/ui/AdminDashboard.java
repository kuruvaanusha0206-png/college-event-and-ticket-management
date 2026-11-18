package com.univibe.ui;

import com.univibe.dao.BookingDAO;
import com.univibe.dao.EventDAO;
import com.univibe.dao.PaymentDAO;
import com.univibe.dao.UserDAO;
import com.univibe.model.*;
import com.univibe.model.Event;
import com.univibe.service.AuthenticationService;
import com.univibe.service.ReportGenerator;
import com.univibe.util.FileHandler;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Administrator Operations and Analytics Dashboard.
 * Demonstrates:
 *  1. Swing JTabbedPane, JTable, and custom KPI cards
 *  2. Multithreaded Background Report Generation with JProgressBar
 *  3. Event approval/rejection workflows
 *  4. User account management (deactivate/activate, delete)
 *  5. Financial and Booking Audits
 */
public class AdminDashboard extends JFrame {

    private final Admin admin;
    private final EventDAO eventDAO;
    private final BookingDAO bookingDAO;
    private final PaymentDAO paymentDAO;
    private final UserDAO userDAO;

    // KPI Labels
    private JLabel totalStudentsLbl;
    private JLabel totalEventsLbl;
    private JLabel totalBookingsLbl;
    private JLabel cancelledBookingsLbl;
    private JLabel totalRevenueLbl;

    // Tables
    private JTable eventsTable;
    private DefaultTableModel eventsModel;

    private JTable studentsTable;
    private DefaultTableModel studentsModel;

    private JTable organizersTable;
    private DefaultTableModel organizersModel;

    private JTable bookingsTable;
    private DefaultTableModel bookingsModel;

    // Reporting controls
    private JProgressBar reportProgressBar;
    private JLabel reportStatusLbl;
    private JTextArea auditLogArea;

    public AdminDashboard(Admin admin) {
        super("UniVibe - Administrator Operations Console");
        this.admin = admin;
        this.eventDAO = new EventDAO();
        this.bookingDAO = new BookingDAO();
        this.paymentDAO = new PaymentDAO();
        this.userDAO = new UserDAO();
        initUI();
        refreshAllData();
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
                "UniVibe Campus Administration",
                "Central Control Console • Academic Event & Revenue Governance",
                admin,
                this::handleLogout
        );
        add(header, BorderLayout.NORTH);

        // Center Panel (KPI Cards + Tabbed Pane)
        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(12, 16, 12, 16));

        // 1. KPI Cards Row
        JPanel kpiPanel = new JPanel(new GridLayout(1, 5, 10, 10));
        kpiPanel.setOpaque(false);

        JPanel cardStudents = UITheme.createKPICard("Students", "0", UITheme.PRIMARY_LIGHT, "Registered");
        totalStudentsLbl = (JLabel) ((BorderLayout) cardStudents.getLayout()).getLayoutComponent(BorderLayout.CENTER);

        JPanel cardEvents = UITheme.createKPICard("Events", "0", UITheme.SUCCESS, "College Total");
        totalEventsLbl = (JLabel) ((BorderLayout) cardEvents.getLayout()).getLayoutComponent(BorderLayout.CENTER);

        JPanel cardBookings = UITheme.createKPICard("Bookings", "0", UITheme.PRIMARY, "Confirmed");
        totalBookingsLbl = (JLabel) ((BorderLayout) cardBookings.getLayout()).getLayoutComponent(BorderLayout.CENTER);

        JPanel cardCancelled = UITheme.createKPICard("Cancelled", "0", UITheme.DANGER, "Refunded");
        cancelledBookingsLbl = (JLabel) ((BorderLayout) cardCancelled.getLayout()).getLayoutComponent(BorderLayout.CENTER);

        JPanel cardRevenue = UITheme.createKPICard("Net Revenue", "₹0.00", UITheme.WARNING, "Collections");
        totalRevenueLbl = (JLabel) ((BorderLayout) cardRevenue.getLayout()).getLayoutComponent(BorderLayout.CENTER);

        kpiPanel.add(cardStudents);
        kpiPanel.add(cardEvents);
        kpiPanel.add(cardBookings);
        kpiPanel.add(cardCancelled);
        kpiPanel.add(cardRevenue);
        center.add(kpiPanel, BorderLayout.NORTH);

        // 2. Tabbed Pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_BOLD);

        tabbedPane.addTab("Event Approvals & Catalog", createEventsTab());
        tabbedPane.addTab("User Accounts", createUsersTab());
        tabbedPane.addTab("Bookings & Tickets", createBookingsTab());
        tabbedPane.addTab("Reports & Background Workers", createReportsTab());

        center.add(tabbedPane, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
    }

    private JPanel createEventsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UITheme.CARD_BG);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] cols = {"ID", "Event Title", "Category", "Venue", "Date", "Price", "Seats (Avail/Total)", "Organizer", "Status"};
        eventsModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        eventsTable = new JTable(eventsModel);
        UITheme.styleTable(eventsTable);
        panel.add(new JScrollPane(eventsTable), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        actions.setOpaque(false);

        JButton approveBtn = UITheme.createSuccessButton("Approve Event");
        approveBtn.addActionListener(e -> updateSelectedEventStatus(Event.STATUS_APPROVED));

        JButton rejectBtn = UITheme.createDangerButton("Reject Event");
        rejectBtn.addActionListener(e -> updateSelectedEventStatus(Event.STATUS_REJECTED));

        JButton refreshBtn = UITheme.createOutlineButton("Refresh");
        refreshBtn.addActionListener(e -> refreshAllData());

        actions.add(approveBtn);
        actions.add(rejectBtn);
        actions.add(refreshBtn);
        panel.add(actions, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createUsersTab() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 10));
        panel.setOpaque(false);

        // Left: Students
        JPanel stuPanel = new JPanel(new BorderLayout(8, 8));
        stuPanel.setBackground(UITheme.CARD_BG);
        stuPanel.setBorder(BorderFactory.createTitledBorder("Registered Students"));

        String[] stuCols = {"User ID", "Name", "Roll No", "Dept", "Sem", "Status"};
        studentsModel = new DefaultTableModel(stuCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        studentsTable = new JTable(studentsModel);
        UITheme.styleTable(studentsTable);
        stuPanel.add(new JScrollPane(studentsTable), BorderLayout.CENTER);

        JButton toggleStuBtn = UITheme.createOutlineButton("Toggle Active/Inactive");
        toggleStuBtn.addActionListener(e -> toggleUserStatus(studentsTable, studentsModel));
        stuPanel.add(toggleStuBtn, BorderLayout.SOUTH);

        // Right: Organizers
        JPanel orgPanel = new JPanel(new BorderLayout(8, 8));
        orgPanel.setBackground(UITheme.CARD_BG);
        orgPanel.setBorder(BorderFactory.createTitledBorder("Event Organizers"));

        String[] orgCols = {"User ID", "Name", "Designation", "Unit", "Status"};
        organizersModel = new DefaultTableModel(orgCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        organizersTable = new JTable(organizersModel);
        UITheme.styleTable(organizersTable);
        orgPanel.add(new JScrollPane(organizersTable), BorderLayout.CENTER);

        JButton toggleOrgBtn = UITheme.createOutlineButton("Toggle Active/Inactive");
        toggleOrgBtn.addActionListener(e -> toggleUserStatus(organizersTable, organizersModel));
        orgPanel.add(toggleOrgBtn, BorderLayout.SOUTH);

        panel.add(stuPanel);
        panel.add(orgPanel);
        return panel;
    }

    private JPanel createBookingsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UITheme.CARD_BG);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] cols = {"Booking Ref", "Student", "Roll No", "Event Title", "Seats", "Total Paid", "Status", "Date"};
        bookingsModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        bookingsTable = new JTable(bookingsModel);
        UITheme.styleTable(bookingsTable);
        panel.add(new JScrollPane(bookingsTable), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        actions.setOpaque(false);
        JButton refreshBtn = UITheme.createOutlineButton("Refresh Bookings");
        refreshBtn.addActionListener(e -> refreshAllData());
        actions.add(refreshBtn);
        panel.add(actions, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createReportsTab() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(UITheme.CARD_BG);
        panel.setBorder(new EmptyBorder(14, 16, 14, 16));

        // Top: Background Worker Controls
        JPanel workerCard = UITheme.createCardPanel();
        workerCard.setLayout(new BoxLayout(workerCard, BoxLayout.Y_AXIS));

        JLabel infoLbl = new JLabel("Multithreaded Background Report Engine");
        infoLbl.setFont(UITheme.FONT_HEADER);
        infoLbl.setForeground(UITheme.PRIMARY);
        workerCard.add(infoLbl);

        JLabel descLbl = new JLabel("Reports execute on an independent worker thread without freezing the Swing UI.");
        descLbl.setFont(UITheme.FONT_SUBHEADER);
        descLbl.setForeground(UITheme.TEXT_MUTED);
        workerCard.add(descLbl);
        workerCard.add(Box.createRigidArea(new Dimension(0, 12)));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnRow.setOpaque(false);

        JButton sysSummaryBtn = UITheme.createPrimaryButton("System Summary (.txt)");
        sysSummaryBtn.addActionListener(e -> launchReportWorker(ReportGenerator.ReportType.SYSTEM_SUMMARY));

        JButton eventAnalyticsBtn = UITheme.createSuccessButton("Event Analytics Dataset (.csv)");
        eventAnalyticsBtn.addActionListener(e -> launchReportWorker(ReportGenerator.ReportType.EVENT_WISE_ANALYTICS));

        JButton orgPerfBtn = UITheme.createOutlineButton("Organizer Performance (.txt)");
        orgPerfBtn.addActionListener(e -> launchReportWorker(ReportGenerator.ReportType.ORGANIZER_PERFORMANCE));

        btnRow.add(sysSummaryBtn);
        btnRow.add(eventAnalyticsBtn);
        btnRow.add(orgPerfBtn);
        workerCard.add(btnRow);
        workerCard.add(Box.createRigidArea(new Dimension(0, 14)));

        reportProgressBar = new JProgressBar(0, 100);
        reportProgressBar.setStringPainted(true);
        reportProgressBar.setValue(0);
        reportProgressBar.setPreferredSize(new Dimension(400, 22));
        workerCard.add(reportProgressBar);
        workerCard.add(Box.createRigidArea(new Dimension(0, 6)));

        reportStatusLbl = new JLabel("Idle - Ready to generate reports.");
        reportStatusLbl.setFont(UITheme.FONT_SMALL);
        reportStatusLbl.setForeground(UITheme.TEXT_MUTED);
        workerCard.add(reportStatusLbl);

        panel.add(workerCard, BorderLayout.NORTH);

        // Bottom: Live Audit Trail Log viewer
        JPanel auditPanel = new JPanel(new BorderLayout(6, 6));
        auditPanel.setOpaque(false);

        JLabel auditTitle = new JLabel("Real-time System Audit & Activity Log:");
        auditTitle.setFont(UITheme.FONT_BOLD);
        auditTitle.setForeground(UITheme.TEXT_DARK);
        auditPanel.add(auditTitle, BorderLayout.NORTH);

        auditLogArea = new JTextArea(10, 50);
        auditLogArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        auditLogArea.setEditable(false);
        auditLogArea.setBackground(new Color(241, 245, 249));
        auditPanel.add(new JScrollPane(auditLogArea), BorderLayout.CENTER);

        panel.add(auditPanel, BorderLayout.CENTER);

        return panel;
    }

    private void launchReportWorker(ReportGenerator.ReportType type) {
        reportProgressBar.setValue(0);
        reportStatusLbl.setText("Spawning background thread worker...");

        ReportGenerator.ReportProgressListener listener = new ReportGenerator.ReportProgressListener() {
            @Override
            public void onProgress(int percentage, String message) {
                SwingUtilities.invokeLater(() -> {
                    reportProgressBar.setValue(percentage);
                    reportStatusLbl.setText(message + " (" + percentage + "%)");
                });
            }

            @Override
            public void onComplete(File generatedFile) {
                SwingUtilities.invokeLater(() -> {
                    reportProgressBar.setValue(100);
                    reportStatusLbl.setText("Report successfully saved: " + generatedFile.getName());
                    refreshAuditLogs();
                    JOptionPane.showMessageDialog(AdminDashboard.this,
                            "Background report completed!\nFile path: " + generatedFile.getAbsolutePath(),
                            "Report Exported",
                            JOptionPane.INFORMATION_MESSAGE);
                });
            }

            @Override
            public void onError(Exception error) {
                SwingUtilities.invokeLater(() -> {
                    reportProgressBar.setValue(0);
                    reportStatusLbl.setText("Error generating report: " + error.getMessage());
                    JOptionPane.showMessageDialog(AdminDashboard.this,
                            "Report Generation Failed: " + error.getMessage(),
                            "Worker Exception",
                            JOptionPane.ERROR_MESSAGE);
                });
            }
        };

        ReportGenerator generator = new ReportGenerator(type, listener);
        Thread workerThread = new Thread(generator, "Report-Worker-" + type.name());
        workerThread.start();
    }

    private void updateSelectedEventStatus(String newStatus) {
        int row = eventsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an event row first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int eventId = (Integer) eventsModel.getValueAt(row, 0);
        try {
            boolean ok = eventDAO.updateEventStatus(eventId, newStatus);
            if (ok) {
                FileHandler.logAuditActivity("EVENT_STATUS_CHANGE", admin.getUsername(),
                        "Changed event ID " + eventId + " status to " + newStatus);
                refreshAllData();
                JOptionPane.showMessageDialog(this, "Event status updated to " + newStatus, "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error updating event: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void toggleUserStatus(JTable table, DefaultTableModel model) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user row first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int userId = (Integer) model.getValueAt(row, 0);
        String currentStatus = (String) model.getValueAt(row, model.getColumnCount() - 1);
        String newStatus = User.STATUS_ACTIVE.equalsIgnoreCase(currentStatus) ? User.STATUS_INACTIVE : User.STATUS_ACTIVE;

        try {
            boolean ok = userDAO.updateUserStatus(userId, newStatus);
            if (ok) {
                FileHandler.logAuditActivity("USER_STATUS_CHANGE", admin.getUsername(),
                        "Changed user ID " + userId + " status to " + newStatus);
                refreshAllData();
                JOptionPane.showMessageDialog(this, "User status toggled to " + newStatus, "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error updating user: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshAllData() {
        try {
            // Refresh KPIs
            List<Student> students = userDAO.getAllStudents();
            List<Organizer> organizers = userDAO.getAllOrganizers();
            List<Event> events = eventDAO.getAllEvents();
            List<Booking> bookings = bookingDAO.getAllBookings();
            double netRev = paymentDAO.getTotalNetRevenue();

            totalStudentsLbl.setText(String.valueOf(students.size()));
            totalEventsLbl.setText(String.valueOf(events.size()));

            long confirmedCount = bookings.stream().filter(b -> Booking.STATUS_CONFIRMED.equalsIgnoreCase(b.getStatus())).count();
            long cancelledCount = bookings.stream().filter(b -> Booking.STATUS_CANCELLED.equalsIgnoreCase(b.getStatus())).count();

            totalBookingsLbl.setText(String.valueOf(confirmedCount));
            cancelledBookingsLbl.setText(String.valueOf(cancelledCount));
            totalRevenueLbl.setText(String.format("₹%.2f", netRev));

            // Refresh Events Table
            eventsModel.setRowCount(0);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            for (Event e : events) {
                eventsModel.addRow(new Object[]{
                        e.getEventId(),
                        e.getTitle(),
                        e.getCategory(),
                        e.getVenue(),
                        sdf.format(e.getEventDate()),
                        String.format("₹%.2f", e.getTicketPrice()),
                        String.format("%d / %d", e.getAvailableSeats(), e.getTotalSeats()),
                        (e.getOrganizerName() != null ? e.getOrganizerName() : "ID: " + e.getOrganizerId()),
                        e.getStatus()
                });
            }

            // Refresh Students Table
            studentsModel.setRowCount(0);
            for (Student s : students) {
                studentsModel.addRow(new Object[]{
                        s.getUserId(),
                        s.getFullName(),
                        s.getRollNumber(),
                        s.getDepartment(),
                        s.getSemester(),
                        s.getStatus()
                });
            }

            // Refresh Organizers Table
            organizersModel.setRowCount(0);
            for (Organizer o : organizers) {
                organizersModel.addRow(new Object[]{
                        o.getUserId(),
                        o.getFullName(),
                        o.getDesignation(),
                        o.getOrganizationUnit(),
                        o.getStatus()
                });
            }

            // Refresh Bookings Table
            bookingsModel.setRowCount(0);
            for (Booking b : bookings) {
                bookingsModel.addRow(new Object[]{
                        b.getBookingReference(),
                        b.getStudentName(),
                        b.getStudentRollNo(),
                        b.getEventTitle(),
                        b.getNumberOfSeats(),
                        String.format("₹%.2f", b.getTotalAmount()),
                        b.getStatus(),
                        new SimpleDateFormat("yyyy-MM-dd HH:mm").format(b.getBookingDate())
                });
            }

            refreshAuditLogs();

        } catch (Exception ex) {
            System.err.println("[AdminDashboard] Error refreshing data: " + ex.getMessage());
        }
    }

    private void refreshAuditLogs() {
        if (auditLogArea != null) {
            List<String> logs = FileHandler.readRecentAuditLogs(40);
            StringBuilder sb = new StringBuilder();
            for (String line : logs) {
                sb.append(line).append("\n");
            }
            auditLogArea.setText(sb.toString());
            auditLogArea.setCaretPosition(sb.length());
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
