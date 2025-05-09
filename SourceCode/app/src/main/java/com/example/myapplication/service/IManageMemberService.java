package com.example.myapplication.service;

import com.example.myapplication.controller.ManageMemberController;

public interface IManageMemberService {
    void loadAdminId(String groupId, ManageMemberController.OnAdminIdLoadedListener listener);
    void loadGroupMembers(String groupId, ManageMemberController.OnGroupMembersLoadedListener listener);
    void removeUserFromGroup(String groupId, String userId, Runnable onSuccess, Runnable onFailure);
    void changeGroupAdmin(String groupId, String newAdminId, Runnable onSuccess, Runnable onFailure);
}