package com.univibe.ui;

import com.univibe.model.Event;
import com.univibe.model.Feedback;
import com.univibe.model.Student;
import com.univibe.service.EventManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Event Feedback and Star Rating Submission Dialog.
 * Demonstrates:
 *  1. Rating UI Controls and Comment handling
 *  2. Data validation
 *  3. Integration with EventManager
 */
public class FeedbackDialog extends JDialog {

    private final Student student;
    private final Event event;
    private final EventManager eventManager;

    private JComboBox<String> ratingCombo;
    private JTextArea commentArea;

    public FeedbackDialog(Frame parent, Student student, Event event, EventManager eventManager) {
        super(parent, "Rate & Review Event", true);
        this.student = student;
        this.event = event;
        this.eventManager = eventManager;
        initUI();
    }

    private void initUI() {
        setSize(440, 420);
        setLocationRelativeTo(getParent());
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.BACKGROUND);

        // Header
        JPanel header = new JPanel(new GridLayout(2, 1, 2, 2));
        header.setBackground(UITheme.PRIMARY);
        header.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel titleLbl = new JLabel("Share Your Experience", JLabel.CENTER);
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
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JLabel ratingLbl = new JLabel("Select Rating (1 to 5 Stars):");
        ratingLbl.setFont(UITheme.FONT_BOLD);
        ratingLbl.setForeground(UITheme.TEXT_DARK);
        form.add(ratingLbl);
        form.add(Box.createRigidArea(new Dimension(0, 6)));

        String[] ratings = {
                "★★★★★ (5 Stars - Outstanding)",
                "★★★★☆ (4 Stars - Very Good)",
                "★★★☆☆ (3 Stars - Good)",
                "★★☆☆☆ (2 Stars - Fair)",
                "★☆☆☆☆ (1 Star - Poor)"
        };
        ratingCombo = new JComboBox<>(ratings);
        ratingCombo.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 14));
        form.add(ratingCombo);

        form.add(Box.createRigidArea(new Dimension(0, 16)));

        JLabel commentLbl = new JLabel("Your Comments & Feedback:");
        commentLbl.setFont(UITheme.FONT_BOLD);
        commentLbl.setForeground(UITheme.TEXT_DARK);
        form.add(commentLbl);
        form.add(Box.createRigidArea(new Dimension(0, 6)));

        commentArea = new JTextArea(5, 20);
        commentArea.setFont(UITheme.FONT_REGULAR);
        commentArea.setLineWrap(true);
        commentArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(commentArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1));
        form.add(scrollPane);

        add(form, BorderLayout.CENTER);

        // Footer Actions
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        footer.setBackground(UITheme.BACKGROUND);

        JButton submitBtn = UITheme.createSuccessButton("Submit Feedback");
        submitBtn.addActionListener(e -> submitFeedbackAction());

        JButton cancelBtn = UITheme.createOutlineButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        footer.add(submitBtn);
        footer.add(cancelBtn);
        add(footer, BorderLayout.SOUTH);
    }

    private void submitFeedbackAction() {
        int selectedIndex = ratingCombo.getSelectedIndex();
        int rating = 5 - selectedIndex; // index 0 = 5 stars, index 4 = 1 star
        String comment = commentArea.getText().trim();

        if (comment.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a brief feedback comment.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Feedback fb = new Feedback();
            fb.setStudentId(student.getStudentId());
            fb.setEventId(event.getEventId());
            fb.setRating(rating);
            fb.setComment(comment);

            boolean ok = eventManager.submitFeedback(fb);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Thank you! Your feedback has been submitted.", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to record feedback.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error submitting review: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
