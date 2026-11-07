package com.scc.model;

public record ActivityLog(
    int id,
    int userId,
    String username,
    String action,
    String timestamp
){
    @Override
    public String toString(){
        return String.format("[%s] User '%s' (ID: %d) -> Action: %s", timestamp, username, userId, action);

    }
}
