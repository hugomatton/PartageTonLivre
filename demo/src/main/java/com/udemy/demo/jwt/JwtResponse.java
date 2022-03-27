package com.udemy.demo.jwt;

public class JwtResponse {

    private final int userId;
    private final String userName;

    public JwtResponse(int userId, String userName) {
        this.userId = userId;
        this.userName = userName;
    }

    public int getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    

    
}
