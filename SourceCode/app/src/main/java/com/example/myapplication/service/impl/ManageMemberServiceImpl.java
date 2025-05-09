package com.example.myapplication.service.impl;

import android.widget.Toast;

import androidx.annotation.NonNull;
import com.example.myapplication.controller.ManageMemberController;
import com.example.myapplication.model.Users;
import com.example.myapplication.repository.GroupRepository;
import com.example.myapplication.repository.UserRepository;
import com.example.myapplication.service.IManageMemberService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ManageMemberServiceImpl implements IManageMemberService {
    private final ManageMemberController controller;
    private final GroupRepository repository;
    private final UserRepository userRepository;

    public ManageMemberServiceImpl(ManageMemberController controller) {
        this.controller = controller;
        this.repository = new GroupRepository();
        this.userRepository = new UserRepository();
    }

    @Override
    public void loadAdminId(String groupId, ManageMemberController.OnAdminIdLoadedListener listener) {
        repository.getGroup(groupId, new ValueEventListener() {
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

    @Override
    public void loadGroupMembers(String groupId, ManageMemberController.OnGroupMembersLoadedListener listener) {
        repository.getGroupMemnerRef(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Users> members = new ArrayList<>();
                for (DataSnapshot memberSnapshot : snapshot.getChildren()) {
                    String userId = memberSnapshot.getKey();
                    userRepository.getUser(userId, new ValueEventListener() {
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
                            Toast.makeText(controller.getContext(), "Error loading member data", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(controller.getContext(), "Error loading members", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void removeUserFromGroup(String groupId, String userId, Runnable onSuccess, Runnable onFailure) {
        repository.removeMember(groupId, userId, task -> {
            if (task.isSuccessful()) {
                onSuccess.run();
            } else {
                onFailure.run();
            }
        });
    }

    @Override
    public void changeGroupAdmin(String groupId, String newAdminId, Runnable onSuccess, Runnable onFailure) {
        repository.getGroupRef(groupId).child("adminId").setValue(newAdminId)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        onSuccess.run();
                    } else {
                        onFailure.run();
                    }
                });
    }
}