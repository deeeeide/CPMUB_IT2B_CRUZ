package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {
    private final UserDao userDao;
    private final Scanner scanner;

    public AdminMenu(UserDao userDao, Scanner scanner){
        this.userDao = userDao;
        this.scanner = scanner;
    }


    public void display(User admin){
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n==================================");
            System.out.print("           ADMIN DASHBOARD          ");
            System.out.println("\n==================================");
            System.out.println("1. View All Users");
            System.out.println("2. View Pending User Approvals");
            System.out.println("3. Approve User Registration");
            System.out.println("4. Add New User");
            System.out.println("5. Update User Role / Status");
            System.out.println("6. Delete User Account");
            System.out.println("7. Logout");
            System.out.print("Choose an option (1-7): ");

            String choice = scanner.nextLine().trim();

            switch (choice){
                case "1" -> viewAllUser();
                case "2" -> viewPendingUsers();
                case "3" -> approvedUser();
                default -> System.out.println(" -> [ERROR] invalid Option.");
            }


        }

    }

    private void displayUserTable(List<User> users, String title){
        System.out.println("\n==========================" +title+ "==========================");
        if(users.isEmpty()){
            System.out.println("-> No records Found");
            return;
        }

        System.out.println("+------+------------------+----------------------+---------------+");
        System.out.printf("| %-5s | %-15s | %-20s | %-10s |\n", "ID", "Username", "Role", "Status");
        System.out.println("+------+------------------+----------------------+---------------+");
            for(User u : users){
                System.out.printf("| %-5s | %-15s | %-20s | %-10s |\n",
                        u.id(), u.username(), u.roles(), u.status());
            }
        System.out.println("+------+------------------+----------------------+---------------+");
    }

    private void viewAllUser(){
        displayUserTable(userDao.getAllUser(), "ALL REGISTERED USER");

    }

    private void viewPendingUsers(){
        displayUserTable(userDao.getPendingUser(), "PENDING APPROVALS");

    }

    private void approvedUser(){
        List<User> pendingUsers = userDao.getPendingUser();
        displayUserTable(pendingUsers, "PENDING APPROVALS");

        if(pendingUsers.isEmpty()){
            return;
        }

        System.out.print("Enter User ID to approve (or 0 to cancel): ");
        int userId = scanner.nextInt();

        if (userId == 0){
            System.out.println(" -> Operation Cancelled by the users.");
        }

        boolean exits = pendingUsers.stream().anyMatch(u ->u.id() == userId);

        if(!exits){
            System.out.println(" -> [ERROR] User ID not found");
            return;
        }

        userDao.approvedUser(userId);

    }

}
