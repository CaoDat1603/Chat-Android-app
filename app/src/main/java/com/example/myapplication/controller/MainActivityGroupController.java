package com.example.myapplication.controller;

import android.app.AlertDialog;
import android.app.Dialog;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;

import com.example.myapplication.model.Users;
import com.example.myapplication.view.MainActivityGroup;
import com.example.myapplication.model.Group;
import com.example.myapplication.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivityGroupController {

    private MainActivityGroup view;
    private FirebaseDatabase database;
    private DatabaseReference groupMembersRef;
    private DatabaseReference groupsRef;
    private FirebaseAuth auth;
    private DatabaseReference reference;

    public MainActivityGroupController(MainActivityGroup view) {
        this.view = view;
        database = FirebaseDatabase.getInstance();
        groupMembersRef = database.getReference().child("group_members");
        groupsRef = database.getReference().child("groups");
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference().child("user");
    }

    // Kiểm tra trạng thái đăng nhập
    public void checkUserLoginStatus() {
        if (auth.getCurrentUser() == null) {
            signOut();
        } else {
            checkUserPinAndProceed(); // Tải dữ liệu nhóm nếu người dùng đã đăng nhập
        }
    }

    // Phương thức kiểm tra mã PIN và thực hiện chức năng tiếp theo
    public void checkUserPinAndProceed() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();

        reference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Users user = dataSnapshot.getValue(Users.class);
                if (user != null) {
                    if (user.getPIN() == null || user.getPIN().isEmpty()) {
                        // Yêu cầu thiết lập mã PIN mới
                        showPinDialog("Vui lòng thiết lập mã PIN mới:", true, userId, () -> loadGroupData());
                    } else {
                        // Yêu cầu xác thực mã PIN
                        showPinDialog("Nhập mã PIN của bạn:", false, userId, () -> loadGroupData());
                    }
                } else {
                    view.showMessage("Không tìm thấy thông tin người dùng!");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                view.showMessage("Lỗi khi kiểm tra mã PIN: " + databaseError.getMessage());
            }
        });
    }

    // Hiển thị hộp thoại nhập mã PIN
    private void showPinDialog(String message, boolean isSettingPin, String userId, Runnable onSuccess) {
        AlertDialog.Builder builder = new AlertDialog.Builder(view);
        builder.setTitle(message);

        final EditText input = new EditText(view);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("Xác nhận", (dialog, which) -> {
            String pin = input.getText().toString().trim();
            if (pin.isEmpty()) {
                view.showMessage("Mã PIN không được để trống!");
                showPinDialog(message, isSettingPin, userId, onSuccess);
                return;
            }

            if (isSettingPin) {
                // Lưu mã PIN mới vào Firebase
                reference.child(userId).child("PIN").setValue(pin).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        view.showMessage("Thiết lập mã PIN thành công!");
                        onSuccess.run(); // Thực hiện chức năng tiếp theo
                    } else {
                        view.showMessage("Lỗi khi lưu mã PIN!");
                        showPinDialog(message, isSettingPin, userId, onSuccess);
                        return;
                    }
                });
            } else {
                // Xác thực mã PIN đã nhập
                reference.child(userId).child("PIN").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        String storedPin = snapshot.getValue(String.class);
                        if (pin.equals(storedPin)) {
                            view.showMessage("Xác thực mã PIN thành công!");
                            onSuccess.run(); // Thực hiện chức năng tiếp theo
                        } else {
                            view.showMessage("Mã PIN không đúng!");
                            signOut();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        view.showMessage("Lỗi khi kiểm tra mã PIN: " + error.getMessage());
                    }
                });
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> signOut());

        builder.show();
    }

    // Tải thông tin nhóm từ Firebase
    private void loadGroupData() {
        String currentUserId = auth.getCurrentUser().getUid();
        groupMembersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                ArrayList<String> groupIds = new ArrayList<>();
                for (DataSnapshot groupSnapshot : snapshot.getChildren()) {
                    String groupId = groupSnapshot.getKey();
                    if (groupId != null) {
                        for (DataSnapshot memberSnapshot : groupSnapshot.getChildren()) {
                            String userId = memberSnapshot.getKey();
                            if (currentUserId.equals(userId)) {
                                groupIds.add(groupId);
                                break;
                            }
                        }
                    }
                }
                loadGroupDetails(groupIds);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                view.showMessage("Error loading groups: " + error.getMessage());
            }
        });
    }

    // Tải chi tiết các nhóm
    private void loadGroupDetails(ArrayList<String> groupIds) {
        for (String groupId : groupIds) {
            groupsRef.child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    Group group = snapshot.getValue(Group.class);
                    if (group != null) {
                        view.addGroupToList(group);
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    view.showMessage("Error loading group details: " + error.getMessage());
                }
            });
        }
    }

    // Hiển thị dialog đăng xuất
    public void showLogoutDialog() {
        Dialog dialog = new Dialog(view, R.style.dialoge);
        dialog.setContentView(R.layout.dialog_layout);
        Button no = dialog.findViewById(R.id.nobnt);
        Button yes = dialog.findViewById(R.id.yesbnt);

        yes.setOnClickListener(v -> {
            signOut();
        });

        no.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    public  void signOut() {
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(view, GoogleSignInOptions.DEFAULT_SIGN_IN);
        googleSignInClient.signOut().addOnCompleteListener(task -> {
            // Firebase sign-out
            FirebaseAuth.getInstance().signOut();
            view.navigateToLogin();
        });

    }

    // Chuyển đến trang MainActivity
    public void navigateToMainActivity() {
        view.navigateToMainActivity();
    }

    // Chuyển đến trang tạo nhóm
    public void navigateToCreateGroupActivity() {
        view.navigateToCreateGroupActivity();
    }

    public void navigateToProfile() {
        view.navigateToProfile();
    }
}