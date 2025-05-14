package com.example.myapplication.view;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.graphics.Insets;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.R;
import com.squareup.picasso.Picasso;

public class ShowImageMessageActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image_message);
        EdgeToEdge.enable(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars()).toPlatformInsets();
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView imageView = findViewById(R.id.full_image);


        // Lấy URL hình ảnh từ Intent
        String imageUrl = getIntent().getStringExtra("image_url");
        String imageName = getIntent().getStringExtra("image_name");

        // Hiển thị hình ảnh bằng Picasso
        if (imageUrl != null) {
            // Giả sử bạn có thêm 1 biến "file_type" truyền qua Intent
                imageView.setVisibility(View.VISIBLE);
                Picasso.get().load(imageUrl).into(imageView);
        }

        ImageButton turnBack = findViewById(R.id.turnback);
        ImageButton dowImage = findViewById(R.id.dowimage);

        // Quay lại
        turnBack.setOnClickListener(view -> finish());

        // Tải ảnh
        dowImage.setOnClickListener(view -> downloadImage(imageUrl, imageName));
    }

    private void downloadImage(String imageUrl, String imageName) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            Log.e("DownloadImage", "Image URL is null or empty");
            return;
        }

        if (imageName == null || imageName.isEmpty()) {
            imageName = "default_image_name.jpg"; // Giá trị mặc định
        }

        // Sử dụng DownloadManager để tải ảnh
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(imageUrl);

        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setDescription("Downloading file...");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, imageName);

        try {
            if (downloadManager != null) {
                downloadManager.enqueue(request);
            }
        } catch (Exception e) {
            Log.e("ShowImageMessageActivity", "Download failed", e);
        }

    }
}