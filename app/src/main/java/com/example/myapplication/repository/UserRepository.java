// repository/UserRepository.java
package com.example.myapplication.repository;

import com.example.myapplication.model.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class UserRepository {
    private final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("user");

    public void getUser(String userId, ValueEventListener listener) {
        reference.child(userId).addListenerForSingleValueEvent(listener);
    }

    public Task<DataSnapshot> getUser(String userId) {
        return reference.child(userId).get();
    }

    public Task<Void> saveUser(String userId, Users user, OnCompleteListener<Void> listener) {
        return reference.child(userId).setValue(user).addOnCompleteListener(listener);
    }

    public void updateStatus(String userId, String time) {
        reference.child(userId).child("status").setValue(time);
    }

    public void getUserPIN(String userId, ValueEventListener listener) {
        reference.child(userId).child("PIN").addListenerForSingleValueEvent(listener);
    }

    public void setUserPIN(String userId, String pin, OnCompleteListener<Void> listener) {
        reference.child(userId).child("PIN").setValue(pin).addOnCompleteListener(listener);
    }

    public void getAllUsers(ValueEventListener listener) {
        reference.addValueEventListener(listener);
    }

    public void setUserName(String userId, String name, OnCompleteListener<Void> listener) {
        reference.child(userId).child("fullname").setValue(name).addOnCompleteListener(listener);
    }
}
