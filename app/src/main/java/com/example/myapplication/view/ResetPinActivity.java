package com.example.myapplication.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
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
    private TextView tvStatus;
    private TextView tvVerificationCode; // Thêm TextView để hiển thị mã xác nhận

    // Các phần giao diện cho từng bước
    private LinearLayout loginStep, verificationStep, resetPinStep;

    // Các trường nhập liệu
    private EditText etVerificationCode, etNewPin;
    private Button btnVerifyCode, btnSetNewPin;

    // Lưu trữ email người dùng sau khi đăng nhập
    private String userEmail;

    // Lưu trữ mã xác thực đã nhập
    private String verificationCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_pin);

        // Khởi tạo controller
        resetPinController = new ResetPinController(this);

        // Khởi tạo các phần giao diện
        loginStep = findViewById(R.id.loginStep);
        verificationStep = findViewById(R.id.verificationStep);
        resetPinStep = findViewById(R.id.resetPinStep);
        tvStatus = findViewById(R.id.tvStatus);

        // Tìm TextView để hiển thị mã xác nhận
        tvVerificationCode = findViewById(R.id.tvVerificationCode);
        if (tvVerificationCode == null) {
            // Nếu không có trong layout, tạo một TextView mới và thêm vào verificationStep
            tvVerificationCode = new TextView(this);
            tvVerificationCode.setId(View.generateViewId());
            tvVerificationCode.setPadding(16, 16, 16, 16);
            tvVerificationCode.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            tvVerificationCode.setTextSize(18);
            tvVerificationCode.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

            // Thêm TextView vào layout verificationStep ở vị trí đầu tiên
            if (verificationStep instanceof LinearLayout) {
                ((LinearLayout) verificationStep).addView(tvVerificationCode, 0);
            }
        }

        // Khởi tạo các trường nhập liệu
        etVerificationCode = findViewById(R.id.etVerificationCode);
        etNewPin = findViewById(R.id.etNewPin);
        btnVerifyCode = findViewById(R.id.btnVerifyCode);
        btnSetNewPin = findViewById(R.id.btnSetNewPin);

        // Nút quay lại
        ImageButton turnback = findViewById(R.id.turnback);
        turnback.setOnClickListener(view -> finish());

        // Cấu hình Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Gán sự kiện cho nút Google Sign-In
        SignInButton btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        btnGoogleSignIn.setOnClickListener(v -> signInAndGetVerificationCode());

        // Gán sự kiện cho nút xác nhận mã
        btnVerifyCode.setOnClickListener(v -> verifyCode());

        // Gán sự kiện cho nút đặt PIN mới
        btnSetNewPin.setOnClickListener(v -> setNewPin());

        // Hiển thị bước đầu tiên
        showStep(1);
    }

    // Phương thức để đăng nhập và lấy mã xác nhận
    private void signInAndGetVerificationCode() {
        showStatus("Đang xử lý...");

        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // Phương thức xác thực mã
    private void verifyCode() {
        verificationCode = etVerificationCode.getText().toString().trim();

        if (verificationCode.isEmpty()) {
            showToast("Vui lòng nhập mã xác nhận");
            return;
        }

        if (verificationCode.length() != 6) {
            showToast("Mã xác nhận phải có 6 chữ số");
            return;
        }

        showStatus("Đang xác thực mã...");

        // Xóa đoạn code tạo mã PIN ngẫu nhiên
        // Chỉ thực hiện xác thực mã, không đặt PIN
        resetPinController.verifyCode(userEmail, verificationCode, new ResetPinController.OnVerificationListener() {
            @Override
            public void onCodeSent(String message) {
                // Không được gọi trong trường hợp này
            }

            @Override
            public void onVerified(String message) {
                // Khi xác thực thành công, chuyển sang bước 3 để nhập PIN mới
                runOnUiThread(() -> {
                    showStep(3);
                    showStatus("Mã xác nhận hợp lệ. Vui lòng nhập mã PIN mới.");
                    // Xóa giá trị cũ trong trường nhập PIN nếu có
                    etNewPin.setText("");
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showToast(error);
                    showStatus(error);
                });
            }
        });
    }

    // Phương thức đặt PIN mới
    private void setNewPin() {
        String newPin = etNewPin.getText().toString().trim();

        if (newPin.isEmpty()) {
            showToast("Vui lòng nhập mã PIN mới");
            return;
        }

        if (newPin.length() != 6) {
            showToast("Mã PIN phải có 6 chữ số");
            return;
        }

        showStatus("Đang cập nhật mã PIN...");

        resetPinController.verifyCodeAndResetPin(userEmail, verificationCode, newPin,
                new ResetPinController.OnPinResetListener() {
                    @Override
                    public void onCodeSent(String message) {
                        // Không được gọi trong trường hợp này
                    }

                    @Override
                    public void onSuccess(String message) {
                        runOnUiThread(() -> {
                            showToast("Mã PIN mới đã được thiết lập thành công!");
                            showStatus("Mã PIN mới đã được thiết lập thành công!");

                            // Delay 2 giây trước khi chuyển hướng
                            tvStatus.postDelayed(() -> {
                                Intent intent = new Intent(ResetPinActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            }, 2000);
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            showToast(error);
                            showStatus(error);
                        });
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            if (task.isSuccessful()) {
                GoogleSignInAccount account = task.getResult();
                if (account != null) {
                    // Lưu email người dùng
                    userEmail = account.getEmail();

                    // Hiển thị đang xác thực
                    showStatus("Đang xác thực tài khoản...");

                    // Đăng nhập với Google và lấy mã xác thực
                    resetPinController.firebaseAuthWithGoogle(account, new ResetPinController.OnPinResetListener() {
                        @Override
                        public void onCodeSent(String message) {
                            runOnUiThread(() -> {
                                // Chuyển sang bước tiếp theo
                                showStep(2);

                                // Hiển thị mã xác nhận trực tiếp trên giao diện
                                if (message.contains("Mã xác nhận của bạn là:")) {
                                    // Hiển thị mã xác nhận đặc biệt
                                    tvVerificationCode.setText(message);
                                    tvVerificationCode.setVisibility(View.VISIBLE);

                                    // Lấy mã từ thông báo và đặt vào ô nhập
                                    String[] parts = message.split(":");
                                    if (parts.length > 1) {
                                        String code = parts[1].trim().split("\n")[0].trim();
                                        etVerificationCode.setText(code);
                                    }
                                }

                                showStatus("Mã xác nhận đã được gửi. Bạn có thể xem mã ở trên.");
                                showToast("Mã xác nhận đã được tạo");
                            });
                        }

                        @Override
                        public void onSuccess(String message) {
                            // Không được gọi trong bước này
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> {
                                showToast(error);
                                showStatus(error);
                            });
                        }
                    });
                }
            } else {
                showStatus("Đăng nhập Google thất bại.");
                showToast("Đăng nhập Google thất bại.");
            }
        }
    }

    // Hiển thị bước tương ứng
    private void showStep(int step) {
        loginStep.setVisibility(step == 1 ? View.VISIBLE : View.GONE);
        verificationStep.setVisibility(step == 2 ? View.VISIBLE : View.GONE);
        resetPinStep.setVisibility(step == 3 ? View.VISIBLE : View.GONE);
    }

    // Hiển thị trạng thái
    private void showStatus(String message) {
        tvStatus.setText(message);
        tvStatus.setVisibility(View.VISIBLE);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}