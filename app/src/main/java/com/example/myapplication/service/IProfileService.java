package com.example.myapplication.service;

import com.google.android.gms.tasks.OnCompleteListener;

public interface IProfileService {
    void fetchUserName(IUsernameCallback callback);
    void updateUserName(String newName, OnCompleteListener<Void> listener);
    void deleteAccount();
    void navigateToMainActivity();
    void navigateToResetPinActivity();
    void navigateToLogin();
}
