package com.example.myapplication.controller;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.model.Users;
import com.example.myapplication.view.ProfileActivity;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileController {
    private ProfileActivity view;
    private FirebaseAuth auth;
    private DatabaseReference reference;

    public ProfileController(ProfileActivity view) {
        this.view = view;
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference().child("user");
    }

    public void fetchUserName(MainActivityController.OnUserNameFetchedListener listener) {
        String userId = auth.getCurrentUser().getUid();
        reference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Users user = dataSnapshot.getValue(Users.class);
                if (user != null && user.getFullname() != null) {
                    listener.onFetched(user.getFullname());
                } else {
                    listener.onFetched(null);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.onFetched(null);
            }
        });
    }

    public void updateUserName(String newName, OnCompleteListener<Void> listener) {
        String userId = auth.getCurrentUser().getUid();
        reference.child(userId).child("fullname").setValue(newName).addOnCompleteListener(listener);
    }

    public void navigateToMainActivity() {
        view.navigateToMainActivity();
    }

    public void navigateToResetPinActivity() {
        view.navigateToResetPinActivity();
    }

    public void deleteAccount() {
        Dialog dialog = new Dialog(view, R.style.dialoge);
        dialog.setContentView(R.layout.dialog_layout);
        Button no = dialog.findViewById(R.id.nobnt);
        Button yes = dialog.findViewById(R.id.yesbnt);

        yes.setOnClickListener(v -> {
            dialog.dismiss();
            deleteFirebaseAccountOnly();
        });

        no.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }


    private void deleteFirebaseAccountOnly() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            currentUser.delete().addOnCompleteListener(deleteTask -> {
                if (deleteTask.isSuccessful()) {
                    clearAppData();
                    signOutFromGoogle();

                    Toast.makeText(view, "Tài khoản đã được xóa, dữ liệu vẫn được giữ lại!", Toast.LENGTH_SHORT).show();
                    view.navigateToLogin();
                } else {
                    handleDeleteError(deleteTask.getException(), currentUser);
                }
            });
        } else {
            Toast.makeText(view, "Người dùng không tồn tại!", Toast.LENGTH_SHORT).show();
        }
    }



    private void handleDeleteError(Exception exception, FirebaseUser user) {
        if (exception != null && exception.getMessage().contains("requires recent login")) {
            reauthenticateAndDelete(user);
        } else {
            Toast.makeText(view, "Không thể xóa tài khoản: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void reauthenticateAndDelete(FirebaseUser user) {
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(view, GoogleSignInOptions.DEFAULT_SIGN_IN);
        googleSignInClient.silentSignIn().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String idToken = task.getResult().getIdToken();
                AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

                user.reauthenticate(credential).addOnCompleteListener(authTask -> {
                    if (authTask.isSuccessful()) {
                        deleteFirebaseAccountOnly();
                    } else {
                        Toast.makeText(view, "Xác thực lại thất bại: " + authTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(view, "Không thể xác thực Google!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void signOut() {
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(view, GoogleSignInOptions.DEFAULT_SIGN_IN);
        googleSignInClient.signOut().addOnCompleteListener(task -> {
            FirebaseAuth.getInstance().signOut();
            clearAppData();
            view.navigateToLogin();
        });
    }

    private void signOutFromGoogle() {
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(view, GoogleSignInOptions.DEFAULT_SIGN_IN);
        googleSignInClient.revokeAccess().addOnCompleteListener(task -> {
            googleSignInClient.signOut();
        });
    }

    private void clearAppData() {
        // Xóa SharedPreferences
        SharedPreferences preferences = view.getSharedPreferences("your_pref_name", 0);
        if (preferences != null) {
            preferences.edit().clear().apply();
        }

        // Xóa cache
        try {
            String[] files = view.fileList();
            for (String file : files) {
                view.deleteFile(file);
            }

            // Xóa cache nội bộ
            view.getCacheDir().delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
