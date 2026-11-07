package com.scc.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ActivityLogDao {

    public void logActivity(int userId,String username,String action){
        String sql = "INSERT INTO activity_logs(user_id, username, action) VALUES(?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setInt(1, userId);
            pstmt.setString(2, username);
            pstmt.setString(3, action);
            pstmt.executeUpdate();

        }catch (SQLException e){
            System.err.println("ERROR saving activity log: "+e.getMessage());
        }
    }
}
