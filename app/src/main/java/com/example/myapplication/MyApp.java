package com.example.myapplication;

import android.app.Application;
import android.os.Handler;
import android.util.Log;

import com.example.myapplication.controller.StatusController;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MyApp extends Application {
    private static final String TAG = "MyApp";
    private Handler handler = new Handler();
    private Runnable statusRunnable;

    @Override
    public void onCreate() {
        super.onCreate();

        StatusController statusController = new StatusController();

        // ✅ Cập nhật mỗi 60 giây (vòng lặp nền)
        statusRunnable = new Runnable() {
            @Override
            public void run() {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    
                    statusController.updateDateOnline();
                    Log.d(TAG, "Cập nhật trạng thái online mỗi 60s");
                } else {
                    Log.d(TAG, "User đã đăng xuất, không cập nhật online");
                }
                /*try {
                    new StatusController().updateDateOnline();
                    Log.d(TAG, "Đã cập nhật trạng thái online.");
                } catch (Exception e) {
                    Log.e(TAG, "Lỗi khi cập nhật trạng thái: " + e.getMessage());
                }*/

                handler.postDelayed(this, 60000); // 60 giây
            }
        };

        handler.post(statusRunnable);
    }
}
