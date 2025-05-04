package com.example.myapplication.service.impl;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.myapplication.R;
import com.example.myapplication.controller.MainActivityGroupController;
import com.example.myapplication.controller.SettingGroupController;
import com.example.myapplication.model.Group;
import com.example.myapplication.model.Users;
import com.example.myapplication.repository.GroupRepository;
import com.example.myapplication.repository.UserRepository;
import com.example.myapplication.service.GroupService;
import com.example.myapplication.service.SettingGroupService;
import com.example.myapplication.view.MainActivityGroup;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class SettingGroupServiceImpl implements SettingGroupService {
    private final SettingGroupController controller;
    private final GroupRepository repository;

    public SettingGroupServiceImpl(SettingGroupController controller) {
        this.controller = controller;
        this.repository = new GroupRepository();
    }

    @Override
    public void getGroupInfo(SettingGroupController.OnGroupInfoListener listener) {
        repository.getGroup(controller.groupId, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Group group = dataSnapshot.getValue(Group.class);
                    listener.onSuccess(group);
                } else {
                    listener.onFailure("Không tìm thấy thông tin nhóm");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure("Lỗi kết nối");
            }
        });
    }

    @Override
    public void updateGroupName(String newGroupName, SettingGroupController.OnGroupUpdateListener listener) {
        repository.setGroupName(controller.groupId, newGroupName, task -> {
            if (task.isSuccessful()) {
                listener.onSuccess();
            } else {
                listener.onFailure("Cập nhật tên nhóm thất bại. Thử lại sau.");
            }
        });
    }

    @Override
    public void outGroup(String userId, String groupId) {
        repository.getGroup(groupId, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Group group = dataSnapshot.getValue(Group.class);

                    // Kiểm tra nếu nhóm tồn tại và người rời là admin
                    if(group != null && group.getAdminId().equals(userId)) {
                        // Kiểm tra xem nhóm còn thành viên nào không
                        repository.getMember(groupId, new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    // Tìm một thành viên khác để làm admin
                                    for (DataSnapshot memberSnapshot : dataSnapshot.getChildren()) {
                                        String memberId = memberSnapshot.getKey();
                                        // Không bổ nhiệm lại admin cho người rời nhóm
                                        if (!memberId.equals(userId)) {
                                            // Cập nhật admin mới
                                            repository.getGroupRef(groupId).child("adminId").setValue(memberId);
                                            break;
                                        }
                                    }
                                } else {
                                    // Nếu không còn thành viên nào sau khi admin rời nhóm, xử lý theo yêu cầu
                                    repository.getGroupRef(groupId).child("adminId").removeValue();  // Không còn admin
                                }

                                // Tiến hành xóa người dùng khỏi nhóm
                                removeUserFromGroup(userId, groupId);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(controller.view, "Error getting group members", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        // Nếu người rời nhóm không phải là admin, chỉ cần xóa người đó khỏi nhóm
                        removeUserFromGroup(userId, groupId);
                    }
                } else {
                    Toast.makeText(controller.view, "Group not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(controller.view, "Error retrieving group info", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeUserFromGroup(String userId, String groupId) {
        repository.removeMember(groupId, userId, task -> {
            if (task.isSuccessful()) {
                if (controller.view != null) {
                    Intent intent = new Intent(controller.view, MainActivityGroup.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    controller.view.startActivity(intent);
                    controller.view.finish();
                } else {
                    // Nếu view là null, không thực hiện bất kỳ hành động nào
                    Toast.makeText(controller.view, "Error: View is null", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(controller.view, "Error removing user from group", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void deleteGroup(String userId, String groupId) {
        repository.getGroup(groupId, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Group group = dataSnapshot.getValue(Group.class);

                    // Kiểm tra nếu người yêu cầu xóa nhóm là admin của nhóm
                    if (group != null && group.getAdminId().equals(userId)) {
                        // Xóa thông tin nhóm trong "groups"
                        repository.getGroupRef(groupId).removeValue().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Xóa tất cả thành viên trong nhóm khỏi "group_members"
                                repository.getGroupMemnerRef(groupId).removeValue()
                                        .addOnCompleteListener(memberRemovalTask -> {
                                            if (memberRemovalTask.isSuccessful()) {
                                                Toast.makeText(controller.view, "Nhóm đã được xóa thành công.", Toast.LENGTH_SHORT).show();
                                                if (controller.view != null) {
                                                    Intent intent = new Intent(controller.view, MainActivityGroup.class);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    controller.view.startActivity(intent);
                                                    controller.view.finish();
                                                } else {
                                                    // Nếu view là null, không thực hiện bất kỳ hành động nào
                                                    Toast.makeText(controller.view, "Error: View is null", Toast.LENGTH_SHORT).show();
                                                }
                                            } else {
                                                Toast.makeText(controller.view, "Lỗi khi xóa thành viên trong nhóm.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                Toast.makeText(controller.view, "Lỗi khi xóa nhóm.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(controller.view, "Chỉ admin mới có thể xóa nhóm.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(controller.view, "Nhóm không tồn tại.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(controller.view, "Lỗi kết nối với cơ sở dữ liệu.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}