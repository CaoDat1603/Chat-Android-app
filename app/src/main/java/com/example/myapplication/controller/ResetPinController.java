package com.example.myapplication.controller;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.service.IEmailService;
import com.example.myapplication.service.IFirebaseService;
import com.example.myapplication.service.impl.FirebaseServiceImpl;
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
    private IFirebaseService service;

    // Constructor khởi tạo FirebaseAuth và FirebaseDatabase
    public ResetPinController(Context context) {
        this.context = context;
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        service = new FirebaseServiceImpl();
    }

    // Xử lý đăng nhập bằng Google và gửi mã xác thực
    public void firebaseAuthWithGoogle(GoogleSignInAccount acct, OnPinResetListener listener) {
        service.firebaseAuthWithGoogleVef(acct, listener, context);
    }

    public void verifyCode(String email, String code, OnVerificationListener listener) {
        service.verifyCode(email, code, listener, context);
    }

    // Xác thực mã và cho phép đặt PIN mới
    public void verifyCodeAndResetPin(String email, String code, String newPin, OnPinResetListener listener) {
        service.verifyCodeAndResetPin(email, code, newPin, listener, context);
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

    /**
     * Interface lắng nghe kết quả xác thực mã
     */
    public interface OnVerificationListener {
        void onCodeSent(String message);

        void onVerified(String message);

        void onError(String errorMessage);
    }
}