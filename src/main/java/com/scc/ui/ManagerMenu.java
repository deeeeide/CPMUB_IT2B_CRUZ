package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.Scanner;

public class ManagerMenu {

    private final UserDao userDao;
    private final ActivityLogDao logDao;
    private final Scanner scanner;

    public ManagerMenu(UserDao userDao, ActivityLogDao logDao, Scanner scanner) {
        this.userDao = userDao;
        this.logDao = logDao;
        this.scanner = scanner;
    }

    public void display(User manager) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n==============================================");
            System.out.println("       PROPERTY ADMINISTRATOR / MANAGER       ");
            System.out.println("==============================================");
            System.out.println("1. Add Commercial Property / Unit");
            System.out.println("2. Add New Tenant Record");
            System.out.println("3. Create New Lease Agreement");
            System.out.println("4. View All Properties & Units");
            System.out.println("5. View All Tenant Records");
            System.out.println("6. View All Lease Agreements");
            System.out.println("7. Deactivate Expired Lease");
            System.out.println("8. Logout");
            System.out.print("Choose an option (1-8): ");

            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> addPropertyManual(manager);
                    case "2" -> addTenantManual(manager);
                    case "3" -> createLeaseManual(manager);
                    case "4" -> viewProperties();
                    case "5" -> viewTenant();
                    case "6" -> viewLeaseTable();
                    case "7" -> deactivateLeaseManual(manager);
                    case "8" -> {
                        logDao.logActivity(manager.id(), manager.username(), "USER_LOGOUT");
                        loggedIn = false;
                        System.out.println(" -> Logging out from Manager Dashboard...");
                    }
                    default -> System.out.println(" -> [ERROR] Invalid Option.");
                }
            } catch (OperationCancelledException e) {
                System.out.println(" -> Operation cancelled. Returning to Manager Dashboard...");
            }
        }
    }

    private void addPropertyManual(User manager) {
        System.out.println("\n----- ADD NEW COMMERCIAL PROPERTY / UNIT (Type 0 or 'cancel' anytime to go back) -----");
        userDao.displayPropertyTable();
        String propName = readStringInput("Enter Property Name: ");
        String location = readStringInput("Enter Property Location: ");
        int unitNum = readIntInput("Enter Unit Number: ");
        double unitSize = readDoubleInput("Enter Unit Size (sqm): ");
        String availability = readStringInput("Enter Availability (Available or Not): ").toUpperCase();

        userDao.addProperty(manager.id(), manager.username(), propName, unitNum, location, unitSize, availability);
    }

    private void addTenantManual(User manager) {
        System.out.println("\n----- ADD NEW TENANT RECORD (Type 0 or 'cancel' anytime to go back) -----");
        userDao.displayTenantTable();
        String name = readStringInput("Enter Tenant Name: ");
        String contact = readStringInput("Enter Contact Number: ");
        String email = readStringInput("Enter Email Address: ");
        String business = readStringInput("Enter Business Name: ");

        userDao.addTenant(manager.id(), manager.username(), name, contact, email, business);
    }

    private void viewProperties() {
        userDao.displayPropertyTable();
    }

    private void viewTenant() {
        userDao.displayTenantTable();
    }

    private void viewLeaseTable() {
        userDao.displayLeasesTable();
    }

    private void createLeaseManual(User manager) {
        userDao.displayLeasesTable();

        System.out.println("\n----- CREATE NEW LEASE CONTRACT (Type 0 or 'cancel' anytime to go back) -----");
        int leaseId = readIntInput("Enter Lease ID: ");
        int tenantId = readIntInput("Enter Tenant ID: ");
        int propertyId = readIntInput("Enter Property ID: ");
        String startDate = readStringInput("Enter Start Date (YYYY-MM-DD): ");
        String endDate = readStringInput("Enter End Date (YYYY-MM-DD): ");
        double rentalRate = readDoubleInput("Enter Monthly Rental Rate (PHP): ");

        userDao.createLease(manager.id(), manager.username(), leaseId, tenantId, propertyId, startDate, endDate, rentalRate);
    }

    private void deactivateLeaseManual(User manager) {
        userDao.displayLeasesTable();
        int leaseId = readIntInput("Enter Expired Lease ID to deactivate (or 0 to cancel): ");

        userDao.deactivateLease(manager.id(), manager.username(), leaseId);
    }

    private String readStringInput(String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        if (input.equals("0") || input.equalsIgnoreCase("CANCEL")) {
            throw new OperationCancelledException();
        }
        return input;
    }

    private int readIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.equals("0") || input.equalsIgnoreCase("CANCEL")) {
                throw new OperationCancelledException();
            }
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println(" -> [ERROR] Please enter a valid numeric integer (or 0 to cancel).");
            }
        }
    }

    private double readDoubleInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.equals("0") || input.equalsIgnoreCase("CANCEL")) {
                throw new OperationCancelledException();
            }
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.println(" -> [ERROR] Please enter a valid decimal number (or 0 to cancel).");
            }
        }
    }
}