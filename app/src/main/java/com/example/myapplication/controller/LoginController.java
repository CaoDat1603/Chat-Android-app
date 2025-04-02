package com.example.myapplication.controller;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginController {
    private static final String TAG = "LoginController";
    private FirebaseAuth auth;
    private Context context;

    public LoginController(Context context) {
        this.context = context;
        auth = FirebaseAuth.getInstance();
    }

    // Xử lý đăng nhập bằng Google
    public void firebaseAuthWithGoogle(GoogleSignInAccount acct, OnLoginCompleteListener listener) {
        Log.d(TAG, "firebaseAuthWithGoogle: " + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener((AppCompatActivity) context, task -> {
                    if (task.isSuccessful()) {
                        // Đăng nhập thành công
                        Log.d(TAG, "signInWithCredential: success");
                        FirebaseUser user = auth.getCurrentUser();

                        if (user != null) {
                            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                                // Người dùng hợp lệ
                                listener.onSuccess(user);
                            } else {
                                // Trường hợp email null hoặc rỗng
                                listener.onError("Người dùng không có email hợp lệ. Vui lòng thử lại với tài khoản khác.");
                            }
                        } else {
                            listener.onError("Người dùng không tồn tại!");
                        }
                    } else {
                        // Đăng nhập thất bại
                        Log.w(TAG, "signInWithCredential: failure", task.getException());
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Lỗi không xác định";
                        listener.onError("Authentication Failed: " + errorMessage);
                    }
                });
    }


    // Interface thông báo kết quả đăng nhập
    public interface OnLoginCompleteListener {
        void onSuccess(FirebaseUser user); // Trả về người dùng đã đăng nhập thành công
        void onError(String errorMessage);
    }
}
