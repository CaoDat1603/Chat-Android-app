package com.example.myapplication.service.impl;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.controller.LoginController;
import com.example.myapplication.controller.RegisterController;
import com.example.myapplication.controller.ResetPinController;
import com.example.myapplication.model.Users;
import com.example.myapplication.repository.FirebaseRepositor;
import com.example.myapplication.repository.UserRepository;
import com.example.myapplication.service.IEmailService;
import com.example.myapplication.service.IFirebaseService;
import com.example.myapplication.util.VerificationUtil;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FirebaseServiceImpl implements IFirebaseService {
    private static final String TAG = "LoginController";
    private final FirebaseRepositor repositor;
    private final UserRepository userRepository;
    private IEmailService emailService;

    public FirebaseServiceImpl() {
        this.repositor = new FirebaseRepositor();
        this.userRepository = new UserRepository();
        emailService = new EmailServiceImpl();
    }

    //Đăng nhập
    @Override
    public void firebaseAuthWithGoogle(GoogleSignInAccount acct, LoginController.OnLoginCompleteListener listener, Context context) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        // Kiểm tra email có đuôi hợp lệ
        String email = acct.getEmail();
        if (!isValidEmail(email)) {
            listener.onError("Email không hợp lệ! Vui lòng sử dụng email có đuôi @ut.edu.vn.");
            return;
        }

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        repositor.setAuth(credential, (AppCompatActivity) context,  new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "signInWithCredential:success");
                    FirebaseUser user = repositor.getUserCurrent();

                    if (user != null) {
                        checkUser(user, listener);
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

    private void checkUser(FirebaseUser firebaseUser, LoginController.OnLoginCompleteListener listener) {
        String userId = firebaseUser.getUid();
        userRepository.getUser(userId).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                Log.d(TAG, "User already exists.");
                listener.onSuccess(firebaseUser);
            } else {
                Log.w(TAG, "signInWithCredential: failure", task.getException());
                String errorMessage = task.getException() != null ? task.getException().getMessage() : "Lỗi không xác định";
                listener.onError("Authentication Failed: " + errorMessage);
            }
        }).addOnFailureListener(e -> {
            listener.onError("Lỗi khi kiểm tra người dùng: " + e.getMessage());
        });
    }

    //Đăng ký
    @Override
    public void firebaseAuthWithGoogleReg(GoogleSignInAccount acct, RegisterController.OnRegistrationCompleteListener listener, Context context) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        // Kiểm tra email có đuôi hợp lệ
        String email = acct.getEmail();
        if (!isValidEmail(email)) {
            listener.onError("Email không hợp lệ! Vui lòng sử dụng email có đuôi @ut.edu.");
            return;
        }

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        repositor.setAuth(credential, (AppCompatActivity) context, new OnCompleteListener<com.google.firebase.auth.AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<com.google.firebase.auth.AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "signInWithCredential:success");
                    FirebaseUser user = repositor.getUserCurrent();

                    if (user != null) {
                        saveUser(user, listener);
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

    @Override
    public void firebaseAuthWithGoogleVef(GoogleSignInAccount acct, ResetPinController.OnPinResetListener listener, Context context) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        repositor.setAuth(credential, (AppCompatActivity) context,  new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "signInWithCredential:success");
                    FirebaseUser user = repositor.getUserCurrent();

                    if (user != null) {
                        // Gửi mã xác thực qua email
                        sendVerificationCode(user.getEmail(), listener, context);
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

    /**
     * Phương thức: Chỉ xác thực mã, không đặt lại PIN
     *
     * @param email    Email người dùng
     * @param code     Mã xác nhận
     * @param listener Listener xử lý kết quả
     */
    @Override
    public void verifyCode(String email, String code, ResetPinController.OnVerificationListener listener, Context context) {
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

    @Override
    public void verifyCodeAndResetPin(String email, String code, String newPin, ResetPinController.OnPinResetListener listener, Context context) {
        Log.d(TAG, "Thực hiện xác thực và đổi PIN: email=" + email + ", code=" + code);

        // Kiểm tra mã xác thực
        boolean isValid = VerificationUtil.verifyCode(context, code, email);

        if (isValid) {
            Log.d(TAG, "Mã xác thực hợp lệ, tiến hành cập nhật PIN mới: " + newPin);

            // Mã xác thực hợp lệ, tiến hành cập nhật PIN
            FirebaseUser user = repositor.getUserCurrent();;
            if (user != null) {
                String userId = user.getUid();
                userRepository.setUserPIN(userId, newPin, task -> {
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
            String verificationInfo = VerificationUtil.getCurrentVerificationInfo(context);
            if (verificationInfo != null) {
                Log.d(TAG, "Thông tin mã xác nhận: " + verificationInfo);
            } else {
                Log.d(TAG, "Không tìm thấy mã xác nhận cho email này hoặc mã đã hết hạn");
            }

            listener.onError("Mã xác nhận không hợp lệ hoặc đã hết hạn!");
        }
    }

    // Gửi mã xác thực đến email
    private void sendVerificationCode(String email, ResetPinController.OnPinResetListener listener, Context context) {
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

    private void saveUser(FirebaseUser firebaseUser, RegisterController.OnRegistrationCompleteListener listener) {
        String userId = firebaseUser.getUid();
        userRepository.getUser(userId).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                Log.d(TAG, "User already exists.");
                listener.onSuccess();
            } else {
                Users userModel = new Users(
                        userId,
                        firebaseUser.getDisplayName(),
                        firebaseUser.getEmail(),
                        "Xin chào!",
                        null
                );

                userRepository.saveUser(userId, userModel, saveTask -> {
                    if (saveTask.isSuccessful()) {
                        listener.onSuccess();
                    } else {
                        listener.onError("Lỗi khi lưu thông tin người dùng!");
                    }
                }).addOnFailureListener(e -> {
                    listener.onError("Lỗi khi kiểm tra người dùng: " + e.getMessage());
                });;
            }
        });
    }

    private boolean isValidEmail(String email) {
        return email != null && email.endsWith("@ut.edu.vn");
    }

    @Override
    public void updateDateOnline() {
        FirebaseUser user = repositor.getUserCurrent();
        if (user == null) return;

        // Lấy thời gian hiện tại dưới dạng String
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                .format(new Date());
        userRepository.updateStatus(user.getUid(), currentTime);
    }
}
