package com.example.myapplication.controller;

import android.net.Uri;
import com.example.myapplication.service.IChatService;

public class ChatController {
    private final IChatService IChatService;

    public ChatController(IChatService IChatService) {
        this.IChatService = IChatService;
    }

    public void initChat() {
        IChatService.initializeChat();
    }

    public void sendText(String msg) {
        IChatService.sendMessage(msg);
    }

    public void chooseImage() {
        IChatService.selectImage();
    }

    public void chooseFile() {
        IChatService.selectFile();
    }

    public void uploadFile(Uri fileUri, int requestCode) {
        IChatService.sendMediaMessage(fileUri, requestCode);
    }
}
