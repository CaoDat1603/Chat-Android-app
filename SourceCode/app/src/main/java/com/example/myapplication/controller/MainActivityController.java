package com.example.myapplication.controller;

import com.example.myapplication.service.IUserService;

public class MainActivityController {
    private final IUserService userService;

    public MainActivityController(IUserService userService) {
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
