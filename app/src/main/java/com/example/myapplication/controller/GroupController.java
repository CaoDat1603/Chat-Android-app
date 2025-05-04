package com.example.myapplication.controller;

import com.example.myapplication.model.Users;
import com.example.myapplication.service.CreateGroupService;
import com.example.myapplication.service.impl.CreateGroupServiceImpl;
import com.example.myapplication.view.CreateGroupActivity;

import java.util.List;

public class GroupController {
    private final CreateGroupActivity view;
    private final CreateGroupService service;

    public GroupController(CreateGroupActivity view) {
        this.view = view;
        this.service = new CreateGroupServiceImpl(this);
    }

    public void createGroup(String groupName, List<Users> selectedUsers, String adminId) {
        service.createGroup(groupName, selectedUsers, adminId, view);
    }

    public void filterUser(List<Users> allUsers, Users users, String adminId) {
        service.filterUser(allUsers, users, adminId, view);
    }
}