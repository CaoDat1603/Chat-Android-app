package com.example.myapplication.controller;

import android.content.Context;

import com.example.myapplication.model.Users;
import com.example.myapplication.service.IManageMemberService;
import com.example.myapplication.service.impl.ManageMemberServiceImpl;

import java.util.List;

public class ManageMemberController {
    private final Context context;
    private final IManageMemberService service;

    public ManageMemberController(Context context) {
        this.context = context;
        this.service = new ManageMemberServiceImpl(this);
    }

    public interface OnAdminIdLoadedListener {
        void onAdminIdLoaded(String adminId);
    }

    public interface OnGroupMembersLoadedListener {
        void onMembersLoaded(List<Users> members);
    }

    public Context getContext() { return this.context; }

    public void loadAdminId(String groupId, OnAdminIdLoadedListener listener) {
        service.loadAdminId(groupId, listener);
    }

    public void loadGroupMembers(String groupId, OnGroupMembersLoadedListener listener) {
        service.loadGroupMembers(groupId, listener);
    }

    public void removeUserFromGroup(String groupId, String userId, Runnable onSuccess, Runnable onFailure) {
        service.removeUserFromGroup(groupId, userId, onSuccess, onFailure);
    }

    public void changeGroupAdmin(String groupId, String newAdminId, Runnable onSuccess, Runnable onFailure) {
        service.changeGroupAdmin(groupId, newAdminId, onSuccess, onFailure);
    }
}