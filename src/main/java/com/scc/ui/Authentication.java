package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.Locale;
import java.util.Scanner;

public class Authentication {

    private final UserDao userDao;
    private final ActivityLogDao logDao;
    private final Scanner scanner;

    public Authentication(UserDao userDao, ActivityLogDao logDao, Scanner scanner){
        this.userDao = userDao;
        this.logDao = logDao;
        this.scanner = scanner;
    }

    public void start(){
        boolean running = true;

        System.out.println("\n=======================================");
        System.out.print("PROPERTY MANAGEMENT AND UTILITY BILLING ");
        System.out.println("\n=======================================");
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("3. Exit");
        System.out.print("Choose an option: ");
        String choice = scanner.nextLine().trim();

        switch (choice){
            case "1" -> handleLogin();
            case "2" -> handleRegistration();
            case "3" -> {
                running = false;
                System.out.println("\nExiting application. Goodbye!");
            }
            default -> System.out.println(" -> [ERROR] Invalid option.");
        }

    }
    private void handleLogin(){
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        User user = userDao.login(username, password);

        if(user != null){
            logDao.logActivity(user.id(), username, "USER_LOGIN" );
            System.out.println("\n -. WELCOME "+user.username()+ " | ROLE: "+user.roles()+"");


            switch (user.roles().toUpperCase()){
                case "ADMIN" -> new AdminMenu(userDao, logDao, scanner).display(user);
                case "MANAGER" -> new ManagerMenu(userDao, logDao, scanner).display(user);
                case "STAFF" -> new StaffMenu(userDao, logDao, scanner).display(user);
                default -> System.out.println("Account roles not found!");
            }
        }else{
            System.out.println(" -> [ERROR] Invalid username or password. ");
        }

    }
    private void handleRegistration() {
        System.out.print("Enter new username: ");
        String user = scanner.nextLine().trim();
        System.out.print("Enter password: ");
        String pass = scanner.nextLine().trim();

        System.out.print("Select Role [1] Admin | [2] Property Manager | [3] Billing Staff: ");
        String roleChoice = scanner.nextLine().trim();
        String role = switch (roleChoice) {
            case "1" -> "ADMIN";
            case "2" -> "MANAGER";
            case "3" -> "STAFF";
            default -> null;
        };

        if (role != null) {
            userDao.registerUser(user, pass, role);
            System.out.println(" -> Registration submitted successfully! Pending approval.");
        } else {
            System.out.println(" -> [ERROR] Invalid role selection!");
        }
    }
}
