package com.example.myapplication.controller;

import com.example.myapplication.model.Users;
import com.example.myapplication.service.IManagerGroupService;
import com.example.myapplication.service.impl.ManagerGroupServiceImpl;
import com.example.myapplication.view.AddMemeberActivity;
import com.example.myapplication.view.CreateGroupActivity;

import java.util.List;

public class GroupController {
    private CreateGroupActivity view;
    private AddMemeberActivity addView;
    private final IManagerGroupService service;

    public GroupController(CreateGroupActivity view) {
        this.view = view;
        this.service = new ManagerGroupServiceImpl();
    }

    public GroupController(AddMemeberActivity view) {
        this.addView = view;
        this.service = new ManagerGroupServiceImpl();
    }

    public void createGroup(String groupName, List<Users> selectedUsers, String adminId) {
        service.createGroup(groupName, selectedUsers, adminId, view);
    }

    public void filterUser(List<Users> allUsers, Users users, String adminId) {
        service.filterUser(allUsers, users, adminId, view);
    }

    public void addMember( List<Users> selectedUsers, String groupId) {
        service.addMember(selectedUsers, groupId, addView);
    }

    public void filterUserToAdd(List<Users> allUsers, List<Users> members, Users users) {
        service.filterUserToAdd(allUsers, members, users, addView);
    }
}