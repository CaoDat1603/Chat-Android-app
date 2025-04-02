package com.example.myapplication.controller;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.myapplication.model.Users;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ManageMemberController {
    private final Context context;
    private final DatabaseReference groupRef = FirebaseDatabase.getInstance().getReference("groups");
    private final DatabaseReference groupMembersRef = FirebaseDatabase.getInstance().getReference("group_members");

    public ManageMemberController(Context context) {
        this.context = context;
    }

    public interface OnAdminIdLoadedListener {
        void onAdminIdLoaded(String adminId);
    }

    public interface OnGroupMembersLoadedListener {
        void onMembersLoaded(List<Users> members);
    }

    public void loadAdminId(String groupId, OnAdminIdLoadedListener listener) {
        groupRef.child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String adminId = snapshot.child("adminId").getValue(String.class);
                listener.onAdminIdLoaded(adminId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onAdminIdLoaded(null);
            }
        });
    }

    public void loadGroupMembers(String groupId, OnGroupMembersLoadedListener listener) {
        groupMembersRef.child(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Users> members = new ArrayList<>();
                for (DataSnapshot memberSnapshot : snapshot.getChildren()) {
                    String userId = memberSnapshot.getKey();
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("user").child(userId);
                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                            Users user = userSnapshot.getValue(Users.class);
                            if (user != null) {
                                members.add(user);
                                listener.onMembersLoaded(members);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(context, "Error loading member data", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Error loading members", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void removeUserFromGroup(String groupId, String userId, Runnable onSuccess, Runnable onFailure) {
        groupMembersRef.child(groupId).child(userId).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        onSuccess.run();
                    } else {
                        onFailure.run();
                    }
                });
    }

    public void changeGroupAdmin(String groupId, String newAdminId, Runnable onSuccess, Runnable onFailure) {
        groupRef.child(groupId).child("adminId").setValue(newAdminId)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        onSuccess.run();
                    } else {
                        onFailure.run();
                    }
                });
    }
}
