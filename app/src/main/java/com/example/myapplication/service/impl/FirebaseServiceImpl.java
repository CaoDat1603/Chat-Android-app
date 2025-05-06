package com.example.myapplication.service.impl;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.controller.LoginController;
import com.example.myapplication.controller.RegisterController;
import com.example.myapplication.model.Users;
import com.example.myapplication.repository.FirebaseRepositor;
import com.example.myapplication.repository.UserRepository;
import com.example.myapplication.service.IFirebaseService;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FirebaseServiceImpl implements IFirebaseService {
    private static final String TAG = "LoginController";
    private final FirebaseRepositor repositor;
    private final UserRepository userRepository;

    public FirebaseServiceImpl() {
        this.repositor = new FirebaseRepositor();
        this.userRepository = new UserRepository();
    }

    //Đăng nhập
    @Override
    public void firebaseAuthWithGoogle(GoogleSignInAccount acct, LoginController.OnLoginCompleteListener listener, Context context) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        // Kiểm tra email có đuôi hợp lệ
        String email = acct.getEmail();
        if (!isValidEmail(email)) {
            listener.onError("Email không hợp lệ! Vui lòng sử dụng email có đuôi @ut.edu.vn.");
            return;
        }

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        repositor.setAuth(credential, (AppCompatActivity) context,  new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "signInWithCredential:success");
                    FirebaseUser user = repositor.getUserCurrent();

                    if (user != null) {
                        checkUser(user, listener);
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

    private void checkUser(FirebaseUser firebaseUser, LoginController.OnLoginCompleteListener listener) {
        String userId = firebaseUser.getUid();
        userRepository.getUser(userId).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                Log.d(TAG, "User already exists.");
                listener.onSuccess(firebaseUser);
            } else {
                Log.w(TAG, "signInWithCredential: failure", task.getException());
                String errorMessage = task.getException() != null ? task.getException().getMessage() : "Lỗi không xác định";
                listener.onError("Authentication Failed: " + errorMessage);
            }
        }).addOnFailureListener(e -> {
            listener.onError("Lỗi khi kiểm tra người dùng: " + e.getMessage());
        });
    }

    //Đăng ký
    @Override
    public void firebaseAuthWithGoogleReg(GoogleSignInAccount acct, RegisterController.OnRegistrationCompleteListener listener, Context context) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        // Kiểm tra email có đuôi hợp lệ
        String email = acct.getEmail();
        if (!isValidEmail(email)) {
            listener.onError("Email không hợp lệ! Vui lòng sử dụng email có đuôi @ut.edu.");
            return;
        }

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        repositor.setAuth(credential, (AppCompatActivity) context, new OnCompleteListener<com.google.firebase.auth.AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<com.google.firebase.auth.AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "signInWithCredential:success");
                    FirebaseUser user = repositor.getUserCurrent();

                    if (user != null) {
                        saveUser(user, listener);
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

    private void saveUser(FirebaseUser firebaseUser, RegisterController.OnRegistrationCompleteListener listener) {
        String userId = firebaseUser.getUid();
        userRepository.getUser(userId).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                Log.d(TAG, "User already exists.");
                listener.onSuccess();
            } else {
                Users userModel = new Users(
                        userId,
                        firebaseUser.getDisplayName(),
                        firebaseUser.getEmail(),
                        "Xin chào!",
                        null
                );

                userRepository.saveUser(userId, userModel, saveTask -> {
                    if (saveTask.isSuccessful()) {
                        listener.onSuccess();
                    } else {
                        listener.onError("Lỗi khi lưu thông tin người dùng!");
                    }
                }).addOnFailureListener(e -> {
                    listener.onError("Lỗi khi kiểm tra người dùng: " + e.getMessage());
                });;
            }
        });
    }

    private boolean isValidEmail(String email) {
        return email != null && email.endsWith("@ut.edu.vn");
    }

    @Override
    public void updateDateOnline() {
        FirebaseUser user = repositor.getUserCurrent();
        if (user == null) return;

        // Lấy thời gian hiện tại dưới dạng String
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                .format(new Date());
        userRepository.updateStatus(user.getUid(), currentTime);
    }
}
