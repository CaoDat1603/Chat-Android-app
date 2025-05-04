package com.example.myapplication.controller;

import android.content.Context;

import com.example.myapplication.service.IFirebaseService;
import com.example.myapplication.service.impl.FirebaseServiceImpl;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseUser;

public class LoginController {
    private final IFirebaseService service;
    private Context context;

    public LoginController(Context context) {
        this.service = new FirebaseServiceImpl();
        this.context = context;
    }

    // Chỉ đăng nhập, không đăng ký tài khoản mới
    public void firebaseAuthWithGoogle(GoogleSignInAccount acct, OnLoginCompleteListener listener) {
        service.firebaseAuthWithGoogle(acct, listener, context);
    }

    public interface OnLoginCompleteListener {
        void onSuccess(FirebaseUser user); // ✅ GIỮ NGUYÊN
        void onError(String errorMessage);
    }
}
