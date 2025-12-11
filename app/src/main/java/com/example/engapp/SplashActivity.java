package com.example.engapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_DURATION = 2000; // 2 giây

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {
            // Kiểm tra đã xem intro chưa
            SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
            boolean hasSeenIntro = prefs.getBoolean("has_seen_intro", false);

            // Kiểm tra đã đăng nhập chưa
            FirebaseAuth auth = FirebaseAuth.getInstance();
            boolean isLoggedIn = auth.getCurrentUser() != null;

            Intent intent;
            if (!hasSeenIntro) {
                // Chưa xem intro -> đi tới IntroActivity
                intent = new Intent(SplashActivity.this, IntroActivity.class);
            } else if (!isLoggedIn) {
                // Đã xem intro nhưng chưa login -> đi tới LoginActivity
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            } else {
                // Đã login -> đi tới MainActivity
                intent = new Intent(SplashActivity.this, MainActivity.class);
            }
            
            startActivity(intent);
            finish();
        }, SPLASH_DURATION);
    }
}
