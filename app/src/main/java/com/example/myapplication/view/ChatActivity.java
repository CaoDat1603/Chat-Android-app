// File: ChatActivity.java
package com.example.myapplication.view;

import android.content.Intent;
import android.graphics.Insets;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import com.example.myapplication.R;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.controller.ChatController;
import com.example.myapplication.model.msgModel;
import com.example.myapplication.service.IChatService;
import com.example.myapplication.service.impl.ChatServiceImpl;
import com.example.myapplication.view.adapter.messagesAdapter;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {
    public String reciverUID, reciverName, SenderUID;
    private EditText textmsg;
    TextView reciverNameAc;
    CardView sendbtn;
    private RecyclerView msgAdapter;
    private ArrayList<msgModel> messagesArrayList;
    private messagesAdapter messagesAdapter;
    private ChatController chatController;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        EdgeToEdge.enable(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars()).toPlatformInsets();
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime()).toPlatformInsets();
            int bottom = Math.max(systemBars.bottom, imeInsets.bottom);
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, bottom);
            return insets;
        });


        reciverName = getIntent().getStringExtra("name");
        reciverUID = getIntent().getStringExtra("uid");
        SenderUID = FirebaseAuth.getInstance().getUid();

        messagesArrayList = new ArrayList<>();
        msgAdapter = findViewById(R.id.msgadapter);
        msgAdapter.setLayoutManager(new LinearLayoutManager(this));
        messagesAdapter = new messagesAdapter(this, messagesArrayList);
        msgAdapter.setAdapter(messagesAdapter);

        String senderRoom = SenderUID + reciverUID;
        String receiverRoom = reciverUID + SenderUID;

        IChatService IChatService = new ChatServiceImpl(this, senderRoom, receiverRoom, SenderUID, messagesArrayList, messagesAdapter);
        chatController = new ChatController(IChatService);
        chatController.initChat();

        sendbtn = findViewById(R.id.sendbtn);
        textmsg = findViewById(R.id.textmsg);

        sendbtn.setOnClickListener(v -> {
            chatController.sendText(textmsg.getText().toString());
            textmsg.setText("");
        });

        reciverNameAc = findViewById(R.id.recivername);
        reciverNameAc.setText(reciverName);

        findViewById(R.id.sendImage).setOnClickListener(v -> chatController.chooseImage());
        findViewById(R.id.sendFile).setOnClickListener(v -> chatController.chooseFile());
        findViewById(R.id.turnback).setOnClickListener(v -> finish());
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
            chatController.uploadFile(fileUri, requestCode);
        }
    }
}