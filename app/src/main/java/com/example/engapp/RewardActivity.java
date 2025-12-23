package com.example.engapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.engapp.database.GameDatabaseHelper;

public class RewardActivity extends AppCompatActivity {

    private TextView tvCelebration, tvRewardTitle, tvRewardSubtitle;
    private TextView tvStarsAmount, tvFuelAmount, tvCrystalAmount, tvExperience;
    private CardView cardReward;
    private FrameLayout confettiContainer;
    private Button btnContinue;

    private int starsEarned, fuelEarned, crystalsEarned, experienceEarned;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward);

        getIntentData();
        initViews();
        setupUI();
        playAnimations();
        saveRewards();
    }

    private void getIntentData() {
        starsEarned = getIntent().getIntExtra("stars", 0);
        fuelEarned = getIntent().getIntExtra("fuel", 0);
        crystalsEarned = getIntent().getIntExtra("crystals", 0);
        experienceEarned = getIntent().getIntExtra("experience", 0);
    }

    private void initViews() {
        tvCelebration = findViewById(R.id.tvCelebration);
        tvRewardTitle = findViewById(R.id.tvRewardTitle);
        tvRewardSubtitle = findViewById(R.id.tvRewardSubtitle);
        tvStarsAmount = findViewById(R.id.tvStarsAmount);
        tvFuelAmount = findViewById(R.id.tvFuelAmount);
        tvCrystalAmount = findViewById(R.id.tvCrystalAmount);
        tvExperience = findViewById(R.id.tvExperience);
        cardReward = findViewById(R.id.cardReward);
        confettiContainer = findViewById(R.id.confettiContainer);
        btnContinue = findViewById(R.id.btnContinue);

        btnContinue.setOnClickListener(v -> finish());
    }

    private void setupUI() {
        tvStarsAmount.setText("+" + starsEarned);
        tvFuelAmount.setText("+" + fuelEarned);
        tvCrystalAmount.setText("+" + crystalsEarned);
        tvExperience.setText("+" + experienceEarned + " XP");

        // Hide zero rewards
        if (starsEarned == 0) tvStarsAmount.setText("-");
        if (fuelEarned == 0) tvFuelAmount.setText("-");
        if (crystalsEarned == 0) tvCrystalAmount.setText("-");
    }

    private void playAnimations() {
        // Celebration emoji animation
        Animation pulse = AnimationUtils.loadAnimation(this, R.anim.fade_scale_in);
        tvCelebration.startAnimation(pulse);

        // Card animation
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
        slideUp.setStartOffset(300);
        cardReward.startAnimation(slideUp);

        // Create confetti effect
        createConfetti();
    }

    private void createConfetti() {
        String[] confettiEmojis = {"🎊", "🎉", "✨", "⭐", "🌟"};

        for (int i = 0; i < 20; i++) {
            final TextView confetti = new TextView(this);
            confetti.setText(confettiEmojis[i % confettiEmojis.length]);
            confetti.setTextSize(24);

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            );
            params.leftMargin = (int) (Math.random() * getResources().getDisplayMetrics().widthPixels);
            params.topMargin = -100;
            confetti.setLayoutParams(params);

            confettiContainer.addView(confetti);

            // Animate falling
            confetti.animate()
                    .translationY(getResources().getDisplayMetrics().heightPixels + 200)
                    .setDuration(2000 + (int)(Math.random() * 1000))
                    .setStartDelay(i * 100)
                    .withEndAction(() -> confettiContainer.removeView(confetti))
                    .start();
        }
    }

    private void saveRewards() {
        GameDatabaseHelper dbHelper = GameDatabaseHelper.getInstance(this);

        if (starsEarned > 0) {
            dbHelper.addStars(starsEarned);
        }
        if (fuelEarned > 0) {
            dbHelper.addFuelCells(fuelEarned);
        }
        if (crystalsEarned > 0) {
            dbHelper.addCrystals(crystalsEarned);
        }
        if (experienceEarned > 0) {
            dbHelper.addExperience(experienceEarned);
        }
    }

    @Override
    @SuppressLint("MissingSuperCall")
    public void onBackPressed() {
        // Prevent back during reward screen
        finish();
    }
}

