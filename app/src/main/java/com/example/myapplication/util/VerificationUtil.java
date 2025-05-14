package com.example.myapplication.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

/**
 * Tiện ích xử lý mã xác nhận
 */
public class VerificationUtil {
    private static final String PREFS_NAME = "VerificationPrefs";
    private static final String KEY_VERIFICATION_CODE = "verificationCode";
    private static final String KEY_TIMESTAMP = "timestamp";
    private static final String KEY_EMAIL = "email";
    private static final String TAG = "VerificationUtil";

    // Tăng thời gian hiệu lực lên 30 phút để test
    private static final long VERIFICATION_EXPIRY_TIME = 30 * 60 * 1000;

    // Lưu mã xác nhận và email trong biến tĩnh (backup phòng trường hợp
    // SharedPreferences gặp vấn đề)
    private static String lastVerificationCode = null;
    private static String lastVerificationEmail = null;

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

        // Lưu trong biến tĩnh như backup
        lastVerificationCode = verificationCode;
        lastVerificationEmail = email;

        // Thời gian tạo mã
        long timestamp = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String creationTime = sdf.format(new Date(timestamp));

        // Lưu mã xác nhận và thời gian hết hạn
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_VERIFICATION_CODE, verificationCode);
        editor.putLong(KEY_TIMESTAMP, timestamp);
        editor.putString(KEY_EMAIL, email);
        editor.apply();

        // Ghi log mã xác nhận để theo dõi khi email không nhận được
        Log.d(TAG, "Mã xác nhận cho " + email + ": " + verificationCode + " (Tạo lúc: " + creationTime + ")");

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

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String creationTime = sdf.format(new Date(timestamp));

        // Kiểm tra nếu không tìm thấy trong SharedPreferences nhưng có trong biến tĩnh
        if ((savedCode == null || savedCode.isEmpty()) && lastVerificationCode != null) {
            savedCode = lastVerificationCode;
            savedEmail = lastVerificationEmail;
            Log.d(TAG, "Sử dụng mã xác nhận từ bộ nhớ tạm: " + savedCode);
        }

        // Kiểm tra thời gian hiệu lực
        long currentTime = System.currentTimeMillis();
        String currentTimeStr = sdf.format(new Date(currentTime));
        long timeDiff = currentTime - timestamp;
        boolean isExpired = timeDiff > VERIFICATION_EXPIRY_TIME;

        Log.d(TAG, "Kiểm tra mã: " + code + ", mã lưu: " + savedCode);
        Log.d(TAG, "Thời điểm tạo: " + creationTime + ", thời điểm hiện tại: " + currentTimeStr);
        Log.d(TAG, "Độ chênh lệch thời gian: " + (timeDiff / 1000) + " giây, giới hạn: "
                + (VERIFICATION_EXPIRY_TIME / 1000) + " giây");

        // Kiểm tra mã và email
        boolean isCodeValid = savedCode.equals(code);
        boolean isEmailValid = savedEmail.equals(email);

        Log.d(TAG, "Kết quả kiểm tra - Mã: " + isCodeValid + ", Email: " + isEmailValid + ", Còn hạn: " + !isExpired);

        // Chỉ xóa mã xác nhận nếu đã hết hạn
        if (isExpired) {
            clearVerificationCode(context);
            Log.d(TAG, "Mã xác nhận đã hết hạn và bị xóa");
        }

        if (isCodeValid && isEmailValid && !isExpired) {
            return true;
        } else if (code.equals(lastVerificationCode) && email.equals(lastVerificationEmail)) {
            // Phương án dự phòng: nếu trùng với mã trong bộ nhớ tạm, cũng chấp nhận
            Log.d(TAG, "Xác thực thành công qua bộ nhớ tạm");
            return true;
        }

        return false;
    }

    /**
     * Xóa mã xác nhận đã lưu
     * 
     * @param context Context ứng dụng
     */
    public static void clearVerificationCode(Context context) {
        Log.d(TAG, "Xóa mã xác nhận trong SharedPreferences và bộ nhớ tạm");

        // Xóa trong SharedPreferences
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(KEY_VERIFICATION_CODE);
        editor.remove(KEY_TIMESTAMP);
        editor.remove(KEY_EMAIL);
        editor.apply();

        // Xóa trong biến tĩnh
        lastVerificationCode = null;
        lastVerificationEmail = null;
    }

    /**
     * Lấy thông tin mã xác nhận hiện tại từ SharedPreferences
     * Hữu ích để kiểm tra hoặc debug khi email không nhận được
     * 
     * @param context Context ứng dụng
     * @return Chuỗi thông tin mã xác nhận hoặc null nếu không có
     */
    public static String getCurrentVerificationInfo(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedCode = preferences.getString(KEY_VERIFICATION_CODE, null);
        long timestamp = preferences.getLong(KEY_TIMESTAMP, 0);
        String savedEmail = preferences.getString(KEY_EMAIL, null);

        // Nếu không tìm thấy trong SharedPreferences, kiểm tra biến tĩnh
        if ((savedCode == null || savedEmail == null) && lastVerificationCode != null) {
            return String.format("Mã xác nhận (từ bộ nhớ tạm) cho %s: %s",
                    lastVerificationEmail, lastVerificationCode);
        }

        if (savedCode == null || savedEmail == null) {
            return null;
        }

        // Kiểm tra thời gian hiệu lực
        long currentTime = System.currentTimeMillis();
        long timeRemaining = VERIFICATION_EXPIRY_TIME - (currentTime - timestamp);
        boolean isExpired = timeRemaining <= 0;

        if (isExpired) {
            return "Mã xác nhận đã hết hạn";
        }

        // Định dạng thời gian còn lại thành phút:giây
        long minutesRemaining = (timeRemaining / (60 * 1000));
        long secondsRemaining = (timeRemaining % (60 * 1000)) / 1000;

        return String.format("Mã xác nhận cho %s: %s (còn hiệu lực: %02d:%02d)",
                savedEmail, savedCode, minutesRemaining, secondsRemaining);
    }
}