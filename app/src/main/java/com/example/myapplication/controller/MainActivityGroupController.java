package com.example.myapplication.controller;

import com.example.myapplication.model.Group;
import com.example.myapplication.service.GroupService;
import com.example.myapplication.service.impl.GroupServiceImpl;
import com.example.myapplication.view.MainActivityGroup;
import java.util.ArrayList;

public class MainActivityGroupController {
    public final MainActivityGroup view;
    private final GroupService service;

    public MainActivityGroupController(MainActivityGroup view) {
        this.view = view;
        this.service = new GroupServiceImpl(this);
    }

    public void checkUserLoginStatus() {
        service.checkUserLoginStatus();
    }

    public void loadGroupList() {
        service.fetchGroups();
    }

    public void onGroupFetched(Group group) {
        view.addGroupToList(group);
    }

    public void showLogoutDialog() {
        service.showLogoutDialog(view);
    }

    public void navigateToMainActivity() {
        view.navigateToMainActivity();
    }

    public void navigateToCreateGroupActivity() {
        view.navigateToCreateGroupActivity();
    }

    public void navigateToProfile() {
        view.navigateToProfile();
    }

    public void showMessage(String message) {
        view.showMessage(message);
    }

    public void navigateToLogin() {
        view.navigateToLogin();
    }

    public void onGroupsUpdated(ArrayList<Group> groups) {
        view.updateGroupList(groups);
    }
}
