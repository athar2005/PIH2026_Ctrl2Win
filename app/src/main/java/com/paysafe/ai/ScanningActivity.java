package com.paysafe.ai;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;

public class ScanningActivity extends AppCompatActivity {

    TextView aiText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanning);

        aiText = findViewById(R.id.aiText);

        // ✅ SCAN LINE ANIMATION FIX
        View scanLine = findViewById(R.id.scanLine);

        if (scanLine != null) {

            scanLine.post(() -> {

                View parent = (View) scanLine.getParent();

                float moveDistance =
                        parent.getHeight() - scanLine.getHeight();

                ObjectAnimator animator =
                        ObjectAnimator.ofFloat(
                                scanLine,
                                "translationY",
                                0f,
                                moveDistance
                        );

                animator.setDuration(1500);
                animator.setRepeatCount(ObjectAnimator.INFINITE);
                animator.setRepeatMode(ObjectAnimator.REVERSE);
                animator.start();
            });
        }

        String uriString =
                getIntent().getStringExtra("imageUri");

        if (uriString != null) {
            scanImage(Uri.parse(uriString));
        } else {
            openResult("Invalid Screenshot ❌");
        }
    }

    private void scanImage(Uri uri) {

        try {

            Bitmap bitmap =
                    MediaStore.Images.Media.getBitmap(
                            getContentResolver(),
                            uri);

            InputImage image =
                    InputImage.fromBitmap(bitmap, 0);

            TextRecognition.getClient(
                            TextRecognizerOptions.DEFAULT_OPTIONS)
                    .process(image)

                    .addOnSuccessListener(result ->
                            checkPayment(result.getText()))

                    // ✅ OCR FAIL SAFE
                    .addOnFailureListener(e ->
                            openResult("Invalid Screenshot ❌"));

        } catch (IOException e) {

            e.printStackTrace();
            openResult("Invalid Screenshot ❌");
        }
    }

    // ✅ SMART AI LOGIC
    private void checkPayment(String text) {

        String result;

        if (text == null || text.trim().isEmpty()) {

            result = "Invalid Screenshot ❌";

        } else {

            text = text.toLowerCase();

            boolean success =
                    text.contains("success") ||
                            text.contains("paid") ||
                            text.contains("transaction") ||
                            text.contains("credited");

            boolean suspicious =
                    text.contains("failed") ||
                            text.contains("pending") ||
                            text.contains("processing");

            if (success) {
                result = "Payment Safe ✅";
            }
            else if (suspicious) {
                result = "Suspicious Payment ⚠️";
            }
            else {
                result = "Invalid Screenshot ❌";
            }
        }

        openResult(result);
    }

    // ✅ RESULT NAVIGATION
    private void openResult(String result) {

        Intent intent =
                new Intent(
                        ScanningActivity.this,
                        ResultActivity.class);

        intent.putExtra("result", result);

        startActivity(intent);
        finish();
    }
}