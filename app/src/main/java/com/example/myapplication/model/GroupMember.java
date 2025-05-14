package com.example.myapplication.model;

public class GroupMember {
    private String groupId;  // ID của nhóm
    private String userId;   // ID của người dùng
    private long joinedAt;   // Thời gian tham gia nhóm

    public GroupMember() {
    }

    public GroupMember(String groupId, String userId, long joinedAt) {
        this.groupId = groupId;    // Khởi tạo ID nhóm
        this.userId = userId;      // Khởi tạo ID người dùng
        this.joinedAt = joinedAt;  // Khởi tạo thời gian tham gia nhóm
    }

    public String getGroupId() {
        return groupId;  // Phương thức để lấy ID nhóm
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;  // Phương thức để set ID nhóm
    }

    public String getUserId() {
        return userId;  // Phương thức để lấy ID người dùng
    }

    public void setUserId(String userId) {
        this.userId = userId;  // Phương thức để set ID người dùng
    }

    public long getJoinedAt() {
        return joinedAt;  // Phương thức để lấy thời gian tham gia nhóm
    }

    public void setJoinedAt(long joinedAt) {
        this.joinedAt = joinedAt;  // Phương thức để set thời gian tham gia nhóm
    }
}