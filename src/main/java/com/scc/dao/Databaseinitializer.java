package com.scc.dao;

public class Databaseinitializer {
    public static void initialize() {
        new UserDao();
        System.out.println("-> Database tables verified. ");

    }

}
