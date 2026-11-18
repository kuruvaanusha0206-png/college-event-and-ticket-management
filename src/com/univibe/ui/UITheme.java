package com.univibe.ui;

import com.univibe.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * Modern UI Theme, Color Palette, and Styling Utility for Java Swing.
 * Demonstrates:
 *  1. Swing Customization (Colors, Fonts, Borders)
 *  2. Factory Methods for UI Component Creation
 *  3. Table and Card Styling
 */
public final class UITheme {

    // Color Palette
    public static final Color PRIMARY = new Color(67, 56, 202);        // Indigo
    public static final Color PRIMARY_DARK = new Color(49, 46, 129);   // Deep Indigo
    public static final Color PRIMARY_LIGHT = new Color(99, 102, 241);  // Light Indigo
    public static final Color BACKGROUND = new Color(248, 250, 252);   // Slate 50
    public static final Color CARD_BG = Color.WHITE;
    public static final Color TEXT_DARK = new Color(30, 41, 59);        // Slate 800
    public static final Color TEXT_MUTED = new Color(100, 116, 139);    // Slate 500
    public static final Color BORDER = new Color(226, 232, 240);       // Slate 200
    public static final Color SUCCESS = new Color(16, 185, 129);       // Emerald
    public static final Color WARNING = new Color(245, 158, 11);       // Amber
    public static final Color DANGER = new Color(239, 68, 68);         // Rose

    // Typography
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_SUBHEADER = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);

    private UITheme() {}

    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_DARK, 1),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton createSuccessButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(SUCCESS);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(5, 150, 105), 1),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton createDangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(DANGER);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(185, 28, 28), 1),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton createOutlineButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(Color.WHITE);
        btn.setForeground(PRIMARY);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY, 1),
                BorderFactory.createEmptyBorder(7, 14, 7, 14)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        return panel;
    }

    public static JPanel createKPICard(String title, String value, Color accentColor, String badge) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER, 1),
                        BorderFactory.createEmptyBorder(12, 16, 12, 16)
                )
        ));

        JLabel titleLbl = new JLabel(title.toUpperCase());
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLbl.setForeground(TEXT_MUTED);

        JLabel valueLbl = new JLabel(value);
        valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLbl.setForeground(TEXT_DARK);

        JLabel badgeLbl = new JLabel(badge);
        badgeLbl.setFont(FONT_SMALL);
        badgeLbl.setForeground(accentColor);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLbl, BorderLayout.CENTER);
        card.add(badgeLbl, BorderLayout.SOUTH);

        return card;
    }

    public static JPanel createHeaderPanel(String title, String subtitle, User user, Runnable logoutAction) {
        JPanel header = new JPanel(new BorderLayout(15, 10));
        header.setBackground(PRIMARY_DARK);
        header.setBorder(new EmptyBorder(14, 20, 14, 20));

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 2, 2));
        titlePanel.setOpaque(false);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(FONT_TITLE);
        titleLbl.setForeground(Color.WHITE);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(FONT_SUBHEADER);
        subLbl.setForeground(new Color(199, 210, 254)); // Soft lavender

        titlePanel.add(titleLbl);
        titlePanel.add(subLbl);
        header.add(titlePanel, BorderLayout.WEST);

        if (user != null) {
            JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
            userPanel.setOpaque(false);

            JLabel userLbl = new JLabel(String.format("Signed in: %s [%s]", user.getFullName(), user.getRole()));
            userLbl.setFont(FONT_BOLD);
            userLbl.setForeground(Color.WHITE);

            JButton logoutBtn = createDangerButton("Logout");
            logoutBtn.setPreferredSize(new Dimension(85, 32));
            logoutBtn.addActionListener(e -> {
                if (logoutAction != null) logoutAction.run();
            });

            userPanel.add(userLbl);
            userPanel.add(logoutBtn);
            header.add(userPanel, BorderLayout.EAST);
        }

        return header;
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT_REGULAR);
        table.setRowHeight(30);
        table.setGridColor(BORDER);
        table.setSelectionBackground(new Color(224, 231, 255)); // Light indigo
        table.setSelectionForeground(TEXT_DARK);
        table.setShowGrid(true);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(new Color(241, 245, 249)); // Slate 100
        header.setForeground(TEXT_DARK);
        header.setPreferredSize(new Dimension(0, 34));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        // apply to first column (usually ID)
        if (table.getColumnCount() > 0) {
            table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        }
    }
}
