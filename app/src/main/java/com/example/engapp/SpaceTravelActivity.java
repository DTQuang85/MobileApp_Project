package com.example.engapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.engapp.manager.BuddyManager;
import com.example.engapp.manager.TravelManager;
import com.example.engapp.model.Planet;
import com.example.engapp.view.BuddyOverlayView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Activity that shows the spaceship travel animation between planets.
 */
public class SpaceTravelActivity extends AppCompatActivity implements TravelManager.TravelEventListener {

    public static final String EXTRA_DESTINATION_PLANET = "destination_planet";
    public static final String EXTRA_FROM_PLANET_NAME = "from_planet_name";

    private FrameLayout rootLayout;
    private View spaceBackground;
    private ImageView ivSpaceship;
    private ImageView ivEngineGlow;
    private TextView tvStatus;
    private TextView tvDestination;
    private ProgressBar progressBar;
    private CardView cardBuddy;
    private TextView tvBuddyEmoji;
    private TextView tvBuddySpeech;
    private LinearLayout layoutStars;

    private Planet destinationPlanet;
    private String fromPlanetName;

    private TravelManager travelManager;
    private BuddyManager buddyManager;
    private Handler handler;
    private Random random;
    private MediaPlayer engineSound;

    private List<View> starViews;
    private boolean isAnimating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get destination planet from intent
        destinationPlanet = (Planet) getIntent().getSerializableExtra(EXTRA_DESTINATION_PLANET);
        fromPlanetName = getIntent().getStringExtra(EXTRA_FROM_PLANET_NAME);

        if (destinationPlanet == null) {
            finish();
            return;
        }

        handler = new Handler();
        random = new Random();
        starViews = new ArrayList<>();

        travelManager = TravelManager.getInstance(this);
        buddyManager = BuddyManager.getInstance(this);
        travelManager.addListener(this);

