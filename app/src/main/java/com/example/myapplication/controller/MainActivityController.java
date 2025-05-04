package com.example.myapplication.controller;

import com.example.myapplication.service.UserService;

public class MainActivityController {
    private final UserService userService;

    public MainActivityController(UserService userService) {
        this.userService = userService;
    }

    public void handleAppStart() {
        userService.checkUserLoginStatus();
    }

    public void onUserWantsToReload() {
        userService.loadUserData();
    }

    public void onUserSignOut() {
        userService.signOut();
    }
}
