package com.example.engapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
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
    private TextView tvStarCount, tvFuelCount, tvBuddyText;
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
        tvFuelCount = findViewById(R.id.tvFuelCount);
        tvBuddyText = findViewById(R.id.tvBuddyText);
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
        if (progress != null) {
            tvStarCount.setText(String.valueOf(progress.totalStars));
            tvFuelCount.setText(String.valueOf(progress.totalFuelCells));

            // Unlock galaxies based on stars
            for (GalaxyData galaxy : galaxies) {
                if (progress.totalStars >= galaxy.starsRequired) {
                    galaxy.isUnlocked = true;
                }
            }
        }

        adapter = new GalaxyAdapter(galaxies, this);
        rvGalaxies.setAdapter(adapter);

        // Buddy speech
        tvBuddyText.setText("Chọn một thiên hà để khám phá! Mỗi thiên hà có các hành tinh với từ vựng mới! 🚀");

        setupBottomNavigation();
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
            Intent intent = new Intent(this, WordBattleActivity.class);
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
