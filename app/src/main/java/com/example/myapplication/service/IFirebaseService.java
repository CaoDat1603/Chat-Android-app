package com.example.myapplication.service;

import android.content.Context;

import com.example.myapplication.controller.LoginController;
import com.example.myapplication.controller.RegisterController;
import com.example.myapplication.controller.ResetPinController;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

public interface IFirebaseService {
    void firebaseAuthWithGoogle(GoogleSignInAccount acct, LoginController.OnLoginCompleteListener listener, Context context); //Đăng nhập
    void firebaseAuthWithGoogleReg(GoogleSignInAccount acct, RegisterController.OnRegistrationCompleteListener listener, Context context); //Đăng ký
    void firebaseAuthWithGoogleVef(GoogleSignInAccount acct, ResetPinController.OnPinResetListener listener, Context context);
    void verifyCode(String email, String code, ResetPinController.OnVerificationListener listener, Context context);
    void verifyCodeAndResetPin(String email, String code, String newPin, ResetPinController.OnPinResetListener listener, Context context);
    void updateDateOnline();
}
