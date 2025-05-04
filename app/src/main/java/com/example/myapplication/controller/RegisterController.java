package com.example.myapplication.controller;

import android.content.Context;

import com.example.myapplication.service.IFirebaseService;
import com.example.myapplication.service.impl.FirebaseServiceImpl;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

public class RegisterController {
    private final IFirebaseService service;
    private Context context;

    public RegisterController(Context context) {
        this.service = new FirebaseServiceImpl();
        this.context = context;
    }

    public void firebaseAuthWithGoogle(GoogleSignInAccount acct, OnRegistrationCompleteListener listener) {
        service.firebaseAuthWithGoogleReg(acct, listener, context);
    }

    public interface OnRegistrationCompleteListener {
        void onSuccess();
        void onError(String errorMessage);
    }
}