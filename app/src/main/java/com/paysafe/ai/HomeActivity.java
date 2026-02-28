package com.paysafe.ai;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    Button uploadBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Button reference
        uploadBtn = findViewById(R.id.uploadBtn);

        // Open AI Scanning Screen
        uploadBtn.setOnClickListener(v -> {
            Intent intent = new Intent(
                    HomeActivity.this,
                    ScanningActivity.class
            );
            startActivity(intent);
        });
    }
}