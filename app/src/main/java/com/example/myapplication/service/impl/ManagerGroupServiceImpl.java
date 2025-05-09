package com.example.myapplication.service.impl;

import androidx.annotation.NonNull;

import com.example.myapplication.controller.GroupController;
import com.example.myapplication.model.Group;
import com.example.myapplication.model.GroupMember;
import com.example.myapplication.model.Users;
import com.example.myapplication.repository.GroupRepository;
import com.example.myapplication.repository.UserRepository;
import com.example.myapplication.service.IManagerGroupService;
import com.example.myapplication.view.AddMemeberActivity;
import com.example.myapplication.view.CreateGroupActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class ManagerGroupServiceImpl implements IManagerGroupService {
    private final GroupRepository repository;
    private final UserRepository userRreository;

    public ManagerGroupServiceImpl() {
        this.repository = new GroupRepository();
        this.userRreository = new UserRepository();
    }

    @Override
    public void createGroup(String groupName, List<Users> selectedUsers, String adminId, CreateGroupActivity view) {
        final String[] groupNameFinal = {groupName}; // Đảm bảo groupName là final

        userRreository.getUser(adminId, new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Users admin = snapshot.getValue(Users.class);  // Lấy thông tin admin từ Firebase

                    // Thêm người quản trị vào danh sách người dùng nếu chưa có
                    if (admin != null && !selectedUsers.contains(admin)) {
                        selectedUsers.add(admin);
                    }

                    // Tiến hành thêm người dùng hiện tại vào nhóm
                    addCurrentUserToGroup(selectedUsers, adminId, view);

                    // Kiểm tra nếu tên nhóm trống, thì tạo nhóm mặc định
                    if (groupNameFinal[0] == null || groupNameFinal[0].isEmpty()) {
                        groupNameFinal[0] = generateGroupName(selectedUsers);
                    }

                    // Tạo Group object
                    Group group = new Group();
                    group.setGroupName(groupNameFinal[0]);
                    group.setCreatedAt(System.currentTimeMillis());
                    group.setGroupId("group_" + System.currentTimeMillis());  // Tạo ID nhóm (ví dụ: dùng timestamp)
                    group.setAdminId(adminId);  // Thiết lập ID quản trị viên nhóm
                    group.setGroupStatus("Online");

                    // Lưu nhóm và thành viên nhóm vào cơ sở dữ liệu
                    saveGroupToDatabase(group, selectedUsers, view);
                } else {
                    view.showErrorMessage("Không tìm thấy thông tin người quản trị.");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                view.showErrorMessage("Lỗi khi tải thông tin người quản trị.");
            }
        });
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

    private void saveGroupToDatabase(Group group, List<Users> selectedUsers,CreateGroupActivity view) {
        repository.setGroup(group, task -> {
            if (task.isSuccessful()) {
                // Lưu các thành viên nhóm vào cơ sở dữ liệu
                for (Users user : selectedUsers) {
                    GroupMember groupMember = new GroupMember(group.getGroupId(), user.getUserId(), System.currentTimeMillis());
                    repository.saveGroupMemberToDatabase(groupMember);
                }

                view.onGroupCreated(); // Gọi phương thức thông báo khi nhóm được tạo thành công
            } else {
                view.showErrorMessage("Lỗi khi tạo nhóm. Vui lòng thử lại.");
            }
        });
    }

    private void addCurrentUserToGroup(List<Users> selectedUsers, String adminId, CreateGroupActivity view) {
        // Lấy thông tin người dùng hiện tại từ Firebase
        userRreository.getUser(adminId, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Users currentUser = snapshot.getValue(Users.class); // Lấy thông tin người dùng hiện tại

                    // Kiểm tra và thêm người dùng hiện tại vào danh sách nếu chưa có
                    if (currentUser != null && !selectedUsers.contains(currentUser)) {
                        selectedUsers.add(currentUser); // Thêm người dùng hiện tại vào danh sách
                    }
                } else {
                    view.showErrorMessage("Không tìm thấy thông tin người dùng hiện tại.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                view.showErrorMessage("Lỗi khi tải thông tin người dùng hiện tại.");
            }
        });
    }

    @Override
    public void filterUser(List<Users> allUsers, Users users, String adminId, CreateGroupActivity view) {
        userRreository.getAllUsers(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                allUsers.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Users user = dataSnapshot.getValue(Users.class);
                    if (user.getUserId() != null
                            && !adminId.equals(user.getUserId())
                            && (user.getStatus() == null || !user.getStatus().equals("deleted"))) {
                        allUsers.add(user);
                    }
                }
                view.filterUserList(""); // Lọc lại danh sách khi có dữ liệu
            }

            @Override
            public void onCancelled(DatabaseError error) {
                view.showErrorMessage("Lỗi khi tải dữ liệu người dùng.");
            }
        });
    }

    @Override
    public void filterUserToAdd(List<Users> allUsers, List<Users> members, Users users, AddMemeberActivity view) {
        userRreository.getAllUsers(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                allUsers.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Users user = dataSnapshot.getValue(Users.class);
                    if (user.getUserId() != null
                            && !isUserInMembers(user, members)
                            && (user.getStatus() == null || !user.getStatus().equals("deleted"))) {
                        allUsers.add(user);
                    }
                }
                view.filterUserList(""); // Lọc lại danh sách khi có dữ liệu
            }

            @Override
            public void onCancelled(DatabaseError error) {
                view.showErrorMessage("Lỗi khi tải dữ liệu người dùng.");
            }
        });
    }

    private boolean isUserInMembers(Users user, List<Users> members) {
        for (Users member : members) {
            if (member.getUserId().equals(user.getUserId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void addMember(List<Users> selectedUsers, String groupId, AddMemeberActivity view) {
        for (Users user : selectedUsers) {
            GroupMember groupMember = new GroupMember(groupId, user.getUserId(), System.currentTimeMillis());
            repository.saveGroupMemberToDatabase(groupMember);
        }
        view.onAddMember(); // Gọi phương thức thông báo khi xong
    }
}