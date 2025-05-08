package com.example.myapplication.controller;

import android.content.Context;
import android.util.Log;

import com.example.myapplication.service.IEmailService;
import com.example.myapplication.service.impl.EmailServiceImpl;
import com.example.myapplication.util.VerificationUtil;

/**
 * Controller xử lý các quy trình xác nhận
 */
public class VerificationController {
    private static final String TAG = "VerificationController";
    private Context context;
    private IEmailService emailService;

    public VerificationController(Context context) {
        this.context = context;
        this.emailService = new EmailServiceImpl();
    }

    /**
     * Tạo và gửi mã xác nhận đến email người dùng
     * 
     * @param email    Email người dùng
     * @param listener Listener xử lý kết quả
     */
    public void sendVerificationCode(String email, OnVerificationListener listener) {
        // Tạo mã xác nhận
        String verificationCode = VerificationUtil.generateVerificationCode(context, email);

        // Ghi log để dễ dàng kiểm tra nếu email không đến được
        Log.i(TAG, "Đã tạo mã xác nhận: " + verificationCode + " cho email: " + email);

        // Gửi mã xác nhận qua email
        emailService.sendVerificationCode(email, verificationCode, new IEmailService.OnEmailSendListener() {
            @Override
            public void onSuccess(String message) {
                listener.onCodeSent("Mã xác nhận đã được gửi đến email " + email);
            }

            @Override
            public void onError(String errorMessage) {
                // Hiển thị lại mã xác nhận trong log khi gặp lỗi email
                Log.e(TAG, "Lỗi gửi email. Mã xác nhận: " + verificationCode + " cho email: " + email);
                listener.onError("Không thể gửi mã xác nhận: " + errorMessage);
            }
        });
    }

    /**
     * Xác minh mã xác nhận đã nhập
     * 
     * @param email    Email người dùng
     * @param code     Mã xác nhận đã nhập
     * @param listener Listener xử lý kết quả
     */
    public void verifyCode(String email, String code, OnVerificationListener listener) {
        boolean isValid = VerificationUtil.verifyCode(context, code, email);

        if (isValid) {
            listener.onVerified("Xác minh thành công!");
        } else {
            listener.onError("Mã xác nhận không hợp lệ hoặc đã hết hạn!");
        }
    }

    /**
     * Lấy thông tin mã xác nhận hiện tại (để hiển thị khi debug hoặc khi email
     * không nhận được)
     * 
     * @return Thông tin mã xác nhận hoặc thông báo không có mã
     */
    public String getCurrentVerificationInfo() {
        String info = VerificationUtil.getCurrentVerificationInfo(context);
        if (info == null) {
            return "Không có mã xác nhận nào đang hoạt động";
        }
        return info;
    }

    /**
     * Interface lắng nghe kết quả xác thực
     */
    public interface OnVerificationListener {
        void onCodeSent(String message);

        void onVerified(String message);

        void onError(String errorMessage);
    }
}