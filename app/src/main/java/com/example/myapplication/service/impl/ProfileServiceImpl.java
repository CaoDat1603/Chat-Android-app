package com.example.myapplication.service.impl;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import com.example.myapplication.model.Users;
import com.example.myapplication.repository.UserRepository;
import com.example.myapplication.service.ProfileService;
import com.example.myapplication.service.UsernameCallback;
import com.example.myapplication.view.LoginActivity;
import com.example.myapplication.view.MainActivity;
import com.example.myapplication.view.ResetPinActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class ProfileServiceImpl implements ProfileService {
    private final Activity activity;
    private final UserRepository userRepository;
    private final FirebaseUser currentUser;

    public ProfileServiceImpl(Activity activity) {
        this.activity = activity;
        this.userRepository = new UserRepository();
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public void fetchUserName(UsernameCallback callback) {
        if (currentUser != null) {
            userRepository.getUser(currentUser.getUid(), new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Cách 1: dùng đối tượng Users (nếu class Users có hàm getFullname)
                        Users user = dataSnapshot.getValue(Users.class);
                        if (user != null && user.getFullname() != null) {
                            callback.onUsernameFetched(user.getFullname());
                        } else {
                            callback.onUsernameFetched(null);
                        }
                    } else {
                        callback.onUsernameFetched(null);
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    callback.onUsernameFetched(null);
                }
            });
        } else {
            callback.onUsernameFetched(null);
        }
    }


    @Override
    public void updateUserName(String newName, OnCompleteListener<Void> listener) {
        if (currentUser != null) {
            userRepository.setUserName(currentUser.getUid(), newName, listener);
        }
    }

    @Override
    public void deleteAccount() {
        if (currentUser != null) {
            currentUser.delete().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(activity, "Account deleted", Toast.LENGTH_SHORT).show();
                    navigateToLogin();
                } else {
                    Toast.makeText(activity, "Failed to delete account", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void navigateToMainActivity() {
        activity.startActivity(new Intent(activity, MainActivity.class));
        activity.finish();
    }

    @Override
    public void navigateToResetPinActivity() {
        activity.startActivity(new Intent(activity, ResetPinActivity.class));
    }

    @Override
    public void navigateToLogin() {
        activity.startActivity(new Intent(activity, LoginActivity.class));
        activity.finish();
    }
}
