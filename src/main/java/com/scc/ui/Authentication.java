package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;
import java.util.Scanner;

public class Authentication {

    private final UserDao userDao;
    private final Scanner sc;

    public Authentication(UserDao userDao, Scanner sc){
        this.userDao = userDao;
        this.sc = sc;
    }

    public void start(){

        System.out.println("\n=======================================");
        System.out.print("PROPERTY MANAGEMENT AND UTILITY BILLING ");
        System.out.println("\n=======================================");
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("3. Exit");
        System.out.print("Choose an option: ");
        String choice = sc.nextLine();

        switch (choice){

            case "1":
                handleLogin();

                break;
            case "2":
                System.out.print("Enter new username: ");
                String user = sc.nextLine();
                System.out.print("Enter password: ");
                String pass = sc.nextLine();

                System.out.print("Select Role [1] Admin | [2] Property Manager: ");
                int roleChoice = sc.nextInt();
                String role;

                switch(roleChoice){
                    case 1:
                        role = "Admin";
                        break;
                    case 2:
                        role = "Property Manager";
                        break;
                    default:
                        role = null;
                }
                if(role != null){
                    userDao.registerUser(user, pass, role);

                }else{

                    System.out.println("Invalid role selection!");

                }
                break;
            case "3":
                System.out.println("\nExiting application. Goodbye!");
                return;
        }

    }
    private void handleLogin(){
        System.out.print("Enter username: ");
        String username = sc.nextLine();
        System.out.print("Enter password: ");
        String password = sc.nextLine();

        User user = userDao.login(username, password);

        if(user != null){
            System.out.println("\n -. WELCOME "+user.username()+ "| ROLE: "+user.roles()+"");
            switch (user.roles()){
                case "ADMIN" -> new AdminMenu(userDao, sc).display(user);
                default -> System.out.println("Account roles not found!");
            }

        }

    }
}
