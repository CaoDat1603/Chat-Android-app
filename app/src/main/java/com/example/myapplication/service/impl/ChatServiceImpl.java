package com.example.myapplication.service.impl;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.widget.Toast;

import com.example.myapplication.model.msgModel;
import com.example.myapplication.repository.ChatRepository;
import com.example.myapplication.service.IChatService;
import com.example.myapplication.view.ChatActivity;
import com.example.myapplication.view.adapter.messagesAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ChatServiceImpl implements IChatService {
    private final ChatActivity chatActivity;
    private final String senderRoom;
    private final String receiverRoom;
    private final String senderUID;
    private final ArrayList<msgModel> messageList;
    private final messagesAdapter adapter;
    private final ChatRepository repository;

    public ChatServiceImpl(ChatActivity chatActivity, String senderRoom, String receiverRoom,
                           String senderUID, ArrayList<msgModel> messageList, messagesAdapter adapter) {
        this.chatActivity = chatActivity;
        this.senderRoom = senderRoom;
        this.receiverRoom = receiverRoom;
        this.senderUID = senderUID;
        this.messageList = messageList;
        this.adapter = adapter;
        this.repository = new ChatRepository();
    }

    @Override
    public void initializeChat() {
        DatabaseReference chatRef = repository.getMessagesReference(senderRoom);
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    msgModel msg = ds.getValue(msgModel.class);
                    messageList.add(msg);
                }
                adapter.notifyDataSetChanged();
                chatActivity.scrollToLastMessage();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(chatActivity, "Lỗi tải tin nhắn", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void sendMessage(String message) {
        if (message.isEmpty()) {
            Toast.makeText(chatActivity, "Hãy nhập tin nhắn", Toast.LENGTH_SHORT).show();
            return;
        }
        msgModel msg = new msgModel(message, senderUID, new Date().getTime(), "text", null);
        repository.sendMessageToRoom(senderRoom, msg);
        if (!receiverRoom.equals(senderRoom)) {
            repository.sendMessageToRoom(receiverRoom, msg);
        }
    }

    @Override
    public void selectImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        chatActivity.startActivityForResult(intent, 1);
    }

    @Override
    public void selectFile() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        chatActivity.startActivityForResult(intent, 2);
    }

    @Override
    public void sendMediaMessage(Uri fileUri, int requestCode) {
        ProgressDialog progressDialog = new ProgressDialog(chatActivity);
        progressDialog.setMessage("Đang tải...");
        progressDialog.setCancelable(false);
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
                        if (isImageFile(fileName)) {
                            type = "image";
                        }
                        msgModel msg = new msgModel(url, senderUID, new Date().getTime(), type, fileName);
                        repository.sendMessageToRoom(senderRoom, msg);
                        if (!receiverRoom.equals(senderRoom)) {
                            repository.sendMessageToRoom(receiverRoom, msg);
                        }
                    }
                    progressDialog.dismiss();
                    Toast.makeText(chatActivity, "Tải tệp thành công", Toast.LENGTH_SHORT).show();
                });
            } else {
                progressDialog.dismiss();
                Toast.makeText(chatActivity, "Tải thất bại", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isImageFile(String fileName) {
        List<String> imageExtensions = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp", "tiff");
        String extension = getFileExtension(fileName);
        return imageExtensions.contains(extension.toLowerCase());
    }

    private String getFileName(Uri uri) {
        if (uri.getScheme().equals("content")) {
            Cursor cursor = chatActivity.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex >= 0) {
                    String name = cursor.getString(nameIndex);
                    cursor.close();
                    return name;
                }
                cursor.close(); // đừng quên đóng cursor
            }
        }
        return uri.getLastPathSegment();
    }

    private String getFileExtension(String fileName) {
        if (fileName != null && fileName.contains(".")) {
            int lastDotIndex = fileName.lastIndexOf('.');
            if (lastDotIndex != -1 && lastDotIndex < fileName.length() - 1) {
                return fileName.substring(lastDotIndex + 1).toLowerCase();
            }
        }
        return "";
    }

}
