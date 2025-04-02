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

    public void firebaseAuthWithGoogle(GoogleSignInAccount acct, OnRegistrationCompleteListener listener) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        // Kiểm tra email có đuôi hợp lệ
        String email = acct.getEmail();
        if (!isValidEmail(email)) {
            listener.onError("Email không hợp lệ! Vui lòng sử dụng email có đuôi @ut.edu.");
            return;
        }

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener((AppCompatActivity) context, new OnCompleteListener<com.google.firebase.auth.AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<com.google.firebase.auth.AuthResult> task) {
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

    private boolean isValidEmail(String email) {
        return email != null && email.endsWith("@ut.edu.vn");
    }

    private void checkAndSaveUser(FirebaseUser firebaseUser, OnRegistrationCompleteListener listener) {
        String userId = firebaseUser.getUid();
        DatabaseReference userRef = database.getReference("user").child(userId);

        userRef.get().addOnCompleteListener(task -> {
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

    public interface OnRegistrationCompleteListener {
        void onSuccess();
        void onError(String errorMessage);
    }
}
