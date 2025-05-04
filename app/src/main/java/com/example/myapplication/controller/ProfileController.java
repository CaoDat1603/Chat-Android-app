package com.example.myapplication.controller;


import android.app.Activity;
import com.example.myapplication.service.IProfileService;
import com.example.myapplication.service.impl.ProfileServiceImpl;
import com.example.myapplication.service.IUsernameCallback;
import com.google.android.gms.tasks.OnCompleteListener;

public class ProfileController {
    private final IProfileService profileService;

    public ProfileController(Activity activity) {
        this.profileService = new ProfileServiceImpl(activity);
    }

    public void fetchUserName(IUsernameCallback callback) {
        profileService.fetchUserName(callback);
    }

    public void updateUserName(String newName, OnCompleteListener<Void> listener) {
        profileService.updateUserName(newName, listener);
    }

    public void deleteAccount() {
        profileService.deleteAccount();
    }

    public void navigateToMainActivity() {
        profileService.navigateToMainActivity();
    }

    public void navigateToResetPinActivity() {
        profileService.navigateToResetPinActivity();
    }

    public void navigateToLogin() {
        profileService.navigateToLogin();
    }
}
