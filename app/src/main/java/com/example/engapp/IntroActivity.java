package com.example.engapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class IntroActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        Button btnStart = findViewById(R.id.btnStart);
        btnStart.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class))
        );
    }
}
