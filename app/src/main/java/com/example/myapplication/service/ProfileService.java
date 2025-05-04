package com.example.myapplication.service;

import com.example.myapplication.service.UsernameCallback;
import com.google.android.gms.tasks.OnCompleteListener;

public interface ProfileService {
    void fetchUserName(UsernameCallback callback);
    void updateUserName(String newName, OnCompleteListener<Void> listener);
    void deleteAccount();
    void navigateToMainActivity();
    void navigateToResetPinActivity();
    void navigateToLogin();
}
