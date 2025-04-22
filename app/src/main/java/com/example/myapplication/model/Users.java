package com.example.myapplication.model;

public class Users {
    private String userId;
    private String fullname;
    private String email;
    // **Lưu ý:** Không nên lưu mật khẩu trong cơ sở dữ liệu!
    private String lastMessage;
    private String status;
    private String PIN;

    public String getPIN() {
        return PIN;
    }

    public void setPIN(String PIN) {
        this.PIN = PIN;
    }

    // Constructor rỗng cần thiết cho Firebase
    public Users() {
    }

    // Constructor với tham số
    public Users(String userId, String fullname, String email, String status, String PIN) {
        this.userId = userId;
        this.fullname = fullname;
        this.email = email;
        this.status = status;
        this.PIN = PIN;
    }

    // Getters và Setters

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}