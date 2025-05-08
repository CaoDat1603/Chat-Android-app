package com.example.myapplication.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Random;

/**
 * Tiện ích xử lý mã xác nhận
 */
public class VerificationUtil {
    private static final String PREFS_NAME = "VerificationPrefs";
    private static final String KEY_VERIFICATION_CODE = "verificationCode";
    private static final String KEY_TIMESTAMP = "timestamp";
    private static final String KEY_EMAIL = "email";

    // Thời gian hiệu lực của mã xác nhận (10 phút, tính bằng millisecond)
    private static final long VERIFICATION_EXPIRY_TIME = 10 * 60 * 1000;

    /**
     * Tạo mã xác nhận mới (6 chữ số) và lưu vào SharedPreferences
     * 
     * @param context Context ứng dụng
     * @param email   Email cần xác nhận
     * @return Mã xác nhận đã tạo
     */
    public static String generateVerificationCode(Context context, String email) {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // Mã 6 chữ số
        String verificationCode = String.valueOf(code);

        // Lưu mã xác nhận và thời gian hết hạn
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_VERIFICATION_CODE, verificationCode);
        editor.putLong(KEY_TIMESTAMP, System.currentTimeMillis());
        editor.putString(KEY_EMAIL, email);
        editor.apply();

        return verificationCode;
    }

    /**
     * Kiểm tra mã xác nhận nhập vào
     * 
     * @param context Context ứng dụng
     * @param code    Mã xác nhận cần kiểm tra
     * @param email   Email cần xác nhận
     * @return true nếu mã đúng và chưa hết hạn, false nếu ngược lại
     */
    public static boolean verifyCode(Context context, String code, String email) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedCode = preferences.getString(KEY_VERIFICATION_CODE, "");
        long timestamp = preferences.getLong(KEY_TIMESTAMP, 0);
        String savedEmail = preferences.getString(KEY_EMAIL, "");

        // Kiểm tra thời gian hiệu lực
        long currentTime = System.currentTimeMillis();
        boolean isExpired = currentTime - timestamp > VERIFICATION_EXPIRY_TIME;

        // Kiểm tra mã và email
        boolean isCodeValid = savedCode.equals(code);
        boolean isEmailValid = savedEmail.equals(email);

        // Xóa mã xác nhận nếu đã sử dụng hoặc hết hạn
        if (isCodeValid || isExpired) {
            clearVerificationCode(context);
        }

        return isCodeValid && !isExpired && isEmailValid;
    }

    /**
     * Xóa mã xác nhận đã lưu
     * 
     * @param context Context ứng dụng
     */
    public static void clearVerificationCode(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(KEY_VERIFICATION_CODE);
        editor.remove(KEY_TIMESTAMP);
        editor.remove(KEY_EMAIL);
        editor.apply();
    }
}