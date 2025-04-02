package com.example.myapplication.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;
import com.example.myapplication.controller.LoginController;
import com.example.myapplication.controller.ProfileController;

public class ProfileActivity extends AppCompatActivity {

    private TextView displayName;
    private EditText editName;
    private ImageButton btnEdit;
    private ProfileController profileController;
    private ImageButton buttonExit;
    private Button button_reset_PIN;private Button button_delete;
    private Button deleteAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Ánh xạ view
        displayName = findViewById(R.id.display_name);
        editName = findViewById(R.id.edit_name);
        btnEdit = findViewById(R.id.btn_edit);
        buttonExit = findViewById(R.id.buttonQuit);
        button_reset_PIN = findViewById(R.id.button_change_pin);
        deleteAccount = findViewById(R.id.button_delete_account);

        // Khởi tạo controller
        profileController = new ProfileController(this);

        // Load tên người dùng từ Firebase
        profileController.fetchUserName(userName -> {
            if (userName != null) {
                displayName.setText(userName);
            } else {
                displayName.setText("No Name Found");
            }
        });

        // Xử lý sự kiện click nút chỉnh sửa
        btnEdit.setOnClickListener(v -> {
            if (editName.getVisibility() == View.GONE) {
                // Hiện EditText để chỉnh sửa
                editName.setText(displayName.getText());
                editName.setVisibility(View.VISIBLE);
                displayName.setVisibility(View.GONE);
                btnEdit.setImageResource(R.drawable.bordermain);
                btnEdit.setImageResource(R.drawable.check_icon); // Thay đổi icon nút thành "Check"
            } else {
                // Lưu tên người dùng
                String newName = editName.getText().toString().trim();
                if (!newName.isEmpty()) {
                    profileController.updateUserName(newName, task -> {
                        if (task.isSuccessful()) { // Kiểm tra kết quả
                            displayName.setText(newName);
                            Toast.makeText(ProfileActivity.this, "Name updated successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ProfileActivity.this, "Failed to update name", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(ProfileActivity.this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                }
                // Ẩn EditText, hiện TextView
                editName.setVisibility(View.GONE);
                displayName.setVisibility(View.VISIBLE);
                btnEdit.setImageResource(R.drawable.edit_pen); // Đổi lại icon thành "Edit"
            }

        });
        buttonExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                profileController.navigateToMainActivity();
            }
        });
        button_reset_PIN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                profileController.navigateToResetPinActivity();
            }
        });
        deleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                profileController.deleteAccount();
            }
        });
    }
    public void navigateToMainActivity(){
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void navigateToResetPinActivity(){
        Intent intent = new Intent(ProfileActivity.this, ResetPinActivity.class);
        startActivity(intent);
    }

    public void navigateToLogin() {
        Intent intent = new Intent(ProfileActivity.this, LoginController.class);
        startActivity(intent);
        finish();
    }
}
