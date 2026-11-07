package com.scc.dao;

import com.scc.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    private final ActivityLogDao logDao = new ActivityLogDao();

    public UserDao() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            defaultAdminGenerator(conn);

        } catch (SQLException e) {
            System.err.println("Error seeding default admin: " + e.getMessage());
        }
    }

    public void defaultAdminGenerator(Connection conn) {
        String checkSql = "SELECT COUNT(*) FROM users WHERE roles = 'ADMIN'";
        String insertSql = "INSERT INTO users(username, password, roles, status) VALUES('admin', 'admin1234', 'ADMIN', 'APPROVED')";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkSql)) {
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.executeUpdate(insertSql);
                System.out.println("-> Default Admin created");
            }
        } catch (SQLException e) {
            System.err.println("Error seeding default admin: " + e.getMessage());
        }
    }

    public boolean registerUser(String username, String password, String role) {
        String sql = "INSERT INTO users(username, password, roles, status) VALUES(?, ?, ?, 'PENDING')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, role.toUpperCase());
            pstmt.executeUpdate();
            System.out.println("-> Succesful Registration, Waiting for Approval!");
            return true;
        } catch (SQLException e) {
            System.err.println("Failed: " + e.getMessage());
            return false;
        }
    }

    public User login(String username, String password) {
        String sql = "SELECT id, username, password, roles, status FROM users WHERE username = ? AND password =?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String status = rs.getString("status");
                    if ("PENDING".equalsIgnoreCase(status)) {
                        System.out.println(" -> Account is pending, waiting for Approval");
                        return null;
                    }
                    return new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("roles"),
                            status
                    );
                } else {
                    System.out.println(" -> Invalid username or password");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error during login: " + e.getMessage());
        }
        return null;
    }

    public List<User> getAllUser() {
        List<User> usersList = new ArrayList<>();
        String sql = "SELECT id, username, password, roles, status FROM users";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                usersList.add(new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("roles"),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all users: " + e.getMessage());
        }
        return usersList;
    }

    public List<User> getPendingUser() {
        List<User> pendingList = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE status = 'PENDING'";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                pendingList.add(new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("roles"),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching users: " + e.getMessage());
        }
        return pendingList;
    }

    public boolean approvedUser(int userId) {
        String sql = "UPDATE users SET status = 'APPROVED' WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            int rowsUpdated = pstmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println(" -> User ID: " + userId + " approved successfully!");
                return true;
            } else {
                System.out.println(" -> ID not found.");
            }
        } catch (Exception e) {
            System.err.println("Error approving users: " + e.getMessage());
        }
        return false;
    }

    public boolean updateUserAndStatus(int userId, String newRoles, String newStatus) {
        String sql = "UPDATE users SET roles = ?, status = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newRoles.toUpperCase());
            pstmt.setString(2, newStatus.toUpperCase());
            pstmt.setInt(3, userId);

            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println(" -> User Id: " + userId + " updated successfully!");
                return true;
            } else {
                System.out.println(" -> User ID not found.");
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
        }
        return false;
    }

    public void createUserDirectly(String username, String password, String roles) {
        String sql = "INSERT INTO users (username, password, roles, status) VALUES (?, ?, ?, 'PENDING')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, roles);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println(" -> [SUCCESS] User '" + username + "' created successfully with PENDING status.");
            } else {
                System.out.println(" -> [ERROR] Failed to create user.");
            }

        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                System.out.println(" -> [ERROR] Username '" + username + "' already exists. Please choose a different username.");
            } else {
                System.out.println(" -> [DATABASE ERROR] " + e.getMessage());
            }
        }
    }

    public void deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println(" -> [SUCCESS] User record deleted from database.");
            } else {
                System.out.println(" -> [ERROR] No user found with ID: " + userId);
            }

        } catch (SQLException e) {
            System.err.println(" -> [DATABASE ERROR] Failed to delete user: " + e.getMessage());
        }
    }
    public void addProperty(int userId, String username, String propertyName, int unitNum, String location, double unitSize) {
        String sql = "INSERT INTO properties (propertyName, location, unitnumber, unitSize, availability) VALUES (?, ?, ?, ?, 'AVAILABLE')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, propertyName);
            pstmt.setString(2, location);
            pstmt.setInt(3, unitNum);
            pstmt.setDouble(4, unitSize);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println(" -> [SUCCESS] Property record added successfully.");

                // Event Trigger #3 Log Description Format
                String logDesc = "PROPERTY_ADDED: " + propertyName + " (Unit #" + unitNum + ")";
                logDao.logActivity(userId, username, logDesc);
            }

        } catch (SQLException e) {
            System.err.println(" -> [DATABASE ERROR] Failed to add property: " + e.getMessage());
        }
    }

    // Event Category 5: Lease Management
    public void createLease(int userId, String username, int leaseId, int tenantId, int propertyId, String startDate, String endDate, double rentalRate) {
        String sql = "INSERT INTO leases (leaseID, tenantID, propertyID, userID, startDate, endDate, rentalRate, leaseStatus) VALUES (?, ?, ?, ?, ?, ?, ?, 'ACTIVE')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, leaseId);
            pstmt.setInt(2, tenantId);
            pstmt.setInt(3, propertyId);
            pstmt.setInt(4, userId);
            pstmt.setString(5, startDate);
            pstmt.setString(6, endDate);
            pstmt.setDouble(7, rentalRate);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println(" -> [SUCCESS] Lease agreement created successfully.");

                String logDesc = "Lease Created" + leaseId + " for Tenant ID " + tenantId;
                logDao.logActivity(userId, username, logDesc);
            }

        } catch (SQLException e) {
            System.err.println(" -> [DATABASE ERROR] Failed to create lease: " + e.getMessage());
        }
    }
    public void displayLeasesTable() {
        String sql = "SELECT leaseID, tenantID, propertyID, userID, startDate, endDate, rentalRate, leaseStatus FROM leases";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n=================================== ACTIVE & HISTORICAL LEASES ===================================");
            System.out.println("+---------+-----------+-------------+---------+------------+------------+---------------+--------------+");
            System.out.printf("| %-7s | %-9s | %-11s | %-7s | %-10s | %-10s | %-13s | %-12s |\n",
                    "LeaseID", "TenantID", "PropertyID", "UserID", "Start Date", "End Date", "Rate (PHP)", "Status");
            System.out.println("+---------+-----------+-------------+---------+------------+------------+---------------+--------------+");

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                System.out.printf("| %-7d | %-9d | %-11d | %-7d | %-10s | %-10s | %-13.2f | %-12s |\n",
                        rs.getInt("leaseID"),
                        rs.getInt("tenantID"),
                        rs.getInt("propertyID"),
                        rs.getInt("userID"),
                        rs.getString("startDate"),
                        rs.getString("endDate"),
                        rs.getDouble("rentalRate"),
                        rs.getString("leaseStatus"));
            }

            if (!hasData) {
                System.out.println("|                                    No lease records found.                                   |");
            }
            System.out.println("+---------+-----------+-------------+---------+------------+------------+---------------+--------------+");

        } catch (SQLException e) {
            System.err.println(" -> [DATABASE ERROR] Failed to fetch leases: " + e.getMessage());
        }
    }
}
