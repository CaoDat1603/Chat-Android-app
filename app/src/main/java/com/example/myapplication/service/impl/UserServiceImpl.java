package com.example.myapplication.service.impl;

import android.app.AlertDialog;
import android.text.InputType;
import android.widget.EditText;

import com.example.myapplication.model.Users;
import com.example.myapplication.repository.UserRepository;
import com.example.myapplication.service.IUserService;
import com.example.myapplication.view.MainActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class UserServiceImpl implements IUserService {

    private final MainActivity view;
    private final FirebaseAuth auth;
    private final UserRepository userRepository;

    public UserServiceImpl(MainActivity view) {
        this.view = view;
        this.auth = FirebaseAuth.getInstance();
        this.userRepository = new UserRepository();
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
                        showPinDialog("Vui lòng thiết lập mã PIN mới:", true, userId, UserServiceImpl.this::loadUserData);
                    } else {
                        showPinDialog("Nhập mã PIN của bạn:", false, userId, UserServiceImpl.this::loadUserData);
                    }
                } else {
                    view.showMessage("Không tìm thấy thông tin người dùng!");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                view.showMessage("Lỗi khi kiểm tra mã PIN: " + error.getMessage());
            }
        });
    }

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
                userRepository.setUserPIN(userId, pin, task -> {
                    if (task.isSuccessful()) {
                        view.showMessage("Thiết lập mã PIN thành công!");
                        onSuccess.run();
                    } else {
                        view.showMessage("Lỗi khi lưu mã PIN!");
                        showPinDialog(message, isSettingPin, userId, onSuccess);
                    }
                });
            } else {
                userRepository.getUserPIN(userId, new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        String storedPin = snapshot.getValue(String.class);
                        if (pin.equals(storedPin)) {
                            view.showMessage("Xác thực mã PIN thành công!");
                            onSuccess.run();
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

    @Override
    public void loadUserData() {
        userRepository.getAllUsers(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                ArrayList<Users> usersList = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Users user = dataSnapshot.getValue(Users.class);
                    if (user.getUserId() != null) {
                        usersList.add(user);
                    }
                }
                view.updateUserList(usersList);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                view.showMessage("Lỗi tải dữ liệu: " + error.getMessage());
            }
        });
    }

    @Override
    public void signOut() {
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(view, GoogleSignInOptions.DEFAULT_SIGN_IN);
        googleSignInClient.signOut().addOnCompleteListener(task -> {
            FirebaseAuth.getInstance().signOut();
            view.navigateToLogin();
        });
    }

    @Override
    public void checkUserLoginStatus() {
        if (auth.getCurrentUser() == null) {
            signOut();
        } else {
            checkUserPinAndProceed();
        }
    }
}
