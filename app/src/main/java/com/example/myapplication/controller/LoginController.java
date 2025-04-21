package com.example.myapplication.controller;
import com.example.myapplication.view.LoginActivity;
import com.example.myapplication.model.AuthModel;
import com.example.myapplication.R;

import android.text.TextUtils;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;

public class LoginController {
    private AuthModel authModel;

    public LoginController() {
        authModel = new AuthModel();
    }

    public void loginUser(String email, String password, OnCompleteListener<AuthResult> listener, LoginActivity activity) {
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(activity, "Enter The Email", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(activity, "Enter The Password", Toast.LENGTH_SHORT).show();
        } else if (!authModel.isEmailValid(email)) {
            activity.findViewById(R.id.editTextLogEmail).setTop(Integer.parseInt("Give Proper Email Address"));
        } else if (!authModel.isPasswordValid(password)) {
            activity.findViewById(R.id.editTextLogPassword).setTop(Integer.parseInt("Password must be at least 8 characters"));
        } else {
            authModel.login(email, password, listener);
        }
    }
}
