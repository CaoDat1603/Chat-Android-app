package com.example.myapplication.controller;

import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class NotificationHelper {
    private static final String TAG = "NotificationHelper";
    private static final String FCM_API = "https://fcm.googleapis.com/fcm/send";
    private static final String SERVER_KEY = "key=AAAAMbwBIQg:APA91bGnz7NnX-lQKpj1FSB6eoURSuRZgwfCm5LnVzgQRkEk2rlnSqyL6Exu9io4zX9TW8T0LDpFmYJxhmXKFVFpQeOQBXr_N8F-pVQXEEzBSYXgyFDV1zMBBqIgzQ99j9-cL1IbHROH"; // Thay
                                                                                                                                                                                                             // bằng
                                                                                                                                                                                                             // Server
                                                                                                                                                                                                             // Key
                                                                                                                                                                                                             // từ
                                                                                                                                                                                                             // Firebase
                                                                                                                                                                                                             // Console
    private static final String CONTENT_TYPE = "application/json";

    /**
     * Gửi thông báo tin nhắn mới đến người dùng
     * 
     * @param receiverId ID của người nhận tin nhắn
     * @param senderName Tên người gửi tin nhắn
     * @param message    Nội dung tin nhắn
     */
    public static void sendMessageNotification(String receiverId, String senderName, String message) {
        // Lấy token FCM của người nhận từ Firebase
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("user").child(receiverId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String receiverToken = snapshot.child("fcmToken").getValue(String.class);
                    if (receiverToken != null && !receiverToken.isEmpty()) {
                        // Gửi thông báo qua FCM
                        sendNotification(receiverToken, senderName, message);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Lỗi khi lấy token FCM: " + error.getMessage());
            }
        });
    }

    /**
     * Gửi thông báo nhóm tin nhắn mới
     * 
     * @param groupId    ID của nhóm chat
     * @param senderName Tên người gửi tin nhắn
     * @param message    Nội dung tin nhắn
     */
    public static void sendGroupMessageNotification(String groupId, String senderName, String message) {
        // Lấy danh sách thành viên trong nhóm
        DatabaseReference groupRef = FirebaseDatabase.getInstance().getReference("groups").child(groupId)
                .child("members");
        groupRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot memberSnapshot : snapshot.getChildren()) {
                        String memberId = memberSnapshot.getKey();
                        if (memberId != null) {
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
                                            sendNotification(token, "Nhóm: " + senderName, message);
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
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Lỗi khi lấy danh sách thành viên nhóm: " + error.getMessage());
            }
        });
    }

    private static void sendNotification(String token, String title, String message) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    URL url = new URL(FCM_API);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setUseCaches(false);
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Authorization", SERVER_KEY);
                    conn.setRequestProperty("Content-Type", CONTENT_TYPE);

                    // Tạo dữ liệu JSON cho thông báo
                    JSONObject json = new JSONObject();
                    JSONObject dataJson = new JSONObject();
                    dataJson.put("title", title);
                    dataJson.put("message", message);
                    json.put("data", dataJson);
                    json.put("to", token);

                    // Gửi dữ liệu
                    OutputStream os = conn.getOutputStream();
                    os.write(json.toString().getBytes("UTF-8"));
                    os.close();

                    // Kiểm tra kết quả
                    int responseCode = conn.getResponseCode();
                    Log.d(TAG, "Kết quả gửi FCM: " + responseCode + " " + conn.getResponseMessage());

                    conn.disconnect();
                } catch (Exception e) {
                    Log.e(TAG, "Lỗi khi gửi thông báo: " + e.getMessage());
                }
                return null;
            }
        }.execute();
    }
}