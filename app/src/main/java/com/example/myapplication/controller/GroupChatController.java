package com.example.myapplication.controller;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.myapplication.view.adapter.messagesAdapter;
import com.example.myapplication.view.GroupChatActivity;
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

public class GroupChatController {
    private final GroupChatActivity groupChatActivity;
    private final FirebaseDatabase database;
    private final String groupId;
    private final ArrayList<msgModel> messagesArrayList;
    private final messagesAdapter messagesAdapter;

    public GroupChatController(GroupChatActivity groupChatActivity, FirebaseDatabase database, String groupId, ArrayList<msgModel> messagesArrayList, messagesAdapter messagesAdapter) {
        this.groupChatActivity = groupChatActivity;
        this.database = database;
        this.groupId = groupId;
        this.messagesArrayList = messagesArrayList;
        this.messagesAdapter = messagesAdapter;
    }

    // Khởi tạo cuộc trò chuyện nhóm
    public void initializeGroupChat() {
        DatabaseReference groupChatReference = database.getReference().child("groups").child(groupId).child("messages");
        groupChatReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messagesArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    msgModel message = dataSnapshot.getValue(msgModel.class);
                    messagesArrayList.add(message);
                }
                messagesAdapter.notifyDataSetChanged();

                groupChatActivity.scrollToLastMessage();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(groupChatActivity, "Error loading messages", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Gửi tin nhắn văn bản
    public void sendMessage(String message, String senderUID) {
        if (message.isEmpty()) {
            Toast.makeText(groupChatActivity, "Enter The Message First", Toast.LENGTH_SHORT).show();
            return;
        }

        Date date = new Date();
        msgModel newMessage = new msgModel(message, senderUID, date.getTime(), "text", null, true);

        // Lưu tin nhắn vào Firebase
        database.getReference().child("groups").child(groupId)
                .child("messages").push().setValue(newMessage)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Toast.makeText(groupChatActivity, "Failed to send message", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Chọn hình ảnh từ bộ nhớ
    public void selectImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        groupChatActivity.startActivityForResult(intent, 1);
    }

    // Chọn file từ bộ nhớ
    public void selectFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        groupChatActivity.startActivityForResult(intent, 2);
    }

    // Upload hình ảnh hoặc file lên Firebase Storage
    public void uploadToFirebaseStorage(Uri fileUri, int requestCode) {
        // Hiển thị ProgressDialog
        ProgressDialog progressDialog = new ProgressDialog(groupChatActivity);
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
                    Toast.makeText(groupChatActivity, "Tải tệp thành công", Toast.LENGTH_SHORT).show();
                });
            } else {
                // Hiển thị thông báo lỗi và ẩn ProgressDialog
                progressDialog.dismiss();
                Toast.makeText(groupChatActivity, "Tải tệp thất bại", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Gửi tin nhắn file hoặc hình ảnh
    private void sendFileMessage(String downloadUrl, int requestCode, String fileName) {
        String messageType = (requestCode == 1) ? "image" : "file";
        msgModel newMessage = new msgModel(downloadUrl, groupChatActivity.senderUID, new Date().getTime(), messageType, fileName, true);

        database.getReference().child("groups").child(groupId).child("messages").push().setValue(newMessage)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Toast.makeText(groupChatActivity, "Failed to send file", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Lấy tên file từ URI
    private String getFileName(Uri uri) {
        String fileName = "";
        if (uri.getScheme().equals("content")) {
            Cursor cursor = groupChatActivity.getContentResolver().query(uri, null, null, null, null);
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