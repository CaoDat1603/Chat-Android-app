package com.example.myapplication.view;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.squareup.picasso.Picasso;

public class ShowImageMessageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image_message);

        ImageView imageView = findViewById(R.id.full_image);

        // Lấy URL hình ảnh từ Intent
        String imageUrl = getIntent().getStringExtra("image_url");
        String imageName = getIntent().getStringExtra("image_name");

        // Hiển thị hình ảnh bằng Picasso
        if (imageUrl != null) {
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
            return;
        }

        // Sử dụng DownloadManager để tải ảnh
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(imageUrl);

        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setDescription("Downloading file...");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, imageName);

        // Bắt đầu tải
        if (downloadManager != null) {
            downloadManager.enqueue(request);
        }
    }
}