        createUI();
        startTravelSequence();
    }

    private void createUI() {
        rootLayout = new FrameLayout(this);
        rootLayout.setBackgroundColor(Color.parseColor("#0A0A1A"));
        setContentView(rootLayout);

        // Stars container
        layoutStars = new LinearLayout(this);
        layoutStars.setLayoutParams(new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT));
        rootLayout.addView(layoutStars);

        // Generate initial stars
        generateStars(100);

        // Engine glow
        ivEngineGlow = new ImageView(this);
        ivEngineGlow.setBackgroundColor(Color.parseColor("#FF6B00"));
        FrameLayout.LayoutParams glowParams = new FrameLayout.LayoutParams(60, 100);
        glowParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        glowParams.bottomMargin = 150;
        ivEngineGlow.setLayoutParams(glowParams);
        ivEngineGlow.setAlpha(0f);
        rootLayout.addView(ivEngineGlow);

        // Spaceship
        ivSpaceship = new ImageView(this);
        ivSpaceship.setBackgroundColor(Color.parseColor("#4ECDC4"));
        FrameLayout.LayoutParams shipParams = new FrameLayout.LayoutParams(80, 120);
        shipParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        shipParams.bottomMargin = 200;
        ivSpaceship.setLayoutParams(shipParams);

        // Create rocket shape appearance
        ivSpaceship.setRotation(0);
        rootLayout.addView(ivSpaceship);

        // Status text
        tvStatus = new TextView(this);
        tvStatus.setTextColor(Color.WHITE);
        tvStatus.setTextSize(18);
        tvStatus.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams statusParams = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT);
        statusParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        statusParams.topMargin = 100;
        tvStatus.setLayoutParams(statusParams);
        tvStatus.setText("Chuẩn bị khởi hành...");
        rootLayout.addView(tvStatus);

        // Destination text
        tvDestination = new TextView(this);
        tvDestination.setTextColor(Color.parseColor("#FFD700"));
        tvDestination.setTextSize(24);
        tvDestination.setGravity(Gravity.CENTER);
        tvDestination.setTypeface(null, android.graphics.Typeface.BOLD);
        FrameLayout.LayoutParams destParams = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT);
        destParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        destParams.topMargin = 140;
        tvDestination.setLayoutParams(destParams);
        tvDestination.setText(destinationPlanet.getEmoji() + " " + destinationPlanet.getName());
        rootLayout.addView(tvDestination);

        // Progress bar
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);
        progressBar.setProgress(0);
        FrameLayout.LayoutParams progressParams = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            20);
        progressParams.gravity = Gravity.BOTTOM;
        progressParams.setMargins(50, 0, 50, 50);
        progressBar.setLayoutParams(progressParams);
        rootLayout.addView(progressBar);

        // Buddy card
        cardBuddy = new CardView(this);
        cardBuddy.setCardBackgroundColor(Color.parseColor("#40FFFFFF"));
        cardBuddy.setRadius(60);
        cardBuddy.setCardElevation(8);
        FrameLayout.LayoutParams buddyParams = new FrameLayout.LayoutParams(100, 100);
        buddyParams.gravity = Gravity.BOTTOM | Gravity.START;
        buddyParams.setMargins(30, 0, 0, 100);
        cardBuddy.setLayoutParams(buddyParams);

        tvBuddyEmoji = new TextView(this);
        tvBuddyEmoji.setText(buddyManager.getCurrentBuddyEmoji());
        tvBuddyEmoji.setTextSize(40);
        tvBuddyEmoji.setGravity(Gravity.CENTER);
        tvBuddyEmoji.setLayoutParams(new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT));
        cardBuddy.addView(tvBuddyEmoji);
        rootLayout.addView(cardBuddy);

        // Buddy speech bubble
        tvBuddySpeech = new TextView(this);
        tvBuddySpeech.setBackgroundColor(Color.WHITE);
        tvBuddySpeech.setTextColor(Color.parseColor("#2C3E50"));
        tvBuddySpeech.setTextSize(14);
        tvBuddySpeech.setPadding(20, 15, 20, 15);
        tvBuddySpeech.setVisibility(View.GONE);
        FrameLayout.LayoutParams speechParams = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT);
        speechParams.gravity = Gravity.BOTTOM | Gravity.START;
        speechParams.setMargins(140, 0, 50, 120);
        tvBuddySpeech.setLayoutParams(speechParams);
        rootLayout.addView(tvBuddySpeech);
    }

    private void generateStars(int count) {
        for (int i = 0; i < count; i++) {
            View star = new View(this);
            star.setBackgroundColor(Color.WHITE);

            int size = random.nextInt(4) + 2;
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(size, size);
            params.leftMargin = random.nextInt(getResources().getDisplayMetrics().widthPixels);
            params.topMargin = random.nextInt(getResources().getDisplayMetrics().heightPixels);
            star.setLayoutParams(params);
            star.setAlpha(random.nextFloat() * 0.5f + 0.5f);

            rootLayout.addView(star, 0); // Add behind other views
            starViews.add(star);
        }
    }

    private void startTravelSequence() {
        isAnimating = true;
        buddyManager.onTravelStart();

        // Pre-launch phase
        handler.postDelayed(this::startPreLaunchAnimation, 500);
    }

    private void startPreLaunchAnimation() {
        tvStatus.setText("🔥 Khởi động động cơ...");
        showBuddySpeech("Chuẩn bị cất cánh! 🚀");

        // Engine glow animation
        ObjectAnimator glowFadeIn = ObjectAnimator.ofFloat(ivEngineGlow, "alpha", 0f, 1f);
        glowFadeIn.setDuration(500);

        ObjectAnimator glowPulse = ObjectAnimator.ofFloat(ivEngineGlow, "scaleY", 1f, 1.5f, 1f);
        glowPulse.setDuration(300);
        glowPulse.setRepeatCount(3);

        AnimatorSet prelaunchSet = new AnimatorSet();
        prelaunchSet.playSequentially(glowFadeIn, glowPulse);
        prelaunchSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                startTakeoffAnimation();
            }
        });
        prelaunchSet.start();

        // Shake effect
        ObjectAnimator shake = ObjectAnimator.ofFloat(ivSpaceship, "translationX", 0, -5, 5, -5, 5, 0);
        shake.setDuration(500);
        shake.setRepeatCount(3);
        shake.start();
    }

    private void startTakeoffAnimation() {
        tvStatus.setText("🚀 Cất cánh!");
        showBuddySpeech("Lên nào! Wheee! 🎉");

        // Spaceship moves up
        ObjectAnimator moveUp = ObjectAnimator.ofFloat(ivSpaceship, "translationY", 0, -300);
        moveUp.setDuration(1500);
        moveUp.setInterpolator(new AccelerateInterpolator());

        // Engine glow follows
        ObjectAnimator glowMoveUp = ObjectAnimator.ofFloat(ivEngineGlow, "translationY", 0, -300);
        glowMoveUp.setDuration(1500);
        glowMoveUp.setInterpolator(new AccelerateInterpolator());

        // Scale down as it goes up
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(ivSpaceship, "scaleX", 1f, 0.7f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(ivSpaceship, "scaleY", 1f, 0.7f);
        scaleX.setDuration(1500);
        scaleY.setDuration(1500);

        AnimatorSet takeoffSet = new AnimatorSet();
        takeoffSet.playTogether(moveUp, glowMoveUp, scaleX, scaleY);
        takeoffSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                startWarpAnimation();
            }
        });
        takeoffSet.start();

        // Start star animation
        startStarStreakAnimation();
    }

    private void startStarStreakAnimation() {
        for (View star : starViews) {
            // Make stars streak downward
            float startY = star.getY();
            float endY = getResources().getDisplayMetrics().heightPixels + 100;

            ObjectAnimator streak = ObjectAnimator.ofFloat(star, "translationY", 0, endY - startY);
            streak.setDuration(random.nextInt(1000) + 500);
            streak.setInterpolator(new LinearInterpolator());
            streak.setRepeatCount(ValueAnimator.INFINITE);
            streak.start();

            // Elongate stars to look like streaks
            ObjectAnimator stretch = ObjectAnimator.ofFloat(star, "scaleY", 1f, 20f);
            stretch.setDuration(500);
            stretch.start();
        }
    }

    private void startWarpAnimation() {
        tvStatus.setText("⚡ Tốc độ ánh sáng!");
        showBuddySpeech("Nhìn kìa! Các ngôi sao! ✨");

        // Move spaceship to center
        ivSpaceship.setTranslationY(0);

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) ivSpaceship.getLayoutParams();
        params.gravity = Gravity.CENTER;
        ivSpaceship.setLayoutParams(params);

        FrameLayout.LayoutParams glowParams = (FrameLayout.LayoutParams) ivEngineGlow.getLayoutParams();
        glowParams.gravity = Gravity.CENTER;
        glowParams.topMargin = 100;
        ivEngineGlow.setLayoutParams(glowParams);
        ivEngineGlow.setTranslationY(0);

        // Pulsing glow effect
        ObjectAnimator pulse = ObjectAnimator.ofFloat(ivEngineGlow, "alpha", 0.5f, 1f, 0.5f);
        pulse.setDuration(300);
        pulse.setRepeatCount(10);
        pulse.start();

        // Buddy excited animation
        animateBuddyExcited();

        // Progress update
        startProgressAnimation();

        // After warp duration, start approach
        handler.postDelayed(this::startApproachAnimation, 3500);
    }

    private void startProgressAnimation() {
        ValueAnimator progressAnim = ValueAnimator.ofInt(0, 100);
        progressAnim.setDuration(6000);
        progressAnim.addUpdateListener(animation -> {
            int progress = (int) animation.getAnimatedValue();
            progressBar.setProgress(progress);
        });
        progressAnim.start();
    }

    private void startApproachAnimation() {
        tvStatus.setText("🌍 Đang tiếp cận " + destinationPlanet.getName() + "...");
        showBuddySpeech("Gần tới rồi! " + destinationPlanet.getEmoji());

        // Stop star streaking, slow down
        for (View star : starViews) {
            star.animate().scaleY(1f).setDuration(500).start();
            star.animate().cancel();
        }

        // Show approaching planet
        TextView planetView = new TextView(this);
        planetView.setText(destinationPlanet.getEmoji());
        planetView.setTextSize(20);
        FrameLayout.LayoutParams planetParams = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT);
        planetParams.gravity = Gravity.CENTER;
        planetView.setLayoutParams(planetParams);
        planetView.setScaleX(0.1f);
        planetView.setScaleY(0.1f);
        rootLayout.addView(planetView);

        // Planet grows as we approach
        AnimatorSet approachSet = new AnimatorSet();
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(planetView, "scaleX", 0.1f, 5f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(planetView, "scaleY", 0.1f, 5f);
        scaleX.setDuration(2000);
        scaleY.setDuration(2000);
        scaleX.setInterpolator(new DecelerateInterpolator());
        scaleY.setInterpolator(new DecelerateInterpolator());

        approachSet.playTogether(scaleX, scaleY);
        approachSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                startLandingAnimation(planetView);
            }
        });
        approachSet.start();
    }

    private void startLandingAnimation(View planetView) {
        tvStatus.setText("🛬 Hạ cánh...");
        showBuddySpeech("Chuẩn bị hạ cánh! 🎯");

        // Fade out planet (we're landing on it)
        planetView.animate().alpha(0f).setDuration(1000).start();

        // Spaceship landing animation
        ObjectAnimator descend = ObjectAnimator.ofFloat(ivSpaceship, "translationY", 0, 200);
        descend.setDuration(2000);
        descend.setInterpolator(new DecelerateInterpolator());

        // Scale up as we land
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(ivSpaceship, "scaleX", 0.7f, 1.2f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(ivSpaceship, "scaleY", 0.7f, 1.2f);
        scaleX.setDuration(2000);
        scaleY.setDuration(2000);

        AnimatorSet landingSet = new AnimatorSet();
        landingSet.playTogether(descend, scaleX, scaleY);
        landingSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                completeLanding();
            }
        });
        landingSet.start();

        // Engine glow fades
        ivEngineGlow.animate().alpha(0f).setDuration(1500).start();
    }

    private void completeLanding() {
        tvStatus.setText("✅ Đã đến " + destinationPlanet.getName() + "!");
        showBuddySpeech("Chúng ta đến rồi! Tuyệt vời! 🎉");

        // Celebration effect
        animateBuddyCelebration();

        // Show arrival message
        TextView arrivalText = new TextView(this);
        arrivalText.setText("Chào mừng đến " + destinationPlanet.getEmoji() + " " + destinationPlanet.getName() + "!");
        arrivalText.setTextColor(Color.parseColor("#FFD700"));
        arrivalText.setTextSize(28);
        arrivalText.setGravity(Gravity.CENTER);
        arrivalText.setAlpha(0f);
        FrameLayout.LayoutParams arrivalParams = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT);
        arrivalParams.gravity = Gravity.CENTER;
        arrivalText.setLayoutParams(arrivalParams);
        rootLayout.addView(arrivalText);

        arrivalText.animate().alpha(1f).setDuration(500).start();

        // Buddy announces arrival
        buddyManager.onTravelArrive(destinationPlanet.getName());

        // Finish after delay
        handler.postDelayed(this::finishTravel, 3000);
    }

    private void finishTravel() {
        isAnimating = false;

        // Return result to calling activity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("arrived_planet_id", destinationPlanet.getId());
        setResult(RESULT_OK, resultIntent);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void showBuddySpeech(String text) {
        tvBuddySpeech.setText(text);
        tvBuddySpeech.setVisibility(View.VISIBLE);
        tvBuddySpeech.setAlpha(0f);
        tvBuddySpeech.animate().alpha(1f).setDuration(300).start();

        handler.postDelayed(() -> {
            if (tvBuddySpeech != null) {
                tvBuddySpeech.animate().alpha(0f).setDuration(300)
                    .withEndAction(() -> tvBuddySpeech.setVisibility(View.GONE))
                    .start();
            }
        }, 2500);
    }

    private void animateBuddyExcited() {
        ObjectAnimator jump = ObjectAnimator.ofFloat(cardBuddy, "translationY", 0, -30, 0);
        jump.setDuration(400);
        jump.setRepeatCount(3);
        jump.setInterpolator(new AccelerateDecelerateInterpolator());
        jump.start();
    }

    private void animateBuddyCelebration() {
        AnimatorSet celebrationSet = new AnimatorSet();

        ObjectAnimator jump = ObjectAnimator.ofFloat(cardBuddy, "translationY", 0, -50, 0);
        jump.setDuration(300);
        jump.setRepeatCount(4);

        ObjectAnimator rotate = ObjectAnimator.ofFloat(cardBuddy, "rotation", 0, 360);
        rotate.setDuration(600);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(cardBuddy, "scaleX", 1f, 1.3f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(cardBuddy, "scaleY", 1f, 1.3f, 1f);
        scaleX.setDuration(600);
        scaleY.setDuration(600);

        celebrationSet.playTogether(jump, rotate, scaleX, scaleY);
        celebrationSet.start();
    }

    // TravelEventListener methods
    @Override
    public void onTravelPhaseChanged(int phase, String phaseName) {
        // Already handled by our own animation sequence
    }

    @Override
    public void onTravelProgress(float progress) {
        progressBar.setProgress((int)(progress * 100));
    }

    @Override
    public void onTravelComplete(Planet destination) {
        // Already handled
    }

    @Override
    public void onTravelCancelled() {
        finish();
    }

    @Override
    public void onFuelChanged(int currentFuel, int maxFuel) {
        // Not needed during travel
    }

    @Override
    public void onBackPressed() {
        // Disable back during travel animation
        if (!isAnimating) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        travelManager.removeListener(this);
        if (engineSound != null) {
            engineSound.release();
        }
    }
}

