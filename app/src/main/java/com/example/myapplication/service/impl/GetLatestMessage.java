package com.example.myapplication.service.impl;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.myapplication.service.IOnLatestMessageCallback;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class GetLatestMessage {
    public void getLatestMessage(String _senderUID, String _receiverUID, IOnLatestMessageCallback callback) {
        DatabaseReference chatRef = FirebaseDatabase.getInstance()
                .getReference("chats")
                .child(_senderUID + _receiverUID)
                .child("messages");
        Log.d("FirebasePath", "Trying path: chats/" + _senderUID + "_" + _receiverUID + "/messengers");


        chatRef.orderByChild("timeStamp").limitToLast(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot messageSnap : snapshot.getChildren()) {
                                String message = messageSnap.child("message").getValue(String.class);
                                Long timestamp = messageSnap.child("timeStamp").getValue(Long.class);
                                String type = messageSnap.child("type").getValue(String.class);

                                callback.onMessageReceived(message, timestamp != null ? timestamp : 0L, type);
                                return;
                            }
                        } else {
                            callback.onError("Không có tin nhắn.");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }
}
