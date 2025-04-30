package com.example.myapplication.controller;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.model.Users;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginController {
    private static final String TAG = "LoginController";
    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private Context context;

    public LoginController(Context context) {
        this.context = context;
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
    }

    // Chỉ đăng nhập, không đăng ký tài khoản mới
    public void firebaseAuthWithGoogle(GoogleSignInAccount acct, OnLoginCompleteListener listener) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        // Kiểm tra email có đuôi hợp lệ
        String email = acct.getEmail();
        if (!isValidEmail(email)) {
            listener.onError("Email không hợp lệ! Vui lòng sử dụng email có đuôi @ut.edu.vn.");
            return;
        }

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener((AppCompatActivity) context, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = auth.getCurrentUser();

                            if (user != null) {
                                checkAndSaveUser(user, listener);
                            } else {
                                listener.onError("Người dùng không tồn tại!");
                            }
                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            listener.onError("Authentication Failed: " + task.getException().getMessage());
                        }
                    }
                });
    }

    private void checkAndSaveUser(FirebaseUser firebaseUser, OnLoginCompleteListener listener) {
        String userId = firebaseUser.getUid();
        DatabaseReference userRef = database.getReference("user").child(userId);

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                Log.d(TAG, "User already exists.");
                listener.onSuccess(firebaseUser); // ✅ TRUYỀN ĐÚNG KIỂU
            } else {
                Log.w(TAG, "signInWithCredential: failure", task.getException());
                String errorMessage = task.getException() != null ? task.getException().getMessage() : "Lỗi không xác định";
                listener.onError("Authentication Failed: " + errorMessage);
            }
        }).addOnFailureListener(e -> {
            listener.onError("Lỗi khi kiểm tra người dùng: " + e.getMessage());
        });
    }

    private boolean isValidEmail(String email) {
        return email != null && email.endsWith("@ut.edu.vn");
    }

    public interface OnLoginCompleteListener {
        void onSuccess(FirebaseUser user); // ✅ GIỮ NGUYÊN
        void onError(String errorMessage);
    }
}
