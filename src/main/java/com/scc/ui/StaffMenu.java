package com.scc.ui;

import com.scc.dao.*;
import com.scc.model.User;

import java.time.LocalDate;
import java.util.Scanner;

public class StaffMenu {

    private final UserDao userDao;
    private final ActivityLogDao logDao;
    private final Scanner scanner;

    public StaffMenu(UserDao userDao, ActivityLogDao logDao, Scanner scanner) {
        this.userDao = userDao;
        this.logDao = logDao;
        this.scanner = scanner;
    }

    public void display(User staff){
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n==============================================");
            System.out.println("             BILLING STAFF DASHBOARD          ");
            System.out.println("==============================================");
            System.out.println("1. Record Utility Meter Readings & Generate Bill");
            System.out.println("2. Record Tenant Payment");
            System.out.println("3. Apply Late Penalty to Unpaid Bill");
            System.out.println("4. Update Payment Status");
            System.out.println("5. View Property Utility Readings");
            System.out.println("6. View Generated Bills");
            System.out.println("7. View All Active & Historical Leases");
            System.out.println("8. Payment Receipts / Payment History ");
            System.out.println("9. Logout");
            System.out.print("Choose an option (1-9): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> recordUtilityReading(staff);
                case "2" -> recordPayment(staff);
                case "3" -> applyLatePenaltyManual(staff);
                case "4" -> updatePaymentStatusManual(staff);
                case "5" -> viewMeterReadingTable();
                case "6" -> viewBillsTable();
                case "7" -> viewLeaseTable();
                case "8" -> viewReceipts();
                case "9" -> {

                    logDao.logActivity(staff.id(), staff.username(), "USER_LOGOUT");
                    loggedIn = false;
                    System.out.println(" -> Logging out from Billing Staff Dashboard...");
                }
                default -> System.out.println(" -> [ERROR] Invalid Option.");
            }
        }
    }

    private void recordUtilityReading(User staff) {
        System.out.println("\n----- RECORD UTILITY READINGS & GENERATE BILL -----");
        userDao.displayLeasesTable();
        int leaseId = readIntInput("Enter Lease ID (or 0 to cancel): ");
        if (leaseId == 0) return;

        double prevElec = readDoubleInput("Enter Previous Electricity Reading (kWh): ");
        double currElec = readDoubleInput("Enter Current Electricity Reading (kWh): ");
        boolean isElecRollover = false;

        if (currElec < prevElec) {
            System.out.println(" -> [NOTICE] Current electricity reading is lower than previous reading.");
            System.out.print("Is this a meter rollover (e.g. 9999 -> 0000)? (Y/N): ");
            String ans = scanner.nextLine().trim();
            if (ans.equalsIgnoreCase("Y")) {
                isElecRollover = true;
            } else {
                System.out.println(" -> Reading cancelled due to lower current reading. Please re-check meter.");
                return;
            }
        }

        double prevWater = readDoubleInput("Enter Previous Water Reading (m3): ");
        double currWater = readDoubleInput("Enter Current Water Reading (m3): ");
        boolean isWaterRollover = false;

        if (currWater < prevWater) {
            System.out.println(" -> [NOTICE] Current water reading is lower than previous reading.");
            System.out.print("Is this a meter rollover (e.g. 9999 -> 0000)? (Y/N): ");
            String ans = scanner.nextLine().trim();
            if (ans.equalsIgnoreCase("Y")) {
                isWaterRollover = true;
            } else {
                System.out.println(" -> Reading cancelled due to lower current reading. Please re-check meter.");
                return;
            }
        }

        System.out.print("Enter Payment Due Date (YYYY-MM-DD): ");
        String dueDate = scanner.nextLine().trim();

        userDao.recordUtilityReading(staff.id(), staff.username(), leaseId, prevElec, currElec, prevWater, currWater, dueDate, isElecRollover, isWaterRollover);
    }

    private void recordPayment(User staff) {
        System.out.println("\n----- RECORD TENANT PAYMENT -----");
        userDao.displayBillsTable();
        int billId = readIntInput("Enter Bill ID (or 0 to cancel): ");
        if (billId == 0) return;

        double amount = readDoubleInput("Enter Payment Amount (PHP): ");
        String paymentDate = LocalDate.now().toString();

        userDao.recordPayment(staff.id(), staff.username(), billId, amount, paymentDate);
    }

    private void applyLatePenaltyManual(User staff) {
        System.out.println("\n----- APPLY LATE PENALTY TO UNPAID BILL -----");
        userDao.displayBillsTable();
        int billId = readIntInput("Enter Bill ID (or 0 to cancel): ");
        if (billId == 0) return;

        double penaltyAmount = readDoubleInput("Enter Late Penalty Charge (PHP): ");
        userDao.applyLatePenalty(staff.id(), staff.username(), billId, penaltyAmount);
    }
    private void updatePaymentStatusManual(User staff) {
        System.out.println("\n----- UPDATE PAYMENT STATUS -----");
        userDao.displayBillsTable();
        int billId = readIntInput("Enter Bill ID to update (or 0 to cancel): ");
        if (billId == 0) return;

        System.out.print("Enter New Payment Status (PAID / PARTIALLY PAID / UNPAID): ");
        String newStatus = scanner.nextLine().trim();

        userDao.updatePaymentStatus(staff.id(), staff.username(), billId, newStatus);
    }

    private void viewMeterReadingTable(){
        userDao.displayBillingReadingsTable();
    }
    private void viewBillsTable(){
        userDao.displayBillsTable();
    }

    private void viewLeaseTable(){
        userDao.displayLeasesTable();
    }
    private void viewReceipts() {
        userDao.displayLeasesTable();

        System.out.print("\nEnter Lease ID to print receipt bill (or 0 to cancel): ");
        int leaseId = Integer.parseInt(scanner.nextLine());
        if (leaseId > 0) {
        userDao.printReceiptByLeaseId(leaseId);
        }
    }


    private int readIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println(" -> [ERROR] Please enter a valid integer.");
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