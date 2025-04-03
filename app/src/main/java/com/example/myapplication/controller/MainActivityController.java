package com.example.myapplication.controller;

import android.app.AlertDialog;
import android.app.Dialog;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.myapplication.view.MainActivity;
import com.example.myapplication.model.Users;
import com.example.myapplication.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;

public class MainActivityController {

    private MainActivity view; // Direct reference to MainActivity
    private FirebaseAuth auth;
    private DatabaseReference reference;
    private Dialog logoutDialog; // Add this as a class field

    public MainActivityController(MainActivity view) {
        this.view = view;
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference().child("user");
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
                        showPinDialog("Vui lòng thiết lập mã PIN mới:", true, userId, () -> loadUserData());
                    } else {
                        // Yêu cầu xác thực mã PIN
                        showPinDialog("Nhập mã PIN của bạn:", false, userId, () -> loadUserData());
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

    // Load user data from Firebase
    public void loadUserData() {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                ArrayList<Users> usersList = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Users user = dataSnapshot.getValue(Users.class);
                    usersList.add(user);
                }
                view.updateUserList(usersList); // Call the method to update user list
            }

            @Override
            public void onCancelled(DatabaseError error) {
                view.showMessage("Error loading data: " + error.getMessage()); // Show error message
            }
        });
    }

    public void signOut() {
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(view, GoogleSignInOptions.DEFAULT_SIGN_IN);
        googleSignInClient.signOut().addOnCompleteListener(task -> {
            // Firebase sign-out
            FirebaseAuth.getInstance().signOut();
            view.navigateToLogin();
        });

    }

    // Check user login status
    public void checkUserLoginStatus() {
        if (auth.getCurrentUser() == null) {
            signOut();
        } else {
            checkUserPinAndProceed(); // Load user data if user is logged in
        }
    }

    // Show logout dialog
    public void showLogoutDialog() {
        logoutDialog = new AlertDialog.Builder(view)
                .setTitle("Bạn có chắc chắn muốn đăng xuất không?")
                .setMessage("Đăng xuất sẽ đưa bạn trở lại trang đăng nhập.")
                .setPositiveButton("Đăng xuất", (dialog, which) -> signOut())
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .create();
        logoutDialog.show();
    }

    public void dismissDialogs() {
        if (logoutDialog != null && logoutDialog.isShowing()) {
            logoutDialog.dismiss();
        }
    }

    // Phương thức lấy tên người dùng

    // Interface listener để truyền kết quả về
    public interface OnUserNameFetchedListener {
        void onFetched(String userName);
    }

    // Navigate to group chat activity
    public void navigateToChatGroup() {
        view.navigateToChatGroup(); // Navigate to chat group
    }

    public void navigateToProfile() {
        view.navigateToProfile();
    }

}