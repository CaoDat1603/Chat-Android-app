package com.example.myapplication.controller;

import android.net.Uri;
import com.example.myapplication.service.ChatService;

public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    public void initChat() {
        chatService.initializeChat();
    }

    public void sendText(String msg) {
        chatService.sendMessage(msg);
    }

    public void chooseImage() {
        chatService.selectImage();
    }

    public void chooseFile() {
        chatService.selectFile();
    }

    public void uploadFile(Uri fileUri, int requestCode) {
        chatService.sendMediaMessage(fileUri, requestCode);
    }
}
