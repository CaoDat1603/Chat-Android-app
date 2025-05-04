package com.example.myapplication.controller;


import android.app.Activity;
import android.app.Dialog;
import android.widget.Button;

import com.example.myapplication.R;
import com.example.myapplication.service.IProfileService;
import com.example.myapplication.service.impl.ProfileServiceImpl;
import com.example.myapplication.service.IUsernameCallback;
import com.google.android.gms.tasks.OnCompleteListener;

public class ProfileController {
    private Activity activity;
    private final IProfileService profileService;

    public ProfileController(Activity activity) {
        this.activity = activity;
        this.profileService = new ProfileServiceImpl(activity);
    }

    public void fetchUserName(IUsernameCallback callback) {
        profileService.fetchUserName(callback);
    }

    public void updateUserName(String newName, OnCompleteListener<Void> listener) {
        profileService.updateUserName(newName, listener);
    }

    public void deleteAccount() {
        Dialog dialog = new Dialog(activity, R.style.dialoge);
        dialog.setContentView(R.layout.dialog_layout);
        Button no = dialog.findViewById(R.id.nobnt);
        Button yes = dialog.findViewById(R.id.yesbnt);

        yes.setOnClickListener(v -> {
            dialog.dismiss();
            profileService.deleteAccount();
        });

        no.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
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
