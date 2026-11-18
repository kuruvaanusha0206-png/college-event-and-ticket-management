package com.univibe.ui;

import com.univibe.model.Ticket;
import com.univibe.util.FileHandler;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

/**
 * Digital Ticket Admit Pass and Printable Receipt Dialog.
 * Demonstrates:
 *  1. Swing JDialog layout & custom graphics integration
 *  2. File I/O triggers via FileHandler
 *  3. User feedback dialogs (JOptionPane)
 */
public class TicketReceiptDialog extends JDialog {

    private final Ticket ticket;

    public TicketReceiptDialog(Frame parent, Ticket ticket) {
        super(parent, "UniVibe - Official Digital Admit Pass", true);
        this.ticket = ticket;
        initUI();
    }

    private void initUI() {
        setSize(460, 620);
        setLocationRelativeTo(getParent());
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.BACKGROUND);

        // Header
        JPanel header = new JPanel(new GridLayout(2, 1, 2, 2));
        header.setBackground(UITheme.PRIMARY);
        header.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel titleLbl = new JLabel("EVENT ADMIT PASS", JLabel.CENTER);
        titleLbl.setFont(UITheme.FONT_TITLE);
        titleLbl.setForeground(Color.WHITE);

        JLabel subLbl = new JLabel("Scan at entrance • UniVibe Verification System", JLabel.CENTER);
        subLbl.setFont(UITheme.FONT_SMALL);
        subLbl.setForeground(new Color(224, 231, 255));

        header.add(titleLbl);
        header.add(subLbl);
        add(header, BorderLayout.NORTH);

        // Center Content Card
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(new EmptyBorder(20, 24, 20, 24));

        // QR Component
        JPanel qrContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        qrContainer.setOpaque(false);
        QRComponent qrComp = new QRComponent(ticket.getTicketCode() + "#" + ticket.getQrHash());
        qrContainer.add(qrComp);
        card.add(qrContainer);

        JLabel qrCodeLbl = new JLabel(ticket.getTicketCode(), JLabel.CENTER);
        qrCodeLbl.setFont(new Font("Consolas", Font.BOLD, 15));
        qrCodeLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(qrCodeLbl);

        card.add(Box.createRigidArea(new Dimension(0, 15)));

        // Details Grid
        JPanel details = new JPanel(new GridLayout(7, 2, 6, 8));
        details.setOpaque(false);

        addDetailRow(details, "Event:", ticket.getEventTitle());
        addDetailRow(details, "Attendee:", ticket.getStudentName());
        addDetailRow(details, "Roll No:", ticket.getRollNumber());
        addDetailRow(details, "Venue:", ticket.getVenue());
        addDetailRow(details, "Date & Time:", ticket.getEventDateStr());
        addDetailRow(details, "Seat Allocated:", ticket.getSeatNumber());
        addDetailRow(details, "Check-in Status:", ticket.getAttendanceStatus());

        card.add(details);
        add(new JScrollPane(card), BorderLayout.CENTER);

        // Footer Actions
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        footer.setBackground(UITheme.BACKGROUND);

        JButton saveBtn = UITheme.createSuccessButton("Save Ticket (.txt)");
        saveBtn.addActionListener(e -> saveTicketAction());

        JButton closeBtn = UITheme.createOutlineButton("Close");
        closeBtn.addActionListener(e -> dispose());

        footer.add(saveBtn);
        footer.add(closeBtn);
        add(footer, BorderLayout.SOUTH);
    }

    private void addDetailRow(JPanel panel, String label, String value) {
        JLabel l = new JLabel(label);
        l.setFont(UITheme.FONT_BOLD);
        l.setForeground(UITheme.TEXT_MUTED);

        JLabel v = new JLabel(value != null ? value : "N/A");
        v.setFont(UITheme.FONT_REGULAR);
        v.setForeground(UITheme.TEXT_DARK);

        panel.add(l);
        panel.add(v);
    }

    private void saveTicketAction() {
        try {
            File f = FileHandler.saveTicketToFile(ticket);
            JOptionPane.showMessageDialog(this,
                    "Ticket saved successfully to disk!\nLocation: " + f.getAbsolutePath(),
                    "Ticket Exported",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to save ticket file: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
