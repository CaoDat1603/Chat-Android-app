package com.example.myapplication.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.controller.MainActivityController;
import com.example.myapplication.model.Users;
import com.example.myapplication.service.IUserService;
import com.example.myapplication.service.impl.UserServiceImpl;
import com.example.myapplication.view.adapter.UserAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MainActivityController controller;
    private UserAdapter userAdapter;
    private ArrayList<Users> userList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ImageView imgLogout, imgSettingProfile, chatGroup; // <- Khai báo logout button

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.mainUserRecyclerView);
        userAdapter = new UserAdapter(this, userList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(userAdapter);


        // Khởi tạo Service và Controller
        IUserService userService = new UserServiceImpl(this);
        controller = new MainActivityController(userService);

        // Bắt đầu xử lý khi mở app
        controller.handleAppStart();

        // Gán nút logout và bắt sự kiện
        imgLogout = findViewById(R.id.logoutimg);
        imgLogout.setOnClickListener(view -> showLogoutDialog());

        // Xử lý sự kiện khi nhấn vào chat nhóm
        chatGroup = findViewById(R.id.chatGroup);
        chatGroup.setOnClickListener(view -> navigateToChatGroup());

        imgSettingProfile = findViewById(R.id.setting_profile);
        imgSettingProfile.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {navigateToProfile();}
        });
    }

    public void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void updateUserList(List<Users> usersList) {
        userList.clear();
        userList.addAll(usersList);
        userAdapter.notifyDataSetChanged();
    }

    public void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> controller.onUserSignOut())
                .setNegativeButton("Hủy", null)
                .show();
    }

    public void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public void navigateToChatGroup() {
        Intent intent = new Intent(this, MainActivityGroup.class);
        startActivity(intent);
    }

    public void navigateToProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }
}
