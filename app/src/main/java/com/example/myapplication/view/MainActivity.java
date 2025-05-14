package com.example.myapplication.view;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.controller.MainActivityController;
import com.example.myapplication.model.Users;
import com.example.myapplication.service.IUserService;
import com.example.myapplication.service.impl.GetLatestMessage;
import com.example.myapplication.service.impl.UserServiceImpl;
import com.example.myapplication.view.adapter.UserAdapter;
import com.example.myapplication.view.VerificationActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MainActivityController controller;
    private UserAdapter userAdapter;
    private ArrayList<Users> userList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ImageView imgLogout, imgSettingProfile, chatGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.mainUserRecyclerView);
        userAdapter = new UserAdapter(this, userList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(userAdapter);

        // Khởi tạo Service và Controller
        IUserService userService = new UserServiceImpl(this);
        GetLatestMessage getLatestMessage = new GetLatestMessage();
        controller = new MainActivityController(userService);

        // Bắt đầu xử lý khi mở app
        controller.handleAppStart();

        // Gán nút logout và bắt sự kiện
        imgLogout = findViewById(R.id.logoutimg);
        imgLogout.setOnClickListener(view -> showLogoutDialog());

        // Xử lý sự kiện khi nhấn vào chat nhóm
        chatGroup = findViewById(R.id.chatGroup);
        chatGroup.setOnClickListener(view -> navigateToChatGroup());

        imgSettingProfile = findViewById(R.id.setting_profile);
        imgSettingProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigateToProfile();
            }
        });
    }

    public void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void updateUserList(List<Users> usersList) {
        userList.clear();
        userList.addAll(usersList);
        userAdapter.notifyDataSetChanged();
    }

    public void showLogoutDialog() {
        Dialog dialog = new Dialog(this, R.style.dialoge);
        dialog.setContentView(R.layout.dialog_layout);
        Button no = dialog.findViewById(R.id.nobnt);
        Button yes = dialog.findViewById(R.id.yesbnt);

        yes.setOnClickListener(v -> {
            controller.onUserSignOut();
        });

        no.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    public void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public void navigateToChatGroup() {
        Intent intent = new Intent(this, MainActivityGroup.class);
        startActivity(intent);
    }

    public void navigateToProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }
}
