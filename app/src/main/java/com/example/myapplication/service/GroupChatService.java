package com.example.myapplication.service;

import android.net.Uri;

public interface GroupChatService {
    void sendMessage(String message);
    void sendMediaMessage(Uri fileUri, int requestCode);
    void selectImage();
    void selectFile();
    void initializeChat();
}
