// repository/UserRepository.java
package com.example.myapplication.repository;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.model.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class FirebaseRepositor {
    private FirebaseAuth auth;

    public FirebaseRepositor() {
        auth = FirebaseAuth.getInstance();
    }

    public void setAuth(AuthCredential credential, AppCompatActivity context, OnCompleteListener<AuthResult> listener) {
        auth.signInWithCredential(credential).addOnCompleteListener(context, listener);
    }

    public FirebaseUser getUserCurrent() {
        return auth.getCurrentUser();
    }


}
