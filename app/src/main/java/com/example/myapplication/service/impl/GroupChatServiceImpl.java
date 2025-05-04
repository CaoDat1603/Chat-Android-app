package com.example.myapplication.service.impl;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.widget.Toast;

import com.example.myapplication.model.msgModel;
import com.example.myapplication.repository.GroupChatRepository;
import com.example.myapplication.service.IGroupChatService;
import com.example.myapplication.view.GroupChatActivity;
import com.example.myapplication.view.adapter.messagesAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;

public class GroupChatServiceImpl implements IGroupChatService {
    private final GroupChatActivity groupChatActivity;
    private final String groupID;
    private final String senderID;
    private final ArrayList<msgModel> messageList;
    private final messagesAdapter adapter;
    private final GroupChatRepository repository;

    public GroupChatServiceImpl(GroupChatActivity groupChatActivity, String groupID,
                                String senderID, ArrayList<msgModel> messageList, messagesAdapter adapter) {
        this.groupChatActivity = groupChatActivity;
        this.groupID = groupID;
        this.senderID = senderID;
        this.messageList = messageList;
        this.adapter = adapter;
        this.repository = new GroupChatRepository();
    }

    @Override
    public void initializeChat() {
        DatabaseReference groupChatRef = repository.getMessagesReference(groupID);
        groupChatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    msgModel msg = ds.getValue(msgModel.class);
                    messageList.add(msg);
                }
                adapter.notifyDataSetChanged();
                groupChatActivity.scrollToLastMessage();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(groupChatActivity, "Lỗi tải tin nhắn", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Gửi tin nhắn văn bản
    @Override
    public void sendMessage(String message) {
        if (message.isEmpty()) {
            Toast.makeText(groupChatActivity, "Hãy nhập tin nhắn", Toast.LENGTH_SHORT).show();
            return;
        }
        Date date = new Date();
        msgModel msg = new msgModel(message, senderID, date.getTime(), "text", null, true);
        repository.sendMessageToRoom(groupID, msg) // đúng groupID
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Toast.makeText(groupChatActivity, "Failed to send message", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void selectImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        groupChatActivity.startActivityForResult(intent, 1);
    }

    @Override
    public void selectFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        groupChatActivity.startActivityForResult(intent, 2);
    }

    @Override
    public void sendMediaMessage(Uri fileUri, int requestCode) {
        // Hiển thị ProgressDialog
        ProgressDialog progressDialog = new ProgressDialog(groupChatActivity);
        progressDialog.setMessage("Đang tải...");
        progressDialog.setCancelable(false); // Không cho phép hủy bằng cách nhấn ra ngoài
        progressDialog.show();

        String fileName = getFileName(fileUri);
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("uploads");
        StorageReference filePath = storageRef.child(System.currentTimeMillis() + "");

        filePath.putFile(fileUri).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                filePath.getDownloadUrl().addOnCompleteListener(urlTask -> {
                    if (urlTask.isSuccessful()) {
                        String url = urlTask.getResult().toString();
                        String type = (requestCode == 1) ? "image" : "file";
                        msgModel msg = new msgModel(url, senderID, new Date().getTime(), type, fileName);
                        repository.sendMessageToRoom(groupID, msg);
                    }
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

    private String getFileName(Uri uri) {
        if (uri.getScheme().equals("content")) {
            Cursor cursor = groupChatActivity.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex >= 0) {
                    String name = cursor.getString(nameIndex);
                    cursor.close();
                    return name;
                }
                cursor.close();
            }
        }
        return uri.getLastPathSegment();
    }
}
