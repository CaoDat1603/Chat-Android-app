package com.example.myapplication.model;

public class Group {
    private String groupId; // ID của nhóm
    private String groupName; // Tên của nhóm
    private String adminId; // ID của quản trị viên nhóm
    private String status;
    private long createdAt; // Thời gian tạo nhóm

    // Constructor
    public Group() {
        // Constructor rỗng cần thiết cho Firestore
    }

    public Group(String groupId, String groupName, String adminId, long createdAt) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.adminId = adminId;
        this.createdAt = createdAt;
    }

    // Getter và Setter
    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public void setGroupStatus(String status) {this.status = status;}

    public String getStatus() {
        return status;
    }
}