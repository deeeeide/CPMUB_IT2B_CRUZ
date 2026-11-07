package com.scc.dao;

import java.sql.Connection;
import java.sql.Statement;

public class Databaseinitializer {

    public static void initialize() {
        String createUsers = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE,
                password TEXT NOT NULL,
                roles TEXT NOT NULL,
                status TEXT NOT NULL DEFAULT 'PENDING'
            );
            """;

        String createActivityLogs = """
            CREATE TABLE IF NOT EXISTS activity_logs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                username TEXT NOT NULL,
                action TEXT NOT NULL,
                timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            );
            """;

        String createProperties = """
            CREATE TABLE IF NOT EXISTS properties (
                propertyID INTEGER PRIMARY KEY AUTOINCREMENT,
                propertyName TEXT NOT NULL,
                location TEXT NOT NULL,
                unitnumber INTEGER NOT NULL,
                unitSize REAL NOT NULL,
                availability TEXT NOT NULL DEFAULT 'AVAILABLE'
            );
            """;

        String createLeases = """
            CREATE TABLE IF NOT EXISTS leases (
                leaseID INTEGER PRIMARY KEY AUTOINCREMENT,
                tenantID INTEGER NOT NULL,
                propertyID INTEGER NOT NULL,
                userID INTEGER NOT NULL,
                startDate TEXT NOT NULL,
                endDate TEXT NOT NULL,
                rentalRate REAL NOT NULL,
                leaseStatus TEXT NOT NULL DEFAULT 'ACTIVE',
                FOREIGN KEY (propertyID) REFERENCES properties(propertyID) ON DELETE CASCADE,
                FOREIGN KEY (userID) REFERENCES users(id) ON DELETE CASCADE
            );
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createUsers);
            stmt.execute(createActivityLogs);
            stmt.execute(createProperties);
            stmt.execute(createLeases);

            new UserDao();
            System.out.println(" -> Database tables verified.");

        } catch (Exception e) {
            System.err.println("ERROR initializing database tables: " + e.getMessage());
        }
    }
}