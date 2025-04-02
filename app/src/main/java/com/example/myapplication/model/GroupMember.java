package com.example.myapplication.model;

public class GroupMember {
    private String groupId;  // ID của nhóm
    private String userId;   // ID của người dùng
    private long joinedAt;   // Thời gian tham gia nhóm

    // Constructor mặc định
    public GroupMember() {}

    public GroupMember(String groupId, String userId, long joinedAt) {
        this.groupId = groupId;
        this.userId = userId;
        this.joinedAt = joinedAt;
    }

    // Getter và Setter
    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(long joinedAt) {
        this.joinedAt = joinedAt;
    }
}
