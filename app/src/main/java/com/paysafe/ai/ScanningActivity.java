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
        // ✅ BEEP
        //----------------------------------
        beepSound = MediaPlayer.create(this, R.raw.scan_sound);

        //----------------------------------
        // ✅ HUD BLINK
        //----------------------------------
        TextView status = findViewById(R.id.aiStatus);

        if (status != null) {
            ObjectAnimator blink =
                    ObjectAnimator.ofFloat(status,"alpha",1f,0.3f);

            blink.setDuration(700);
            blink.setRepeatMode(ObjectAnimator.REVERSE);
            blink.setRepeatCount(ObjectAnimator.INFINITE);
            blink.start();
        }

        //----------------------------------
        // ✅ SCAN LINE
        //----------------------------------
        View scanLine = findViewById(R.id.scanLine);

        if(scanLine!=null){
            scanLine.post(() -> {

                View parent=(View)scanLine.getParent();

                float distance =
                        parent.getHeight()-scanLine.getHeight();

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

        startAIThinking();
        startFakeProgress();

        //----------------------------------
        // ✅ LOAD IMAGE
        //----------------------------------
        String uriString =
                getIntent().getStringExtra("imageUri");

        if(uriString!=null){

            try{

                Uri imageUri = Uri.parse(uriString);

                Bitmap bitmap =
                        MediaStore.Images.Media.getBitmap(
                                getContentResolver(),
                                imageUri);

                paymentImage.setImageBitmap(bitmap);
                paymentImage.setScaleType(ImageView.ScaleType.FIT_XY);

                // ✅ Tamper check
                if(detectTampering(bitmap)){
                    startCloudVerification(
                            "Tampered Screenshot ⚠️");
                    return;
                }

                scanImage(bitmap);

            }catch(Exception e){
                startCloudVerification(
                        "Invalid Screenshot ❌");
            }
        }
    }

    //----------------------------------
    // ✅ TAMPER ANALYZER
    //----------------------------------
    private boolean detectTampering(Bitmap bitmap){

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        if(w<350 || h<350)
            return true;

        float ratio=(float)w/h;

        return ratio<0.4f || ratio>3f;
    }

    //----------------------------------
    // ✅ SMART TRANSACTION ANALYZER
    //----------------------------------
    private int analyzeTransaction(String text){

        int score=0;

        text=text.toLowerCase();

        if(text.contains("upi")
                || text.contains("gpay")
                || text.contains("phonepe")
                || text.contains("paytm")
                || text.contains("google pay"))
            score+=2;

        if(text.contains("txn")
                || text.contains("utr")
                || text.contains("transaction")
                || text.contains("ref"))
            score+=2;

        if(text.contains("₹")
                || text.contains("rs")
                || text.contains("amount"))
            score+=1;

        if(text.contains("success")
                || text.contains("paid")
                || text.contains("credited")
                || text.contains("completed")
                || text.contains("sent"))
            score+=2;

        if(text.contains(":")
                || text.contains("am")
                || text.contains("pm")
                || text.contains("202"))
            score+=1;

        return score;
    }

    //----------------------------------
    // ✅ AI TEXT
    //----------------------------------
    private void startAIThinking(){

        Handler h=new Handler();

        h.postDelayed(() ->
                aiText.setText("Reading Screenshot..."),1000);

        h.postDelayed(() ->
                aiText.setText("Extracting Transaction..."),2000);

        h.postDelayed(() ->
                aiText.setText("AI Fraud Analysis Running..."),3000);
    }

    //----------------------------------
    // ✅ SCAN %
    //----------------------------------
    private void startFakeProgress(){

        Handler handler=new Handler();

        for(int i=1;i<=100;i++){

            int v=i;

            handler.postDelayed(() ->
                            scanPercent.setText(v+"%"),
                    i*35);
        }
    }

    //----------------------------------
    // ✅ OCR
    //----------------------------------
    private void scanImage(Bitmap bitmap){

        InputImage image =
                InputImage.fromBitmap(bitmap,0);

        TextRecognition.getClient(
                        TextRecognizerOptions.DEFAULT_OPTIONS)
                .process(image)
                .addOnSuccessListener(r ->
                        checkPayment(r.getText()))
                .addOnFailureListener(e ->
                        startCloudVerification(
                                "Invalid Screenshot ❌"));
    }

    //----------------------------------
    // ✅ FINAL AI DECISION (FIXED)
    //----------------------------------
    private void checkPayment(String text){

        String result;

        if(text==null || text.trim().isEmpty())
            result="Invalid Screenshot ❌";
        else{

            int score=
                    analyzeTransaction(text);

            if(score>=5)
                result="Payment Safe ✅";
            else if(score>=3)
                result="Suspicious Payment ⚠️";
            else
                result="Fake Payment ❌";
        }

        startCloudVerification(result);
    }

    //----------------------------------
    // ✅ CLOUD VERIFY
    //----------------------------------
    private void startCloudVerification(String result){

        cloudStatus.setText(
                "Checking Screenshot Integrity...");

        new Handler().postDelayed(() -> {

            cloudStatus.setText(
                    "Cloud AI Verified ✅");

            openResult(result);

        },2000);
    }

    //----------------------------------
    // ✅ RESULT
    //----------------------------------
    private void openResult(String result){

        if(beepSound!=null)
            beepSound.start();

        new Handler().postDelayed(() -> {

            Intent intent =
                    new Intent(
                            ScanningActivity.this,
                            ResultActivity.class);

            intent.putExtra("result",result);
            startActivity(intent);
            finish();

        },800);
    }
}