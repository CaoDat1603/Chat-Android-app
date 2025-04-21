package com.example.myapplication.controller;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.myapplication.view.adapter.messagesAdapter;
import com.example.myapplication.view.ChatActivity;
import com.example.myapplication.model.msgModel;
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

    public ChatController(ChatActivity chatActivity, FirebaseDatabase database, String senderRoom, String reciverRoom, ArrayList<msgModel> messagesArrayList, messagesAdapter messagesAdapter) {
        this.chatActivity = chatActivity;
        this.database = database;
        this.senderRoom = senderRoom;
        this.reciverRoom = reciverRoom;
        this.messagesArrayList = messagesArrayList;
        this.messagesAdapter = messagesAdapter;
    }

    // Khởi tạo cuộc trò chuyện
    public void initializeChat() {
        DatabaseReference chatReference = database.getReference().child("chats").child(senderRoom).child("messages");
        chatReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messagesArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    msgModel messages = dataSnapshot.getValue(msgModel.class);
                    messagesArrayList.add(messages);
                }
                messagesAdapter.notifyDataSetChanged();

                // Cuộn đến tin nhắn cuối cùng
                chatActivity.scrollToLastMessage();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(chatActivity, "Error loading messages", Toast.LENGTH_SHORT).show();
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
        msgModel messages = new msgModel(downloadUrl, chatActivity.SenderUID, new Date().getTime(), messageType, fileName);

        database.getReference().child("chats").child(senderRoom).child("messages").push().setValue(messages)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !reciverRoom.equals(senderRoom)) {
                        database.getReference().child("chats").child(reciverRoom).child("messages").push().setValue(messages);
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
}