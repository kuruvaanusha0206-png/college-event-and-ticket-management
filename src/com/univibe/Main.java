package com.univibe;

import com.univibe.database.DatabaseInitializer;
import com.univibe.ui.LoginFrame;
import com.univibe.util.FileHandler;

import javax.swing.*;

/**
 * Main Application Entry Point for UniVibe.
 * Demonstrates:
 *  1. Application initialization lifecycle
 *  2. Swing Event Dispatch Thread (EDT) safety (SwingUtilities.invokeLater)
 *  3. System Look and Feel configuration
 *  4. Database bootstrap and health check
 *  5. Centralized Audit Logging
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("  UNIVIBE - College Event and Ticket Management System (v2.0.0)");
        System.out.println("  Core Java & Swing Academic Edition");
        System.out.println("======================================================================");

        // 1. Initialize Look and Feel
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equalsIgnoreCase(info.getName()) || "Windows".equalsIgnoreCase(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("[Main] Could not apply system look and feel: " + e.getMessage());
        }

        // 2. Initialize Database Schema & Seed Academic Demonstration Records
        System.out.println("[Main] Bootstrapping persistent storage layer...");
        DatabaseInitializer.initializeDatabase();

        // 3. Log System Startup
        FileHandler.logAuditActivity("SYSTEM_STARTUP", "HOST", "UniVibe Application launched successfully.");

        // 4. Launch GUI on Swing EDT
        SwingUtilities.invokeLater(() -> {
            try {
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
                System.out.println("[Main] GUI Gateway initialized and rendered successfully.");
            } catch (Exception e) {
                System.err.println("[Main] Critical GUI rendering failure: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
