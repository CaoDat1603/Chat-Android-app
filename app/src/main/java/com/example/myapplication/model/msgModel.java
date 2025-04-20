package com.example.myapplication.model;

public class msgModel {
    String message;
    String senderid;
    long timeStamp;
    String messageType;  // Thêm trường này để phân loại tin nhắn (text, image, file)
    String fileName;     // Thêm trường để lưu tên file

    public msgModel() {
    }

    public msgModel(String message, String senderid, long timeStamp, String messageType, String fileName) {
        this.message = message;
        this.senderid = senderid;
        this.timeStamp = timeStamp;
        this.messageType = messageType;
        this.fileName = fileName;  // Khởi tạo tên file
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderid() {
        return senderid;
    }

    public void setSenderid(String senderid) {
        this.senderid = senderid;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getType() {
        return messageType;
    }

    public void setType(String messageType) {
        this.messageType = messageType;
    }

    public String getFileName() {
        return fileName;  // Phương thức để lấy tên file
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;  // Phương thức để set tên file
    }
}