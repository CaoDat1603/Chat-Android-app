package com.example.myapplication.service;

import com.example.myapplication.controller.SettingGroupController;

public interface ISettingGroupService {

    void getGroupInfo(final SettingGroupController.OnGroupInfoListener listener);
    void updateGroupName(String newGroupName, SettingGroupController.OnGroupUpdateListener listener);
    void outGroup(String userId, String groupId);
    void deleteGroup(String userId, String groupId);
}