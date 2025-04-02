package com.example.myapplication.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.Users;
import com.example.myapplication.controller.MainActivityController;
import com.example.myapplication.view.adapter.UserAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mainUserRecyclerView;
    private UserAdapter adapterUse;
    private ArrayList<Users> usersArrayList;
    private ImageView imglogout;
    private ImageView imgSettingProfile;

    private MainActivityController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainUserRecyclerView = findViewById(R.id.mainUserRecyclerView);
        imglogout = findViewById(R.id.logoutimg);

        // Initialize the list and adapter
        usersArrayList = new ArrayList<>();
        adapterUse = new UserAdapter(MainActivity.this, usersArrayList);
        mainUserRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mainUserRecyclerView.setAdapter(adapterUse);

        controller = new MainActivityController(this); // Pass the view instance to controller

        // Handle logout button click
        imglogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controller.showLogoutDialog();
            }
        });

        imgSettingProfile = findViewById(R.id.setting_profile);
        imgSettingProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controller.navigateToProfile();
            }
        });

        // Handle group chat button click
        ImageView chatGroup = findViewById(R.id.chatGroup);
        chatGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controller.navigateToChatGroup();
            }
        });

        // Check if the user is logged in
        controller.checkUserLoginStatus();

        // Đăng ký token FCM khi mở ứng dụng
        updateFCMToken();
    }

    // Cập nhật token FCM cho người dùng hiện tại
    private void updateFCMToken() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(new OnCompleteListener<String>() {
                        @Override
                        public void onComplete(@NonNull Task<String> task) {
                            if (!task.isSuccessful()) {
                                showMessage("Không thể cập nhật trạng thái thông báo");
                                return;
                            }

                            // Lấy token mới
                            String token = task.getResult();

                            // Lưu token vào Firebase
                            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            DatabaseReference userRef = FirebaseDatabase.getInstance()
                                    .getReference("user")
                                    .child(userId);
                            userRef.child("fcmToken").setValue(token);
                        }
                    });
        }
    }

    // Method to update user list from controller
    public void updateUserList(ArrayList<Users> usersList) {
        usersArrayList.clear();
        usersArrayList.addAll(usersList);
        adapterUse.notifyDataSetChanged();
    }

    // Method to show Toast message
    public void showMessage(String message) {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    // Method to navigate to login activity
    public void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    // Method to navigate to chat group activity
    public void navigateToChatGroup() {
        Intent intent = new Intent(MainActivity.this, MainActivityGroup.class);
        startActivity(intent);
        finish();
    }

    // Phương thức chuyển hướng sang Profile
    public void navigateToProfile() {
        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(intent);
        finish();
    }
}
