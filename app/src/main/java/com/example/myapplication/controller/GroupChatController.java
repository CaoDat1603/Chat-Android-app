package com.example.myapplication.controller;

import android.net.Uri;
import com.example.myapplication.service.IGroupChatService;

public class GroupChatController {
    private final IGroupChatService groupChatService;

    public GroupChatController(IGroupChatService groupChatService) {
        this.groupChatService = groupChatService;
    }

    public void initGroupChat() {
        groupChatService.initializeChat();
    }

    public void sendText(String msg) {
        groupChatService.sendMessage(msg);
    }

    public void chooseImage() {
        groupChatService.selectImage();
    }

    public void chooseFile() {
        groupChatService.selectFile();
    }

    public void uploadFile(Uri fileUri, int requestCode) {
        groupChatService.sendMediaMessage(fileUri, requestCode);
    }
}
