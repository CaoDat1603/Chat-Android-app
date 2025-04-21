package com.example.myapplication.controller;

import android.app.Dialog;
import android.widget.Button;

import com.example.myapplication.view.MainActivityGroup;
import com.example.myapplication.model.Group;
import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivityGroupController {

    private MainActivityGroup view;
    private FirebaseDatabase database;
    private DatabaseReference groupMembersRef;
    private DatabaseReference groupsRef;
    private FirebaseAuth auth;

    public MainActivityGroupController(MainActivityGroup view) {
        this.view = view;
        database = FirebaseDatabase.getInstance();
        groupMembersRef = database.getReference().child("group_members");
        groupsRef = database.getReference().child("groups");
        auth = FirebaseAuth.getInstance();
    }

    // Kiểm tra trạng thái đăng nhập
    public void checkUserLoginStatus() {
        if (auth.getCurrentUser() == null) {
            view.navigateToLogin();
        } else {
            loadGroupData(); // Tải dữ liệu nhóm nếu người dùng đã đăng nhập
        }
    }

    // Tải thông tin nhóm từ Firebase
    private void loadGroupData() {
        String currentUserId = auth.getCurrentUser().getUid();
        groupMembersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                ArrayList<String> groupIds = new ArrayList<>();
                for (DataSnapshot groupSnapshot : snapshot.getChildren()) {
                    String groupId = groupSnapshot.getKey();
                    if (groupId != null) {
                        for (DataSnapshot memberSnapshot : groupSnapshot.getChildren()) {
                            String userId = memberSnapshot.getKey();
                            if (currentUserId.equals(userId)) {
                                groupIds.add(groupId);
                                break;
                            }
                        }
                    }
                }
                loadGroupDetails(groupIds);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                view.showMessage("Error loading groups: " + error.getMessage());
            }
        });
    }

    // Tải chi tiết các nhóm
    private void loadGroupDetails(ArrayList<String> groupIds) {
        for (String groupId : groupIds) {
            groupsRef.child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    Group group = snapshot.getValue(Group.class);
                    if (group != null) {
                        view.addGroupToList(group);
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    view.showMessage("Error loading group details: " + error.getMessage());
                }
            });
        }
    }

    // Hiển thị dialog đăng xuất
    public void showLogoutDialog() {
        Dialog dialog = new Dialog(view, R.style.dialoge);
        dialog.setContentView(R.layout.dialog_layout);
        Button no = dialog.findViewById(R.id.nobnt);
        Button yes = dialog.findViewById(R.id.yesbnt);

        yes.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            view.navigateToLogin(); // Điều hướng về trang đăng nhập sau khi đăng xuất
        });

        no.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // Chuyển đến trang MainActivity
    public void navigateToMainActivity() {
        view.navigateToMainActivity();
    }

    // Chuyển đến trang tạo nhóm
    public void navigateToCreateGroupActivity() {
        view.navigateToCreateGroupActivity();
    }
}