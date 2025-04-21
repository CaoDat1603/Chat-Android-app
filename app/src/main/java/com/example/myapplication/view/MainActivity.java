package com.example.myapplication.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.Users;
import com.example.myapplication.controller.MainActivityController;
import com.example.myapplication.view.adapter.UserAdapter;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mainUserRecyclerView;
    private UserAdapter adapterUse;
    private ArrayList<Users> usersArrayList;
    private ImageView imglogout;

    private MainActivityController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainUserRecyclerView = findViewById(R.id.mainUserRecyclerView);
        imglogout = findViewById(R.id.logoutimg);

        // Initialize the list and adapter
        usersArrayList = new ArrayList<>();
        adapterUse = new UserAdapter(usersArrayList, MainActivity.this);
        mainUserRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mainUserRecyclerView.setAdapter(adapterUse);

        controller = new MainActivityController(this); // Pass the view instance to controller

        // Handle logout button click
        imglogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controller.showLogoutDialog();
            }
        });

        // Handle group chat button click
        ImageView chatGroup = findViewById(R.id.chatGroup);
        chatGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controller.navigateToChatGroup();
            }
        });

        // Check if the user is logged in
        controller.checkUserLoginStatus();
    }

    // Method to update user list from controller
    public void updateUserList(ArrayList<Users> usersList) {
        usersArrayList.clear();
        usersArrayList.addAll(usersList);
        adapterUse.notifyDataSetChanged();
    }

    // Method to show Toast message
    public void showMessage(String message) {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    // Method to navigate to login activity
    public void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    // Method to navigate to chat group activity
    public void navigateToChatGroup() {
        Intent intent = new Intent(MainActivity.this, MainActivityGroup.class);
        startActivity(intent);
        finish();
    }
}