package com.example.myapplication.controller;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.widget.Toast;

import com.example.myapplication.model.Group;
import com.example.myapplication.view.MainActivityGroup;
import com.example.myapplication.view.SettingGroupActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class SettingGroupController {
    private final DatabaseReference groupReference;
    private FirebaseDatabase database;
    private SettingGroupActivity view; // Giữ reference đến SettingGroupActivity

    // Constructor với groupId
    public SettingGroupController(String groupId) {
        this.database = FirebaseDatabase.getInstance();
        this.groupReference = database.getReference("groups").child(groupId);
    }

    // Constructor với view (SettingGroupActivity)
    public SettingGroupController(SettingGroupActivity view) {
        this.database = FirebaseDatabase.getInstance();
        this.groupReference = database.getReference("groups").child(view.getGroupId());
        this.view = view;  // Lưu trữ reference của activity
    }

    // Lấy thông tin nhóm từ Firebase
    public void getGroupInfo(final OnGroupInfoListener listener) {
        groupReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Group group = dataSnapshot.getValue(Group.class);
                    listener.onSuccess(group);
                } else {
                    listener.onFailure("Không tìm thấy thông tin nhóm");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.onFailure("Lỗi kết nối");
            }
        });
    }

    // Cập nhật tên nhóm
    public void updateGroupName(String newGroupName, OnGroupUpdateListener listener) {
        groupReference.child("groupName").setValue(newGroupName).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                listener.onSuccess();
            } else {
                listener.onFailure("Cập nhật tên nhóm thất bại. Thử lại sau.");
            }
        });
    }

    public void outGroup(String userId, String groupId) {
        // Tạo đường dẫn đến node group_members và group_info trong Firebase Database
        DatabaseReference groupMemberReference = database.getReference()
                .child("group_members")
                .child(groupId)
                .child(userId);

        // Lấy thông tin nhóm để kiểm tra xem người rời nhóm có phải là admin không
        DatabaseReference groupInfoReference = database.getReference().child("groups").child(groupId);

        groupInfoReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Group group = dataSnapshot.getValue(Group.class);

                    // Kiểm tra nếu nhóm tồn tại và người rời là admin
                    if (group != null && group.getAdminId().equals(userId)) {
                        // Kiểm tra xem nhóm còn thành viên nào không
                        DatabaseReference groupMembersRef = database.getReference()
                                .child("group_members")
                                .child(groupId);

                        groupMembersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    // Tìm một thành viên khác để làm admin
                                    for (DataSnapshot memberSnapshot : dataSnapshot.getChildren()) {
                                        String memberId = memberSnapshot.getKey();
                                        // Không bổ nhiệm lại admin cho người rời nhóm
                                        if (!memberId.equals(userId)) {
                                            // Cập nhật admin mới
                                            groupInfoReference.child("adminId").setValue(memberId);
                                            break;
                                        }
                                    }
                                } else {
                                    // Nếu không còn thành viên nào sau khi admin rời nhóm, xử lý theo yêu cầu
                                    groupInfoReference.child("adminId").removeValue();  // Không còn admin
                                }

                                // Tiến hành xóa người dùng khỏi nhóm
                                removeUserFromGroup(userId, groupId);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(view, "Error getting group members", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        // Nếu người rời nhóm không phải là admin, chỉ cần xóa người đó khỏi nhóm
                        removeUserFromGroup(userId, groupId);
                    }
                } else {
                    Toast.makeText(view, "Group not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(view, "Error retrieving group info", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Phương thức xóa người dùng khỏi nhóm
    private void removeUserFromGroup(String userId, String groupId) {
        DatabaseReference groupMemberReference = database.getReference()
                .child("group_members")
                .child(groupId)
                .child(userId);

        groupMemberReference.removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (view != null) {
                            Intent intent = new Intent(view, MainActivityGroup.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            view.startActivity(intent);
                            view.finish();
                        } else {
                            // Nếu view là null, không thực hiện bất kỳ hành động nào
                            Toast.makeText(view, "Error: View is null", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(view, "Error removing user from group", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void deleteGroup(String userId, String groupId) {
        // Tạo đường dẫn đến node groups và group_info trong Firebase Database
        DatabaseReference groupInfoReference = database.getReference().child("groups").child(groupId);

        // Lấy thông tin nhóm để kiểm tra xem người yêu cầu có phải là admin không
        groupInfoReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Group group = dataSnapshot.getValue(Group.class);

                    // Kiểm tra nếu người yêu cầu xóa nhóm là admin của nhóm
                    if (group != null && group.getAdminId().equals(userId)) {
                        // Xóa thông tin nhóm trong "groups"
                        groupInfoReference.removeValue()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        // Xóa tất cả thành viên trong nhóm khỏi "group_members"
                                        DatabaseReference groupMembersReference = database.getReference().child("group_members").child(groupId);
                                        groupMembersReference.removeValue()
                                                .addOnCompleteListener(memberRemovalTask -> {
                                                    if (memberRemovalTask.isSuccessful()) {
                                                        Toast.makeText(view, "Nhóm đã được xóa thành công.", Toast.LENGTH_SHORT).show();
                                                        if (view != null) {
                                                            Intent intent = new Intent(view, MainActivityGroup.class);
                                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                            view.startActivity(intent);
                                                            view.finish();
                                                        } else {
                                                            // Nếu view là null, không thực hiện bất kỳ hành động nào
                                                            Toast.makeText(view, "Error: View is null", Toast.LENGTH_SHORT).show();
                                                        }
                                                    } else {
                                                        Toast.makeText(view, "Lỗi khi xóa thành viên trong nhóm.", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    } else {
                                        Toast.makeText(view, "Lỗi khi xóa nhóm.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(view, "Chỉ admin mới có thể xóa nhóm.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(view, "Nhóm không tồn tại.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(view, "Lỗi kết nối với cơ sở dữ liệu.", Toast.LENGTH_SHORT).show();
            }
        });
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

