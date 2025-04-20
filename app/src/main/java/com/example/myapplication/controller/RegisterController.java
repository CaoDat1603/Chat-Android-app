package com.example.myapplication.controller;
import com.example.myapplication.Users;
import android.content.Context;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterController {
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private Context context;

    public RegisterController(Context context) {
        this.context = context;
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
    }

    public void registerUser(String fullname, String email, String password, String cPassword, OnRegistrationCompleteListener listener) {
        // Kiểm tra các điều kiện
        if (fullname.isEmpty()) {
            listener.onError("Xin hãy điền tên đầy đủ!");
            return;
        }
        if (email.isEmpty()) {
            listener.onError("Xin hãy điền địa chỉ email!");
            return;
        }
        if (password.isEmpty()) {
            listener.onError("Xin hãy điền mật khẩu!");
            return;
        }
        if (password.length() < 8) {
            listener.onError("Mật khẩu phải có ít nhất 8 ký tự!");
            return;
        }
        if (!password.equals(cPassword)) {
            listener.onError("Mật khẩu không khớp!");
            return;
        }

        // Đăng ký người dùng với Firebase Auth
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String userId = task.getResult().getUser().getUid();
                            DatabaseReference reference = database.getReference("user").child(userId);
                            Users user = new Users(userId, fullname, email, password, "Xin Chào Các Con Dợ Xinh Yêu.");

                            reference.setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        listener.onSuccess();
                                    } else {
                                        listener.onError("Lỗi khi lưu thông tin người dùng!");
                                    }
                                }
                            });
                        } else {
                            listener.onError(task.getException().getMessage());
                        }
                    }
                });
    }

    public interface OnRegistrationCompleteListener {
        void onSuccess();
        void onError(String errorMessage);
    }
}
