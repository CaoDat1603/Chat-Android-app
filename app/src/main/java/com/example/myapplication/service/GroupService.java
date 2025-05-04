package com.example.myapplication.service;

import com.example.myapplication.view.MainActivityGroup;

public interface GroupService {
    void checkUserLoginStatus();
    void fetchGroups();
    void showLogoutDialog(MainActivityGroup view);
    void checkUserPinAndProceed();
    void signOut();
}
