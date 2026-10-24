package com.scc.dao;

import com.scc.model.User;

import javax.xml.crypto.Data;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UserDao {

    public UserDao() {

        try (Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement()){

            defaultAdminGenerator(conn);

        }catch(SQLException e) {
            System.err.println("Error seeding default admin: " + e.getMessage());
        }
    }
    public void defaultAdminGenerator(Connection conn){
        String checkSql = "SELECT COUNT(*) FROM users WHERE roles = 'ADMIN'";
        String insertSql = "INSERT INTO users(username, password, roles, status) VALUES('admin', 'admin1234', 'ADMIN', 'APPROVED')";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkSql)) {
            if (rs.next() && rs.getInt(1) == 0){
                stmt.executeUpdate(insertSql);
                System.out.println("-> Default Admin created");
            }
        } catch (SQLException e){
            System.err.println("Error seeding default admin: "+ e.getMessage());
        }
    }

    public boolean registerUser(String username, String password, String role){

        String sql = "INSERT INTO users(username, password, roles, status) VALUES(?, ?, ?, 'PENDING')";

        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, role.toUpperCase());
            pstmt.executeUpdate();
            System.out.println("-> Succesful Registration, Waiting for Approval!");
            return true;
        } catch (SQLException e){
            System.err.println("Failed: "+ e.getMessage());
            return false;
        }

    }

    public User login(String username, String password){

        String sql = "SELECT id, username, password, roles, status FROM users WHERE username = ? AND password =?";

        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try(ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String status = rs.getString("status");
                    if ("PENDING".equalsIgnoreCase(status)) {
                        System.out.println(" -> Account is pending, waiting for Approval");
                        return null;
                    }
                    return new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("roles"),
                            status
                    );
                } else {
                    System.out.println(" -> Invalid username or password");
                }
            }

            }catch(SQLException e) {

                System.out.println("Error during login: "+ e.getMessage());

             }
            return null;

        }

        public List<User> getAllUser(){
            List<User> usersList = new ArrayList<>();
            String sql = "SELECT id, username, password, roles, status FROM users";

            try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)){

                while (rs.next()) {
                    usersList.add(new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("roles"),
                            rs.getString("status")
                    ));
                }
            } catch (SQLException e){
                System.err.println("Error fetching all users: " + e.getMessage());
            }
            return usersList;

        }

    public List<User> getPendingUser(){
        List<User> pendingList = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE status = 'PENDING'";

            try(Connection conn = DatabaseConnection.getConnection()){
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);

                while(rs.next()){
                    pendingList.add(new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("roles"),
                            rs.getString("status")
                    ));
                }

            }catch (SQLException e){
                System.err.println("Error fetching users: "+e.getMessage());
            }
            return pendingList;

    }

    public boolean approvedUser(int userId){
        String sql = "UPDATE users SET status = 'APPROVED' WHERE id = ?";

        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            int rowsUpdated = pstmt.executeUpdate();

            if(rowsUpdated > 0){
                System.out.println(" -> User ID: "+userId+" approved successfully!");
                return true;
            }else{
                System.out.println(" -> ID not found.");
            }
        }catch (Exception e){
            System.err.println("Error approving users: "+e.getMessage());

    } return false;

    }

    }