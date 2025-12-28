package com.example.engapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.engapp.database.GameDatabaseHelper;
import com.example.engapp.database.GameDatabaseHelper.*;
import java.util.Random;

public class SpaceshipHubActivity extends AppCompatActivity {

    private TextView tvPlayerName, tvLevel, tvStars, tvFuelCells, tvCrystals;
    private TextView tvBuddyMessage, tvDailyProgress;
    private ProgressBar progressLevel, progressDailyMission;
    private CardView cardAvatar;
    private CardView cardFunMemory, cardFunCatch, cardFunFuel;

    private LinearLayout btnNavHub, btnNavWordLab, btnNavBuddy, btnNavAdventure;

    private GameDatabaseHelper dbHelper;
    private UserProgressData userProgress;

    private String[] buddyMessages = {
        "Xin chào! Hôm nay chúng ta học gì nhỉ? 🚀",
        "Tuyệt vời! Bạn đã sẵn sàng khám phá chưa? 🌟",
        "Cùng thu thập thêm Word Crystals nào! 💎",
        "Mỗi ngày học một ít, giỏi lên từng chút! 📚",
        "Wow! Bạn thật là siêu sao! ⭐",
        "Hành tinh mới đang chờ bạn! 🌍",
        "Đừng quên hoàn thành nhiệm vụ hôm nay nhé! 🎯"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spaceship_hub);

