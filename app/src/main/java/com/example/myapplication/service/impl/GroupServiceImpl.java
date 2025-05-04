package com.example.myapplication.service.impl;

import android.app.AlertDialog;
import android.app.Dialog;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;

import com.example.myapplication.R;
import com.example.myapplication.controller.MainActivityGroupController;
import com.example.myapplication.model.Users;
import com.example.myapplication.repository.GroupRepository;
import com.example.myapplication.repository.UserRepository;
import com.example.myapplication.service.IGroupService;
import com.example.myapplication.view.MainActivityGroup;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class GroupServiceImpl implements IGroupService {
    private final MainActivityGroupController controller;
    private final GroupRepository repository;
    private final FirebaseAuth auth;
    private final UserRepository userRepository;

    public GroupServiceImpl(MainActivityGroupController controller) {
        this.controller = controller;
        this.repository = new GroupRepository(controller);
        this.auth = FirebaseAuth.getInstance();
        this.userRepository = new UserRepository();
    }

    @Override
    public void fetchGroups() {
        repository.listenToGroups();
    }

    @Override
    public void checkUserPinAndProceed() {
        String userId = auth.getCurrentUser().getUid();

        userRepository.getUser(userId, new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Users user = dataSnapshot.getValue(Users.class);
                if (user != null) {
                    if (user.getPIN() == null || user.getPIN().isEmpty()) {
                        showPinDialog("Vui lòng thiết lập mã PIN mới:", true, userId,() ->  controller.loadGroupList());
                    } else {
                        showPinDialog("Nhập mã PIN của bạn:", false, userId,() ->  controller.loadGroupList());
                    }
                } else {
                    controller.showMessage("Không tìm thấy thông tin người dùng!");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                controller.showMessage("Lỗi khi kiểm tra mã PIN: " + error.getMessage());
            }
        });
    }

    private void showPinDialog(String message, boolean isSettingPin, String userId, Runnable onSuccess) {
        AlertDialog.Builder builder = new AlertDialog.Builder(controller.view);
        builder.setTitle(message);

        final EditText input = new EditText(controller.view);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("Xác nhận", (dialog, which) -> {
            String pin = input.getText().toString().trim();
            if (pin.isEmpty()) {
                controller.showMessage("Mã PIN không được để trống!");
                showPinDialog(message, isSettingPin, userId, onSuccess);
                return;
            }

            if (isSettingPin) {
                userRepository.setUserPIN(userId, pin, task -> {
                    if (task.isSuccessful()) {
                        controller.showMessage("Thiết lập mã PIN thành công!");
                    } else {
                        controller.showMessage("Lỗi khi lưu mã PIN!");
                        showPinDialog(message, isSettingPin, userId, onSuccess);
                    }
                });
            } else {
                userRepository.getUserPIN(userId, new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        String storedPin = snapshot.getValue(String.class);
                        if (pin.equals(storedPin)) {
                            controller.showMessage("Xác thực mã PIN thành công!");
                            onSuccess.run();
                        } else {
                            controller.showMessage("Mã PIN không đúng!");
                            signOut();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        controller.showMessage("Lỗi khi kiểm tra mã PIN: " + error.getMessage());
                    }
                });
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> signOut());
        builder.show();
    }

    @Override
    public void signOut() {
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(controller.view, GoogleSignInOptions.DEFAULT_SIGN_IN);
        googleSignInClient.signOut().addOnCompleteListener(task -> {
            FirebaseAuth.getInstance().signOut();
            controller.navigateToLogin();
        });
    }

    @Override
    public void checkUserLoginStatus() {
        if (auth.getCurrentUser() == null) {
            controller.navigateToLogin();
            signOut();
        } else {
            checkUserPinAndProceed();
        }
    }

    @Override
    public void showLogoutDialog(MainActivityGroup view) {
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
}