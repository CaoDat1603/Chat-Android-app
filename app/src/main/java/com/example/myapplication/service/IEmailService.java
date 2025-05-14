package com.example.myapplication.service;

public interface IEmailService {
    /**
     * Gửi mã xác nhận đến email người dùng
     * 
     * @param email    Địa chỉ email người dùng
     * @param code     Mã xác nhận
     * @param listener Callback lắng nghe kết quả gửi email
     */
    void sendVerificationCode(String email, String code, OnEmailSendListener listener);

    /**
     * Gửi email chứa mã PIN mới đến email người dùng
     * 
     * @param email    Địa chỉ email người dùng
     * @param newPin   Mã PIN mới đã được tạo
     * @param listener Callback lắng nghe kết quả gửi email
     */
    void sendPinResetCode(String email, String newPin, OnEmailSendListener listener);

    /**
     * Interface lắng nghe kết quả gửi email
     */
    interface OnEmailSendListener {
        void onSuccess(String message);

        void onError(String errorMessage);
    }
}