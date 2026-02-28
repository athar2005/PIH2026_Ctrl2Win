package com.paysafe.ai;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ScanningActivity extends AppCompatActivity {

    private TextView aiText;
    private ProgressBar progressBar;

    private Handler handler;
    private Runnable resultRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanning);

        aiText = findViewById(R.id.aiText);
        progressBar = findViewById(R.id.progressBar);

        View scanArea = findViewById(R.id.scanArea);
        View scanLine = findViewById(R.id.scanLine);

        handler = new Handler(Looper.getMainLooper());

        // ✅ FULL HEIGHT SCAN ANIMATION
        scanArea.post(() -> {

            float height = scanArea.getHeight();

            ObjectAnimator animator =
                    ObjectAnimator.ofFloat(
                            scanLine,
                            "translationY",
                            0f,
                            height - scanLine.getHeight()
                    );

            animator.setDuration(1500);
            animator.setRepeatCount(ObjectAnimator.INFINITE);
            animator.setRepeatMode(ObjectAnimator.REVERSE);
            animator.start();
        });

        startFakeAI();
    }

    private void startFakeAI() {

        handler.postDelayed(() ->
                aiText.setText("Checking Transaction ID..."), 1500);

        handler.postDelayed(() ->
                aiText.setText("Validating Bank Server..."), 3000);

        handler.postDelayed(() ->
                aiText.setText("Detecting Screenshot Tampering..."), 4500);

        resultRunnable = () -> {
            Intent intent =
                    new Intent(ScanningActivity.this,
                            ResultActivity.class);

            startActivity(intent);
            finish();
        };

        handler.postDelayed(resultRunnable, 6000);
    }

    // ✅ CRASH PREVENTION
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (handler != null && resultRunnable != null) {
            handler.removeCallbacks(resultRunnable);
        }
    }
}