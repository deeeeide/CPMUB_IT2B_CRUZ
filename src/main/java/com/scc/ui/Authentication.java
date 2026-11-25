package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.Scanner;

public class Authentication {

    private final UserDao userDao;
    private final ActivityLogDao logDao;
    private final Scanner scanner;

    public Authentication(UserDao userDao, ActivityLogDao logDao, Scanner scanner) {
        this.userDao = userDao;
        this.logDao = logDao;
        this.scanner = scanner;
    }

    public void start() {
        boolean running = true;

        while (running) {
            System.out.println("\n=======================================");
            System.out.println("PROPERTY MANAGEMENT AND UTILITY BILLING");
            System.out.println("=======================================");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> handleLogin();
                    case "2" -> handleRegistration();
                    case "3" -> {
                        running = false;
                        System.out.println("\nExiting application. Goodbye!");
                    }
                    default -> System.out.println(" -> [ERROR] Invalid option.");
                }
            } catch (OperationCancelledException e) {
                System.out.println(" -> Operation cancelled. Returning to main menu...");
            }
        }
    }

    private void handleLogin() {
        System.out.println("\n----- LOGIN (Type 0 or 'cancel' anytime to go back) -----");
        String username = readStringInput("Enter username: ");
        String password = readStringInput("Enter password: ");

        User user = userDao.login(username, password);

        if (user != null) {
            logDao.logActivity(user.id(), username, "USER_LOGIN");
            System.out.println("\n -> WELCOME " + user.username() + " | ROLE: " + user.roles());

            switch (user.roles().toUpperCase()) {
                case "ADMIN" -> new AdminMenu(userDao, logDao, scanner).display(user);
                case "MANAGER" -> new ManagerMenu(userDao, logDao, scanner).display(user);
                case "STAFF" -> new StaffMenu(userDao, logDao, scanner).display(user);
                default -> System.out.println(" -> [ERROR] Account role not found!");
            }
        } else {
            System.out.println(" -> [ERROR] Invalid username or password.");
        }
    }

    private void handleRegistration() {
        System.out.println("\n----- REGISTER (Type 0 or 'cancel' anytime to go back) -----");
        String user = readStringInput("Enter new username: ");
        if (user.isEmpty()) {
            System.out.println(" -> [ERROR] Username cannot be empty.");
            return;
        }

        String pass = readStringInput("Enter password: ");
        if (pass.isEmpty()) {
            System.out.println(" -> [ERROR] Password cannot be empty.");
            return;
        }

        String roleChoice = readStringInput("Select Role [1] Admin | [2] Property Manager | [3] Billing Staff: ");
        String role = switch (roleChoice) {
            case "1" -> "ADMIN";
            case "2" -> "MANAGER";
            case "3" -> "STAFF";
            default -> null;
        };

        if (role != null) {
            if (userDao.registerUser(user, pass, role)) {
                System.out.println(" -> Registration submitted successfully! Pending approval.");
            }
        } else {
            System.out.println(" -> [ERROR] Invalid role selection!");
        }
    }

    private String readStringInput(String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        if (input.equals("0") || input.equalsIgnoreCase("CANCEL")) {
            throw new OperationCancelledException();
        }
        return input;
    }
}