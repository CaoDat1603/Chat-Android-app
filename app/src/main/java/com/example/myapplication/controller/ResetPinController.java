package com.example.myapplication.controller;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.service.IEmailService;
import com.example.myapplication.service.impl.EmailServiceImpl;
import com.example.myapplication.util.VerificationUtil;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Random;

public class ResetPinController {
    private static final String TAG = "ResetPinController";
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private Context context;
    private IEmailService emailService;

    // Constructor khởi tạo FirebaseAuth và FirebaseDatabase
    public ResetPinController(Context context) {
        this.context = context;
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        emailService = new EmailServiceImpl();
    }

    // Xử lý đăng nhập bằng Google và gửi mã xác thực
    public void firebaseAuthWithGoogle(GoogleSignInAccount acct, OnPinResetListener listener) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        auth.signInWithCredential(credential)
                .addOnCompleteListener((AppCompatActivity) context, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = auth.getCurrentUser();

                            if (user != null) {
                                // Gửi mã xác thực qua email
                                sendVerificationCode(user.getEmail(), listener);
                            } else {
                                listener.onError("Người dùng không tồn tại!");
                            }
                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            listener.onError("Authentication Failed: " + task.getException().getMessage());
                        }
                    }
                });
    }

    // Gửi mã xác thực đến email
    private void sendVerificationCode(String email, OnPinResetListener listener) {
        // Tạo mã xác thực và lưu vào SharedPreferences
        String verificationCode = VerificationUtil.generateVerificationCode(context, email);
        Log.d(TAG, "Đã tạo mã xác nhận mới: " + verificationCode + " cho email: " + email);

        // Gửi mã xác thực qua email
        emailService.sendVerificationCode(email, verificationCode, new IEmailService.OnEmailSendListener() {
            @Override
            public void onSuccess(String message) {
                listener.onCodeSent("Mã xác nhận đã được gửi đến email " + email);
            }

            @Override
            public void onError(String errorMessage) {
                listener.onError("Không thể gửi mã xác nhận: " + errorMessage);
                // Hiển thị lại thông tin mã để theo dõi khi email không gửi được
                Log.e(TAG, "Lỗi gửi email. Xem mã trong log hoặc dùng getCurrentVerificationInfo");
            }
        });
    }

    /**
     * Phương thức mới: Chỉ xác thực mã, không đặt lại PIN
     * 
     * @param email    Email người dùng
     * @param code     Mã xác nhận
     * @param listener Listener xử lý kết quả
     */
    public void verifyCode(String email, String code, OnVerificationListener listener) {
        Log.d(TAG, "Đang xác thực mã: " + code + " cho email: " + email);

        // Kiểm tra mã xác thực
        boolean isValid = VerificationUtil.verifyCode(context, code, email);

        if (isValid) {
            Log.d(TAG, "Mã xác nhận hợp lệ");
            listener.onVerified("Mã xác nhận hợp lệ");
        } else {
            // Thêm log để xem thông tin mã xác nhận hiện tại
            String verificationInfo = VerificationUtil.getCurrentVerificationInfo(context);
            if (verificationInfo != null) {
                Log.d(TAG, "Thông tin mã xác nhận: " + verificationInfo);
            } else {
                Log.d(TAG, "Không tìm thấy mã xác nhận cho email này hoặc mã đã hết hạn");
            }

            listener.onError("Mã xác nhận không hợp lệ hoặc đã hết hạn!");
        }
    }

    // Xác thực mã và cho phép đặt PIN mới
    public void verifyCodeAndResetPin(String email, String code, String newPin, OnPinResetListener listener) {
        Log.d(TAG, "Thực hiện xác thực và đổi PIN: email=" + email + ", code=" + code);

        // Kiểm tra mã xác thực
        boolean isValid = VerificationUtil.verifyCode(context, code, email);

        if (isValid) {
            Log.d(TAG, "Mã xác thực hợp lệ, tiến hành cập nhật PIN mới: " + newPin);

            // Mã xác thực hợp lệ, tiến hành cập nhật PIN
            FirebaseUser user = auth.getCurrentUser();
            if (user != null) {
                String userId = user.getUid();
                DatabaseReference userRef = database.getReference("user").child(userId);

                userRef.child("PIN").setValue(newPin).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Cập nhật PIN thành công");
                        // Xóa mã xác nhận sau khi đã hoàn tất toàn bộ quy trình
                        VerificationUtil.clearVerificationCode(context);
                        listener.onSuccess("Mã PIN mới đã được thiết lập thành công!");
                    } else {
                        Log.e(TAG, "Cập nhật PIN thất bại", task.getException());
                        listener.onError("Không thể cập nhật mã PIN: " + task.getException().getMessage());
                    }
                });
            } else {
                Log.e(TAG, "Người dùng không tồn tại hoặc chưa đăng nhập");
                listener.onError("Người dùng không tồn tại hoặc chưa đăng nhập!");
            }
        } else {
            // Thêm log để xem thông tin mã xác nhận hiện tại
            String verificationInfo = VerificationUtil.getCurrentVerificationInfo(context);
            if (verificationInfo != null) {
                Log.d(TAG, "Thông tin mã xác nhận: " + verificationInfo);
            } else {
                Log.d(TAG, "Không tìm thấy mã xác nhận cho email này hoặc mã đã hết hạn");
            }

            listener.onError("Mã xác nhận không hợp lệ hoặc đã hết hạn!");
        }
    }

    // Tạo số ngẫu nhiên 6 chữ số cho PIN
    public static String generateRandomPin() {
        Random random = new Random();
        int pin = 100000 + random.nextInt(900000); // Pin có 6 chữ số
        return String.valueOf(pin);
    }

    // Interface xử lý kết quả reset PIN
    public interface OnPinResetListener {
        void onCodeSent(String message);

        void onSuccess(String message);

        void onError(String error);
    }

    /**
     * Interface lắng nghe kết quả xác thực mã
     */
    public interface OnVerificationListener {
        void onCodeSent(String message);

        void onVerified(String message);

        void onError(String errorMessage);
    }
}