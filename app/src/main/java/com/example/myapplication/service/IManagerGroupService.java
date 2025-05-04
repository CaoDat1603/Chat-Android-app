package com.example.myapplication.service;

import com.example.myapplication.model.Users;
import com.example.myapplication.view.AddMemeberActivity;
import com.example.myapplication.view.CreateGroupActivity;

import java.util.List;

public interface IManagerGroupService {
    public void createGroup(String groupName, List<Users> selectedUsers, String adminId, CreateGroupActivity view);
    public void filterUser(List<Users> allUsers, Users users, String adminId, CreateGroupActivity view);
    public void filterUserToAdd(List<Users> allUsers, List<Users> members, Users users, AddMemeberActivity view);
    public void addMember( List<Users> selectedUsers, String groupId, AddMemeberActivity view);
}
