package com.example.engapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.engapp.adapter.GalaxyAdapter;
import com.example.engapp.database.GameDatabaseHelper;
import java.util.ArrayList;
import java.util.List;

public class GalaxyMapActivity extends AppCompatActivity implements GalaxyAdapter.OnGalaxyClickListener {

    private RecyclerView rvGalaxies;
    private TextView tvStarCount, tvBuddyText;
    private TextView tvNextUnlockRequirement, tvNextUnlockTarget, tvProgressText;
    private ProgressBar progressNextUnlock;
    private CardView buddySpeech;
    private FrameLayout loadingOverlay;
    private ImageButton btnBack;

    private GameDatabaseHelper dbHelper;
    private GalaxyAdapter adapter;
    private List<GalaxyData> galaxies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_galaxy_map);

        dbHelper = GameDatabaseHelper.getInstance(this);

        initViews();
        loadData();
        setupUI();
    }

    private void initViews() {
        rvGalaxies = findViewById(R.id.rvGalaxies);
        tvStarCount = findViewById(R.id.tvStarCount);
        tvBuddyText = findViewById(R.id.tvBuddyText);
        tvNextUnlockRequirement = findViewById(R.id.tvNextUnlockRequirement);
        tvNextUnlockTarget = findViewById(R.id.tvNextUnlockTarget);
        tvProgressText = findViewById(R.id.tvProgressText);
        progressNextUnlock = findViewById(R.id.progressNextUnlock);
        buddySpeech = findViewById(R.id.buddySpeech);
        loadingOverlay = findViewById(R.id.loadingOverlay);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> onBackPressed());

        rvGalaxies.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadData() {
        galaxies = new ArrayList<>();

        // Create galaxy data - each galaxy contains multiple planets
        galaxies.add(new GalaxyData(1, "🌌", "Beginner Galaxy", "Thiên hà Khởi đầu",
                "Learn basic words", true, 0,
                new String[]{"🎨", "🧸", "🔢"}));

        galaxies.add(new GalaxyData(2, "🌠", "Explorer Galaxy", "Thiên hà Khám phá",
                "Food, Family & Nature", false, 30,
                new String[]{"🍎", "👨‍👩‍👧", "🌳"}));

        galaxies.add(new GalaxyData(3, "✨", "Advanced Galaxy", "Thiên hà Nâng cao",
                "Body, School & Actions", false, 60,
                new String[]{"🫀", "🏫", "🏃"}));
    }

    private void setupUI() {
        // Get user progress
        GameDatabaseHelper.UserProgressData progress = dbHelper.getUserProgress();
        int totalStars = 0;
        if (progress != null) {
            totalStars = progress.totalStars;
            tvStarCount.setText(String.valueOf(totalStars));

            // Unlock galaxies based on stars
            for (GalaxyData galaxy : galaxies) {
                if (totalStars >= galaxy.starsRequired) {
                    galaxy.isUnlocked = true;
                }
            }
        }

        // Setup next unlock preview
        setupNextUnlockPreview(totalStars);

        adapter = new GalaxyAdapter(galaxies, this, totalStars);
        rvGalaxies.setAdapter(adapter);

        // Smart buddy speech based on progress
        updateBuddyMessage(totalStars);

        setupBottomNavigation();
    }

    private void updateBuddyMessage(int totalStars) {
        String message;
        // Find next locked galaxy
        GalaxyData nextUnlock = null;
        for (GalaxyData galaxy : galaxies) {
            if (!galaxy.isUnlocked) {
                nextUnlock = galaxy;
                break;
            }
        }

        if (nextUnlock != null) {
            int remaining = nextUnlock.starsRequired - totalStars;
            if (remaining <= 0) {
                message = "Tuyệt vời! Bạn đã sẵn sàng mở khóa " + nextUnlock.nameVi + "! 🎉";
            } else if (remaining <= 5) {
                message = "Sắp mở khóa " + nextUnlock.nameVi + " rồi! Còn " + remaining + " ⭐ nữa! 💪";
            } else if (remaining <= 10) {
                message = "Tiếp tục phấn đấu! Còn " + remaining + " ⭐ nữa để mở " + nextUnlock.nameVi + "! ⭐";
            } else {
                message = "Chọn một thiên hà để khám phá! Mỗi thiên hà có các hành tinh với từ vựng mới! 🚀";
            }
        } else {
            message = "Tuyệt vời! Bạn đã mở khóa tất cả thiên hà! 🌟 Hãy tiếp tục khám phá!";
        }
        tvBuddyText.setText(message);
    }

    private void setupNextUnlockPreview(int currentStars) {
        // Find next locked galaxy
        GalaxyData nextUnlock = null;
        for (GalaxyData galaxy : galaxies) {
            if (!galaxy.isUnlocked) {
                nextUnlock = galaxy;
                break;
            }
        }

        if (nextUnlock != null) {
            int required = nextUnlock.starsRequired;
            int remaining = Math.max(0, required - currentStars);
            int progressPercent = required > 0 ? (int) ((float) currentStars / required * 100) : 0;
            progressPercent = Math.min(100, progressPercent);

            tvNextUnlockRequirement.setText("/ " + required);
            tvNextUnlockTarget.setText("→ Mở khóa " + nextUnlock.nameVi);
            progressNextUnlock.setProgress(progressPercent);

            if (remaining > 0) {
                tvProgressText.setText("Cần thêm " + remaining + " ⭐ nữa!");
                tvProgressText.setTextColor(0xFFFFD700); // Gold
            } else {
                tvProgressText.setText("Sẵn sàng mở khóa! 🎉");
                tvProgressText.setTextColor(0xFF4CAF50); // Green
            }
        } else {
            // All galaxies unlocked
            tvNextUnlockRequirement.setText("");
            tvNextUnlockTarget.setText("→ Tất cả thiên hà đã mở khóa! 🌟");
            progressNextUnlock.setProgress(100);
            tvProgressText.setText("Hoàn thành tất cả! 🎊");
            tvProgressText.setTextColor(0xFF4CAF50);
        }
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
            // Already on Galaxy Map - do nothing or show toast
            Toast.makeText(this, "Đang ở Bản đồ Thiên hà 🌌", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnNavAdventure).setOnClickListener(v -> {
            // Navigate to battle with current planet
            Intent intent = new Intent(this, BattleActivity.class);
            intent.putExtra("planet_id", 1); // Default to first planet
            startActivity(intent);
            overridePendingTransition(R.anim.fade_scale_in, 0);
        });

        findViewById(R.id.btnNavBuddy).setOnClickListener(v -> {
            startActivity(new Intent(this, BuddyRoomActivity.class));
        });
    }

    @Override
    public void onGalaxyClick(GalaxyData galaxy) {
        if (!galaxy.isUnlocked) {
            Toast.makeText(this, "Cần " + galaxy.starsRequired + " ⭐ để mở khóa thiên hà này!",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Navigate to InteractiveStarMapActivity (Planet Map) for this galaxy
        Intent intent = new Intent(this, InteractiveStarMapActivity.class);
        intent.putExtra("galaxy_id", galaxy.id);
        intent.putExtra("galaxy_name", galaxy.name);
        intent.putExtra("galaxy_name_vi", galaxy.nameVi);
        intent.putExtra("galaxy_emoji", galaxy.emoji);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_scale_in, 0);
    }

    // Galaxy data class
    public static class GalaxyData {
        public int id;
        public String emoji;
        public String name;
        public String nameVi;
        public String description;
        public boolean isUnlocked;
        public int starsRequired;
        public String[] planetEmojis;
        public int progress;

        public GalaxyData(int id, String emoji, String name, String nameVi,
                         String description, boolean isUnlocked, int starsRequired,
                         String[] planetEmojis) {
            this.id = id;
            this.emoji = emoji;
            this.name = name;
            this.nameVi = nameVi;
            this.description = description;
            this.isUnlocked = isUnlocked;
            this.starsRequired = starsRequired;
            this.planetEmojis = planetEmojis;
            this.progress = 0;
        }
    }
}