        dbHelper = GameDatabaseHelper.getInstance(this);
        initViews();
        setupFunZone();
        loadData();
        updateUI(); // Update UI với data đã load
        setupBottomNav();
        setRandomBuddyMessage();
        loadBuddyAndAnimate();
    }

    private void loadBuddyAndAnimate() {
        SharedPreferences prefs = getSharedPreferences("game_prefs", MODE_PRIVATE);
        int buddyIndex = prefs.getInt("buddy_index", 0);
        String[] buddyEmojis = {"🤖", "👽", "🐱", "🦊"};

        TextView tvBuddy = findViewById(R.id.tvBuddy);
        if (tvBuddy != null && buddyIndex < buddyEmojis.length) {
            tvBuddy.setText(buddyEmojis[buddyIndex]);
            Animation floatAnim = AnimationUtils.loadAnimation(this, R.anim.float_up_down);
            tvBuddy.startAnimation(floatAnim);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
        updateUI();
    }


    private void initViews() {
        tvPlayerName = findViewById(R.id.tvPlayerName);
        tvLevel = findViewById(R.id.tvLevel);
        tvStars = findViewById(R.id.tvStars);
        tvFuelCells = findViewById(R.id.tvFuelCells);
        tvCrystals = findViewById(R.id.tvCrystals);
        tvBuddyMessage = findViewById(R.id.tvBuddyMessage);
        tvDailyProgress = findViewById(R.id.tvDailyProgress);
        progressLevel = findViewById(R.id.progressLevel);
        progressDailyMission = findViewById(R.id.progressDailyMission);
        cardAvatar = findViewById(R.id.cardAvatar);
        cardFunMemory = findViewById(R.id.cardFunMemory);
        cardFunCatch = findViewById(R.id.cardFunCatch);
        cardFunFuel = findViewById(R.id.cardFunFuel);

        btnNavHub = findViewById(R.id.btnNavHub);
        btnNavWordLab = findViewById(R.id.btnNavWordLab);
        btnNavBuddy = findViewById(R.id.btnNavBuddy);
        btnNavAdventure = findViewById(R.id.btnNavAdventure);

        cardAvatar.setOnClickListener(v -> openProfile());

        // Daily Mission card click
        CardView cardDailyMission = findViewById(R.id.cardDailyMission);
        if (cardDailyMission != null) {
            cardDailyMission.setOnClickListener(v -> {
                Intent intent = new Intent(this, DailyMissionsActivity.class);
                startActivity(intent);
            });
        }

        // Quick Actions - Galaxy Map
        CardView cardGalaxyMap = findViewById(R.id.cardGalaxyMap);
        if (cardGalaxyMap != null) {
            cardGalaxyMap.setOnClickListener(v -> {
                Intent intent = new Intent(this, InteractiveStarMapActivity.class);
                startActivity(intent);
            });
        }

        // Quick Actions - Word Review
        CardView cardWordReview = findViewById(R.id.cardWordReview);
        if (cardWordReview != null) {
            cardWordReview.setOnClickListener(v -> {
                Intent intent = new Intent(this, WordReviewActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_scale_in, 0);
            });
        }

        // Quick Actions - Battle (Old system with ABCD and images)
        CardView cardBattle = findViewById(R.id.cardBattle);
        if (cardBattle != null) {
            cardBattle.setOnClickListener(v -> {
                Intent intent = new Intent(this, WordBattleActivity.class);
                intent.putExtra("planet_id", userProgress != null ? userProgress.currentPlanetId : 1);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_scale_in, 0);
            });
        }
    }

    private void setupFunZone() {
        if (cardFunMemory != null) {
            cardFunMemory.setOnClickListener(v -> openConstellationMemory());
        }
        if (cardFunCatch != null) {
            cardFunCatch.setOnClickListener(v -> openStarCatch());
        }
        if (cardFunFuel != null) {
            cardFunFuel.setOnClickListener(v -> openFuelMix());
        }
    }

    private void openConstellationMemory() {
        Intent intent = new Intent(this, ConstellationMemoryActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_scale_in, 0);
    }

    private void openStarCatch() {
        Intent intent = new Intent(this, StarCatchActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_scale_in, 0);
    }

    private void openFuelMix() {
        Intent intent = new Intent(this, FuelMixActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_scale_in, 0);
    }

    private void loadData() {
        userProgress = dbHelper.getUserProgress();
    }


    private void updateUI() {
        if (userProgress != null) {
            tvStars.setText(String.valueOf(userProgress.totalStars));
            tvFuelCells.setText(String.valueOf(userProgress.totalFuelCells));
            tvCrystals.setText(String.valueOf(userProgress.totalCrystals));
            tvLevel.setText(String.valueOf(userProgress.currentLevel));

            int xpInLevel = userProgress.experiencePoints % 100;
            progressLevel.setProgress(xpInLevel);

            int dailyProgress = Math.min(userProgress.wordsLearned % 10, 10);
            progressDailyMission.setProgress(dailyProgress);
            tvDailyProgress.setText(dailyProgress + "/10 tu");
        }
    }





    private void setupBottomNav() {
        btnNavHub.setOnClickListener(v -> {
            // Already on Hub
        });

        btnNavWordLab.setOnClickListener(v -> {
            Intent intent = new Intent(this, WordLabActivity.class);
            startActivity(intent);
        });

        btnNavBuddy.setOnClickListener(v -> {
            Intent intent = new Intent(this, BuddyRoomActivity.class);
            startActivity(intent);
        });

        com.google.android.material.floatingactionbutton.FloatingActionButton fabGalaxyMap =
            findViewById(R.id.fabGalaxyMap);
        if (fabGalaxyMap != null) {
            fabGalaxyMap.setOnClickListener(v -> {
                // Navigate to full planet map
                Intent intent = new Intent(this, InteractiveStarMapActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_scale_in, 0);
            });

            Animation pulseAnim = AnimationUtils.loadAnimation(this, R.anim.pulse);
            fabGalaxyMap.startAnimation(pulseAnim);
        }

        btnNavAdventure.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_press));
            Intent intent = new Intent(this, WordBattleActivity.class);
            intent.putExtra("planet_id", userProgress != null ? userProgress.currentPlanetId : 1);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_scale_in, 0);
        });
    }

    private void openProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    private void setRandomBuddyMessage() {
        Random random = new Random();
        String message = buddyMessages[random.nextInt(buddyMessages.length)];
        tvBuddyMessage.setText(message);
    }



    @Override
    @SuppressLint("MissingSuperCall")
    public void onBackPressed() {
        finishAffinity();
    }

}


