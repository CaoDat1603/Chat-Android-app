package com.example.myapplication.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.Group;
import com.example.myapplication.controller.MainActivityGroupController;
import com.example.myapplication.view.adapter.GroupAdapter;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivityGroup extends AppCompatActivity {

    private RecyclerView recyclerView;
    private GroupAdapter adapter;
    private ArrayList<Group> groupArrayList;
    private ImageView imglogout;
    private MainActivityGroupController controller;
    private ImageView imgSettingProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_group);

        recyclerView = findViewById(R.id.groupChatText);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        groupArrayList = new ArrayList<>();
        adapter = new GroupAdapter(this, groupArrayList);
        recyclerView.setAdapter(adapter);

        controller = new MainActivityGroupController(this); // Khởi tạo controller

        // Kiểm tra trạng thái đăng nhập
        controller.checkUserLoginStatus();

        // Xử lý sự kiện đăng xuất
        imglogout = findViewById(R.id.logoutimg);
        imglogout.setOnClickListener(view -> controller.showLogoutDialog());

        imgSettingProfile = findViewById(R.id.setting_profile);
        imgSettingProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controller.navigateToProfile();
            }
        });

        ImageView chatOneOne = findViewById(R.id.chatOneOne);
        chatOneOne.setOnClickListener(view -> controller.navigateToMainActivity());

        ImageView createGroup = findViewById(R.id.createGroup);
        createGroup.setOnClickListener(view -> controller.navigateToCreateGroupActivity());

        updateFCMToken();
    }

    private void updateFCMToken() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(new OnCompleteListener<String>() {
                        @Override
                        public void onComplete(@NonNull Task<String> task) {
                            if (!task.isSuccessful()) {
                                showMessage("Không thể cập nhật trạng thái thông báo");
                                return;
                            }

                            String token = task.getResult();

                            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            DatabaseReference userRef = FirebaseDatabase.getInstance()
                                    .getReference("user")
                                    .child(userId);
                            userRef.child("fcmToken").setValue(token);
                        }
                    });
        }
    }

    // Phương thức thêm nhóm vào danh sách
    public void addGroupToList(Group group) {
        groupArrayList.add(group);
        adapter.notifyDataSetChanged();
    }

    // Phương thức hiển thị thông báo
    public void showMessage(String message) {
        Toast.makeText(MainActivityGroup.this, message, Toast.LENGTH_SHORT).show();
    }

    // Phương thức chuyển đến màn hình đăng nhập
    public void navigateToLogin() {
        Intent intent = new Intent(MainActivityGroup.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    // Phương thức chuyển đến màn hình MainActivity
    public void navigateToMainActivity() {
        Intent intent = new Intent(MainActivityGroup.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    // Phương thức chuyển đến màn hình tạo nhóm
    public void navigateToCreateGroupActivity() {
        Intent intent = new Intent(MainActivityGroup.this, CreateGroupActivity.class);
        startActivity(intent);
    }

    public void navigateToProfile() {
        Intent intent = new Intent(MainActivityGroup.this, ProfileActivity.class);
        startActivity(intent);
        finish();
    }
}
