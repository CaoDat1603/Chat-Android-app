package com.example.myapplication.controller;

import com.example.myapplication.service.FirebaseService;
import com.example.myapplication.service.impl.FirebaseServiceImpl;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StatusController {
    private final FirebaseService service = new FirebaseServiceImpl();

    public void updateDateOnline() {
        service.updateDateOnline();
    }
    public String checkOnline(String status) {
        try {
            // Format bạn đã lưu trong Firebase (giả sử là yyyy-MM-dd HH:mm)
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date lastOnlineDate = sdf.parse(status);

            Date now = new Date();
            long diffInMillis = now.getTime() - lastOnlineDate.getTime();
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
            return "Không xác định";
        }
    }
}
