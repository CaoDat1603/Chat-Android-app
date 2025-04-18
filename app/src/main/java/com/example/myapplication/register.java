package com.example.myapplication;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class register extends AppCompatActivity {
    TextView loginbut;
    EditText rg_fullname, rg_email, rg_password, rg_repassword;
    Button rg_signup;
    FirebaseAuth auth;
    FirebaseDatabase database;

    // Mẫu kiểm tra địa chỉ email
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        // Khởi tạo Firebase
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

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
                Intent intent = new Intent(register.this, login.class);
                startActivity(intent);
                finish();
            }
        });

        // Xử lý sự kiện click vào nút đăng ký
        rg_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    private void registerUser() {
        String fullname = rg_fullname.getText().toString().trim();
        String email = rg_email.getText().toString().trim();
        String password = rg_password.getText().toString().trim();
        String cPassword = rg_repassword.getText().toString().trim();

        // Kiểm tra các trường nhập liệu
        if (TextUtils.isEmpty(fullname)) {
            rg_fullname.setError("Xin hãy điền tên đầy đủ!");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            rg_email.setError("Xin hãy điền địa chỉ email!");
            return;
        }
        if (!email.matches(emailPattern)) {
            rg_email.setError("Địa chỉ email không hợp lệ!");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            rg_password.setError("Xin hãy điền mật khẩu!");
            return;
        }
        if (password.length() < 8) {
            rg_password.setError("Mật khẩu phải có ít nhất 8 ký tự!");
            return;
        }
        if (!password.equals(cPassword)) {
            rg_repassword.setError("Mật khẩu không khớp!");
            return;
        }

        // Đăng ký người dùng với Firebase Auth
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Lưu thông tin người dùng vào Firebase Database
                            String userId = task.getResult().getUser().getUid();
                            DatabaseReference reference = database.getReference("user").child(userId);

                            // Tạo đối tượng người dùng
                            String status = "Xin Chào Các Con Dợ Xinh Yêu.";
                            Users user = new Users(userId, fullname, email, password, status);

                            // Lưu vào Firebase Database
                            reference.setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        // Chuyển đến màn hình đăng nhập sau khi đăng ký thành công
                                        Intent intent = new Intent(register.this, login.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(register.this, "Lỗi khi lưu thông tin người dùng!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(register.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}