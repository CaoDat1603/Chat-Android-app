package com.example.myapplication.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

//hiển thị màn hiình khởi động
public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    private static final int SPLASH_DELAY = 2000; // 2 giây

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Yêu cầu quyền thông báo cho Android 13+ (API 33+)
        requestNotificationPermission();

        // Lấy FCM token để đảm bảo đã đăng ký
        getFCMToken();
    }

    // Yêu cầu quyền thông báo nếu cần thiết
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

                Log.d(TAG, "Requesting notification permission");
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            } else {
                Log.d(TAG, "Notification permission already granted");
                delayedNavigation();
            }
        } else {
            Log.d(TAG, "Notification permission not required on this Android version");
            delayedNavigation();
        }
    }

    // Launcher để yêu cầu quyền thông báo
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.d(TAG, "Notification permission granted");
                } else {
                    Log.w(TAG, "Notification permission denied");
                    Toast.makeText(this,
                            "Thông báo tin nhắn sẽ không hoạt động nếu không cấp quyền thông báo",
                            Toast.LENGTH_LONG).show();
                }

                // Dù được cấp quyền hay không, vẫn tiếp tục chuyển màn hình
                delayedNavigation();
            });

    // Lấy token FCM
    private void getFCMToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    // Lấy token mới
                    String token = task.getResult();
                    Log.d(TAG, "FCM token: " + token);
                });
    }

    // Chuyển màn hình sau khi hoàn tất kiểm tra
    private void delayedNavigation() {
        new Handler().postDelayed(() -> {
            // Kiểm tra đăng nhập
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            Intent intent;

            if (currentUser != null) {
                // Đã đăng nhập, chuyển sang MainActivity
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                // Chưa đăng nhập, chuyển sang LoginActivity
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }

            startActivity(intent);
            finish();
        }, SPLASH_DELAY);
    }
}
