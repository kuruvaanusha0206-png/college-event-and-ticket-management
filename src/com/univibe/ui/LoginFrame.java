package com.univibe.ui;

import com.univibe.exception.AuthenticationException;
import com.univibe.model.Admin;
import com.univibe.model.Organizer;
import com.univibe.model.Student;
import com.univibe.model.User;
import com.univibe.service.AuthenticationService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Modern Authentication & Registration Gateway Frame.
 * Demonstrates:
 *  1. Modern Split-Panel UI with Branding & Visual Highlights
 *  2. Tabbed Form Layouts (Sign In, Student Registration, Organizer Registration)
 *  3. One-Click Demo Credentials for rapid academic evaluation
 *  4. Role-based polymorphic Dashboard routing
 */
public class LoginFrame extends JFrame {

    private final AuthenticationService authService;

    // Login Form fields
    private JTextField loginUsernameField;
    private JPasswordField loginPasswordField;

    // Student Reg fields
    private JTextField stuUserField;
    private JPasswordField stuPassField;
    private JTextField stuNameField;
    private JTextField stuEmailField;
    private JTextField stuPhoneField;
    private JTextField stuRollField;
    private JTextField stuDeptField;
    private JSpinner stuSemSpinner;

    // Organizer Reg fields
    private JTextField orgUserField;
    private JPasswordField orgPassField;
    private JTextField orgNameField;
    private JTextField orgEmailField;
    private JTextField orgPhoneField;
    private JTextField orgDeptField;
    private JTextField orgDesigField;
    private JTextField orgUnitField;

    public LoginFrame() {
        super("UniVibe - College Event and Ticket Management System");
        this.authService = new AuthenticationService();
        initUI();
    }

    private void initUI() {
        setSize(960, 640);
        setMinimumSize(new Dimension(850, 580));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Left Branding Panel
        JPanel leftBanner = createLeftBanner();
        add(leftBanner, BorderLayout.WEST);

        // Right Tabs (Sign In, Student Register, Organizer Register)
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_BOLD);
        tabs.setBackground(UITheme.BACKGROUND);

        tabs.addTab("Sign In", createSignInPanel());
        tabs.addTab("Student Registration", createStudentRegPanel());
        tabs.addTab("Organizer Registration", createOrganizerRegPanel());

