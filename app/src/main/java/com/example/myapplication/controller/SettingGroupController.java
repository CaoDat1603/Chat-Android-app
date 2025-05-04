package com.example.myapplication.controller;

import android.content.Intent;
import android.widget.Toast;

import com.example.myapplication.model.Group;
import com.example.myapplication.service.SettingGroupService;
import com.example.myapplication.service.impl.SettingGroupServiceImpl;
import com.example.myapplication.view.MainActivityGroup;
import com.example.myapplication.view.SettingGroupActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class SettingGroupController {
    public String groupId;
    public SettingGroupActivity view; // Giữ reference đến SettingGroupActivity
    private final SettingGroupService service;


    // Constructor với groupId
    public SettingGroupController(String groupId) {
        this.groupId = groupId;
        this.service = new SettingGroupServiceImpl(this);
    }

    // Constructor với view (SettingGroupActivity)
    public SettingGroupController(SettingGroupActivity view) {
        this.groupId = view.groupId;
        this.view = view;  // Lưu trữ reference của activity
        this.service = new SettingGroupServiceImpl(this);
    }

    // Lấy thông tin nhóm từ Firebase
    public void getGroupInfo(final OnGroupInfoListener listener) {
        service.getGroupInfo(listener);
    }

    // Cập nhật tên nhóm
    public void updateGroupName(String newGroupName, OnGroupUpdateListener listener) {
        service.updateGroupName(newGroupName, listener);
    }

    public void outGroup(String userId, String groupId) {
        service.outGroup(userId, groupId);
    }

    public void deleteGroup(String userId, String groupId) {
        service.deleteGroup(userId, groupId);
    }

    // Interface listeners
    public interface OnGroupInfoListener {
        void onSuccess(Group group);
        void onFailure(String errorMessage);
    }

    public interface OnGroupUpdateListener {
        void onSuccess();
        void onFailure(String errorMessage);
    }
}
