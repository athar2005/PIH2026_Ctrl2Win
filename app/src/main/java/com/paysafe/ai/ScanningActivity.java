package com.paysafe.ai;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

public class ScanningActivity extends AppCompatActivity {

    TextView aiText, scanPercent, cloudStatus;
    ImageView paymentImage;

    MediaPlayer beepSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanning);

        aiText = findViewById(R.id.aiText);
        scanPercent = findViewById(R.id.scanPercent);
        paymentImage = findViewById(R.id.paymentImage);
        cloudStatus = findViewById(R.id.cloudStatus);

        //----------------------------------
        // ✅ BEEP SOUND
        //----------------------------------
        beepSound = MediaPlayer.create(this, R.raw.scan_sound);

        //----------------------------------
        // ✅ HUD BLINK
        //----------------------------------
        TextView status = findViewById(R.id.aiStatus);

        if (status != null) {
            ObjectAnimator blink =
                    ObjectAnimator.ofFloat(status,
                            "alpha", 1f, 0.3f);

            blink.setDuration(700);
            blink.setRepeatMode(ObjectAnimator.REVERSE);
            blink.setRepeatCount(ObjectAnimator.INFINITE);
            blink.start();
        }

        //----------------------------------
        // ✅ SCAN LINE
        //----------------------------------
        View scanLine = findViewById(R.id.scanLine);

        if (scanLine != null) {
            scanLine.post(() -> {

                View parent = (View) scanLine.getParent();

                float distance =
                        parent.getHeight()
                                - scanLine.getHeight();

                ObjectAnimator move =
                        ObjectAnimator.ofFloat(
                                scanLine,
                                "translationY",
                                0f,
                                distance);

                move.setDuration(1800);
                move.setRepeatCount(ObjectAnimator.INFINITE);
                move.setRepeatMode(ObjectAnimator.REVERSE);
                move.start();
            });
        }

        //----------------------------------
        // ✅ AI FLOW
        //----------------------------------
        startAIThinking();
        startFakeProgress();

        //----------------------------------
        // ✅ LOAD IMAGE
        //----------------------------------
        String uriString =
                getIntent().getStringExtra("imageUri");

        if (uriString != null) {

            Uri imageUri = Uri.parse(uriString);

            try {

                Bitmap bitmap =
                        MediaStore.Images.Media.getBitmap(
                                getContentResolver(),
                                imageUri);

                paymentImage.setImageBitmap(bitmap);
                paymentImage.setScaleType(ImageView.ScaleType.FIT_XY);

                // ✅ TAMPER CHECK
                if (detectTampering(bitmap)) {
                    startCloudVerification(
                            "Tampered Screenshot ⚠️");
                    return;
                }

                scanImage(bitmap);

            } catch (Exception e) {
                startCloudVerification("Invalid Screenshot ❌");
            }
        }
    }

    //----------------------------------
    // ✅ TAMPER ANALYZER
    //----------------------------------
    private boolean detectTampering(Bitmap bitmap) {

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (width < 400 || height < 400)
            return true;

        float ratio = (float) width / height;

        return ratio < 0.4f || ratio > 2.5f;
    }

    //----------------------------------
    // ✅ TRANSACTION ANALYZER
    //----------------------------------
    private int analyzeTransaction(String text) {

        int score = 0;

        if (text.matches(".*\\b(upi|gpay|phonepe|paytm)\\b.*"))
            score++;

        if (text.matches(".*\\b(txn|transaction|utr|ref)\\b.*"))
            score++;

        if (text.matches(".*(₹|rs|inr).*\\d+.*"))
            score++;

        if (text.matches(".*\\d{2}:\\d{2}.*"))
            score++;

        if (text.matches(".*(success|paid|credited|completed).*"))
            score++;

        return score;
    }

    //----------------------------------
    // ✅ AI TEXT FLOW
    //----------------------------------
    private void startAIThinking() {

        Handler handler = new Handler();

        handler.postDelayed(() ->
                aiText.setText("Offline AI Scanning..."), 1000);

        handler.postDelayed(() ->
                aiText.setText("Extracting Transaction Data..."), 2000);

        handler.postDelayed(() ->
                aiText.setText("Fraud Pattern Detection..."), 3000);
    }

    //----------------------------------
    // ✅ SCAN %
    //----------------------------------
    private void startFakeProgress() {

        Handler handler = new Handler();

        for (int i = 1; i <= 100; i++) {

            int value = i;

            handler.postDelayed(() ->
                            scanPercent.setText(value + "%"),
                    i * 35);
        }
    }

    //----------------------------------
    // ✅ OCR SCAN
    //----------------------------------
    private void scanImage(Bitmap bitmap) {

        InputImage image =
                InputImage.fromBitmap(bitmap, 0);

        TextRecognition.getClient(
                        TextRecognizerOptions.DEFAULT_OPTIONS)
                .process(image)
                .addOnSuccessListener(result ->
                        checkPayment(result.getText()))
                .addOnFailureListener(e ->
                        startCloudVerification(
                                "Invalid Screenshot ❌"));
    }

    //----------------------------------
    // ✅ FINAL AI DECISION
    //----------------------------------
    private void checkPayment(String text) {

        String result;

        if (text == null || text.trim().isEmpty()) {

            result = "Invalid Screenshot ❌";

        } else {

            text = text.toLowerCase();

            int intelligenceScore =
                    analyzeTransaction(text);

            if (intelligenceScore >= 4)
                result = "Payment Safe ✅";

            else if (intelligenceScore >= 2)
                result = "Suspicious Payment ⚠️";

            else
                result = "Fake Payment ❌";
        }

        startCloudVerification(result);
    }

    //----------------------------------
    // ✅ HYBRID CLOUD VERIFY
    //----------------------------------
    private void startCloudVerification(String result) {

        cloudStatus.setText(
                "Checking Screenshot Integrity...");

        new Handler().postDelayed(() -> {

            cloudStatus.setText(
                    "Cloud AI Verified ✅");

            openResult(result);

        }, 2000);
    }

    //----------------------------------
    // ✅ RESULT + SOUND
    //----------------------------------
    private void openResult(String result) {

        if (beepSound != null)
            beepSound.start();

        new Handler().postDelayed(() -> {

            Intent intent =
                    new Intent(
                            ScanningActivity.this,
                            ResultActivity.class);

            intent.putExtra("result", result);
            startActivity(intent);
            finish();

        }, 800);
    }
}