package com.example.engapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.engapp.database.GameDatabaseHelper;
import com.example.engapp.view.ConstellationGalaxyMapView;
import com.example.engapp.view.ConfettiView;

/**
 * Constellation Galaxy Map - Redesigned for kids with beautiful constellation path
 * Features: Vertical path, glowing nodes, progress rings, space decorations, Buddy integration
 */
public class InteractiveGalaxyMapActivity extends AppCompatActivity {

    private ConstellationGalaxyMapView constellationGalaxyMapView;
    private TextView tvStarCount, tvNextUnlockRequirement, tvProgressText;
    private ProgressBar progressNextUnlock;
    private CardView buddySpeechCard;
    private TextView tvBuddyText;
    private ImageButton btnBack;
    private ConfettiView confettiView;
    private GameDatabaseHelper dbHelper;
    
    private int currentGalaxyId = 1;
    private int previousStars = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_constellation_galaxy_map);

        dbHelper = GameDatabaseHelper.getInstance(this);

        initViews();
        loadGalaxyData();
        setupBuddyMessage();
    }

    private void initViews() {
        constellationGalaxyMapView = findViewById(R.id.constellationGalaxyMapView);
        tvStarCount = findViewById(R.id.tvStarCount);
        tvNextUnlockRequirement = findViewById(R.id.tvNextUnlockRequirement);
        tvProgressText = findViewById(R.id.tvProgressText);
        progressNextUnlock = findViewById(R.id.progressNextUnlock);
        buddySpeechCard = findViewById(R.id.buddySpeechCard);
        tvBuddyText = findViewById(R.id.tvBuddyText);
        confettiView = findViewById(R.id.confettiView);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> onBackPressed());

        // Set galaxy click listeners
        constellationGalaxyMapView.setOnGalaxyClickListener(
            new ConstellationGalaxyMapView.OnGalaxyClickListener() {
                @Override
                public void onGalaxyClick(int galaxyId, String galaxyName, String galaxyEmoji) {
                    // Navigate to Interactive Star Map (Planet Map)
                    Intent intent = new Intent(InteractiveGalaxyMapActivity.this, 
                        InteractiveStarMapActivity.class);
                    intent.putExtra("galaxy_id", galaxyId);
                    intent.putExtra("galaxy_name", galaxyName);
                    intent.putExtra("galaxy_emoji", galaxyEmoji);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_scale_in, 0);
                }

                @Override
                public void onLockedGalaxyClick(int galaxyId, int starsRequired) {
                    // Show friendly message
                    String message = "Cần " + starsRequired + " ⭐ để mở khóa thiên hà này!";
                    Toast.makeText(InteractiveGalaxyMapActivity.this, message, 
                        Toast.LENGTH_SHORT).show();
                    
                    // Update buddy message
                    updateBuddyLockedMessage(starsRequired);
                }
            }
        );

        setupBottomNavigation();
    }
    
    private void setupBuddyMessage() {
        // Ẩn thông báo buddy - không hiển thị nữa
        buddySpeechCard.setVisibility(View.GONE);
    }
    
    private void updateBuddyLockedMessage(int starsRequired) {
        // Ẩn thông báo buddy - không hiển thị nữa
        buddySpeechCard.setVisibility(View.GONE);
    }

    private void setupBottomNavigation() {
        findViewById(R.id.btnNavHub).setOnClickListener(v -> {
            startActivity(new Intent(this, SpaceshipHubActivity.class));
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        findViewById(R.id.btnNavWordLab).setOnClickListener(v -> {
            startActivity(new Intent(this, WordLabActivity.class));
        });

        findViewById(R.id.btnNavMap).setOnClickListener(v -> {
            // Already on Galaxy Map - do nothing
            android.widget.Toast.makeText(this, "Đang ở Bản đồ Thiên hà 🌌", android.widget.Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnNavAdventure).setOnClickListener(v -> {
            Intent intent = new Intent(this, BattleActivity.class);
            intent.putExtra("planet_id", 1); // Default to first planet
            startActivity(intent);
            overridePendingTransition(R.anim.fade_scale_in, 0);
        });

        findViewById(R.id.btnNavBuddy).setOnClickListener(v -> {
            startActivity(new Intent(this, BuddyRoomActivity.class));
        });
    }

    private void loadGalaxyData() {
        // Get user progress
        GameDatabaseHelper.UserProgressData progress = dbHelper.getUserProgress();
        int totalStars = progress != null ? progress.totalStars : 0;
        
        if (progress != null) {
            tvStarCount.setText(String.valueOf(totalStars));
        }

        // Check for new unlocks
        boolean galaxy2Unlocked = totalStars >= 30 && previousStars < 30;
        boolean galaxy3Unlocked = totalStars >= 60 && previousStars < 60;
        
        if (galaxy2Unlocked || galaxy3Unlocked) {
            // Celebrate unlock!
            celebrateUnlock(galaxy2Unlocked ? 2 : 3);
        }

        // Determine current galaxy (first unlocked or last completed)
        currentGalaxyId = 1;
        if (totalStars >= 60) {
            currentGalaxyId = 3;
        } else if (totalStars >= 30) {
            currentGalaxyId = 2;
        }

        // Load galaxies into constellation view
        constellationGalaxyMapView.loadGalaxies(totalStars, currentGalaxyId);
        
        // Update progress HUD
        updateProgressHUD(totalStars);
        
        // Update buddy message
        updateBuddyMessage(totalStars);
        
        // Save previous stars for next check
        previousStars = totalStars;
    }
    
    private void celebrateUnlock(int galaxyId) {
        // Confetti!
        confettiView.setVisibility(View.VISIBLE);
        confettiView.startConfetti();
        
        // Ẩn thông báo buddy - không hiển thị nữa
        buddySpeechCard.setVisibility(View.GONE);
        
        // Hide confetti after animation
        getWindow().getDecorView().postDelayed(() -> {
            confettiView.setVisibility(View.GONE);
        }, 3000);
    }
    
    private void updateProgressHUD(int currentStars) {
        // Find next locked galaxy
        int nextUnlockStars = 30; // Explorer Galaxy
        if (currentStars >= 30) {
            nextUnlockStars = 60; // Advanced Galaxy
        }
        if (currentStars >= 60) {
            // All unlocked
            tvNextUnlockRequirement.setText("");
            tvProgressText.setText("Tất cả thiên hà đã mở khóa! 🌟");
            tvProgressText.setTextColor(0xFF4CAF50);
            progressNextUnlock.setProgress(100);
            return;
        }
        
        int remaining = nextUnlockStars - currentStars;
        int progressPercent = nextUnlockStars > 0 ? 
            (int)((float)currentStars / nextUnlockStars * 100) : 0;
        progressPercent = Math.min(100, progressPercent);
        
        tvNextUnlockRequirement.setText("/ " + nextUnlockStars);
        progressNextUnlock.setProgress(progressPercent);
        
        if (remaining <= 0) {
            tvProgressText.setText("Sẵn sàng mở khóa! 🎉");
            tvProgressText.setTextColor(0xFF4CAF50);
        } else if (remaining <= 5) {
            tvProgressText.setText("Sắp mở rồi! Còn " + remaining + " ⭐ nữa! 💪");
            tvProgressText.setTextColor(0xFFFFD700);
        } else {
            tvProgressText.setText("Cần thêm " + remaining + " ⭐ nữa!");
            tvProgressText.setTextColor(0xFFFFD700);
        }
    }
    
    private void updateBuddyMessage(int totalStars) {
        // Ẩn thông báo buddy - không hiển thị nữa
        buddySpeechCard.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGalaxyData();
    }
}

