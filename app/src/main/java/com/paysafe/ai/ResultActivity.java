package com.paysafe.ai;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ResultActivity extends AppCompatActivity {

    TextView resultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // ✅ Connect TextView
        resultText = findViewById(R.id.resultText);

        // ✅ Get AI Result
        String result =
                getIntent().getStringExtra("result");

        if (result != null) {
            resultText.setText(result);
        } else {
            resultText.setText("Result Not Found");
        }
    }
}