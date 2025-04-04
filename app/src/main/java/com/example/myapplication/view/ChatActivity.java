// File: ChatActivity.java
package com.example.myapplication.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.app.NotificationManager;
import android.content.Context;

import com.example.myapplication.R;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.controller.ChatController;
import com.example.myapplication.model.msgModel;
import com.example.myapplication.view.adapter.messagesAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

    public String reciverUID, reciverName, SenderUID;
    TextView reciverNameAc;
    CardView sendbtn;
    EditText textmsg;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase database;
    public String senderRoom, reciverRoom;
    public RecyclerView msgAdapter;
    ArrayList<msgModel> messagesArrayList;
    messagesAdapter messagesAdapter;
    ChatController chatController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        reciverName = getIntent().getStringExtra("name");
        reciverUID = getIntent().getStringExtra("uid");

        messagesArrayList = new ArrayList<>();

        // Setup RecyclerView
        msgAdapter = findViewById(R.id.msgadapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        msgAdapter.setLayoutManager(linearLayoutManager);
        messagesAdapter = new messagesAdapter(ChatActivity.this, messagesArrayList);
        msgAdapter.setAdapter(messagesAdapter);

        sendbtn = findViewById(R.id.sendbtn);
        textmsg = findViewById(R.id.textmsg);
        reciverNameAc = findViewById(R.id.recivername);
        reciverNameAc.setText(reciverName);

        SenderUID = firebaseAuth.getUid();
        senderRoom = SenderUID + reciverUID;
        reciverRoom = reciverUID + SenderUID;

        chatController = new ChatController(this, database, senderRoom, reciverRoom, messagesArrayList,
                messagesAdapter);
        chatController.initializeChat();

        sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chatController.sendMessage(textmsg.getText().toString(), SenderUID);
                textmsg.setText("");
            }
        });

        ImageButton sendImage = findViewById(R.id.sendImage);
        ImageButton sendFile = findViewById(R.id.sendFile);
        ImageButton turnback = findViewById(R.id.turnback);

        // Chọn ảnh
        sendImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chatController.selectImage();
            }
        });

        // Chọn file
        sendFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chatController.selectFile();
            }
        });

        // Turn back
        turnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // Kiểm tra quyền truy cập bộ nhớ
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.READ_EXTERNAL_STORAGE }, 100);
        }

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[] { Manifest.permission.POST_NOTIFICATIONS }, 101);
            }
        }
    }

    public void scrollToLastMessage() {
        if (!messagesArrayList.isEmpty()) {
            msgAdapter.scrollToPosition(messagesArrayList.size() - 1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @NonNull Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            chatController.uploadToFirebaseStorage(fileUri, requestCode);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            // Xóa các thông báo liên quan đến cuộc trò chuyện này
            NotificationManager notificationManager = (NotificationManager) getSystemService(
                    Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.cancelAll();
            }
        } catch (Exception e) {
            Log.e("ChatActivity", "Error in onResume: " + e.getMessage());
        }
    }
}
