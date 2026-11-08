package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.UserDao;
import com.scc.model.ActivityLog;
import com.scc.model.User;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {
    private final UserDao userDao;
    private final ActivityLogDao logDao;
    private final Scanner scanner;

    public AdminMenu(UserDao userDao, ActivityLogDao logDao, Scanner scanner) {
        this.userDao = userDao;
        this.logDao = logDao;
        this.scanner = scanner;
    }

    public void display(User admin) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n==================================");
            System.out.println("           ADMIN DASHBOARD          ");
            System.out.println("==================================");
            System.out.println("1. View All Users");
            System.out.println("2. View Pending User Approvals");
            System.out.println("3. Approve User Registration");
            System.out.println("4. Add New User");
            System.out.println("5. Update User Role / Status");
            System.out.println("6. Delete User Account");
            System.out.println("7. View System Audit Logs");
            System.out.println("8. Logout");
            System.out.print("Choose an option (1-8): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> viewAllUser();
                case "2" -> viewPendingUsers();
                case "3" -> approvedUser(admin);
                case "4" -> addUserManual(admin);
                case "5" -> updateUser(admin);
                case "6" -> deleteUser(admin);
                case "7" -> displayAuditLogMenu();
                case "8" -> {
                    logDao.logActivity(admin.id(), admin.username(), "USER_LOGOUT");
                    loggedIn = false;
                    System.out.println("-> Logging out from Admin Dashboard... ");
                }
                default -> System.out.println(" -> [ERROR] Invalid Option.");
            }
        }
    }

    private void displayUserTable(List<User> users, String title) {
        System.out.println("\n==========================" + title + "==========================");
        if (users.isEmpty()) {
            System.out.println("-> No records Found");
            return;
        }

        System.out.println("+------+------------------+----------------------+---------------+");
        System.out.printf("| %-5s | %-15s | %-20s | %-10s |\n", "ID", "Username", "Role", "Status");
        System.out.println("+------+------------------+----------------------+---------------+");
        for (User u : users) {
            System.out.printf("| %-5s | %-15s | %-20s | %-10s |\n",
                    u.id(), u.username(), u.roles(), u.status());
        }
        System.out.println("+------+------------------+----------------------+---------------+");
    }

    private void viewAllUser() {
        displayUserTable(userDao.getAllUser(), "ALL REGISTERED USERS");
    }

    private void viewPendingUsers() {
        displayUserTable(userDao.getPendingUser(), "PENDING APPROVALS");
    }

    private void approvedUser(User admin) {
        List<User> pendingUsers = userDao.getPendingUser();
        displayUserTable(pendingUsers, "PENDING APPROVALS");

        if (pendingUsers.isEmpty()) {
            return;
        }

        int userId = readIntInput("Enter User ID to approve (or 0 to cancel): ");

        if (userId == 0) {
            System.out.println(" -> Operation Cancelled by the user.");
            return;
        }

        boolean exists = pendingUsers.stream().anyMatch(u -> u.id() == userId);

        if (!exists) {
            System.out.println(" -> [ERROR] User ID not found in pending list.");
            return;
        }

        userDao.approvedUser(userId);
        String logDesc = "Admin "+admin.id()+" approved account for User ID#"+userId;
        logDao.logActivity(admin.id(), admin.username(), logDesc);
    }

    private void addUserManual(User admin) {
        System.out.println("\n----- ADD NEW USER -----");
        System.out.print("Enter Username (or 0 to cancel): ");
        String username = scanner.nextLine().trim();

        if (username.equals("0") || username.equalsIgnoreCase("CANCEL")) {
            System.out.println(" -> Operation cancelled.");
            return;
        }

        if (username.isEmpty()) {
            System.out.println(" -> [ERROR] Username cannot be empty.");
            return;
        }

        System.out.print("Enter Password: ");
        String password = scanner.nextLine().trim();
        if (password.isEmpty()) {
            System.out.println(" -> [ERROR] Password cannot be empty.");
            return;
        }

        String role = readValidRole();
        if (role == null) {
            System.out.println(" -> Operation Cancelled.");
            return;
        }

        userDao.createUserDirectly(username, password, role);
        String logDesc = "Admin #"+admin.id()+" created user account for "+username+" ("+role+")";
        logDao.logActivity(admin.id(), admin.username(), logDesc);
    }

    private void updateUser(User currentAdmin) {
        List<User> users = userDao.getAllUser();
        displayUserTable(users, "SELECT USER TO UPDATE");

        if (users.isEmpty()) {
            return;
        }

        int userId = readIntInput("Enter selected ID to update (or 0 to cancel): ");
        if (userId == 0) {
            System.out.println(" -> Operation Cancelled.");
            return;
        }

        if (userId == currentAdmin.id()) {
            System.out.println(" -> [WARNING] Modifying current active Admin is restricted.");
            return;
        }

        String newRole = readValidRole();
        if (newRole == null) {
            System.out.println(" -> Operation Cancelled.");
            return;
        }

        String newStatus = readValidStatus();
        if (newStatus == null) {
            System.out.println(" -> Operation Cancelled.");
            return;
        }

        userDao.updateUserAndStatus(userId, newRole, newStatus);
        String logDesc = "Admin "+currentAdmin.id()+" changed role of User #"+userId+" to "+newRole;
        logDao.logActivity(currentAdmin.id(), currentAdmin.username(), logDesc);
    }

    private void deleteUser(User currentAdmin) {
        List<User> users = userDao.getAllUser();
        displayUserTable(users, "SELECT USER TO DELETE");

        if (users.isEmpty()) {
            return;
        }

        int userId = readIntInput("Enter User ID to delete (or 0 to cancel): ");
        if (userId == 0) {
            System.out.println(" -> Operation Cancelled.");
            return;
        }

        if (userId == currentAdmin.id()) {
            System.out.println(" -> [ERROR] Self-deletion is restricted! You cannot delete your logged-in Admin account.");
            return;
        }

        boolean exists = users.stream().anyMatch(u -> u.id() == userId);
        if (!exists) {
            System.out.println(" -> [ERROR] User ID not found.");
            return;
        }

        System.out.print("Are you sure you want to delete User ID #"+userId+"? (YES/NO): ");
        String confirm = scanner.nextLine().trim();

        if (confirm.equalsIgnoreCase("YES") || confirm.equalsIgnoreCase("Y")) {
            userDao.deleteUser(userId);
            String logDesc = "Admin #"+currentAdmin.id()+" deleted User ID #"+userId;
            logDao.logActivity(currentAdmin.id(), currentAdmin.username(), logDesc);
            System.out.println(" -> User ID #" +userId+ " successfully deleted.");
        } else {
            System.out.println(" -> Deletion Cancelled.");
        }
    }

    private void displayAuditLogMenu() {
        while (true) {
            System.out.println("\n========== SYSTEM AUDIT LOGS ==========");
            System.out.println("1. View All System Logs");
            System.out.println("2. Filter Logs by User ID");
            System.out.println("3. Back to Admin Dashboard");
            System.out.print("Select an option (1-3): ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> renderLogTable(logDao.getAllLogs());
                case "2" -> {
                    displayUserTable(userDao.getAllUser(), "REGISTERED USERS REFERENCE");
                    int userId = readIntInput("Enter User ID to filter: ");
                    renderLogTable(logDao.getLogsByUserId(userId));
                }
                case "3" -> { return; }
                default -> System.out.println(" -> [ERROR] Invalid Option.");
            }
        }
    }
    private void renderLogTable(List<ActivityLog> logs) {
        if (logs.isEmpty()) {
            System.out.println(" -> No activity log records found.");
            return;
        }
        System.out.println("\n" + "-".repeat(160));
        System.out.printf("| %-5s | %-8s | %-15s | %-95s | %-20s |%n", "ID", "User ID", "Username", "Action", "Timestamp");
        System.out.println("-".repeat(160));

        for (ActivityLog log : logs) {
            System.out.printf("| %-5d | %-8d | %-15s | %-95s | %-20s |%n",
                    log.id(),
                    log.userId(),
                    log.username(),
                    log.action(),
                    log.timestamp());
        }
        System.out.println("-".repeat(160));
    }

    private String readValidRole() {
        while (true) {
            System.out.print("Enter Role (MANAGER/STAFF/ADMIN) [0 to cancel]: ");
            String role = scanner.nextLine().trim().toUpperCase();
            if (role.equals("0") || role.equals("CANCEL")) {
                return null;
            }

            if (role.equals("MANAGER") || role.equals("STAFF") || role.equals("ADMIN")) {
                return role;
            }
            System.out.println(" -> [ERROR] Invalid Role.");
        }
    }

    private String readValidStatus() {
        while (true) {
            System.out.print("Enter Status (APPROVED / PENDING) [0 to cancel]: ");
            String status = scanner.nextLine().trim().toUpperCase();
            if (status.equals("0") || status.equals("CANCEL")) {
                return null;
            }
            if (status.equals("APPROVED") || status.equals("PENDING")) {
                return status;
            }
            System.out.println(" -> [ERROR] Invalid status. Must be APPROVED or PENDING.");
        }
    }

    private int readIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println(" -> [ERROR] Please enter a valid numeric ID.");
            }
        }
    }
}