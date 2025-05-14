package com.example.myapplication.model;

import java.util.List;

public class GroupMsgModel extends msgModel {
    private String groupId; // ID của nhóm
    private List<String> receiverIds; // Danh sách ID của người nhận trong nhóm

    // Constructor mặc định
    public GroupMsgModel() {
        super(); // Gọi constructor của lớp cha
    }

    // Constructor có tham số
    public GroupMsgModel(String message, String senderid, long timeStamp, String messageType, String fileName, String groupId, List<String> receiverIds) {
        super(message, senderid, timeStamp, messageType, fileName); // Gọi constructor của lớp cha
        this.groupId = groupId;
        this.receiverIds = receiverIds;
    }

    // Getter và Setter cho groupId
    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    // Getter và Setter cho receiverIds
    public List<String> getReceiverIds() {
        return receiverIds;
    }

    public void setReceiverIds(List<String> receiverIds) {
        this.receiverIds = receiverIds;
    }
}