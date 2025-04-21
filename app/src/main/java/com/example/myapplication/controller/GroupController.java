package com.example.myapplication.controller;

import com.example.myapplication.model.Group;
import com.example.myapplication.model.GroupMember;
import com.example.myapplication.model.Users;
import com.example.myapplication.view.CreateGroupActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class GroupController {

    private CreateGroupActivity createGroupActivity;
    private FirebaseDatabase database;

    public GroupController(CreateGroupActivity createGroupActivity) {
        this.createGroupActivity = createGroupActivity;
        this.database = FirebaseDatabase.getInstance();

    }

    public void createGroup(String groupName, List<Users> selectedUsers, String adminId) {
        if (groupName == null || groupName.isEmpty()) {
            groupName = generateGroupName(selectedUsers);
        }

        // Tự động thêm người dùng hiện tại (admin) vào danh sách thành viên
        addCurrentUserToGroup(selectedUsers, adminId);

        // Tạo Group object
        Group group = new Group();
        group.setGroupName(groupName);
        group.setCreatedAt(System.currentTimeMillis());
        group.setGroupId("group_" + System.currentTimeMillis());  // Tạo ID nhóm (ví dụ: dùng timestamp)
        group.setAdminId(adminId);  // Thiết lập ID quản trị viên nhóm
        saveGroupToDatabase(group, selectedUsers);
    }

    private String generateGroupName(List<Users> selectedUsers) {
        StringBuilder groupName = new StringBuilder();
        for (int i = 0; i < Math.min(3, selectedUsers.size()); i++) {
            groupName.append(selectedUsers.get(i).getFullname());
            if (i < 2 && i < selectedUsers.size() - 1) {
                groupName.append(", ");
            }
        }
        if (selectedUsers.size() > 3) {
            groupName.append(",...");
        }
        return groupName.toString();
    }

    private void saveGroupToDatabase(Group group, List<Users> selectedUsers) {
        DatabaseReference groupReference = database.getReference().child("groups").child(group.getGroupId());

        groupReference.setValue(group).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (Users user : selectedUsers) {
                    GroupMember groupMember = new GroupMember(group.getGroupId(), user.getUserId(), System.currentTimeMillis());
                    saveGroupMemberToDatabase(groupMember);
                }

                createGroupActivity.onGroupCreated(); // Gọi phương thức thông báo khi nhóm được tạo thành công
            } else {
                createGroupActivity.showErrorMessage("Lỗi khi tạo nhóm. Vui lòng thử lại.");
            }
        });
    }

    private void saveGroupMemberToDatabase(GroupMember groupMember) {
        DatabaseReference groupMemberReference = database.getReference().child("group_members")
                .child(groupMember.getGroupId()).child(groupMember.getUserId());
        groupMemberReference.setValue(groupMember);
    }

    private void addCurrentUserToGroup(List<Users> selectedUsers, String adminId) {
        // Lấy thông tin người dùng hiện tại từ Firebase
        DatabaseReference currentUserRef = database.getReference().child("user").child(adminId);
        currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Users currentUser = snapshot.getValue(Users.class); // Lấy thông tin người dùng hiện tại

                    // Kiểm tra và thêm người dùng hiện tại vào danh sách nếu chưa có
                    if (!selectedUsers.contains(currentUser)) {
                        selectedUsers.add(currentUser);
                    }
                } else {
                    createGroupActivity.showErrorMessage("Không tìm thấy thông tin người dùng hiện tại.");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                createGroupActivity.showErrorMessage("Lỗi khi tải thông tin người dùng hiện tại.");
            }
        });
    }
}