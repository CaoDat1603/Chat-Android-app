package com.example.myapplication.repository;

import com.example.myapplication.model.msgModel;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChatRepository {
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();

    public DatabaseReference getMessagesReference(String roomId) {
        return database.getReference().child("chats").child(roomId).child("messages");
    }

    public void sendMessageToRoom(String roomId, msgModel message) {
        getMessagesReference(roomId).push().setValue(message);
    }
}
