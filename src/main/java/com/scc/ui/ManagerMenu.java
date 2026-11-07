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
            System.out.println("\n==================================");
            System.out.println("     PROPERTY MANAGER DASHBOARD   ");
            System.out.println("==================================");
            System.out.println("1. Add New Property / Unit");
            System.out.println("2. View or Create Lease Agreement");
            System.out.println("3. Logout");
            System.out.print("Choose an option (1-3): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> addPropertyManual(manager);
                case "2" -> createLeaseManual(manager);
                case "3" -> {
                    logDao.logActivity(manager.id(), manager.username(), "USER_LOGOUT");
                    loggedIn = false;
                    System.out.println(" -> Logging out from Manager Dashboard...");
                }
                default -> System.out.println(" -> [ERROR] Invalid Option.");
            }
        }
    }

    private void addPropertyManual(User manager) {
        System.out.println("\n----- ADD NEW PROPERTY -----");
        System.out.print("Enter Property Name (or 0 to cancel): ");
        String propName = scanner.nextLine().trim();
        if (propName.equals("0") || propName.equalsIgnoreCase("CANCEL")) {
            System.out.println(" -> Operation cancelled.");
            return;
        }

        System.out.print("Enter Property Location: ");
        String location = scanner.nextLine().trim();

        int unitNum = readIntInput("Enter Unit Number: ");
        double unitSize = readDoubleInput("Enter Unit Size (sqm): ");

        userDao.addProperty(manager.id(), manager.username(), propName, unitNum, location, unitSize);
    }

    private void createLeaseManual(User manager) {
        userDao.displayLeasesTable();

        System.out.println("\n----- CREATE NEW LEASE CONTRACT -----");
        int leaseId = readIntInput("Enter Lease ID (or 0 to cancel): ");
        if (leaseId == 0) {
            System.out.println(" -> Operation cancelled.");
            return;
        }

        int tenantId = readIntInput("Enter Tenant ID: ");
        int propertyId = readIntInput("Enter Property ID: ");

        System.out.print("Enter Start Date (YYYY-MM-DD): ");
        String startDate = scanner.nextLine().trim();

        System.out.print("Enter End Date (YYYY-MM-DD): ");
        String endDate = scanner.nextLine().trim();

        double rentalRate = readDoubleInput("Enter Monthly Rental Rate (PHP): ");

        userDao.createLease(manager.id(), manager.username(), leaseId, tenantId, propertyId, startDate, endDate, rentalRate);
    }

    private int readIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println(" -> [ERROR] Please enter a valid numeric integer.");
            }
        }
    }

    private double readDoubleInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.println(" -> [ERROR] Please enter a valid decimal number.");
            }
        }
    }
}