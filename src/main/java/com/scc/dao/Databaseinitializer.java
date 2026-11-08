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

        String createTenants = """
            CREATE TABLE IF NOT EXISTS tenants (
                tenantID INTEGER PRIMARY KEY AUTOINCREMENT,
                tenantName TEXT NOT NULL,
                contactNum TEXT NOT NULL,
                email TEXT NOT NULL,
                businessName TEXT NOT NULL
            );
            """;

        String createProperties = """
                CREATE TABLE IF NOT EXISTS properties (
                  propertyID INTEGER PRIMARY KEY AUTOINCREMENT,
                  propertyName TEXT NOT NULL,
                  location TEXT NOT NULL,
                  unitnumber INTEGER NOT NULL,
                  unitSize REAL NOT NULL,
                  availability TEXT NOT NULL DEFAULT 'AVAILABLE',
                  UNIQUE (propertyName, unitnumber)
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
                FOREIGN KEY (tenantID) REFERENCES tenants(tenantID) ON DELETE CASCADE,
                FOREIGN KEY (propertyID) REFERENCES properties(propertyID) ON DELETE CASCADE,
                FOREIGN KEY (userID) REFERENCES users(id) ON DELETE CASCADE
            );
            """;

        String createBilling = """
            CREATE TABLE IF NOT EXISTS billing (
                readingID INTEGER PRIMARY KEY AUTOINCREMENT,
                leaseID INTEGER NOT NULL,
                previousElectricReading REAL NOT NULL DEFAULT 0.0,
                currentElectricReading REAL NOT NULL,
                previousWaterReading REAL NOT NULL DEFAULT 0.0,
                currentWaterReading REAL NOT NULL,
                FOREIGN KEY (leaseID) REFERENCES leases(leaseID) ON DELETE CASCADE
            );
            """;

        String createBills = """
            CREATE TABLE IF NOT EXISTS bills (
                billID INTEGER PRIMARY KEY AUTOINCREMENT,
                leaseID INTEGER NOT NULL,
                readingID INTEGER,
                monthlyRent REAL NOT NULL,
                utilityCharge REAL NOT NULL,
                latePenalty REAL NOT NULL DEFAULT 0.0,
                totalBill REAL NOT NULL,    
                dueDate TEXT NOT NULL,
                paymentStatus TEXT NOT NULL DEFAULT 'UNPAID',
                FOREIGN KEY (leaseID) REFERENCES leases(leaseID) ON DELETE CASCADE
                FOREIGN KEY (readingID) REFERENCES billing(readingID) ON DELETE SET NULL
            );
            """;

        String createPayments = """
            CREATE TABLE IF NOT EXISTS payments (
                paymentID INTEGER PRIMARY KEY AUTOINCREMENT,
                billID INTEGER NOT NULL,
                paymentAmount REAL NOT NULL,
                paymentDate TEXT NOT NULL,
                balance REAL NOT NULL,
                FOREIGN KEY (billID) REFERENCES bills(billID) ON DELETE CASCADE
            );
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createUsers);
            stmt.execute(createActivityLogs);
            stmt.execute(createTenants);
            stmt.execute(createProperties);
            stmt.execute(createLeases);
            stmt.execute(createBilling);
            stmt.execute(createBills);
            stmt.execute(createPayments);

            new UserDao();
            System.out.println(" -> Database tables verified successfully.");

        } catch (Exception e) {
            System.err.println("ERROR initializing database tables: " + e.getMessage());
        }
    }
}