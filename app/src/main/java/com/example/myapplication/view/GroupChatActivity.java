package com.example.myapplication.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.controller.GroupChatController;
import com.example.myapplication.model.msgModel;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.view.adapter.messagesAdapter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class GroupChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText messageInput;
    private ImageButton sendFileButton, sendImageButton;
    private CardView sendButton;
    private String groupId, reciverName;
    public String senderUID;
    private ArrayList<msgModel> messagesArrayList;
    private messagesAdapter messagesAdapter;
    private FirebaseDatabase database;
    private GroupChatController groupChatController;
    FirebaseAuth firebaseAuth;
    TextView reciverNameAc;

    //private TextView groupNameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        // Lấy thông tin từ Intent
        Intent intent = getIntent();
        reciverName = getIntent().getStringExtra("groupName");
        groupId = intent.getStringExtra("groupId");
        firebaseAuth = FirebaseAuth.getInstance();
        senderUID = firebaseAuth.getUid();

        reciverNameAc = findViewById(R.id.recivername);
        reciverNameAc.setText(reciverName);

        // Khởi tạo Firebase và các thành phần
        database = FirebaseDatabase.getInstance();
        messagesArrayList = new ArrayList<>();
        messagesAdapter = new messagesAdapter(this, messagesArrayList);

        // Ánh xạ các thành phần trong layout XML
        recyclerView = findViewById(R.id.msgadapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messagesAdapter);

        messageInput = findViewById(R.id.textmsg);
        sendFileButton = findViewById(R.id.sendFile);
        sendImageButton = findViewById(R.id.sendImage);
        sendButton = findViewById(R.id.sendbtn);
        ImageButton turnback = findViewById(R.id.turnback);
        //groupNameTextView = findViewById(R.id.groupNameTextView);

        // Hiển thị tên nhóm
        //groupNameTextView.setText("Group: " + groupId);

        // Khởi tạo GroupChatController
        groupChatController = new GroupChatController(this, database, groupId, messagesArrayList, messagesAdapter);

        // Xử lý sự kiện nút gửi tin nhắn
        sendButton.setOnClickListener(v -> {
            String message = messageInput.getText().toString();
            if (!message.isEmpty()) {
                groupChatController.sendMessage(message, senderUID);  // Gửi tin nhắn qua GroupChatController
                messageInput.setText(""); // Xóa nội dung tin nhắn sau khi gửi
            } else {
                Toast.makeText(GroupChatActivity.this, "Please enter a message", Toast.LENGTH_SHORT).show();
            }
        });

        // Xử lý sự kiện nút gửi file
        sendFileButton.setOnClickListener(v -> {
            // Thực hiện chức năng gửi file
            groupChatController.selectFile();  // Gọi phương thức để chọn file
        });

        // Xử lý sự kiện nút gửi hình ảnh
        sendImageButton.setOnClickListener(v -> {
            // Thực hiện chức năng gửi hình ảnh
            groupChatController.selectImage();  // Gọi phương thức để chọn ảnh
        });

        // Turn back
        turnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // Kiểm tra quyền truy cập bộ nhớ
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Khởi tạo lại chat khi Activity bắt đầu
        groupChatController.initializeGroupChat();  // Đảm bảo gửi tin nhắn khi activity khởi động lại
    }

    // Phương thức này sẽ được gọi từ GroupChatController để gửi tin nhắn lên UI
    public void sendMessageToUI(msgModel message) {
        messagesArrayList.add(message);
        messagesAdapter.notifyItemInserted(messagesArrayList.size() - 1);
    }

    // Phương thức này sẽ được gọi từ GroupChatController để tải các tin nhắn từ Firebase
    public void loadMessages(ArrayList<msgModel> messages) {
        messagesArrayList.clear();
        messagesArrayList.addAll(messages);
        messagesAdapter.notifyDataSetChanged();
    }

    // Phương thức cuộn tới tin nhắn mới nhất
    public void scrollToLastMessage() {
        recyclerView.scrollToPosition(messagesArrayList.size() - 1);
    }

    // Xử lý kết quả khi chọn hình ảnh hoặc file từ bộ nhớ
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @NonNull Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            if (fileUri != null) {
                groupChatController.uploadToFirebaseStorage(fileUri, requestCode);  // Gửi file hoặc hình ảnh lên Firebase
            }
        }
    }
}