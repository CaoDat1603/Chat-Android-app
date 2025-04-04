package com.example.myapplication.controller;

import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class NotificationHelper {
    private static final String TAG = "NotificationHelper";
    private static final String FCM_API = "https://fcm.googleapis.com/fcm/send";
    private static final String SERVER_KEY = "key=AAAAMbwBIQg:APA91bGnz7NnX-lQKpj1FSB6eoURSuRZgwfCm5LnVzgQRkEk2rlnSqyL6Exu9io4zX9TW8T0LDpFmYJxhmXKFVFpQeOQBXr_N8F-pVQXEEzBSYXgyFDV1zMBBqIgzQ99j9-cL1IbHROH";
    private static final String CONTENT_TYPE = "application/json";

    // Sử dụng Executor thay vì AsyncTask (đã bị deprecated)
    private static final Executor executor = Executors.newSingleThreadExecutor();

    /**
     * Gửi thông báo tin nhắn mới đến người dùng
     * 
     * @param receiverId ID của người nhận tin nhắn
     * @param senderName Tên người gửi tin nhắn
     * @param message    Nội dung tin nhắn
     */
    public static void sendMessageNotification(String receiverId, String senderName, String message) {
        try {
            Log.d(TAG, "SELF_MSG: Attempting to send notification to: " + receiverId);
            Log.d(TAG, "SELF_MSG: Current user ID: " + FirebaseAuth.getInstance().getCurrentUser().getUid());
            Log.d(TAG, "SELF_MSG: Are they equal? " +
                    FirebaseAuth.getInstance().getCurrentUser().getUid().equals(receiverId));

            Log.d(TAG, "Sending notification to user: " + receiverId);

            // Lấy token FCM của người nhận từ Firebase
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("user").child(receiverId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        if (snapshot.exists()) {
                            String receiverToken = snapshot.child("fcmToken").getValue(String.class);
                            Log.d(TAG, "Receiver token: " + (receiverToken != null ? "available" : "null"));

                            if (receiverToken != null && !receiverToken.isEmpty()) {
                                // Gửi thông báo qua FCM
                                Log.d(TAG, "Sending notification to: " + receiverToken);

                                // Thêm các dữ liệu cho intent khi nhấn vào thông báo
                                JSONObject dataObject = new JSONObject();
                                try {
                                    dataObject.put("title", senderName);
                                    dataObject.put("message", message);
                                    dataObject.put("senderId", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                    dataObject.put("isGroup", "false");
                                } catch (Exception e) {
                                    Log.e(TAG, "Error creating data JSON: " + e.getMessage());
                                }

                                sendNotification(receiverToken, senderName, message, dataObject);
                            } else {
                                Log.w(TAG, "Cannot send notification: token is empty");
                            }
                        } else {
                            Log.w(TAG, "Cannot send notification: user not found");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error in onDataChange: " + e.getMessage());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Lỗi khi lấy token FCM: " + error.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in sendMessageNotification: " + e.getMessage());
        }
    }

    /**
     * Gửi thông báo nhóm tin nhắn mới
     * 
     * @param groupId    ID của nhóm chat
     * @param senderName Tên người gửi tin nhắn
     * @param message    Nội dung tin nhắn
     */
    public static void sendGroupMessageNotification(String groupId, String senderName, String message) {
        Log.d(TAG, "Sending group notification for group: " + groupId);

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Lấy danh sách thành viên trong nhóm
        DatabaseReference groupRef = FirebaseDatabase.getInstance().getReference("groups").child(groupId)
                .child("members");
        groupRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Log.d(TAG, "Members count: " + snapshot.getChildrenCount());

                    for (DataSnapshot memberSnapshot : snapshot.getChildren()) {
                        String memberId = memberSnapshot.getKey();

                        if (memberId != null) {
                            Log.d(TAG, "Processing member: " + memberId);

                            // Lấy token của từng thành viên và gửi thông báo
                            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("user")
                                    .child(memberId);
                            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                    if (userSnapshot.exists()) {
                                        String token = userSnapshot.child("fcmToken").getValue(String.class);

                                        if (token != null && !token.isEmpty()) {
                                            // Gửi thông báo về tin nhắn nhóm
                                            Log.d(TAG, "Sending group notification to member: " + memberId);

                                            // Thêm các dữ liệu cho intent khi nhấn vào thông báo
                                            JSONObject dataObject = new JSONObject();
                                            try {
                                                dataObject.put("title", "Nhóm: " + senderName);
                                                dataObject.put("message", message);
                                                dataObject.put("isGroup", "true");
                                                dataObject.put("groupId", groupId);
                                            } catch (Exception e) {
                                                Log.e(TAG, "Error creating group data JSON: " + e.getMessage());
                                            }

                                            sendNotification(token, "Nhóm: " + senderName, message, dataObject);
                                        } else {
                                            Log.w(TAG,
                                                    "Cannot send notification: token is empty for member " + memberId);
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e(TAG, "Lỗi khi lấy token FCM của thành viên: " + error.getMessage());
                                }
                            });
                        }
                    }
                } else {
                    Log.w(TAG, "Cannot send group notification: group not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Lỗi khi lấy danh sách thành viên nhóm: " + error.getMessage());
            }
        });
    }

    private static void sendNotification(String token, String title, String message, JSONObject dataObject) {
        executor.execute(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(FCM_API);
                conn = (HttpURLConnection) url.openConnection();
                conn.setUseCaches(false);
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", SERVER_KEY);
                conn.setRequestProperty("Content-Type", CONTENT_TYPE);

                // Tạo dữ liệu JSON cho thông báo
                JSONObject json = new JSONObject();

                // Dữ liệu để xử lý khi ứng dụng đang chạy (data message)
                json.put("data", dataObject);

                // Dữ liệu hiển thị thông báo khi ứng dụng không chạy (notification message)
                JSONObject notificationObj = new JSONObject();
                notificationObj.put("title", title);
                notificationObj.put("body", message);
                notificationObj.put("sound", "default");
                notificationObj.put("icon", "icon_check");

                json.put("notification", notificationObj);
                json.put("to", token);
                json.put("priority", "high");

                // Gửi dữ liệu
                OutputStream os = conn.getOutputStream();
                os.write(json.toString().getBytes("UTF-8"));
                os.close();

                // Kiểm tra kết quả
                int responseCode = conn.getResponseCode();
                String responseMessage = conn.getResponseMessage();
                Log.d(TAG, "FCM Response: " + responseCode + " " + responseMessage);

            } catch (Exception e) {
                Log.e(TAG, "Error sending FCM notification", e);
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        });
    }
}