package com.example.myapplication.view;
import com.example.myapplication.controller.RegisterController;


import com.example.myapplication.R;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterActivity extends AppCompatActivity {
    TextView loginbut;
    EditText rg_fullname, rg_email, rg_password, rg_repassword;
    Button rg_signup;
    RegisterController registerController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        registerController = new RegisterController(this);

        // Khai báo các thành phần giao diện
        loginbut = findViewById(R.id.textViewLogin);
        rg_fullname = findViewById(R.id.editTextFullName);
        rg_email = findViewById(R.id.editTextEmailAddress);
        rg_password = findViewById(R.id.editTextPassword);
        rg_repassword = findViewById(R.id.editReEnterTextPassword);
        rg_signup = findViewById(R.id.signupbutton);

        // Xử lý sự kiện click vào nút đăng nhập
        loginbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Xử lý sự kiện click vào nút đăng ký
        rg_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fullname = rg_fullname.getText().toString().trim();
                String email = rg_email.getText().toString().trim();
                String password = rg_password.getText().toString().trim();
                String cPassword = rg_repassword.getText().toString().trim();

                registerController.registerUser(fullname, email, password, cPassword, new RegisterController.OnRegistrationCompleteListener() {
                    @Override
                    public void onSuccess() {
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
