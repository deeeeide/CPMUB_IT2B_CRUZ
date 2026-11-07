package com.scc;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.Databaseinitializer;
import com.scc.dao.UserDao;
import com.scc.ui.Authentication;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Databaseinitializer.initialize();

        UserDao userDao = new UserDao();
        ActivityLogDao logDao = new ActivityLogDao();
        Scanner scanner = new Scanner(System.in);
        Authentication auth = new Authentication(userDao, logDao, scanner);
        auth.start();
        scanner.close();



    }

}
