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
import com.example.myapplication.controller.ProfileController;

public class ProfileActivity extends AppCompatActivity {

    private TextView displayName;
    private EditText editName;
    private ImageButton btnEdit, buttonExit;
    private Button button_reset_PIN, deleteAccount;
    private ProfileController profileController;

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

        // Lấy tên người dùng
        profileController.fetchUserName(name -> {
            if (name != null) {
                displayName.setText(name);
            } else {
                displayName.setText("No Name Found");
            }
        });

        btnEdit.setOnClickListener(v -> {
            if (editName.getVisibility() == View.GONE) {
                editName.setText(displayName.getText());
                editName.setVisibility(View.VISIBLE);
                displayName.setVisibility(View.GONE);
                btnEdit.setImageResource(R.drawable.check_icon);
            } else {
                String newName = editName.getText().toString().trim();
                if (!newName.isEmpty()) {
                    profileController.updateUserName(newName, task -> {
                        if (task.isSuccessful()) {
                            displayName.setText(newName);
                            Toast.makeText(this, "Name updated successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Failed to update name", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                }
                editName.setVisibility(View.GONE);
                displayName.setVisibility(View.VISIBLE);
                btnEdit.setImageResource(R.drawable.edit_pen);
            }
        });

        buttonExit.setOnClickListener(v -> navigateToMainActivity());
        button_reset_PIN.setOnClickListener(v -> navigateToResetPinActivity());
        deleteAccount.setOnClickListener(v -> profileController.deleteAccount());
    }

    public void navigateToMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    public void navigateToResetPinActivity() {
        startActivity(new Intent(this, ResetPinActivity.class));
    }

    public void navigateToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
