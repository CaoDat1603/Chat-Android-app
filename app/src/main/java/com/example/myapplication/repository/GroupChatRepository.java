package com.example.myapplication.repository;

import com.example.myapplication.model.msgModel;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class GroupChatRepository {
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();

    public DatabaseReference getMessagesReference(String groupId) {
        return database.getReference().child("groups").child(groupId).child("messages");
    }

    public Task<Void> sendMessageToRoom(String groupId, msgModel message) {
        return getMessagesReference(groupId).push().setValue(message);
    }
}
