package com.example.myapplication.repository;

import androidx.annotation.NonNull;

import com.example.myapplication.controller.MainActivityGroupController;
import com.example.myapplication.model.Group;
import com.example.myapplication.model.GroupMember;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class GroupRepository {
    private final DatabaseReference groupRef = FirebaseDatabase.getInstance().getReference().child("groups");
    private final DatabaseReference groupMemberRef = FirebaseDatabase.getInstance().getReference().child("group_members");
    private MainActivityGroupController controller;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    private ArrayList<Group> groups = new ArrayList<>(); // Danh sách nhóm hiện tại

    public GroupRepository() {};
    public GroupRepository(MainActivityGroupController controller) {
        this.controller = controller;
    }

    public String getThisUserID() {return auth.getUid(); }

    public void setGroup(Group group, OnCompleteListener<Void> listener) {
        groupRef.child(group.getGroupId()).setValue(group).addOnCompleteListener(listener);
    }

    public void setGroupName(String groupId, String newName, OnCompleteListener<Void> listener) {
        groupRef.child(groupId).child("groupName").setValue(newName).addOnCompleteListener(listener);
    }

    public void getGroup(String groupId, ValueEventListener listener) {
        groupRef.child(groupId).addListenerForSingleValueEvent(listener);
    }

    public void getMember(String groupId, ValueEventListener listener) {
        groupMemberRef.child(groupId).addListenerForSingleValueEvent(listener);
    }

    public void removeMember(String groupId, String userId, OnCompleteListener<Void> listener) {
        groupMemberRef.child(groupId).child(userId).removeValue().addOnCompleteListener(listener);
    }

    public DatabaseReference getGroupRef(String groupId) {
        return groupRef.child(groupId);
    }

    public DatabaseReference getGroupMemnerRef(String groupId) {
        return groupMemberRef.child(groupId);
    }

    // Lưu thành viên nhóm
    public void saveGroupMemberToDatabase(GroupMember groupMember) {
        groupMemberRef.child(groupMember.getGroupId()).child(groupMember.getUserId()).setValue(groupMember);
    }

    public void listenToGroups() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        groupMemberRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> userGroupIds = new ArrayList<>();

                for (DataSnapshot groupSnapshot : snapshot.getChildren()) {
                    String groupId = groupSnapshot.getKey(); // ví dụ: "group_1745997268296"
                    if (groupId != null) {
                        for (DataSnapshot memberSnapshot : groupSnapshot.getChildren()) {
                            String memberId = memberSnapshot.getKey(); // ví dụ: userId
                            if (memberId != null && memberId.equals(currentUserId)) {
                                userGroupIds.add(groupId); // Người dùng là thành viên của group này
                                break;
                            }
                        }
                    }
                }

                // Sau khi có danh sách groupId, lấy thông tin nhóm tương ứng
                groupRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Group> userGroups = new ArrayList<>();

                        for (DataSnapshot groupSnapshot : snapshot.getChildren()) {
                            Group group = groupSnapshot.getValue(Group.class);
                            if (group != null && userGroupIds.contains(group.getGroupId())) {
                                userGroups.add(group);
                            }
                        }

                        updateGroupList(userGroups);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        controller.showMessage("Lỗi khi tải thông tin nhóm.");
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                controller.showMessage("Lỗi khi kiểm tra thành viên nhóm.");
            }
        });
    }


    // Cập nhật danh sách nhóm trong repository
    private void updateGroupList(List<Group> newGroups) {
        // Xóa tất cả nhóm cũ
        List<Group> groupsToRemove = new ArrayList<>(groups);

        // Cập nhật các nhóm mới
        for (Group newGroup : newGroups) {
            boolean isExisting = false;
            for (Group group : groups) {
                if (group.getGroupId().equals(newGroup.getGroupId())) {
                    // Nếu nhóm đã tồn tại, cập nhật thông tin nhóm
                    isExisting = true;
                    if (!group.getGroupName().equals(newGroup.getGroupName())) {
                        // Nếu tên nhóm thay đổi, cập nhật lại thông tin nhóm
                        groups.remove(group);
                        groups.add(newGroup);
                    }
                    break;
                }
            }
            if (!isExisting) {
                // Nếu nhóm chưa có, thêm nhóm mới vào danh sách
                groups.add(newGroup);
            }
        }

        // Loại bỏ các nhóm không còn tồn tại trong Firebase
        for (Group group : groupsToRemove) {
            if (!newGroups.contains(group)) {
                groups.remove(group);
            }
        }

        // Cập nhật lại danh sách nhóm trên UI
        controller.onGroupsUpdated(groups);
    }
}
