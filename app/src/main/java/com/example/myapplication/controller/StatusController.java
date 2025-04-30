package com.example.myapplication.controller;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StatusController {
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseDatabase database = FirebaseDatabase.getInstance();

    public void updateDateOnline() {
        if (firebaseAuth.getCurrentUser() == null) return; // Kiểm tra đã login chưa
        String currentUID = firebaseAuth.getCurrentUser().getUid();
        DatabaseReference userRef = database.getReference("user").child(currentUID);
        // Lấy thời gian hiện tại dưới dạng String
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                .format(new Date());
        // Cập nhật status
        userRef.child("status").setValue(currentTime);
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
