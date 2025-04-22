package com.example.myapplication.controller;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

public class ResetPinController {
    private static final String TAG = "ResetPinController";
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private Context context;

    // Constructor khởi tạo FirebaseAuth và FirebaseDatabase
    public ResetPinController(Context context) {
        this.context = context;
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
    }

    // Xử lý đăng nhập bằng Google và reset PIN
    public void firebaseAuthWithGoogle(GoogleSignInAccount acct, OnPinResetListener listener) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        auth.signInWithCredential(credential)
                .addOnCompleteListener((AppCompatActivity) context, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = auth.getCurrentUser();

                            if (user != null) {
                                // Reset PIN sau khi đăng nhập thành công
                                resetPin(listener);
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

    // Phương thức để reset mã PIN về null
    public void resetPin(OnPinResetListener listener) {
        FirebaseUser firebaseUser = auth.getCurrentUser();

        if (firebaseUser == null) {
            listener.onError("Người dùng không tồn tại hoặc chưa đăng nhập!");
            return;
        }

        String userId = firebaseUser.getUid();
        DatabaseReference userRef = database.getReference("user").child(userId);

        userRef.child("PIN").setValue(null).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                listener.onSuccess("Mã PIN đã được reset thành công!");
            } else {
                listener.onError("Không thể reset mã PIN: " + task.getException().getMessage());
            }
        });
    }

    // Interface xử lý kết quả reset PIN
    public interface OnPinResetListener {
        void onSuccess(String message);
        void onError(String error);
    }
}