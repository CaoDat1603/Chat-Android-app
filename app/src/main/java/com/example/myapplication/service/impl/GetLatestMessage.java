package com.example.myapplication.service.impl;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.arch.core.executor.ArchTaskExecutor;

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


    public void CheckUserInGroup(String userId, String groupId, IOnLatestMessageCallback callback) {
        DatabaseReference membersRef = FirebaseDatabase.getInstance()
                .getReference("group_members")
                .child(groupId);

        Log.d("FirebasePath", "Checking path: group_members/" + groupId + "/members");

        membersRef.orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            callback.onCallback("true");
                        } else {
                            callback.onCallback("false");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("FirebaseError", error.getMessage());
                        callback.onCallback("false");
                    }
                });
    }


    public void getGroupLatestMessage(String groupId, IOnLatestMessageCallback callback){
        DatabaseReference chatRef = FirebaseDatabase.getInstance()
                .getReference("room")
                .child(groupId)
                .child("messages");
        Log.d("FirebasePath", "Trying path: chats/" + groupId + "/messengers");

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
