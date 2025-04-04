package com.example.myapplication.controller;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.myapplication.R;
import com.example.myapplication.view.adapter.messagesAdapter;
import com.example.myapplication.view.ChatActivity;
import com.example.myapplication.model.msgModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;

public class ChatController {
    private final ChatActivity chatActivity;
    private final FirebaseDatabase database;
    private final String senderRoom;
    private final String reciverRoom;
    private final ArrayList<msgModel> messagesArrayList;
    private final messagesAdapter messagesAdapter;

    public ChatController(ChatActivity chatActivity, FirebaseDatabase database, String senderRoom, String reciverRoom,
            ArrayList<msgModel> messagesArrayList, messagesAdapter messagesAdapter) {
        this.chatActivity = chatActivity;
        this.database = database;
        this.senderRoom = senderRoom;
        this.reciverRoom = reciverRoom;
        this.messagesArrayList = messagesArrayList;
        this.messagesAdapter = messagesAdapter;
    }

    // Khởi tạo cuộc trò chuyện
    public void initializeChat() {
        database.getReference().child("chats").child(senderRoom).child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messagesArrayList.clear();

                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            msgModel messages = dataSnapshot.getValue(msgModel.class);
                            if (messages != null) {
                                messagesArrayList.add(messages);
                            }
                        }

                        messagesAdapter.notifyDataSetChanged();
                        chatActivity.scrollToLastMessage();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("ChatController", "Database error: " + error.getMessage());
                    }
                });
    }

    // Gửi tin nhắn văn bản
    public void sendMessage(String message, String senderUID) {
        if (message.isEmpty()) {
            Toast.makeText(chatActivity, "Enter The Message First", Toast.LENGTH_SHORT).show();
            return;
        }

        Date date = new Date();
        msgModel messagess = new msgModel(message, senderUID, date.getTime(), "text", null);

        // Lưu tin nhắn vào Firebase
        database.getReference().child("chats").child(senderRoom)
                .child("messages").push().setValue(messagess)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !reciverRoom.equals(senderRoom)) {
                        database.getReference().child("chats").child(reciverRoom)
                                .child("messages").push().setValue(messagess);

                        // Gửi thông báo cho người nhận
                        sendNotificationToReceiver(message);
                    }

                    // Nếu đây là tin nhắn gửi cho chính mình, tạo thông báo cục bộ
                    if (chatActivity.reciverUID.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                        createLocalNotification(message);
                    }
                });
    }

    // Gửi thông báo cho người nhận tin nhắn
    private void sendNotificationToReceiver(String message) {
        // Lấy tên người gửi
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("user").child(currentUserId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String senderName = snapshot.child("fullname").getValue(String.class);
                    if (senderName != null) {
                        // Gửi thông báo sử dụng NotificationHelper
                        try {
                            NotificationHelper.sendMessageNotification(
                                    chatActivity.reciverUID,
                                    senderName,
                                    message);
                            Log.d("ChatController",
                                    "Sending notification to " + chatActivity.reciverUID + " with message: " + message);
                        } catch (Exception e) {
                            Log.e("ChatController", "Error sending notification: " + e.getMessage());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ChatController", "Error getting sender data: " + error.getMessage());
            }
        });
    }

    // Chọn hình ảnh từ bộ nhớ
    public void selectImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        chatActivity.startActivityForResult(intent, 1);
    }

    // Chọn file từ bộ nhớ
    public void selectFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        chatActivity.startActivityForResult(intent, 2);
    }

    // Upload hình ảnh hoặc file lên Firebase Storage
    public void uploadToFirebaseStorage(Uri fileUri, int requestCode) {
        // Hiển thị ProgressDialog
        ProgressDialog progressDialog = new ProgressDialog(chatActivity);
        progressDialog.setMessage("Đang tải...");
        progressDialog.setCancelable(false); // Không cho phép hủy bằng cách nhấn ra ngoài
        progressDialog.show();

        String fileName = getFileName(fileUri);
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("uploads");
        StorageReference filePath = storageReference.child(System.currentTimeMillis() + "");

        filePath.putFile(fileUri).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                filePath.getDownloadUrl().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        String downloadUrl = task1.getResult().toString();
                        sendFileMessage(downloadUrl, requestCode, fileName);
                    }
                    // Ẩn ProgressDialog sau khi hoàn thành
                    progressDialog.dismiss();
                    Toast.makeText(chatActivity, "Tải tệp thành công", Toast.LENGTH_SHORT).show();
                });
            } else {
                // Hiển thị thông báo lỗi và ẩn ProgressDialog
                progressDialog.dismiss();
                Toast.makeText(chatActivity, "Tải tệp thất bại", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Gửi tin nhắn file hoặc hình ảnh
    private void sendFileMessage(String downloadUrl, int requestCode, String fileName) {
        String messageType = (requestCode == 1) ? "image" : "file";
        msgModel messages = new msgModel(downloadUrl, chatActivity.SenderUID, new Date().getTime(), messageType,
                fileName);

        database.getReference().child("chats").child(senderRoom).child("messages").push().setValue(messages)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !reciverRoom.equals(senderRoom)) {
                        database.getReference().child("chats").child(reciverRoom).child("messages").push()
                                .setValue(messages);

                        // Gửi thông báo cho người nhận
                        String notificationMessage = messageType.equals("image") ? "Đã gửi hình ảnh"
                                : "Đã gửi file: " + fileName;
                        sendNotificationToReceiver(notificationMessage);
                    }
                });
    }

    // Lấy tên file từ URI
    private String getFileName(Uri uri) {
        String fileName = "";
        if (uri.getScheme().equals("content")) {
            Cursor cursor = chatActivity.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                fileName = cursor.getString(nameIndex);
                cursor.close();
            }
        } else {
            fileName = uri.getLastPathSegment();
        }
        return fileName != null ? fileName : "unknown_file"; // Nếu không có tên, trả về "unknown_file"
    }

    // Tạo thông báo cục bộ cho tin nhắn gửi cho chính mình
    private void createLocalNotification(String message) {
        String channelId = "chat_messages";
        NotificationManager notificationManager = (NotificationManager) chatActivity
                .getSystemService(Context.NOTIFICATION_SERVICE);

        // Tạo notification channel cho Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Chat Messages",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        // Tạo intent để mở lại màn hình chat khi click vào thông báo
        Intent intent = new Intent(chatActivity, ChatActivity.class);
        intent.putExtra("uid", chatActivity.reciverUID);
        intent.putExtra("name", chatActivity.reciverName);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                chatActivity, 0, intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ? PendingIntent.FLAG_IMMUTABLE
                        : PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(chatActivity, channelId)
                .setSmallIcon(R.drawable.icon_check)
                .setContentTitle("Tin nhắn từ chính bạn")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    // Vô hiệu hóa hàm này tạm thời để tránh crash
    // private void createLocalNotificationForReceivedMessage(String message, String
    // senderId) {
    // // ... code cũ ...
    // }
}