        add(tabs, BorderLayout.CENTER);
    }

    private JPanel createLeftBanner() {
        JPanel banner = new JPanel();
        banner.setPreferredSize(new Dimension(360, 0));
        banner.setBackground(UITheme.PRIMARY_DARK);
        banner.setLayout(new BoxLayout(banner, BoxLayout.Y_AXIS));
        banner.setBorder(new EmptyBorder(36, 28, 36, 28));

        JLabel logoLbl = new JLabel("UNIVIBE");
        logoLbl.setFont(new Font("Segoe UI", Font.BOLD, 32));
        logoLbl.setForeground(Color.WHITE);
        logoLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        banner.add(logoLbl);

        JLabel subLogo = new JLabel("College Event & Ticketing Portal");
        subLogo.setFont(UITheme.FONT_SUBHEADER);
        subLogo.setForeground(new Color(199, 210, 254));
        subLogo.setAlignmentX(Component.LEFT_ALIGNMENT);
        banner.add(subLogo);

        banner.add(Box.createRigidArea(new Dimension(0, 30)));

        addFeatureBullet(banner, "• Separate Admin, Organizer & Student Dashboards");
        addFeatureBullet(banner, "• Procedural 2D QR Admit Passes (No APIs)");
        addFeatureBullet(banner, "• Atomic Database Booking Transactions");
        addFeatureBullet(banner, "• Multithreaded Background Report Generation");
        addFeatureBullet(banner, "• Real-Time Seat Tracking & Low-Seat Alerts");
        addFeatureBullet(banner, "• Star Ratings & Peer Feedback System");

        banner.add(Box.createVerticalGlue());

        // Demo Credentials Helper Card
        JPanel demoCard = new JPanel();
        demoCard.setLayout(new BoxLayout(demoCard, BoxLayout.Y_AXIS));
        demoCard.setBackground(new Color(67, 56, 202));
        demoCard.setBorder(new EmptyBorder(12, 12, 12, 12));
        demoCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel demoTitle = new JLabel("Quick Demo Logins (Click to Autofill):");
        demoTitle.setFont(UITheme.FONT_BOLD);
        demoTitle.setForeground(Color.WHITE);
        demoCard.add(demoTitle);
        demoCard.add(Box.createRigidArea(new Dimension(0, 8)));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        btnRow.setOpaque(false);

        JButton adminFillBtn = new JButton("Admin");
        styleQuickBtn(adminFillBtn);
        adminFillBtn.addActionListener(e -> autofill("admin", "admin123"));

        JButton orgFillBtn = new JButton("Organizer");
        styleQuickBtn(orgFillBtn);
        orgFillBtn.addActionListener(e -> autofill("org_sarah", "org123"));

        JButton stuFillBtn = new JButton("Student");
        styleQuickBtn(stuFillBtn);
        stuFillBtn.addActionListener(e -> autofill("student_alex", "student123"));

        btnRow.add(adminFillBtn);
        btnRow.add(orgFillBtn);
        btnRow.add(stuFillBtn);
        demoCard.add(btnRow);

        banner.add(demoCard);

        return banner;
    }

    private void styleQuickBtn(JButton btn) {
        btn.setFont(UITheme.FONT_SMALL);
        btn.setBackground(Color.WHITE);
        btn.setForeground(UITheme.PRIMARY_DARK);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void addFeatureBullet(JPanel panel, String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UITheme.FONT_REGULAR);
        lbl.setForeground(new Color(224, 231, 255));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lbl);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
    }

    private void autofill(String user, String pass) {
        loginUsernameField.setText(user);
        loginPasswordField.setText(pass);
    }

    private JPanel createSignInPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.BACKGROUND);

        JPanel card = UITheme.createCardPanel();
        card.setPreferredSize(new Dimension(420, 380));
        card.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 12, 8, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLbl = new JLabel("Welcome Back", JLabel.CENTER);
        titleLbl.setFont(UITheme.FONT_TITLE);
        titleLbl.setForeground(UITheme.PRIMARY);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        card.add(titleLbl, gbc);

        JLabel subLbl = new JLabel("Please enter your campus credentials to sign in.", JLabel.CENTER);
        subLbl.setFont(UITheme.FONT_SMALL);
        subLbl.setForeground(UITheme.TEXT_MUTED);
        gbc.gridy = 1;
        card.add(subLbl, gbc);

        gbc.gridwidth = 1;

        // Username
        gbc.gridx = 0;
        gbc.gridy = 2;
        card.add(new JLabel("Username:"), gbc);
        loginUsernameField = new JTextField("student_alex", 18);
        loginUsernameField.setFont(UITheme.FONT_REGULAR);
        gbc.gridx = 1;
        card.add(loginUsernameField, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy = 3;
        card.add(new JLabel("Password:"), gbc);
        loginPasswordField = new JPasswordField("student123", 18);
        loginPasswordField.setFont(UITheme.FONT_REGULAR);
        gbc.gridx = 1;
        card.add(loginPasswordField, gbc);

        // Submit Button
        JButton loginBtn = UITheme.createPrimaryButton("Sign In to Portal");
        loginBtn.addActionListener(e -> executeLogin());
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(16, 12, 8, 12);
        card.add(loginBtn, gbc);

        panel.add(card);
        return panel;
    }

    private JPanel createStudentRegPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.BACKGROUND);

        JPanel formCard = UITheme.createCardPanel();
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        stuUserField = new JTextField(16);
        addFormInput(formCard, gbc, row++, "Choose Username:", stuUserField);

        stuPassField = new JPasswordField(16);
        addFormInput(formCard, gbc, row++, "Create Password:", stuPassField);

        stuNameField = new JTextField(16);
        addFormInput(formCard, gbc, row++, "Full Name:", stuNameField);

        stuEmailField = new JTextField(16);
        addFormInput(formCard, gbc, row++, "College Email:", stuEmailField);

        stuPhoneField = new JTextField(16);
        addFormInput(formCard, gbc, row++, "Phone Number:", stuPhoneField);

        stuRollField = new JTextField(16);
        addFormInput(formCard, gbc, row++, "Roll / Registration No:", stuRollField);

        stuDeptField = new JTextField("Computer Science", 16);
        addFormInput(formCard, gbc, row++, "Academic Department:", stuDeptField);

        stuSemSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 8, 1));
        addFormInput(formCard, gbc, row++, "Current Semester:", stuSemSpinner);

        JButton regBtn = UITheme.createSuccessButton("Register as Student");
        regBtn.addActionListener(e -> executeStudentRegistration());
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(12, 10, 8, 10);
        formCard.add(regBtn, gbc);

        panel.add(new JScrollPane(formCard), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createOrganizerRegPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.BACKGROUND);

        JPanel formCard = UITheme.createCardPanel();
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        orgUserField = new JTextField(16);
        addFormInput(formCard, gbc, row++, "Choose Username:", orgUserField);

        orgPassField = new JPasswordField(16);
        addFormInput(formCard, gbc, row++, "Create Password:", orgPassField);

        orgNameField = new JTextField(16);
        addFormInput(formCard, gbc, row++, "Full Name / Title:", orgNameField);

        orgEmailField = new JTextField(16);
        addFormInput(formCard, gbc, row++, "Institutional Email:", orgEmailField);

        orgPhoneField = new JTextField(16);
        addFormInput(formCard, gbc, row++, "Phone Number:", orgPhoneField);

        orgDeptField = new JTextField("Electronics & Comm.", 16);
        addFormInput(formCard, gbc, row++, "Department:", orgDeptField);

        orgDesigField = new JTextField("Assistant Professor", 16);
        addFormInput(formCard, gbc, row++, "Designation / Role:", orgDesigField);

        orgUnitField = new JTextField("Robotics & AI Society", 16);
        addFormInput(formCard, gbc, row++, "Club / Organization Unit:", orgUnitField);

        JButton regBtn = UITheme.createSuccessButton("Register as Event Organizer");
        regBtn.addActionListener(e -> executeOrganizerRegistration());
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(12, 10, 8, 10);
        formCard.add(regBtn, gbc);

        panel.add(new JScrollPane(formCard), BorderLayout.CENTER);
        return panel;
    }

    private void addFormInput(JPanel panel, GridBagConstraints gbc, int row, String label, Component comp) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        JLabel l = new JLabel(label);
        l.setFont(UITheme.FONT_BOLD);
        l.setForeground(UITheme.TEXT_MUTED);
        panel.add(l, gbc);

        gbc.gridx = 1;
        panel.add(comp, gbc);
    }

    private void executeLogin() {
        String username = loginUsernameField.getText().trim();
        String password = new String(loginPasswordField.getPassword());

        try {
            User user = authService.login(username, password);
            dispose(); // close login window

            // Polymorphic routing based on User Role
            if (user instanceof Admin) {
                new AdminDashboard((Admin) user).setVisible(true);
            } else if (user instanceof Organizer) {
                new OrganizerDashboard((Organizer) user).setVisible(true);
            } else if (user instanceof Student) {
                new StudentDashboard((Student) user).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Unrecognized user role: " + user.getRole(), "Access Denied", JOptionPane.ERROR_MESSAGE);
            }

        } catch (AuthenticationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Authentication Failed", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Database or system error: " + ex.getMessage(), "System Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void executeStudentRegistration() {
        String user = stuUserField.getText().trim();
        String pass = new String(stuPassField.getPassword());
        String name = stuNameField.getText().trim();
        String email = stuEmailField.getText().trim();
        String phone = stuPhoneField.getText().trim();
        String roll = stuRollField.getText().trim();
        String dept = stuDeptField.getText().trim();
        int sem = (Integer) stuSemSpinner.getValue();

        try {
            boolean ok = authService.registerStudent(user, pass, name, email, phone, roll, dept, sem);
            if (ok) {
                JOptionPane.showMessageDialog(this,
                        "Registration Successful!\nYou can now log in using your new credentials.",
                        "Welcome to UniVibe",
                        JOptionPane.INFORMATION_MESSAGE);
                loginUsernameField.setText(user);
                loginPasswordField.setText(pass);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Registration Failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void executeOrganizerRegistration() {
        String user = orgUserField.getText().trim();
        String pass = new String(orgPassField.getPassword());
        String name = orgNameField.getText().trim();
        String email = orgEmailField.getText().trim();
        String phone = orgPhoneField.getText().trim();
        String dept = orgDeptField.getText().trim();
        String desig = orgDesigField.getText().trim();
        String unit = orgUnitField.getText().trim();

        try {
            boolean ok = authService.registerOrganizer(user, pass, name, email, phone, dept, desig, unit);
            if (ok) {
                JOptionPane.showMessageDialog(this,
                        "Organizer Account Created!\nYou can now log in and publish college events.",
                        "Welcome to UniVibe",
                        JOptionPane.INFORMATION_MESSAGE);
                loginUsernameField.setText(user);
                loginPasswordField.setText(pass);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Registration Failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
