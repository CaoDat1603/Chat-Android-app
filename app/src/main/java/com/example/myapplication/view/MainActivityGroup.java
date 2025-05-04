package com.example.myapplication.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.controller.SettingGroupController;
import com.example.myapplication.model.Group;
import com.example.myapplication.controller.MainActivityGroupController;
import com.example.myapplication.view.adapter.GroupAdapter;

import java.util.ArrayList;

public class MainActivityGroup extends AppCompatActivity {

    private RecyclerView recyclerView;
    private GroupAdapter adapter;
    private ArrayList<Group> groupArrayList;
    private ImageView imglogout;
    private MainActivityGroupController controller;
    private ImageView imgSettingProfile;
    private boolean isGroupUpdated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_group);

        recyclerView = findViewById(R.id.groupChatText);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        groupArrayList = new ArrayList<>();
        adapter = new GroupAdapter(this, groupArrayList);
        recyclerView.setAdapter(adapter);

        controller = new MainActivityGroupController(this); // Khởi tạo controller

        // Kiểm tra trạng thái đăng nhập
        controller.checkUserLoginStatus();

        // Xử lý sự kiện đăng xuất
        imglogout = findViewById(R.id.logoutimg);
        imglogout.setOnClickListener(view -> controller.showLogoutDialog());

        imgSettingProfile = findViewById(R.id.setting_profile);
        imgSettingProfile.setOnClickListener(view -> controller.navigateToProfile());

        // Xử lý sự kiện khi nhấn vào chat nhóm
        ImageView chatOneOne = findViewById(R.id.chatOneOne);
        chatOneOne.setOnClickListener(view -> controller.navigateToMainActivity());

        // Xử lý sự kiện khi nhấn vào tạo nhóm
        ImageView createGroup = findViewById(R.id.createGroup);
        createGroup.setOnClickListener(view -> controller.navigateToCreateGroupActivity());
    }

    // Phương thức thêm nhóm vào danh sách
    public void addGroupToList(Group group) {
        groupArrayList.add(group);
        adapter.notifyDataSetChanged();
    }

    // Phương thức cập nhật danh sách nhóm
    public void updateGroupList(ArrayList<Group> updatedGroups) {
        groupArrayList.clear();
        groupArrayList.addAll(updatedGroups);
        adapter.notifyDataSetChanged();
    }

    // Phương thức hiển thị thông báo
    public void showMessage(String message) {
        Toast.makeText(MainActivityGroup.this, message, Toast.LENGTH_SHORT).show();
    }

    // Phương thức chuyển đến màn hình đăng nhập
    public void navigateToLogin() {
        Intent intent = new Intent(MainActivityGroup.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    // Phương thức chuyển đến màn hình MainActivity
    public void navigateToMainActivity() {
        Intent intent = new Intent(MainActivityGroup.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    // Phương thức chuyển đến màn hình tạo nhóm
    public void navigateToCreateGroupActivity() {
        Intent intent = new Intent(MainActivityGroup.this, CreateGroupActivity.class);
        startActivity(intent);
    }

    public void navigateToProfile() {
        Intent intent = new Intent(MainActivityGroup.this, ProfileActivity.class);
        startActivity(intent);
        finish();
    }

    protected void onResume() {
        super.onResume();
        if (isGroupUpdated) {
            controller.loadGroupList();
            isGroupUpdated = false;  // Reset flag
        }
    }
}
