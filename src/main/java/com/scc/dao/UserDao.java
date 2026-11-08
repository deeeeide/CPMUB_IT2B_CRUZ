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
                System.out.println(" -> User ID:"+userId+" approved successfully!");
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
                System.out.println(" -> User ID:"+userId+" updated successfully!");
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
                System.out.println(" -> [SUCCESS] User '"+username+"' created successfully with PENDING status.");
            } else {
                System.out.println(" -> [ERROR] Failed to create user.");
            }

        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                System.out.println(" -> [ERROR] Username '"+username+"' already exists. Please choose a different username.");
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
    // Manager Menu

    public void addProperty(int userId, String username, String propertyName, int unitNum, String location, double unitSize, String availability) {
        String sql = "INSERT INTO properties (propertyName, location, unitnumber, unitSize, availability) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, propertyName);
            pstmt.setString(2, location);
            pstmt.setInt(3, unitNum);
            pstmt.setDouble(4, unitSize);
            pstmt.setString(5, availability);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println(" -> [SUCCESS] Property record added successfully.");

                String logDesc = "User " + username + " (ID: " + userId + ") Added new property record " + propertyName + " (Unit #" + unitNum + ")";
                logDao.logActivity(userId, username, logDesc);
            }

        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                System.err.println(" -> [ERROR] Unit #" + unitNum + " already exists for property '" + propertyName + "'.");
            } else {
                System.err.println(" -> [DATABASE ERROR] Failed to add property: " + e.getMessage());
            }
        }
    }
    public void createLease(int userId, String username, int leaseId, int tenantId, int propertyId, String startDate, String endDate, double rentalRate) {
        String checkTenantSql = "SELECT tenantID FROM tenants WHERE tenantID = ?";
        String checkPropSql = "SELECT propertyID FROM properties WHERE propertyID = ?";
        String insertSql = "INSERT INTO leases (leaseID, tenantID, propertyID, userID, startDate, endDate, rentalRate, leaseStatus) VALUES (?, ?, ?, ?, ?, ?, ?, 'ACTIVE')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmtTenant = conn.prepareStatement(checkTenantSql);
             PreparedStatement pstmtProp = conn.prepareStatement(checkPropSql)) {

            pstmtTenant.setInt(1, tenantId);
            try (ResultSet rs = pstmtTenant.executeQuery()) {
                if (!rs.next()) {
                    System.out.println(" -> [ERROR] Tenant ID #" + tenantId + " does not exist!");
                    return;
                }
            }

            pstmtProp.setInt(1, propertyId);
            try (ResultSet rs = pstmtProp.executeQuery()) {
                if (!rs.next()) {
                    System.out.println(" -> [ERROR] Property ID #" + propertyId + " does not exist!");
                    return;
                }
            }

            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setInt(1, leaseId);
                pstmt.setInt(2, tenantId);
                pstmt.setInt(3, propertyId);
                pstmt.setInt(4, userId);
                pstmt.setString(5, startDate);
                pstmt.setString(6, endDate);
                pstmt.setDouble(7, rentalRate);

                int rows = pstmt.executeUpdate();
                if (rows > 0) {
                    System.out.println(" -> [SUCCESS] Lease contract created successfully.");

                    String logDesc = username + " (ID #" + userId + ") Created/Updated lease #" + leaseId + " for Tenant ID #" + tenantId;
                    logDao.logActivity(userId, username, logDesc);
                }
            }

        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                System.err.println(" -> [ERROR] Lease ID #" + leaseId + " already exists!");
            } else {
                System.err.println(" -> [DATABASE ERROR] Failed to create lease: " + e.getMessage());
            }
        }
    }
    public void addTenant(int userId, String username, String tenantName, String contactNum, String email, String businessName) {
        String sql = "INSERT INTO tenants (tenantName, contactNum, email, businessName) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, tenantName);
            pstmt.setString(2, contactNum);
            pstmt.setString(3, email);
            pstmt.setString(4, businessName);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int tenantId = rs.getInt(1);
                        System.out.println(" -> [SUCCESS] Tenant record created successfully.");

                        String logDesc =  username+" (ID #"+userId+") Added/Updated tenant record for "+tenantName+" (ID:"+tenantId+")";
                        logDao.logActivity(userId, username, logDesc);
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println(" -> [DATABASE ERROR] Failed to add tenant: " + e.getMessage());
        }
    }

    public void deactivateLease(int userId, String username, int leaseId) {
        String sql = "UPDATE leases SET leaseStatus = 'DEACTIVATED' WHERE leaseID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, leaseId);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println(" -> [SUCCESS] Lease record deactivated.");

                String logDesc = username+ " (ID #"+userId+") deactivated expired lease record #"+leaseId;
                logDao.logActivity(userId, username, logDesc);
            }

        } catch (SQLException e) {
            System.err.println(" -> [DATABASE ERROR] Failed to deactivate lease: " + e.getMessage());
        }
    }

    //Staff menu
    public double calculateConsumption(double prev, double curr, boolean isRollover, double maxLimit) {
        if (curr >= prev) {
            return curr - prev;
        } else if (isRollover) {
            return (maxLimit - prev) + curr;
        } else {
            return 0.0;
        }
    }
    public void recordUtilityReading(int userId, String username, int leaseId, double prevElec, double currElec, double prevWater, double currWater, String dueDate, boolean isElecRollover, boolean isWaterRollover) {
        String checkLeaseSql = "SELECT leaseID FROM leases WHERE leaseID = ?";
        String insertReadingSql = "INSERT INTO billing (leaseID, previousElectricReading, currentElectricReading, previousWaterReading, currentWaterReading) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {

            try (PreparedStatement pstmtCheck = conn.prepareStatement(checkLeaseSql)) {
                pstmtCheck.setInt(1, leaseId);
                try (ResultSet rsCheck = pstmtCheck.executeQuery()) {
                    if (!rsCheck.next()) {
                        System.out.println(" -> [ERROR] Lease ID #" + leaseId + " does not exist!");
                        return;
                    }
                }
            }
            try (PreparedStatement pstmt = conn.prepareStatement(insertReadingSql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, leaseId);
                pstmt.setDouble(2, prevElec);
                pstmt.setDouble(3, currElec);
                pstmt.setDouble(4, prevWater);
                pstmt.setDouble(5, currWater);

                int rows = pstmt.executeUpdate();
                if (rows > 0) {
                    try (ResultSet rs = pstmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            int readingId = rs.getInt(1);

                            double elecKwh = calculateConsumption(prevElec, currElec, isElecRollover, 10000.0);
                            double waterM3 = calculateConsumption(prevWater, currWater, isWaterRollover, 10000.0);

                            System.out.println(" -> [SUCCESS] Utility readings recorded.");

                            String logDesc = username + " (ID #" + userId + ") recorded utility readings for lease #" + leaseId + " - Elec:" + elecKwh + "[kWh], Water:" + waterM3 + "[m3]";
                            logDao.logActivity(userId, username, logDesc);

                            generateBillFromReading(conn, userId, username, leaseId, readingId, elecKwh, waterM3, dueDate);
                        }
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println(" -> [DATABASE ERROR] Failed to record utility readings: " + e.getMessage());
        }
    }
        private void generateBillFromReading(Connection conn, int userId, String username, int leaseId, int readingId, double elecKwh, double waterM3, String dueDate) {
            String getRentSql = "SELECT rentalRate FROM leases WHERE leaseID = ?";
            String insertBillSql = "INSERT INTO bills (leaseID, readingID, monthlyRent, utilityCharge, latePenalty, totalBill, dueDate, paymentStatus) VALUES (?, ?, ?, ?, 0.0, ?, ?, 'UNPAID')";

            try (PreparedStatement pstmtRent = conn.prepareStatement(getRentSql)) {
                pstmtRent.setInt(1, leaseId);
                try (ResultSet rs = pstmtRent.executeQuery()) {
                    if (rs.next()) {
                        double monthlyRent = rs.getDouble("rentalRate");
                        double utilityCharge = (elecKwh * 12.50) + (waterM3 * 45.00);
                        double totalBill = monthlyRent + utilityCharge;

                        try (PreparedStatement pstmtBill = conn.prepareStatement(insertBillSql)) {
                            pstmtBill.setInt(1, leaseId);
                            pstmtBill.setInt(2, readingId);
                            pstmtBill.setDouble(3, monthlyRent);
                            pstmtBill.setDouble(4, utilityCharge);
                            pstmtBill.setDouble(5, totalBill);
                            pstmtBill.setString(6, dueDate);
                            pstmtBill.executeUpdate();

                            System.out.printf(" -> [AUTOMATED BILL GENERATED] Monthly Rent: PHP %.2f | Utility Charge: PHP %.2f | TOTAL BILL: PHP %.2f (Due: %s)%n",
                                    monthlyRent, utilityCharge, totalBill, dueDate);
                        }
                    } else {
                        System.out.println(" -> [ERROR] Lease ID #" + leaseId + " not found while generating bill.");
                    }
                }
            } catch (SQLException e) {
                System.err.println(" -> [AUTOMATION ERROR] Failed to auto-generate bill statement: " + e.getMessage());
            }
        }

    public void applyLatePenalty(int userId, String username, int billId, double penaltyAmount) {
        String selectBillSql = "SELECT monthlyRent, utilityCharge, latePenalty, paymentStatus FROM bills WHERE billID = ?";
        String updatePenaltySql = "UPDATE bills SET latePenalty = ?, totalBill = ? WHERE billID = ?";

        double newTotal = 0.0;
        boolean updatedSuccessfully = false;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmtSelect = conn.prepareStatement(selectBillSql)) {

            pstmtSelect.setInt(1, billId);
            try (ResultSet rs = pstmtSelect.executeQuery()) {
                if (rs.next()) {
                    String status = rs.getString("paymentStatus");
                    if ("PAID".equalsIgnoreCase(status)) {
                        System.out.println(" -> [NOTICE] Bill #" + billId + " is already PAID. Late penalty cannot be applied.");
                        return;
                    }

                    double currentRent = rs.getDouble("monthlyRent");
                    double currentUtility = rs.getDouble("utilityCharge");
                    double existingPenalty = rs.getDouble("latePenalty");

                    double newPenaltyTotal = existingPenalty + penaltyAmount;
                    newTotal = currentRent + currentUtility + newPenaltyTotal;

                    try (PreparedStatement pstmtUpdate = conn.prepareStatement(updatePenaltySql)) {
                        pstmtUpdate.setDouble(1, newPenaltyTotal);
                        pstmtUpdate.setDouble(2, newTotal);
                        pstmtUpdate.setInt(3, billId);

                        int rows = pstmtUpdate.executeUpdate();
                        if (rows > 0) {
                            updatedSuccessfully = true;
                        }
                    }
                } else {
                    System.out.println(" -> [ERROR] Bill ID #" + billId + " does not exist!");
                    return;
                }
            }
        } catch (SQLException e) {
            System.err.println(" -> [DATABASE ERROR] Failed to apply late penalty: " + e.getMessage());
            return;
        }

        if (updatedSuccessfully) {
            System.out.printf(" -> [SUCCESS] Applied late penalty of PHP %.2f to Bill #%d. Updated Total: PHP %.2f%n",
                    penaltyAmount, billId, newTotal);

            String logDesc = username + " (ID #" + userId + ") applied late penalty of PHP" + penaltyAmount + " to Bill #" + billId;
            logDao.logActivity(userId, username, logDesc);
        }
    }
    public void recordPayment(int userId, String username, int billId, double paymentAmount, String paymentDate) {
        String selectBillSql = "SELECT totalBill, paymentStatus FROM bills WHERE billID = ?";
        String selectPreviousPaymentsSql = "SELECT SUM(paymentAmount) FROM payments WHERE billID = ?";
        String insertPaymentSql = "INSERT INTO payments (billID, paymentAmount, paymentDate, balance) VALUES (?, ?, ?, ?)";
        String updateBillStatusSql = "UPDATE bills SET paymentStatus = ? WHERE billID = ?";

        double remainingBalance = 0.0;
        String oldStatus = "UNPAID";
        String newStatus = "UNPAID";
        boolean paymentRecorded = false;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmtBill = conn.prepareStatement(selectBillSql)) {

            pstmtBill.setInt(1, billId);
            try (ResultSet rsBill = pstmtBill.executeQuery()) {
                if (rsBill.next()) {
                    double totalBill = rsBill.getDouble("totalBill");
                    oldStatus = rsBill.getString("paymentStatus");

                    double previousPaid = 0.0;
                    try (PreparedStatement pstmtPrev = conn.prepareStatement(selectPreviousPaymentsSql)) {
                        pstmtPrev.setInt(1, billId);
                        try (ResultSet rsPrev = pstmtPrev.executeQuery()) {
                            if (rsPrev.next()) {
                                previousPaid = rsPrev.getDouble(1);
                            }
                        }
                    }

                    double totalPaidSoFar = previousPaid + paymentAmount;
                    remainingBalance = Math.max(0.0, totalBill - totalPaidSoFar);

                    try (PreparedStatement pstmtPay = conn.prepareStatement(insertPaymentSql)) {
                        pstmtPay.setInt(1, billId);
                        pstmtPay.setDouble(2, paymentAmount);
                        pstmtPay.setString(3, paymentDate);
                        pstmtPay.setDouble(4, remainingBalance);
                        pstmtPay.executeUpdate();
                    }

                    if (remainingBalance <= 0) {
                        newStatus = "PAID";
                    } else {
                        newStatus = "PARTIALLY PAID";
                    }

                    try (PreparedStatement pstmtUpdate = conn.prepareStatement(updateBillStatusSql)) {
                        pstmtUpdate.setString(1, newStatus);
                        pstmtUpdate.setInt(2, billId);
                        pstmtUpdate.executeUpdate();
                    }

                    paymentRecorded = true;
                } else {
                    System.out.println(" -> [ERROR] Bill ID #" + billId + " not found.");
                    return;
                }
            }
        } catch (SQLException e) {
            System.err.println(" -> [DATABASE ERROR] Failed to record payment: " + e.getMessage());
            return;
        }

        if (paymentRecorded) {
            System.out.printf(" -> [SUCCESS] Payment of PHP %.2f recorded for Bill #%d. Status: %s | Remaining Balance: PHP %.2f%n",
                    paymentAmount, billId, newStatus, remainingBalance);

            String logDesc = username + " (ID #" + userId + ") recorded payment of PHP" + paymentAmount + " for Bill #" + billId;
            logDao.logActivity(userId, username, logDesc);

            if (!oldStatus.equalsIgnoreCase(newStatus)) {
                String statusLogDesc = username + " (ID #" + userId + ") updated Bill #" + billId + " payment status from " + oldStatus + " to " + newStatus;
                logDao.logActivity(userId, username, statusLogDesc);
            }
        }
    }
    public void updatePaymentStatus(int userId, String username, int billId, String newStatus) {
        String selectStatusSql = "SELECT paymentStatus FROM bills WHERE billID = ?";
        String updateSql = "UPDATE bills SET paymentStatus = ? WHERE billID = ?";

        String oldStatus = "";
        boolean updatedSuccessfully = false;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmtSelect = conn.prepareStatement(selectStatusSql)) {

            pstmtSelect.setInt(1, billId);
            try (ResultSet rs = pstmtSelect.executeQuery()) {
                if (rs.next()) {
                    oldStatus = rs.getString("paymentStatus");

                    try (PreparedStatement pstmtUpdate = conn.prepareStatement(updateSql)) {
                        pstmtUpdate.setString(1, newStatus.toUpperCase());
                        pstmtUpdate.setInt(2, billId);

                        int rows = pstmtUpdate.executeUpdate();
                        if (rows > 0) {
                            updatedSuccessfully = true;
                        }
                    }
                } else {
                    System.out.println(" -> [ERROR] Bill ID #" + billId + " does not exist!");
                    return;
                }
            }
        } catch (SQLException e) {
            System.err.println(" -> [DATABASE ERROR] Failed to update payment status: " + e.getMessage());
            return;
        }
        if (updatedSuccessfully) {
            System.out.println(" -> [SUCCESS] Bill #" + billId + " payment status updated from " + oldStatus + " to " + newStatus.toUpperCase() + ".");

            String logDesc = username+" (ID #"+userId+") updated Bill #" + billId + " payment status from " + oldStatus + " to " + newStatus.toUpperCase();
            logDao.logActivity(userId, username, logDesc);
        }
    }


    // Read

    public void displayPropertyTable(){
        String sql = "SELECT propertyID, propertyName, location, unitnumber, unitSize, availability FROM properties";

        try(Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)){

            System.out.println("\n=================================== COMMERCIAL PROPERTIES ===================================");
            System.out.println("+------------+--------------------+----------------------+----------+------------+---------------+");
            System.out.printf("| %-10s | %-18s | %-20s | %-8s | %-10s | %-13s |\n",
                    "PropertyID", "Name", "Location", "Unit #", "Size (sqm)", "Availability");
            System.out.println("+------------+--------------------+----------------------+----------+------------+---------------+");

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                System.out.printf("| %-10d | %-18s | %-20s | %-8d | %-10.2f | %-13s |\n",
                        rs.getInt("propertyID"), rs.getString("propertyName"), rs.getString("location"),
                        rs.getInt("unitnumber"), rs.getDouble("unitSize"), rs.getString("availability"));
            }
            if (!hasData) System.out.println("|                                 No property records found.                                 |");
            System.out.println("+------------+--------------------+----------------------+----------+------------+---------------+");

        } catch (SQLException e) {
            System.err.println(" -> [DATABASE ERROR] Failed to fetch properties: " + e.getMessage());
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
    public void displayTenantTable(){
        String sql = "SELECT tenantID, tenantName, contactNum, email, businessName FROM tenants";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n==================================== REGISTERED TENANTS ====================================");
            System.out.println("+----------+----------------------+-----------------+----------------------+--------------------+");
            System.out.printf("| %-8s | %-20s | %-15s | %-20s | %-18s |\n", "TenantID", "Tenant Name", "Contact Num", "Email", "Business Name");
            System.out.println("+----------+----------------------+-----------------+----------------------+--------------------+");

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                System.out.printf("| %-8d | %-20s | %-15s | %-20s | %-18s |\n",
                        rs.getInt("tenantID"), rs.getString("tenantName"), rs.getString("contactNum"),
                        rs.getString("email"), rs.getString("businessName"));
            }
            if (!hasData) System.out.println("|                                No tenant records found.                                   |");
            System.out.println("+----------+----------------------+-----------------+----------------------+--------------------+");

        } catch (SQLException e) {
            System.err.println(" -> [DATABASE ERROR] Failed to fetch tenants: " + e.getMessage());
        }
    }
    public void displayBillsTable() {
        String sql = "SELECT billID, leaseID, readingID, monthlyRent, utilityCharge, latePenalty, totalBill, dueDate, paymentStatus FROM bills";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n============================================ GENERATED BILLING STATEMENTS ============================================");
            System.out.println("+--------+---------+-----------+--------------+----------------+--------------+--------------+------------+----------------+");
            System.out.printf("| %-6s | %-7s | %-9s | %-12s | %-14s | %-12s | %-12s | %-10s | %-14s |\n",
                    "BillID", "LeaseID", "ReadingID", "Rent (PHP)", "Utility (PHP)", "Penalty", "Total (PHP)", "Due Date", "Status");
            System.out.println("+--------+---------+-----------+--------------+----------------+--------------+--------------+------------+----------------+");

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                System.out.printf("| %-6d | %-7d | %-9d | %-12.2f | %-14.2f | %-12.2f | %-12.2f | %-10s | %-14s |\n",
                        rs.getInt("billID"), rs.getInt("leaseID"), rs.getInt("readingID"),
                        rs.getDouble("monthlyRent"), rs.getDouble("utilityCharge"), rs.getDouble("latePenalty"),
                        rs.getDouble("totalBill"), rs.getString("dueDate"), rs.getString("paymentStatus"));
            }
            if (!hasData) System.out.println("|                                                 No billing statements found.                                        |");
            System.out.println("+--------+---------+-----------+--------------+----------------+--------------+--------------+------------+----------------+");

        } catch (SQLException e) {
            System.err.println(" -> [DATABASE ERROR] Failed to fetch bills: " + e.getMessage());
        }
    }
    public void displayBillingReadingsTable() {
        String sql = "SELECT readingID, leaseID, previousElectricReading, currentElectricReading, previousWaterReading, currentWaterReading FROM billing";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n===================================== UTILITY METER READINGS (BILLING) =====================================");
            System.out.println("+-----------+---------+--------------------+-------------------+------------------+------------------+");
            System.out.printf("| %-9s | %-7s | %-18s | %-17s | %-16s | %-16s |\n",
                    "ReadingID", "LeaseID", "Prev Elec (kWh)", "Curr Elec (kWh)", "Prev Water (m3)", "Curr Water (m3)");
            System.out.println("+-----------+---------+--------------------+-------------------+------------------+------------------+");

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                System.out.printf("| %-9d | %-7d | %-18.2f | %-17.2f | %-16.2f | %-16.2f |\n",
                        rs.getInt("readingID"),
                        rs.getInt("leaseID"),
                        rs.getDouble("previousElectricReading"),
                        rs.getDouble("currentElectricReading"),
                        rs.getDouble("previousWaterReading"),
                        rs.getDouble("currentWaterReading"));
            }
            if (!hasData) {
                System.out.println("|                                     No utility reading records found.                                     |");
            }
            System.out.println("+-----------+---------+--------------------+-------------------+------------------+------------------+");

        } catch (SQLException e) {
            System.err.println(" -> [DATABASE ERROR] Failed to fetch utility readings: " + e.getMessage());
        }
    }
    public void printReceiptByLeaseId(int leaseId) {
        String sql = "SELECT b.billID, b.leaseID, t.tenantName, p.propertyName, p.unitnumber, " +
                "b.monthlyRent, b.utilityCharge, b.latePenalty, b.totalBill, b.dueDate, b.paymentStatus, " +
                "COALESCE(SUM(pay.paymentAmount), 0.0) AS totalPaid " +
                "FROM bills b " +
                "JOIN leases l ON b.leaseID = l.leaseID " +
                "JOIN tenants t ON l.tenantID = t.tenantID " +
                "JOIN properties p ON l.propertyID = p.propertyID " +
                "LEFT JOIN payments pay ON b.billID = pay.billID " +
                "WHERE b.leaseID = ? " +
                "GROUP BY b.billID " +
                "ORDER BY b.billID DESC LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, leaseId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    double totalBill = rs.getDouble("totalBill");
                    double totalPaid = rs.getDouble("totalPaid");
                    double remainingBalance = Math.max(0.0, totalBill - totalPaid);

                    System.out.println("\n========================================================");
                    System.out.println("            COMMERCIAL PROPERTY BILLING RECEIPT         ");
                    System.out.println("========================================================");
                    System.out.printf(" Bill ID         : #%d%n", rs.getInt("billID"));
                    System.out.printf(" Lease ID        : #%d%n", rs.getInt("leaseID"));
                    System.out.printf(" Tenant Name     : %s%n", rs.getString("tenantName"));
                    System.out.printf(" Property & Unit : %s (Unit #%d)%n", rs.getString("propertyName"), rs.getInt("unitnumber"));
                    System.out.println("--------------------------------------------------------");
                    System.out.printf(" Monthly Rent    : PHP %,10.2f%n", rs.getDouble("monthlyRent"));
                    System.out.printf(" Utility Charge  : PHP %,10.2f%n", rs.getDouble("utilityCharge"));
                    System.out.printf(" Late Penalty    : PHP %,10.2f%n", rs.getDouble("latePenalty"));
                    System.out.println("--------------------------------------------------------");
                    System.out.printf(" TOTAL BILL      : PHP %,10.2f%n", totalBill);
                    System.out.printf(" Amount Paid     : PHP %,10.2f%n", totalPaid);
                    System.out.printf(" Remaining Bal   : PHP %,10.2f%n", remainingBalance);
                    System.out.printf(" Due Date        : %s%n", rs.getString("dueDate"));
                    System.out.printf(" Payment Status  : %s%n", rs.getString("paymentStatus"));
                    System.out.println("========================================================\n");
                } else {
                    System.out.println(" -> [ERROR] No billing statements found for Lease ID #" + leaseId);
                }
            }
        } catch (SQLException e) {
            System.err.println(" -> [DATABASE ERROR] Failed to print lease receipt: " + e.getMessage());
        }
    }
}
