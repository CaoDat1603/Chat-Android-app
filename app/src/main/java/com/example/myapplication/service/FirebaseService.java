package com.example.myapplication.service;

import android.content.Context;

import com.example.myapplication.controller.LoginController;
import com.example.myapplication.controller.RegisterController;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseUser;

public interface FirebaseService {
    void firebaseAuthWithGoogle(GoogleSignInAccount acct, LoginController.OnLoginCompleteListener listener, Context context); //Đăng nhập
    void firebaseAuthWithGoogleReg(GoogleSignInAccount acct, RegisterController.OnRegistrationCompleteListener listener, Context context); //Đăng
    void updateDateOnline();
}
