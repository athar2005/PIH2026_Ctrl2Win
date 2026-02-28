package com.paysafe.ai;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class HomeActivity extends AppCompatActivity {

    Button uploadBtn;

    ActivityResultLauncher<String> imagePicker =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            openScanning(uri);
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        uploadBtn = findViewById(R.id.uploadBtn);

        uploadBtn.setOnClickListener(v ->
                imagePicker.launch("image/*"));
    }

    private void openScanning(Uri imageUri) {

        Intent intent =
                new Intent(HomeActivity.this,
                        ScanningActivity.class);

        intent.putExtra("imageUri",
                imageUri.toString());

        startActivity(intent);
    }
}