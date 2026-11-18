package com.univibe.ui;

import com.univibe.util.QRMatrixGenerator;

import javax.swing.*;
import java.awt.*;

/**
 * Custom Swing Component for Visual 2D QR-Style Ticket Verification.
 * Demonstrates:
 *  1. Java 2D Graphics (Graphics2D, RenderingHints, fillRoundRect, fillRect)
 *  2. Custom JComponent painting (paintComponent)
 *  3. Visual generation of 2D Matrix without third-party libraries
 */
public class QRComponent extends JComponent {

    private boolean[][] matrix;
    private String identifier;

    public QRComponent(String ticketData) {
        setTicketData(ticketData);
        setPreferredSize(new Dimension(180, 180));
        setMinimumSize(new Dimension(150, 150));
    }

    public void setTicketData(String ticketData) {
        this.identifier = (ticketData != null) ? ticketData : "UNIVIBE_PASS";
        this.matrix = QRMatrixGenerator.generateMatrix(this.identifier);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (matrix == null) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // 1. Draw rounded white background
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(0, 0, width, height, 16, 16);

        // 2. Draw border
        g2.setColor(UITheme.BORDER);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(1, 1, width - 2, height - 2, 16, 16);

        // 3. Render Matrix Cells
        int matrixSize = matrix.length;
        int padding = 16;
        int availableSize = Math.min(width, height) - (padding * 2);
        int cellSize = availableSize / matrixSize;

        int startX = (width - (cellSize * matrixSize)) / 2;
        int startY = (height - (cellSize * matrixSize)) / 2;

        g2.setColor(new Color(24, 24, 27)); // Dark charcoal QR modules

        for (int r = 0; r < matrixSize; r++) {
            for (int c = 0; c < matrixSize; c++) {
                if (matrix[r][c]) {
                    g2.fillRect(startX + (c * cellSize), startY + (r * cellSize), cellSize, cellSize);
                }
            }
        }

        g2.dispose();
    }
}
