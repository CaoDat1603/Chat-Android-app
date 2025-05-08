package com.example.myapplication.controller;

import com.example.myapplication.service.IFirebaseService;
import com.example.myapplication.service.impl.FirebaseServiceImpl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class StatusController {
    private final IFirebaseService service = new FirebaseServiceImpl();

    public void updateDateOnline() {
        service.updateDateOnline();
    }
    public String checkOnline(String status) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("UTC")); // Dữ liệu gốc là UTC+0

            Date lastOnlineDate = sdf.parse(status);

            // Chuyển dữ liệu từ UTC+0 sang UTC+7
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(lastOnlineDate);
            calendar.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh")); // Đặt múi giờ mới là UTC+7

            Date dateInUTC7 = calendar.getTime();


            Date now = new Date();
            long diffInMillis = now.getTime() - dateInUTC7.getTime();
            long minutes = diffInMillis / (1000 * 60);
            long hours = diffInMillis / (1000 * 60 * 60);
            long days = diffInMillis / (1000 * 60 * 60 * 24);

            if (minutes < 5) {
                return "Online";
            } else if (days >= 1) {
                return "Offline (" + days + " ngày trước)";
            } else if(hours >= 1){
                return "Offline (" + hours + " giờ trước)";
            } else {
                return "Offline (" + minutes + " phút trước)";
            }

        } catch (ParseException | NullPointerException e) {
            if ("deleted".equals(status)) return "Tài khoản đã xóa";
            return "Không xác định";
        }
    }
}
