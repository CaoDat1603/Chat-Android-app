package com.example.myapplication.controller;

import android.app.Dialog;
import android.view.View;
import android.widget.Button;

import com.example.myapplication.view.MainActivity;
import com.example.myapplication.model.Users;
import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;

public class MainActivityController {

    private MainActivity view; // Direct reference to MainActivity
    private FirebaseAuth auth;
    private DatabaseReference reference;

    public MainActivityController(MainActivity view) {
        this.view = view;
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference().child("user");
    }

    // Load user data from Firebase
    public void loadUserData() {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                ArrayList<Users> usersList = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Users user = dataSnapshot.getValue(Users.class);
                    usersList.add(user);
                }
                view.updateUserList(usersList); // Call the method to update user list
            }

            @Override
            public void onCancelled(DatabaseError error) {
                view.showMessage("Error loading data: " + error.getMessage()); // Show error message
            }
        });
    }

    // Check user login status
    public void checkUserLoginStatus() {
        if (auth.getCurrentUser() == null) {
            view.navigateToLogin(); // Navigate to login if no user is logged in
        } else {
            loadUserData(); // Load user data if user is logged in
        }
    }

    // Show logout dialog
    public void showLogoutDialog() {
        Dialog dialog = new Dialog(view, R.style.dialoge);
        dialog.setContentView(R.layout.dialog_layout);
        Button no = dialog.findViewById(R.id.nobnt);
        Button yes = dialog.findViewById(R.id.yesbnt);

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View views) {
                FirebaseAuth.getInstance().signOut();
                view.navigateToLogin(); // Navigate to login after logout
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    // Navigate to group chat activity
    public void navigateToChatGroup() {
        view.navigateToChatGroup(); // Navigate to chat group
    }
}