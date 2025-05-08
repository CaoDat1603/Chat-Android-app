package com.example.myapplication.service.impl;

import android.util.Log;

import com.example.myapplication.service.IEmailService;

import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailServiceImpl implements IEmailService {
    private static final String TAG = "EmailServiceImpl";

    private static final String EMAIL_HOST = "smtp.gmail.com";
    private static final String EMAIL_PORT = "587";
    private static final String EMAIL_USERNAME = "tdthanh.dev2025@gmail.com";
    private static final String EMAIL_PASSWORD = "vgrn halh ocpw wrki";
    private static final String EMAIL_FROM = "App Chat <tdthanh.dev2025@gmail.com>"; // Tên hiển thị

    private final Executor executor = Executors.newSingleThreadExecutor();

    @Override
    public void sendVerificationCode(String email, String code, OnEmailSendListener listener) {
        String subject = "Mã xác nhận tài khoản";
        String messageBody = "Xin chào,\n\n"
                + "Mã xác nhận của bạn là: " + code + "\n\n"
                + "Mã này có hiệu lực trong 10 phút. Vui lòng không chia sẻ mã này với người khác.\n\n"
                + "Trân trọng,\n"
                + "Đội ngũ phát triển ứng dụng Chat";

        sendEmail(email, subject, messageBody, listener);
    }

    @Override
    public void sendPinResetCode(String email, String newPin, OnEmailSendListener listener) {
        String subject = "Yêu cầu đặt lại mã PIN";
        String messageBody = "Xin chào,\n\n"
                + "Chúng tôi đã nhận được yêu cầu đặt lại mã PIN của bạn.\n\n"
                + "Mã PIN mới của bạn là: " + newPin + "\n\n"
                + "Vui lòng sử dụng mã PIN này để đăng nhập vào ứng dụng. Bạn có thể thay đổi mã PIN này sau khi đăng nhập.\n\n"
                + "Nếu bạn không yêu cầu đặt lại mã PIN, vui lòng bỏ qua email này và liên hệ với chúng tôi.\n\n"
                + "Trân trọng,\n"
                + "Đội ngũ phát triển ứng dụng Chat";

        sendEmail(email, subject, messageBody, listener);
    }

    private void sendEmail(final String recipientEmail, final String subject,
            final String messageBody, final OnEmailSendListener listener) {
        executor.execute(() -> {
            try {
                Properties props = new Properties();
                props.put("mail.smtp.host", EMAIL_HOST);
                props.put("mail.smtp.port", EMAIL_PORT);
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");

                // Tạo session
                Session session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(EMAIL_USERNAME, EMAIL_PASSWORD);
                    }
                });

                // Tạo message
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(EMAIL_FROM));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
                message.setSubject(subject);
                message.setText(messageBody);

                // Gửi message
                Transport.send(message);

                // Chạy callback trên main thread
                android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                mainHandler.post(() -> listener.onSuccess("Email đã được gửi thành công!"));
            } catch (MessagingException e) {
                Log.e(TAG, "Error sending email: " + e.getMessage());
                final String errorMessage = e.getMessage();

                // Chạy callback trên main thread
                android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                mainHandler.post(() -> listener.onError("Không thể gửi email: " + errorMessage));
            }
        });
    }
}