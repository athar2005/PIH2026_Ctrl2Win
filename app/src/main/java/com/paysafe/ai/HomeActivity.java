package com.paysafe.ai;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

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

        //----------------------------------
        // BUTTON BIND
        //----------------------------------
        uploadBtn = findViewById(R.id.uploadBtn);

        //----------------------------------
        // ✅ SCAN BUTTON CLICK
        //----------------------------------
        uploadBtn.setOnClickListener(v ->
                imagePicker.launch("image/*"));

        //----------------------------------
        // 🔥 MAIN MAGIC — PULSE ANIMATION
        //----------------------------------
        startButtonPulse();
    }

    //==================================
    // ✅ BUTTON PULSE EFFECT
    //==================================
    private void startButtonPulse() {

        ObjectAnimator pulse =
                ObjectAnimator.ofPropertyValuesHolder(
                        uploadBtn,
                        PropertyValuesHolder.ofFloat("scaleX", 1f, 1.06f),
                        PropertyValuesHolder.ofFloat("scaleY", 1f, 1.06f)
                );

        pulse.setDuration(900);
        pulse.setRepeatMode(ObjectAnimator.REVERSE);
        pulse.setRepeatCount(ObjectAnimator.INFINITE);
        pulse.start();
    }

    //==================================
    // OPEN SCANNING SCREEN
    //==================================
    private void openScanning(Uri imageUri) {

        Intent intent =
                new Intent(
                        HomeActivity.this,
                        ScanningActivity.class);

        intent.putExtra(
                "imageUri",
                imageUri.toString());

        startActivity(intent);
    }
}