package com.example.myapplication.controller;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.model.Users;
import com.example.myapplication.service.FirebaseService;
import com.example.myapplication.service.impl.FirebaseServiceImpl;
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
    private final FirebaseService service;
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