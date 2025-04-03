package com.example.myapplication.view;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
    private static final String TAG = "MainActivity";

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
        if (imgSettingProfile != null) {
            imgSettingProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    controller.navigateToProfile();
                }
            });
        }

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

        // Kiểm tra xem thông báo có bị tắt không
        checkNotificationsEnabled();
    }

    // Kiểm tra xem thông báo có được bật không
    private void checkNotificationsEnabled() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Trên Android 8.0 trở lên, kiểm tra thông báo theo channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!notificationManager.areNotificationsEnabled() ||
                    notificationManager.getNotificationChannel("chat_messages") == null ||
                    notificationManager.getNotificationChannel("chat_messages")
                            .getImportance() == NotificationManager.IMPORTANCE_NONE) {

                showNotificationSettingsDialog();
            }
        } else {
            // Trên các phiên bản cũ hơn
            if (!notificationManager.areNotificationsEnabled()) {
                showNotificationSettingsDialog();
            }
        }
    }

    // Hiển thị hộp thoại cài đặt thông báo
    private void showNotificationSettingsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Thông báo bị tắt")
                .setMessage("Thông báo đã bị tắt cho ứng dụng này. Bạn cần bật thông báo để nhận tin nhắn mới.")
                .setPositiveButton("Cài đặt", (dialog, which) -> {
                    Intent intent = new Intent();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                        intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                    } else {
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.addCategory(Intent.CATEGORY_DEFAULT);
                        intent.setData(android.net.Uri.parse("package:" + getPackageName()));
                    }

                    startActivity(intent);
                })
                .setNegativeButton("Sau", null)
                .show();
    }

    // Cập nhật token FCM cho người dùng hiện tại
    private void updateFCMToken() {
        Log.d(TAG, "Updating FCM token");
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(new OnCompleteListener<String>() {
                        @Override
                        public void onComplete(@NonNull Task<String> task) {
                            if (!task.isSuccessful()) {
                                Log.e(TAG, "Không thể cập nhật FCM token", task.getException());
                                return;
                            }

                            // Lấy token mới
                            String token = task.getResult();
                            Log.d(TAG, "FCM Token: " + token);

                            // Hiển thị token cho người dùng
                            showTokenDialog(token);

                            // Lưu token vào Firebase
                            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            DatabaseReference userRef = FirebaseDatabase.getInstance()
                                    .getReference("user")
                                    .child(userId);
                            userRef.child("fcmToken").setValue(token)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "FCM token đã được cập nhật thành công");
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Lỗi khi cập nhật FCM token: " + e.getMessage());
                                    });
                        }
                    });
        } else {
            Log.w(TAG, "Không thể cập nhật FCM token: người dùng chưa đăng nhập");
        }
    }

    // Hiện dialog hiển thị token
    private void showTokenDialog(String token) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("FCM Registration Token");

        // Tạo layout với TextView và Button copy
        View view = getLayoutInflater().inflate(R.layout.dialog_token, null);
        TextView tokenTextView = view.findViewById(R.id.tokenTextView);
        tokenTextView.setText(token);

        builder.setView(view);

        builder.setPositiveButton("Copy", (dialog, which) -> {
            // Copy token vào clipboard
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(
                    Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("FCM Token", token);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(MainActivity.this, "Token đã được sao chép", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Đóng", null);

        AlertDialog dialog = builder.create();
        dialog.show();
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

    @Override
    protected void onDestroy() {
        // Make sure controller dismisses any open dialogs
        if (controller != null) {
            controller.dismissDialogs();
        }
        super.onDestroy();
    }
}
