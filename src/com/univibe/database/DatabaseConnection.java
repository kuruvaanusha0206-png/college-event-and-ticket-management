package com.univibe.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Database Connection Manager.
 * Demonstrates:
 *  1. Singleton Pattern (getInstance)
 *  2. JDBC Connectivity (DriverManager, Connection)
 *  3. Configuration persistence via Properties file (univibe_db.properties)
 *  4. Graceful failover: Attempts MySQL 8.0+ first; if MySQL is unreachable,
 *     seamlessly initializes embedded SQLite persistence so testing never crashes.
 *  5. Transaction management helper methods (commit, rollback, setAutoCommit).
 */
public class DatabaseConnection {

    private static DatabaseConnection instance;
    private Connection connection;

    private static final String CONFIG_FILE = "univibe_db.properties";

    private String dbType = "MYSQL"; // MYSQL or SQLITE
    private String host = "localhost";
    private int port = 3306;
    private String dbName = "univibe_db";
    private String username = "root";
    private String password = "";

    private DatabaseConnection() {
        loadConfiguration();
        initializeConnection();
    }

    /**
     * Thread-safe Singleton access.
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    private void loadConfiguration() {
        Properties props = new Properties();
        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            try (FileInputStream in = new FileInputStream(file)) {
                props.load(in);
                this.host = props.getProperty("db.host", "localhost");
                this.port = Integer.parseInt(props.getProperty("db.port", "3306"));
                this.dbName = props.getProperty("db.name", "univibe_db");
                this.username = props.getProperty("db.user", "root");
                this.password = props.getProperty("db.password", "");
            } catch (Exception e) {
                System.err.println("[DatabaseConnection] Using default MySQL configuration: " + e.getMessage());
            }
        } else {
            saveDefaultConfiguration();
        }
    }

    private void saveDefaultConfiguration() {
        Properties props = new Properties();
        props.setProperty("db.host", host);
        props.setProperty("db.port", String.valueOf(port));
        props.setProperty("db.name", dbName);
        props.setProperty("db.user", username);
        props.setProperty("db.password", password);
        try (FileOutputStream out = new FileOutputStream(CONFIG_FILE)) {
            props.store(out, "UniVibe Database Configuration");
        } catch (IOException ignored) {}
    }

    private void initializeConnection() {
        // Try MySQL first
        String mysqlUrl = String.format("jdbc:mysql://%s:%d/%s?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                host, port, dbName);
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(mysqlUrl, username, password);
            dbType = "MYSQL";
            System.out.println("[DatabaseConnection] Successfully connected to MySQL at " + host + ":" + port + "/" + dbName);
            return;
        } catch (ClassNotFoundException e) {
            System.err.println("[DatabaseConnection] MySQL Driver not found on classpath: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("[DatabaseConnection] MySQL server unavailable (" + e.getMessage() + ").");
        }

        // Graceful Embedded Fallback (SQLite) for Zero-Config classroom demonstration
        try {
            Class.forName("org.sqlite.JDBC");
            String sqliteUrl = "jdbc:sqlite:univibe.db";
            connection = DriverManager.getConnection(sqliteUrl);
            dbType = "SQLITE";
            System.out.println("[DatabaseConnection] Seamlessly initialized zero-config Embedded Database (SQLite: univibe.db).");
        } catch (Exception ex) {
            System.err.println("[DatabaseConnection] CRITICAL: Could not initialize database connection: " + ex.getMessage());
        }
    }

    public synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                initializeConnection();
            }
        } catch (SQLException e) {
            initializeConnection();
        }
        return connection;
    }

    // Transaction Control Helpers
    public void beginTransaction() throws SQLException {
        Connection conn = getConnection();
        if (conn != null) {
            conn.setAutoCommit(false);
        }
    }

    public void commitTransaction() throws SQLException {
        Connection conn = getConnection();
        if (conn != null && !conn.getAutoCommit()) {
            conn.commit();
            conn.setAutoCommit(true);
        }
    }

    public void rollbackTransaction() {
        try {
            Connection conn = getConnection();
            if (conn != null && !conn.getAutoCommit()) {
                conn.rollback();
                conn.setAutoCommit(true);
                System.out.println("[DatabaseConnection] Transaction rolled back safely.");
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseConnection] Error during rollback: " + e.getMessage());
        }
    }

    public String getDatabaseType() {
        return dbType;
    }

    public boolean isMySQL() {
        return "MYSQL".equalsIgnoreCase(dbType);
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DatabaseConnection] Database connection closed.");
            }
        } catch (SQLException ignored) {}
    }
}
