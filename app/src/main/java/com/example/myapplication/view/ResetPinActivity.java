package com.example.myapplication.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.SignInButton;
import com.example.myapplication.R;
import com.example.myapplication.controller.ResetPinController;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;


public class ResetPinActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 9001; // Request code for Google Sign-In
    private GoogleSignInClient googleSignInClient;
    private ResetPinController resetPinController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_pin);

        ImageButton turnback = findViewById(R.id.turnback);
        // Turn back
        turnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // Khởi tạo ResetPinController
        resetPinController = new ResetPinController(this);

        // Cấu hình Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Gán sự kiện cho nút Google Sign-In
        SignInButton btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn); // Sửa thành SignInButton
        btnGoogleSignIn.setOnClickListener(v -> signInAndResetPin());
    }

    // Phương thức để đăng nhập và reset PIN
    private void signInAndResetPin() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            if (task.isSuccessful()) {
                GoogleSignInAccount account = task.getResult();
                if (account != null) {
                    // Đăng nhập với Google và thực hiện reset PIN
                    resetPinController.firebaseAuthWithGoogle(account, new ResetPinController.OnPinResetListener() {
                        @Override
                        public void onSuccess(String message) {
                            // Đăng nhập và reset PIN thành công
                            Toast.makeText(ResetPinActivity.this, message, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ResetPinActivity.this, LoginActivity.class);
                            startActivity(intent);

                            // Kết thúc trang hiện tại để không quay lại được bằng nút Back
                            finish();
                        }

                        @Override
                        public void onError(String error) {
                            // Đăng nhập hoặc reset PIN thất bại
                            Toast.makeText(ResetPinActivity.this, error, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                Toast.makeText(this, "Đăng nhập Google thất bại.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}