package com.univibe.ui;

import com.univibe.model.Event;
import com.univibe.model.EventCategory;
import com.univibe.model.Organizer;
import com.univibe.service.EventManager;
import com.univibe.util.ValidationHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Event Creation Form Dialog.
 * Demonstrates:
 *  1. Swing Form Controls (JTextField, JTextArea, JComboBox, JSpinner)
 *  2. Input Validation and Exception handling
 *  3. Integration with EventManager
 */
public class CreateEventDialog extends JDialog {

    private final Organizer organizer;
    private final EventManager eventManager;
    private boolean eventCreated = false;

    private JTextField titleField;
    private JComboBox<String> categoryCombo;
    private JTextArea descArea;
    private JTextField venueField;
    private JTextField dateField;
    private JTextField timeField;
    private JTextField priceField;
    private JSpinner seatsSpinner;

    public CreateEventDialog(Frame parent, Organizer organizer, EventManager eventManager) {
        super(parent, "Create New College Event", true);
        this.organizer = organizer;
        this.eventManager = eventManager;
        initUI();
    }

    private void initUI() {
        setSize(520, 640);
        setLocationRelativeTo(getParent());
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.BACKGROUND);

        // Header
        JPanel header = new JPanel(new GridLayout(2, 1, 2, 2));
        header.setBackground(UITheme.PRIMARY);
        header.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel titleLbl = new JLabel("EVENT CREATION STUDIO", JLabel.CENTER);
        titleLbl.setFont(UITheme.FONT_TITLE);
        titleLbl.setForeground(Color.WHITE);

        JLabel subLbl = new JLabel("Submit college event for admin review and ticketing", JLabel.CENTER);
        subLbl.setFont(UITheme.FONT_SUBHEADER);
        subLbl.setForeground(new Color(224, 231, 255));

        header.add(titleLbl);
        header.add(subLbl);
        add(header, BorderLayout.NORTH);

        // Form Card
        JPanel form = UITheme.createCardPanel();
        form.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        titleField = new JTextField(20);
        addFormRow(form, gbc, row++, "Event Title:", titleField);

        categoryCombo = new JComboBox<>(EventCategory.getAllDisplayNames());
        addFormRow(form, gbc, row++, "Category:", categoryCombo);

        venueField = new JTextField(20);
        addFormRow(form, gbc, row++, "Venue / Hall:", venueField);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        dateField = new JTextField(sdf.format(new Date(System.currentTimeMillis() + 86400000L * 7)), 12);
        addFormRow(form, gbc, row++, "Date (YYYY-MM-DD):", dateField);

        timeField = new JTextField("10:00 AM", 12);
        addFormRow(form, gbc, row++, "Time:", timeField);

        priceField = new JTextField("0.00", 12);
        addFormRow(form, gbc, row++, "Ticket Price (₹):", priceField);

        seatsSpinner = new JSpinner(new SpinnerNumberModel(100, 10, 5000, 10));
        addFormRow(form, gbc, row++, "Total Capacity:", seatsSpinner);

        descArea = new JTextArea(4, 20);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descArea);
        addFormRow(form, gbc, row++, "Description:", descScroll);

        add(new JScrollPane(form), BorderLayout.CENTER);

        // Footer Actions
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        footer.setBackground(UITheme.BACKGROUND);

        JButton saveBtn = UITheme.createSuccessButton("Submit Event");
        saveBtn.addActionListener(e -> submitAction());

        JButton cancelBtn = UITheme.createOutlineButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        footer.add(saveBtn);
        footer.add(cancelBtn);
        add(footer, BorderLayout.SOUTH);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, Component comp) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.35;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(UITheme.FONT_BOLD);
        lbl.setForeground(UITheme.TEXT_MUTED);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        panel.add(comp, gbc);
    }

    private void submitAction() {
        String title = titleField.getText().trim();
        String category = (String) categoryCombo.getSelectedItem();
        String venue = venueField.getText().trim();
        String dateStr = dateField.getText().trim();
        String timeStr = timeField.getText().trim();
        String priceStr = priceField.getText().trim();
        String desc = descArea.getText().trim();
        int seats = (Integer) seatsSpinner.getValue();

        if (!ValidationHelper.isNotEmpty(title)) {
            JOptionPane.showMessageDialog(this, "Event title is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!ValidationHelper.isNotEmpty(venue)) {
            JOptionPane.showMessageDialog(this, "Venue is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
            if (price < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid ticket price (>= 0).", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Date parsedDate;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            parsedDate = sdf.parse(dateStr);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid date in YYYY-MM-DD format.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Event event = new Event();
            event.setTitle(title);
            event.setDescription(desc.isEmpty() ? "Official College Event" : desc);
            event.setCategory(category);
            event.setVenue(venue);
            event.setEventDate(parsedDate);
            event.setEventTime(timeStr);
            event.setTicketPrice(price);
            event.setTotalSeats(seats);
            event.setAvailableSeats(seats);
            event.setOrganizerId(organizer.getOrganizerId());
            event.setStatus(Event.STATUS_APPROVED); // Auto-approve or pending

            boolean ok = eventManager.submitNewEvent(event);
            if (ok) {
                eventCreated = true;
                JOptionPane.showMessageDialog(this,
                        "Event created successfully!\nIt is now live on the student portal.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Could not save event to database.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error creating event: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isEventCreated() {
        return eventCreated;
    }
}
