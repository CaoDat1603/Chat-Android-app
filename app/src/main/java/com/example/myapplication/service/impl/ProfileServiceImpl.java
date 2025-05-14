package com.example.myapplication.service.impl;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.example.myapplication.model.Users;
import com.example.myapplication.repository.UserRepository;
import com.example.myapplication.service.IProfileService;
import com.example.myapplication.service.IUsernameCallback;
import com.example.myapplication.view.LoginActivity;
import com.example.myapplication.view.MainActivity;
import com.example.myapplication.view.ResetPinActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class ProfileServiceImpl implements IProfileService {
    private final Activity activity;
    private final UserRepository userRepository;
    private final FirebaseUser currentUser;

    public ProfileServiceImpl(Activity activity) {
        this.activity = activity;
        this.userRepository = new UserRepository();
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public void fetchUserName(IUsernameCallback callback) {
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
            userRepository.setUserStatus(currentUser.getUid(), "deleted");
            currentUser.delete().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    clearAppData();
                    signOutFromGoogle();
                    navigateToLogin();
                    Toast.makeText(activity, "Tài khoản đã được xóa, dữ liệu vẫn được giữ lại!", Toast.LENGTH_SHORT).show();
                } else {
                    handleDeleteError(task.getException(), currentUser);
                }
            });
        } else {
            Toast.makeText(activity, "Người dùng không tồn tại!", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleDeleteError(Exception exception, FirebaseUser user) {
        if (exception != null && exception.getMessage().contains("requires recent login")) {
            reauthenticateAndDelete(user);
        } else {
            Toast.makeText(activity, "Không thể xóa tài khoản: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void reauthenticateAndDelete(FirebaseUser user) {
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(activity, GoogleSignInOptions.DEFAULT_SIGN_IN);
        googleSignInClient.silentSignIn().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String idToken = task.getResult().getIdToken();
                AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

                user.reauthenticate(credential).addOnCompleteListener(authTask -> {
                    if (authTask.isSuccessful()) {
                        deleteAccount();
                    } else {
                        Toast.makeText(activity, "Xác thực lại thất bại: " + authTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(activity, "Không thể xác thực Google!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void signOutFromGoogle() {
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(activity, GoogleSignInOptions.DEFAULT_SIGN_IN);
        googleSignInClient.revokeAccess().addOnCompleteListener(task -> {
            googleSignInClient.signOut();
        });
    }

    private void clearAppData() {
        // Xóa SharedPreferences
        SharedPreferences preferences = activity.getSharedPreferences("your_pref_name", 0);
        if (preferences != null) {
            preferences.edit().clear().apply();
        }

        // Xóa cache
        try {
            String[] files = activity.fileList();
            for (String file : files) {
                activity.deleteFile(file);
            }

            // Xóa cache nội bộ
            activity.getCacheDir().delete();
        } catch (Exception e) {
            e.printStackTrace();
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
