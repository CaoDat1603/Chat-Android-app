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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterController {
    private static final String TAG = "RegisterController";
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private Context context;

    public RegisterController(Context context) {
        this.context = context;
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
    }

    // Đăng nhập hoặc đăng ký bằng Google
    public void firebaseAuthWithGoogle(GoogleSignInAccount acct, OnRegistrationCompleteListener listener) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        auth.signInWithCredential(credential).addOnCompleteListener((AppCompatActivity) context, new OnCompleteListener<com.google.firebase.auth.AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<com.google.firebase.auth.AuthResult> task) {
                if (task.isSuccessful()) {
                    // Đăng nhập thành công
                    Log.d(TAG, "signInWithCredential:success");
                    FirebaseUser user = auth.getCurrentUser();

                    if (user != null) {
                        checkAndSaveUser(user, listener);
                    } else {
                        listener.onError("Người dùng không tồn tại!");
                    }
                } else {
                    // Đăng nhập thất bại
                    Log.w(TAG, "signInWithCredential:failure", task.getException());
                    listener.onError("Authentication Failed: " + task.getException().getMessage());
                }
            }
        });
    }

    // Kiểm tra và lưu thông tin người dùng
    private void checkAndSaveUser(FirebaseUser firebaseUser, OnRegistrationCompleteListener listener) {
        String userId = firebaseUser.getUid();
        DatabaseReference userRef = database.getReference("user").child(userId);

        // Kiểm tra xem người dùng đã tồn tại hay chưa
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                // Người dùng đã tồn tại
                Log.d(TAG, "User already exists.");
                listener.onSuccess();
            } else {
                // Người dùng chưa tồn tại, lưu thông tin mới
                Users userModel = new Users(
                        userId,
                        firebaseUser.getDisplayName(),
                        firebaseUser.getEmail(),
                        "Xin chào!",
                        null
                );

                userRef.setValue(userModel).addOnCompleteListener(saveTask -> {
                    if (saveTask.isSuccessful()) {
                        listener.onSuccess();
                    } else {
                        listener.onError("Lỗi khi lưu thông tin người dùng!");
                    }
                });
            }
        }).addOnFailureListener(e -> {
            listener.onError("Lỗi khi kiểm tra người dùng: " + e.getMessage());
        });
    }

    // Interface để thông báo kết quả đăng ký hoặc đăng nhập
    public interface OnRegistrationCompleteListener {
        void onSuccess();
        void onError(String errorMessage);
    }
}