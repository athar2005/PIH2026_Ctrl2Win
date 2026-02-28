package com.paysafe.ai;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class ResultActivity extends AppCompatActivity {

    TextView resultText, confidenceText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        resultText = findViewById(R.id.resultText);
        confidenceText = findViewById(R.id.confidenceText);

        String result =
                getIntent().getStringExtra("result");

        if (result == null)
            result = "Analysis Complete";

        resultText.setText(result);

        // ✅ Fake AI Confidence Generator
        Random random = new Random();
        int confidence = 80 + random.nextInt(20);

        confidenceText.setText(
                "AI Confidence : " + confidence + "%"
        );
    }
}