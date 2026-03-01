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

import java.io.IOException;

public class ScanningActivity extends AppCompatActivity {

    TextView aiText;
    TextView scanPercent;
    ImageView paymentImage;

    MediaPlayer beepSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanning);

        aiText = findViewById(R.id.aiText);
        scanPercent = findViewById(R.id.scanPercent);
        paymentImage = findViewById(R.id.paymentImage);

        //----------------------------------
        // ✅ LOAD BEEP SOUND
        //----------------------------------
        beepSound = MediaPlayer.create(this, R.raw.scan_sound);

        //----------------------------------
        // ✅ HUD BLINK
        //----------------------------------
        TextView status = findViewById(R.id.aiStatus);

        ObjectAnimator blink =
                ObjectAnimator.ofFloat(status,"alpha",1f,0.3f);

        blink.setDuration(700);
        blink.setRepeatMode(ObjectAnimator.REVERSE);
        blink.setRepeatCount(ObjectAnimator.INFINITE);
        blink.start();

        //----------------------------------
        // ✅ SCAN LINE
        //----------------------------------
        View scanLine = findViewById(R.id.scanLine);

        scanLine.post(() -> {

            View parent = (View) scanLine.getParent();

            float distance =
                    parent.getHeight() - scanLine.getHeight();

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

        //----------------------------------
        // ✅ START AI + %
        //----------------------------------
        startAIThinking();
        startFakeProgress();

        //----------------------------------
        // ✅ LOAD IMAGE
        //----------------------------------
        String uriString =
                getIntent().getStringExtra("imageUri");

        if(uriString!=null){

            Uri imageUri = Uri.parse(uriString);

            try {

                Bitmap bitmap =
                        MediaStore.Images.Media.getBitmap(
                                getContentResolver(),
                                imageUri);

                paymentImage.setImageBitmap(bitmap);
                paymentImage.setScaleType(ImageView.ScaleType.FIT_XY);

            } catch (IOException e){
                e.printStackTrace();
            }

            scanImage(imageUri);
        }
    }

    //----------------------------------
    // ✅ AI TEXT FLOW
    //----------------------------------
    private void startAIThinking(){

        Handler handler=new Handler();

        handler.postDelayed(() ->
                aiText.setText("Reading Screenshot..."),1000);

        handler.postDelayed(() ->
                aiText.setText("Extracting Transaction..."),2000);

        handler.postDelayed(() ->
                aiText.setText("Analyzing Payment..."),3000);
    }

    //----------------------------------
    // ✅ SCAN PERCENTAGE (NEW)
    //----------------------------------
    private void startFakeProgress(){

        Handler handler = new Handler();

        for(int i=1;i<=100;i++){

            int value=i;

            handler.postDelayed(() ->
                            scanPercent.setText(value+"%"),
                    i*35);
        }
    }

    //----------------------------------
    // ✅ OCR
    //----------------------------------
    private void scanImage(Uri uri){

        try{

            Bitmap bitmap =
                    MediaStore.Images.Media.getBitmap(
                            getContentResolver(),
                            uri);

            InputImage image =
                    InputImage.fromBitmap(bitmap,0);

            TextRecognition.getClient(
                            TextRecognizerOptions.DEFAULT_OPTIONS)
                    .process(image)
                    .addOnSuccessListener(result ->
                            checkPayment(result.getText()));

        }catch(Exception e){
            openResult("Invalid Screenshot ❌");
        }
    }

    //----------------------------------
    // ✅ AI LOGIC
    //----------------------------------
    private void checkPayment(String text){

        String result;

        if(text==null || text.isEmpty())
            result="Invalid Screenshot ❌";
        else{

            text=text.toLowerCase();

            int score=0;

            if(text.contains("success")
                    ||text.contains("paid")) score++;

            if(text.contains("upi")
                    ||text.contains("transaction")) score++;

            if(text.contains("₹")
                    ||text.contains("amount")) score++;

            if(score>=2)
                result="Payment Safe ✅";
            else
                result="Fake Payment ❌";
        }

        openResult(result);
    }

    //----------------------------------
    // ✅ RESULT + BEEP
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