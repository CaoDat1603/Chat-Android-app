package com.example.myapplication.controller;

import android.content.Intent;
import android.net.Uri;
import com.example.myapplication.service.GroupChatService;
import com.example.myapplication.view.GroupChatActivity;

public class GroupChatController {
    private final GroupChatService groupChatService;

    public GroupChatController(GroupChatService groupChatService) {
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
