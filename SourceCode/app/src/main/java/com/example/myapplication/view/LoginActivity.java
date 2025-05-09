package com.example.myapplication.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.controller.LoginController;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth auth;
    private LoginController controller;
    private TextView createAccountButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        controller = new LoginController(this);
        createAccountButton = findViewById(R.id.create_account);

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Set Google Sign-In button listener
        SignInButton googleSignInButton = findViewById(R.id.googleSignInButton);
        googleSignInButton.setOnClickListener(v -> signIn());

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            // Kiểm tra trạng thái xác thực bổ sung tại đây nếu cần thiết
            navigateToMainActivity(currentUser);
        }
    }

    private void signIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            try {
                GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class);
                if (account != null) {
                    controller.firebaseAuthWithGoogle(account, new LoginController.OnLoginCompleteListener() {
                        @Override
                        public void onSuccess(FirebaseUser user) {
                            // **Chỉ chuyển hướng nếu người dùng hợp lệ**
                            if (user != null) {
                                navigateToMainActivity(user);
                            } else {
                                showRetryDialog("Đăng nhập thất bại: Không tìm thấy thông tin người dùng.");
                            }
                        }

                        @Override
                        public void onError(String errorMessage) {
                            showRetryDialog(errorMessage); // Hiển thị hộp thoại khi lỗi xảy ra
                        }
                    });
                }
            } catch (ApiException e) {
                Log.e("LoginActivity", "Google Sign-In failed", e);
                showRetryDialog("Google Sign-In failed: " + e.getMessage());
            }
        }
    }


    // Hiển thị hộp thoại lỗi và cho phép thử lại
    private void showRetryDialog(String errorMessage) {
        new AlertDialog.Builder(this)
                .setTitle("Đăng nhập thất bại")
                .setMessage(errorMessage)
                .setPositiveButton("Thử lại", (dialog, which) -> signIn()) // Thử lại
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss()) // Đóng hộp thoại
                .show();
    }


    private void navigateToMainActivity(FirebaseUser user) {
        if (user != null) {
            // Chuyển tới MainActivity nếu đăng nhập thành công
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("USER_NAME", user.getDisplayName());
            intent.putExtra("USER_EMAIL", user.getEmail());
            startActivity(intent);
            finish();
        } else {
            showRetryDialog("Lỗi: Không tìm thấy thông tin người dùng.");
        }
    }


    public void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
