package com.univibe.ui;

import com.univibe.model.Booking;
import com.univibe.model.Event;
import com.univibe.model.Payment;
import com.univibe.model.Student;
import com.univibe.service.BookingManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Ticket Booking and Checkout Dialog.
 * Demonstrates:
 *  1. Dynamic Swing UI calculations (ChangeEvent listeners on JSpinner)
 *  2. Real-time total price updating
 *  3. Payment method selection
 *  4. Integration with BookingManager and TicketReceiptDialog
 */
public class BookingDialog extends JDialog {

    private final Student student;
    private final Event event;
    private final BookingManager bookingManager;

    private JSpinner seatSpinner;
    private JComboBox<String> paymentMethodCombo;
    private JLabel totalAmountLbl;
    private boolean bookingSuccessful = false;

    public BookingDialog(Frame parent, Student student, Event event, BookingManager bookingManager) {
        super(parent, "Book Tickets - " + event.getTitle(), true);
        this.student = student;
        this.event = event;
        this.bookingManager = bookingManager;
        initUI();
    }

    private void initUI() {
        setSize(480, 520);
        setLocationRelativeTo(getParent());
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.BACKGROUND);

        // Header
        JPanel header = new JPanel(new GridLayout(2, 1, 2, 2));
        header.setBackground(UITheme.PRIMARY);
        header.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel titleLbl = new JLabel("Ticket Reservation", JLabel.CENTER);
        titleLbl.setFont(UITheme.FONT_TITLE);
        titleLbl.setForeground(Color.WHITE);

        JLabel subLbl = new JLabel(event.getTitle(), JLabel.CENTER);
        subLbl.setFont(UITheme.FONT_SUBHEADER);
        subLbl.setForeground(new Color(224, 231, 255));

        header.add(titleLbl);
        header.add(subLbl);
        add(header, BorderLayout.NORTH);

        // Center Form
        JPanel form = UITheme.createCardPanel();
        form.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        addFormRow(form, gbc, row++, "Category:", new JLabel(event.getCategory()));
        addFormRow(form, gbc, row++, "Venue:", new JLabel(event.getVenue()));
        addFormRow(form, gbc, row++, "Event Date:", new JLabel(event.getEventDate().toString() + " at " + event.getEventTime()));
        addFormRow(form, gbc, row++, "Ticket Price:", new JLabel(String.format("₹%.2f per ticket", event.getTicketPrice())));
        addFormRow(form, gbc, row++, "Available Seats:", new JLabel(String.valueOf(event.getAvailableSeats())));

        // Seat Selector
        int maxSelectable = Math.min(10, event.getAvailableSeats());
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(1, 1, Math.max(1, maxSelectable), 1);
        seatSpinner = new JSpinner(spinnerModel);
        seatSpinner.setFont(UITheme.FONT_BOLD);
        seatSpinner.addChangeListener(e -> updateTotalAmount());
        addFormRow(form, gbc, row++, "Number of Seats:", seatSpinner);

        // Payment Method
        String[] methods = {Payment.METHOD_UPI, Payment.METHOD_CAMPUS_CARD, Payment.METHOD_NET_BANKING};
        if (event.getTicketPrice() == 0.0) {
            methods = new String[]{Payment.METHOD_FREE};
        }
        paymentMethodCombo = new JComboBox<>(methods);
        paymentMethodCombo.setFont(UITheme.FONT_REGULAR);
        addFormRow(form, gbc, row++, "Payment Method:", paymentMethodCombo);

        // Total Amount Highlight
        totalAmountLbl = new JLabel(String.format("₹%.2f", event.getTicketPrice()));
        totalAmountLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        totalAmountLbl.setForeground(UITheme.PRIMARY);
        addFormRow(form, gbc, row++, "Total Amount Payable:", totalAmountLbl);

        add(form, BorderLayout.CENTER);

        // Footer Actions
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        footer.setBackground(UITheme.BACKGROUND);

        JButton confirmBtn = UITheme.createSuccessButton("Confirm & Pay");
        confirmBtn.addActionListener(e -> executeBookingAction());

        JButton cancelBtn = UITheme.createOutlineButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        footer.add(confirmBtn);
        footer.add(cancelBtn);
        add(footer, BorderLayout.SOUTH);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, Component comp) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.4;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(UITheme.FONT_BOLD);
        lbl.setForeground(UITheme.TEXT_MUTED);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.6;
        if (comp instanceof JLabel) {
            comp.setFont(UITheme.FONT_REGULAR);
            ((JLabel) comp).setForeground(UITheme.TEXT_DARK);
        }
        panel.add(comp, gbc);
    }

    private void updateTotalAmount() {
        int seats = (Integer) seatSpinner.getValue();
        double total = seats * event.getTicketPrice();
        totalAmountLbl.setText(String.format("₹%.2f", total));
    }

    private void executeBookingAction() {
        int seats = (Integer) seatSpinner.getValue();
        String method = (String) paymentMethodCombo.getSelectedItem();

        try {
            Booking booking = bookingManager.bookTickets(student, event, seats, method);
            bookingSuccessful = true;

            JOptionPane.showMessageDialog(this,
                    String.format("Booking Confirmed!\nBooking Reference: %s\nTotal Seats: %d\nAmount: ₹%.2f",
                            booking.getBookingReference(), seats, booking.getTotalAmount()),
                    "Booking Successful",
                    JOptionPane.INFORMATION_MESSAGE);

            dispose();

            // Automatically open ticket admit pass for the first ticket
            if (booking.getTickets() != null && !booking.getTickets().isEmpty()) {
                Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
                TicketReceiptDialog ticketDialog = new TicketReceiptDialog(parentFrame, booking.getTickets().get(0));
                ticketDialog.setVisible(true);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Booking Failed: " + ex.getMessage(),
                    "Reservation Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isBookingSuccessful() {
        return bookingSuccessful;
    }
}
