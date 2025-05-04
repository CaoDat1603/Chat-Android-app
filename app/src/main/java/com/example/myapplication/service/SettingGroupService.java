package com.example.myapplication.service;

import com.example.myapplication.controller.SettingGroupController;
import com.example.myapplication.view.MainActivityGroup;

public interface SettingGroupService {

    void getGroupInfo(final SettingGroupController.OnGroupInfoListener listener);
    void updateGroupName(String newGroupName, SettingGroupController.OnGroupUpdateListener listener);
    void outGroup(String userId, String groupId);
    void deleteGroup(String userId, String groupId);
}