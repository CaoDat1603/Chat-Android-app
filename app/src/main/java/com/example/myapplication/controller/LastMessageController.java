package com.example.myapplication.controller;

import com.example.myapplication.service.IOnLatestMessageCallback;
import com.example.myapplication.service.impl.GetLatestMessage;

public class LastMessageController {
    private final GetLatestMessage getLatestMessage;

    public LastMessageController(GetLatestMessage getLatestMessage) {
        this.getLatestMessage = getLatestMessage;
    }
    public void loadLatestMessage(String senderUID, String receiverUID, IOnLatestMessageCallback callback) {
        getLatestMessage.getLatestMessage(senderUID, receiverUID, callback);
    }
}
