package com.example.myapplication.controller;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.service.IEmailService;
import com.example.myapplication.service.impl.EmailServiceImpl;
import com.example.myapplication.util.VerificationUtil;
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

import java.util.Random;

public class ResetPinController {
    private static final String TAG = "ResetPinController";
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private Context context;
    private IEmailService emailService;

    // Constructor khởi tạo FirebaseAuth và FirebaseDatabase
    public ResetPinController(Context context) {
        this.context = context;
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        emailService = new EmailServiceImpl();
    }

    // Xử lý đăng nhập bằng Google và gửi mã xác thực
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
                                // Gửi mã xác thực qua email
                                sendVerificationCode(user.getEmail(), listener);
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

    // Gửi mã xác thực đến email
    private void sendVerificationCode(String email, OnPinResetListener listener) {
        // Tạo mã xác thực và lưu vào SharedPreferences
        String verificationCode = VerificationUtil.generateVerificationCode(context, email);

        // Gửi mã xác thực qua email
        emailService.sendVerificationCode(email, verificationCode, new IEmailService.OnEmailSendListener() {
            @Override
            public void onSuccess(String message) {
                listener.onCodeSent("Mã xác nhận đã được gửi đến email " + email);
            }

            @Override
            public void onError(String errorMessage) {
                listener.onError("Không thể gửi mã xác nhận: " + errorMessage);
            }
        });
    }

    // Xác thực mã và cho phép đặt PIN mới
    public void verifyCodeAndResetPin(String email, String code, String newPin, OnPinResetListener listener) {
        // Kiểm tra mã xác thực
        boolean isValid = VerificationUtil.verifyCode(context, code, email);

        if (isValid) {
            // Mã xác thực hợp lệ, tiến hành cập nhật PIN
            FirebaseUser user = auth.getCurrentUser();
            if (user != null) {
                String userId = user.getUid();
                DatabaseReference userRef = database.getReference("user").child(userId);

                userRef.child("PIN").setValue(newPin).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listener.onSuccess("Mã PIN mới đã được thiết lập thành công!");
                    } else {
                        listener.onError("Không thể cập nhật mã PIN: " + task.getException().getMessage());
                    }
                });
            } else {
                listener.onError("Người dùng không tồn tại hoặc chưa đăng nhập!");
            }
        } else {
            listener.onError("Mã xác nhận không hợp lệ hoặc đã hết hạn!");
        }
    }

    // Tạo số ngẫu nhiên 6 chữ số cho PIN
    public static String generateRandomPin() {
        Random random = new Random();
        int pin = 100000 + random.nextInt(900000); // Pin có 6 chữ số
        return String.valueOf(pin);
    }

    // Interface xử lý kết quả reset PIN
    public interface OnPinResetListener {
        void onCodeSent(String message);

        void onSuccess(String message);

        void onError(String error);
    }
}