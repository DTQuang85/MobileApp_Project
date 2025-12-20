package com.example.engapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_DURATION = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Get views
        TextView tvLogo = findViewById(R.id.tvLogo);
        TextView tvFire = findViewById(R.id.tvFire);
        TextView tvAppName = findViewById(R.id.tvAppName);
        TextView tvTagline = findViewById(R.id.tvTagline);

        // Star animations
        TextView[] stars = {
            findViewById(R.id.star1),
            findViewById(R.id.star2),
            findViewById(R.id.star3),
            findViewById(R.id.star4),
            findViewById(R.id.star5),
            findViewById(R.id.star6),
            findViewById(R.id.star7)
        };

        Animation twinkle = AnimationUtils.loadAnimation(this, R.anim.twinkle_star);
        for (int i = 0; i < stars.length; i++) {
            if (stars[i] != null) {
                Animation starAnim = AnimationUtils.loadAnimation(this, R.anim.twinkle_star);
                starAnim.setStartOffset(i * 200); // Stagger animations
                stars[i].startAnimation(starAnim);
            }
        }

        // Rocket floating animation (no fire)
        if (tvLogo != null) {
            Animation floatAnim = AnimationUtils.loadAnimation(this, R.anim.float_up_down);
            tvLogo.startAnimation(floatAnim);
        }

        // Orbit ring rotation
        View orbitRing = findViewById(R.id.orbitRing);
        if (orbitRing != null) {
            android.view.animation.RotateAnimation rotateAnim = new android.view.animation.RotateAnimation(
                0, 360,
                android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f,
                android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f
            );
            rotateAnim.setDuration(10000);
            rotateAnim.setRepeatCount(android.view.animation.Animation.INFINITE);
            rotateAnim.setInterpolator(new android.view.animation.LinearInterpolator());
            orbitRing.startAnimation(rotateAnim);
        }

        // App name animation
        if (tvAppName != null) {
            Animation slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
            slideIn.setStartOffset(500);
            tvAppName.startAnimation(slideIn);
        }

        // Tagline animation
        if (tvTagline != null) {
            Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_scale_in);
            fadeIn.setStartOffset(800);
            tvTagline.startAnimation(fadeIn);
        }

        new Handler().postDelayed(() -> {
            SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
            boolean hasSeenCutscene = prefs.getBoolean("cutscene_seen", false);
            boolean hasSeenIntro = prefs.getBoolean("has_seen_intro", false);

            FirebaseAuth auth = FirebaseAuth.getInstance();
            boolean isLoggedIn = auth.getCurrentUser() != null;

            Intent intent;
            if (!hasSeenCutscene) {
                // First time - show cutscene
                intent = new Intent(SplashActivity.this, CutsceneActivity.class);
            } else if (!hasSeenIntro) {
                intent = new Intent(SplashActivity.this, IntroActivity.class);
            } else if (!isLoggedIn) {
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            } else {
                // Navigate to Interactive Galaxy Map as main hub
                intent = new Intent(SplashActivity.this, InteractiveGalaxyMapActivity.class);
            }
            
            startActivity(intent);
            overridePendingTransition(R.anim.fade_scale_in, 0);
            finish();
        }, SPLASH_DURATION);
    }
}
