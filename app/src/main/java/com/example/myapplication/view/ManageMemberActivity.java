package com.example.myapplication.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.controller.ManageMemberController;
import com.example.myapplication.model.Users;
import com.example.myapplication.view.adapter.GroupMemberAdapter;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class ManageMemberActivity extends AppCompatActivity {
    private RecyclerView groupMemberListRecyclerView;
    private EditText searchUsers;
    private ProgressBar progressBar;
    private GroupMemberAdapter groupMemberAdapter;
    private List<Users> groupMembers = new ArrayList<>();
    private String groupId;
    private String adminId;
    private ManageMemberController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_member);

        controller = new ManageMemberController(this);

        groupId = getIntent().getStringExtra("groupId");
        groupMemberListRecyclerView = findViewById(R.id.groupmemberlist);
        searchUsers = findViewById(R.id.searchUsers);

        ImageButton turnback = findViewById(R.id.turnback);
        turnback.setOnClickListener(view -> finish());

        controller.loadAdminId(groupId, adminId -> {
            if (adminId != null) {
                this.adminId = adminId;
                setupAdapter();
                loadGroupMembers();
            } else {
                Toast.makeText(ManageMemberActivity.this, "Admin ID is null", Toast.LENGTH_SHORT).show();
            }
        });

        searchUsers.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                filterMembers(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    private void setupAdapter() {
        groupMemberAdapter = new GroupMemberAdapter(this, groupMembers, adminId, new GroupMemberAdapter.OnMemberActionListener() {
            @Override
            public void onDeleteMember(Users user) {
                if (adminId.equals(FirebaseAuth.getInstance().getUid())) {
                    controller.removeUserFromGroup(groupId, user.getUserId(),
                            () -> {
                                Toast.makeText(ManageMemberActivity.this, "User removed successfully", Toast.LENGTH_SHORT).show();
                                loadGroupMembers();
                            },
                            () -> Toast.makeText(ManageMemberActivity.this, "Error removing user", Toast.LENGTH_SHORT).show());
                } else {
                    Toast.makeText(ManageMemberActivity.this, "You are not an admin!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onChangeAdmin(Users user) {
                if (adminId.equals(FirebaseAuth.getInstance().getUid())) {
                    controller.changeGroupAdmin(groupId, user.getUserId(),
                            () -> {
                                Toast.makeText(ManageMemberActivity.this, "Admin changed successfully", Toast.LENGTH_SHORT).show();
                                reloadActivity();
                            },
                            () -> Toast.makeText(ManageMemberActivity.this, "Error changing admin", Toast.LENGTH_SHORT).show());
                } else {
                    Toast.makeText(ManageMemberActivity.this, "You are not an admin!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        groupMemberListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        groupMemberListRecyclerView.setAdapter(groupMemberAdapter);
    }

    private void loadGroupMembers() {
        controller.loadGroupMembers(groupId, members -> {
            groupMembers.clear(); // Xóa danh sách cũ
            groupMembers.addAll(members); // Lưu danh sách mới vào biến toàn cục
            groupMemberAdapter.updateMemberList(members); // Cập nhật adapter
        });
    }
    private void filterMembers(String query) {
        List<Users> filteredMembers = new ArrayList<>();

        if (query.isEmpty()) {
            filteredMembers = groupMembers;  // Hiển thị tất cả thành viên khi không có tìm kiếm
        } else {
            for (Users user : groupMembers) {
                if (user.getFullname().toLowerCase().contains(query.toLowerCase())) {
                    filteredMembers.add(user); // Lọc các thành viên theo tên
                }
            }
        }

        groupMemberAdapter.updateMemberList(filteredMembers);
    }

    private void reloadActivity() {
        Intent intent = new Intent(this, ManageMemberActivity.class);
        intent.putExtra("groupId", groupId);
        startActivity(intent);
        finish();
    }
}