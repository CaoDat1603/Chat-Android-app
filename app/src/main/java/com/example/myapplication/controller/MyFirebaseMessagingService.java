package com.example.myapplication.controller;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.myapplication.R;
import com.example.myapplication.view.ChatActivity;
import com.example.myapplication.view.GroupChatActivity;
import com.example.myapplication.view.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Random;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FCMService";
    private static final String CHANNEL_ID = "chat_messages";
    private static final String CHANNEL_NAME = "Chat Messages";
    private static final String CHANNEL_DESC = "Thông báo tin nhắn mới";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "FCM Service created");
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);

        // Lưu token mới vào Firebase
        saveTokenToFirebase(token);
    }

    private void saveTokenToFirebase(String token) {
        Log.d(TAG, "Saving token to Firebase");
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            Log.d(TAG, "Saving token for user: " + userId);
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("user").child(userId);
            userRef.child("fcmToken").setValue(token)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Token saved successfully");
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to save token: " + e.getMessage());
                    });
        } else {
            Log.w(TAG, "No user logged in, token not saved");
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Xử lý thông báo từ dữ liệu (data payload)
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            handleDataMessage(remoteMessage.getData());
        }

        // Xử lý thông báo trực tiếp (notification payload)
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            showNotification(title, body, null);
        }
    }

    private void handleDataMessage(Map<String, String> data) {
        Log.d(TAG, "Handling data message");
        String title = data.get("title");
        String message = data.get("message");
        String senderId = data.get("senderId");
        String isGroup = data.get("isGroup");
        String groupId = data.get("groupId");

        // Kiểm tra dữ liệu
        if (title == null) {
            title = "Thông báo mới";
            Log.w(TAG, "No title in data message, using default");
        }

        if (message == null) {
            message = "Bạn có tin nhắn mới";
            Log.w(TAG, "No message in data message, using default");
        }

        Log.d(TAG, "Data message values - Title: " + title + ", Message: " + message);

        // Tạo intent để mở đúng activity khi nhấn vào thông báo
        Intent intent;
        if ("true".equals(isGroup) && groupId != null) {
            Log.d(TAG, "Creating intent for group chat: " + groupId);
            intent = new Intent(this, GroupChatActivity.class);
            intent.putExtra("groupId", groupId);
        } else if (senderId != null) {
            Log.d(TAG, "Creating intent for private chat with: " + senderId);
            intent = new Intent(this, ChatActivity.class);
            intent.putExtra("uid", senderId);
        } else {
            Log.d(TAG, "Creating default intent to MainActivity");
            intent = new Intent(this, MainActivity.class);
        }

        showNotification(title, message, intent);
    }

    private void showNotification(String title, String message, Intent intent) {
        Log.d(TAG, "Showing notification - Title: " + title + ", Message: " + message);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Tạo intent khi nhấn vào thông báo (nếu không có intent cụ thể)
        if (intent == null) {
            intent = new Intent(this, MainActivity.class);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                new Random().nextInt(3000), // Sử dụng request code ngẫu nhiên để tránh intent cũ bị ghi đè
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Âm thanh thông báo
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // Tạo notification channel cho Android O trở lên
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "Creating notification channel");
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(CHANNEL_DESC);
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[] { 0, 500, 200, 500 });
            channel.setShowBadge(true);
            notificationManager.createNotificationChannel(channel);
        }

        // Xây dựng thông báo
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.icon_check)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        // Hiển thị thông báo với ID ngẫu nhiên
        int notificationId = new Random().nextInt(3000);
        Log.d(TAG, "Showing notification with ID: " + notificationId);
        notificationManager.notify(notificationId, notificationBuilder.build());
    }
}
