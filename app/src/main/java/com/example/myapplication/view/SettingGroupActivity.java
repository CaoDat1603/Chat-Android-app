package com.example.myapplication.view;

import android.content.Intent;
import android.graphics.Insets;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.R;
import com.example.myapplication.controller.SettingGroupController;
import com.example.myapplication.model.Group;
import com.google.firebase.auth.FirebaseAuth;

public class SettingGroupActivity extends AppCompatActivity {
    private TextView groupNameTextView;
    private EditText groupNameEditText;
    private ImageButton editNameButton;
    View managemMamber;
    FirebaseAuth firebaseAuth;
    public String userId;

    private SettingGroupController controller;
    public String groupId;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_group);
        EdgeToEdge.enable(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars()).toPlatformInsets();
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Ánh xạ các view
        groupNameTextView = findViewById(R.id.recivername);
        groupNameEditText = findViewById(R.id.edit_group_name);
        editNameButton = findViewById(R.id.editname);
        managemMamber = findViewById(R.id.nextgrmember);
        ImageButton turnback = findViewById(R.id.turnback);
        View outGroup = findViewById(R.id.outgroup);
        View deleteGroup = findViewById(R.id.deletegroup);

        groupId = getIntent().getStringExtra("groupId");
        if (groupId == null) {
            Log.e("SettingGroupActivity", "groupId is null!");
            return;
        }

        firebaseAuth = FirebaseAuth.getInstance();
        userId = firebaseAuth.getUid();

        // Khởi tạo controller
        controller = new SettingGroupController(this);

        // Lấy thông tin nhóm từ Firebase
        getGroupInfo();

        // Sự kiện khi nhấn nút sửa tên
        editNameButton.setOnClickListener(v -> {
            if (groupNameEditText.getVisibility() == View.GONE) {
                // Hiển thị EditText để nhập tên
                groupNameEditText.setText(groupNameTextView.getText().toString());
                groupNameEditText.setVisibility(View.VISIBLE);
                groupNameTextView.setVisibility(View.GONE);
            } else {
                // Lưu tên mới qua Controller
                String newGroupName = groupNameEditText.getText().toString().trim();
                if (!TextUtils.isEmpty(newGroupName)) {
                    updateGroupName(newGroupName);
                } else {
                    Toast.makeText(this, "Tên nhóm không được để trống!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Turn back
        turnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // Next Activity Manage Member
        managemMamber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextManageMemberActivity();
            }
        });

        // Out Group
        outGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controller.outGroup(userId, groupId);
            }
        });

        // Out Group
        deleteGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controller.deleteGroup(userId, groupId);
            }
        });
    }

    // Lấy thông tin nhóm từ Controller và hiển thị lên UI
    private void getGroupInfo() {
        controller.getGroupInfo(new SettingGroupController.OnGroupInfoListener() {
            @Override
            public void onSuccess(Group group) {
                groupNameTextView.setText(group.getGroupName());
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(SettingGroupActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void nextManageMemberActivity() {
        Intent intent = new Intent(SettingGroupActivity.this, ManageMemberActivity.class);
        intent.putExtra("groupId", groupId);
        startActivity(intent);
    }

    public void turnMain() {
        Intent intent = new Intent(SettingGroupActivity.this, MainActivityGroup.class);
        startActivity(intent);
    }

    // Gọi Controller để cập nhật tên nhóm
    private void updateGroupName(String newGroupName) {
        controller.updateGroupName(newGroupName, new SettingGroupController.OnGroupUpdateListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(SettingGroupActivity.this, "Cập nhật tên nhóm thành công!", Toast.LENGTH_SHORT).show();
                groupNameTextView.setText(newGroupName);
                groupNameEditText.setVisibility(View.GONE);
                groupNameTextView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(SettingGroupActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public String getGroupId() {
        return groupId;
    }
}