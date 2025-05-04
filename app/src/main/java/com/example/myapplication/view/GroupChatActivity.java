// File: ChatActivity.java
package com.example.myapplication.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.controller.GroupChatController;
import com.example.myapplication.controller.SettingGroupController;
import com.example.myapplication.model.Group;
import com.example.myapplication.model.msgModel;
import com.example.myapplication.service.IGroupChatService;
import com.example.myapplication.service.impl.GroupChatServiceImpl;
import com.example.myapplication.view.adapter.messagesAdapter;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;

public class GroupChatActivity extends AppCompatActivity {
    private RecyclerView msgAdapter;
    private CardView sendbtn;
    private String groupID, reciverName;
    public String senderUID;
    TextView reciverNameAc;
    private EditText textmsg;
    private ArrayList<msgModel> messagesArrayList;
    private messagesAdapter messagesAdapter;
    private GroupChatController chatController;
    public boolean isChange = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        reciverName = getIntent().getStringExtra("groupName");
        groupID = getIntent().getStringExtra("groupId");
        senderUID = FirebaseAuth.getInstance().getUid();

        reciverNameAc = findViewById(R.id.recivername);
        reciverNameAc.setText(reciverName);

        messagesArrayList = new ArrayList<>();
        msgAdapter = findViewById(R.id.msgadapter);
        msgAdapter.setLayoutManager(new LinearLayoutManager(this));
        messagesAdapter = new messagesAdapter(this, messagesArrayList);
        msgAdapter.setAdapter(messagesAdapter);

        IGroupChatService chatService = new GroupChatServiceImpl(this, groupID, senderUID, messagesArrayList, messagesAdapter);
        chatController = new GroupChatController(chatService);
        chatController.initGroupChat();

        sendbtn = findViewById(R.id.sendbtn);
        textmsg = findViewById(R.id.textmsg);

        sendbtn.setOnClickListener(v -> {
            chatController.sendText(textmsg.getText().toString());
            textmsg.setText("");
        });

        findViewById(R.id.sendImage).setOnClickListener(v -> chatController.chooseImage());
        findViewById(R.id.sendFile).setOnClickListener(v -> chatController.chooseFile());
        findViewById(R.id.settinggroup).setOnClickListener(v -> settingGroup());
        findViewById(R.id.turnback).setOnClickListener(v -> turnBack());
    }
    public void turnBack() {
        Intent intent = new Intent(this, MainActivityGroup.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    void settingGroup() {
        Intent intent = new Intent(GroupChatActivity.this, SettingGroupActivity.class);
        intent.putExtra("groupId", groupID);
        startActivity(intent);
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

    @Override
    protected void onResume() {
        super.onResume();
        // Lấy lại thông tin nhóm sau khi trở lại Activity này
        SettingGroupController settingGroupController = new SettingGroupController(groupID);
        settingGroupController.getGroupInfo(new SettingGroupController.OnGroupInfoListener() {
            @Override
            public void onSuccess(Group group) {
                // Cập nhật tên nhóm vào UI
                if (!reciverName.equals(group.getGroupName())) {
                    TextView groupNameTextView = findViewById(R.id.recivername);
                    groupNameTextView.setText(group.getGroupName());
                    isChange = true;
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                // Xử lý lỗi nếu cần
                Toast.makeText(GroupChatActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}