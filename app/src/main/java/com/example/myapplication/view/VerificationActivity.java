package com.example.myapplication.view;

import android.annotation.SuppressLint;
import android.graphics.Insets;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.R;
import com.example.myapplication.controller.VerificationController;

/**
 * Activity xử lý xác thực email
 */
public class VerificationActivity extends AppCompatActivity {
    private EditText etEmail, etVerificationCode;
    private Button btnSendCode, btnVerify;
    private TextView tvStatus;
    private VerificationController verificationController;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);
        EdgeToEdge.enable(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars()).toPlatformInsets();
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Khởi tạo controller
        verificationController = new VerificationController(this);

        // Khởi tạo các view
        etEmail = findViewById(R.id.etEmail);
        etVerificationCode = findViewById(R.id.etVerificationCode);
        btnSendCode = findViewById(R.id.btnSendCode);
        btnVerify = findViewById(R.id.btnVerify);
        tvStatus = findViewById(R.id.tvStatus);

        // Sự kiện gửi mã
        btnSendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();

                if (email.isEmpty()) {
                    showToast("Vui lòng nhập email");
                    return;
                }

                // Hiển thị trạng thái đang gửi
                tvStatus.setText("Đang gửi mã xác nhận...");
                tvStatus.setVisibility(View.VISIBLE);

                // Gửi mã xác nhận
                verificationController.sendVerificationCode(email, new VerificationController.OnVerificationListener() {
                    @Override
                    public void onCodeSent(String message) {
                        runOnUiThread(() -> {
                            tvStatus.setText(message);
                            btnVerify.setEnabled(true);
                            showToast(message);
                        });
                    }

                    @Override
                    public void onVerified(String message) {
                        // Không được gọi trong trường hợp này
                    }

                    @Override
                    public void onError(String errorMessage) {
                        runOnUiThread(() -> {
                            tvStatus.setText(errorMessage);
                            showToast(errorMessage);
                        });
                    }
                });
            }
        });

        // Sự kiện xác minh mã
        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String code = etVerificationCode.getText().toString().trim();

                if (email.isEmpty()) {
                    showToast("Vui lòng nhập email");
                    return;
                }

                if (code.isEmpty()) {
                    showToast("Vui lòng nhập mã xác nhận");
                    return;
                }

                // Xác minh mã
                verificationController.verifyCode(email, code, new VerificationController.OnVerificationListener() {
                    @Override
                    public void onCodeSent(String message) {
                        // Không được gọi trong trường hợp này
                    }

                    @Override
                    public void onVerified(String message) {
                        runOnUiThread(() -> {
                            tvStatus.setText(message);
                            // TODO: Thực hiện hành động sau khi xác minh thành công
                            showToast(message);
                        });
                    }

                    @Override
                    public void onError(String errorMessage) {
                        runOnUiThread(() -> {
                            tvStatus.setText(errorMessage);
                            showToast(errorMessage);
                        });
                    }
                });
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}