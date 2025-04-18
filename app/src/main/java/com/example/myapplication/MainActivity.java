package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log; // Thêm import này để sử dụng Log
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.FirebaseApp; // Thêm import này
import com.google.firebase.auth.AuthResult;
import androidx.annotation.NonNull;


public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Khởi tạo Firebase
        FirebaseApp.initializeApp(this);
        Log.d("Firebase", "Firebase is initialized: " + FirebaseApp.getApps(this).size());

        auth = FirebaseAuth.getInstance();

        // Kiểm tra xem người dùng đã đăng nhập chưa
        if (auth.getCurrentUser() == null) {
            Intent intent = new Intent(MainActivity.this, login.class);
            startActivity(intent);
            finish();
            return;
        }

        // Kiểm tra kết nối với Firebase
        checkFirebaseConnection();
    }
    private void checkFirebaseConnection() {
        database = FirebaseDatabase.getInstance().getReference();
        // Thực hiện ghi dữ liệu vào Firebase chỉ để kiểm tra
        database.child("test").setValue("Hello Firebase").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d("FirebaseDB", "Data written successfully");
                } else {
                    Log.d("FirebaseDB", "Data write failed: " + task.getException().getMessage());
                }
            }
        });
    }

}