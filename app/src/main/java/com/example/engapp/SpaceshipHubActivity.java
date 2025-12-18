package com.example.engapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.engapp.database.GameDatabaseHelper;
import com.example.engapp.database.GameDatabaseHelper.*;
import java.util.List;
import java.util.Random;

public class SpaceshipHubActivity extends AppCompatActivity {

    private RecyclerView recyclerPlanets;
    private TextView tvPlayerName, tvLevel, tvStars, tvFuelCells, tvCrystals;
    private TextView tvBuddyMessage, tvDailyProgress;
    private ProgressBar progressLevel, progressDailyMission;
    private CardView cardAvatar;

    private LinearLayout btnNavHub, btnNavWordLab, btnNavBuddy, btnNavBadges, btnNavProfile;

    private GameDatabaseHelper dbHelper;
    private List<PlanetData> planets;
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
        loadData();
        setupRecyclerView();
        setupBottomNav();
        setRandomBuddyMessage();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
        updateUI();
    }

    private void initViews() {
        recyclerPlanets = findViewById(R.id.recyclerPlanets);
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

        btnNavHub = findViewById(R.id.btnNavHub);
        btnNavWordLab = findViewById(R.id.btnNavWordLab);
        btnNavBuddy = findViewById(R.id.btnNavBuddy);
        btnNavBadges = findViewById(R.id.btnNavBadges);
        btnNavProfile = findViewById(R.id.btnNavProfile);

        cardAvatar.setOnClickListener(v -> openProfile());
    }

    private void loadData() {
        planets = dbHelper.getAllPlanets();
        userProgress = dbHelper.getUserProgress();
    }

    private void updateUI() {
        if (userProgress != null) {
            tvStars.setText(String.valueOf(userProgress.totalStars));
            tvFuelCells.setText(String.valueOf(userProgress.totalFuelCells));
            tvCrystals.setText(String.valueOf(userProgress.totalCrystals));
            tvLevel.setText(String.valueOf(userProgress.currentLevel));

            // XP progress (every 100 XP = 1 level)
            int xpInLevel = userProgress.experiencePoints % 100;
            progressLevel.setProgress(xpInLevel);

            // Daily mission progress (learn 10 words)
            int dailyProgress = Math.min(userProgress.wordsLearned % 10, 10);
            progressDailyMission.setProgress(dailyProgress);
            tvDailyProgress.setText(dailyProgress + "/10 từ");
        }

        if (recyclerPlanets.getAdapter() != null) {
            recyclerPlanets.getAdapter().notifyDataSetChanged();
        }
    }

    private void setupRecyclerView() {
        recyclerPlanets.setLayoutManager(new LinearLayoutManager(this));
        recyclerPlanets.setAdapter(new PlanetAdapter());
        recyclerPlanets.setNestedScrollingEnabled(false);
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

        btnNavBadges.setOnClickListener(v -> {
            Intent intent = new Intent(this, BadgesActivity.class);
            startActivity(intent);
        });

        btnNavProfile.setOnClickListener(v -> openProfile());
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

    private void openPlanet(PlanetData planet) {
        if (!planet.isUnlocked) {
            int fuelNeeded = planet.requiredFuelCells;
            int currentFuel = userProgress != null ? userProgress.totalFuelCells : 0;

            if (currentFuel >= fuelNeeded) {
                // Unlock the planet
                dbHelper.unlockPlanet(planet.id);
                planet.isUnlocked = true;
                Toast.makeText(this, "🔓 Mở khóa " + planet.nameVi + "!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Cần " + fuelNeeded + " 🔋 Fuel Cells để mở khóa!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Intent intent = new Intent(this, PlanetActivity.class);
        intent.putExtra("planet_id", planet.id);
        intent.putExtra("planet_name", planet.name);
        intent.putExtra("planet_name_vi", planet.nameVi);
        intent.putExtra("planet_emoji", planet.emoji);
        intent.putExtra("planet_color", planet.themeColor);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        // Close app instead of going back
        finishAffinity();
    }

    // ============ PLANET ADAPTER ============

    class PlanetAdapter extends RecyclerView.Adapter<PlanetAdapter.PlanetViewHolder> {

        private int[] gradientColors = {
            Color.parseColor("#FF6B6B"), // Coloria - Red/Pink
            Color.parseColor("#4ECDC4"), // Toytopia - Teal
            Color.parseColor("#45B7D1"), // Animania - Blue
            Color.parseColor("#96CEB4"), // Citytron - Green
            Color.parseColor("#FFEAA7"), // Foodora - Yellow
            Color.parseColor("#74B9FF"), // Weatheron - Light Blue
            Color.parseColor("#A29BFE"), // RoboLab - Purple
            Color.parseColor("#FD79A8"), // TimeLapse - Pink
            Color.parseColor("#E17055"), // Storyverse - Orange
        };

        @NonNull
        @Override
        public PlanetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_planet_new, parent, false);
            return new PlanetViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PlanetViewHolder holder, int position) {
            PlanetData planet = planets.get(position);

            holder.tvPlanetName.setText(planet.name);
            holder.tvPlanetNameVi.setText(planet.nameVi);
            holder.tvPlanetEmoji.setText(planet.emoji);
            holder.tvSkillFocus.setText("📚 " + planet.skillFocus);
            holder.tvCollectibleEmoji.setText(planet.collectibleEmoji);

            // Set gradient background color based on planet
            int colorIndex = position % gradientColors.length;
            holder.planetContainer.setBackgroundColor(gradientColors[colorIndex]);

            // Handle lock state
            if (planet.isUnlocked) {
                holder.lockOverlay.setVisibility(View.GONE);
                holder.btnPlay.setVisibility(View.VISIBLE);
            } else {
                holder.lockOverlay.setVisibility(View.VISIBLE);
                holder.btnPlay.setVisibility(View.GONE);
                holder.tvRequiredFuel.setText(String.valueOf(planet.requiredFuelCells));
            }

            // Progress (placeholder - should be calculated from scenes)
            holder.progressPlanet.setProgress(0);
            holder.tvProgress.setText("0%");

            // Click listener
            holder.itemView.setOnClickListener(v -> openPlanet(planet));
            holder.btnPlay.setOnClickListener(v -> openPlanet(planet));
        }

        @Override
        public int getItemCount() {
            return planets != null ? planets.size() : 0;
        }

        class PlanetViewHolder extends RecyclerView.ViewHolder {
            View planetContainer;
            FrameLayout lockOverlay;
            TextView tvPlanetName, tvPlanetNameVi, tvPlanetEmoji;
            TextView tvSkillFocus, tvCollectibleEmoji, tvCollectibleCount;
            TextView tvRequiredFuel, tvProgress, btnPlay;
            TextView tvStar1, tvStar2, tvStar3;
            ProgressBar progressPlanet;

            PlanetViewHolder(@NonNull View itemView) {
                super(itemView);
                planetContainer = itemView.findViewById(R.id.planetContainer);
                lockOverlay = itemView.findViewById(R.id.lockOverlay);
                tvPlanetName = itemView.findViewById(R.id.tvPlanetName);
                tvPlanetNameVi = itemView.findViewById(R.id.tvPlanetNameVi);
                tvPlanetEmoji = itemView.findViewById(R.id.tvPlanetEmoji);
                tvSkillFocus = itemView.findViewById(R.id.tvSkillFocus);
                tvCollectibleEmoji = itemView.findViewById(R.id.tvCollectibleEmoji);
                tvCollectibleCount = itemView.findViewById(R.id.tvCollectibleCount);
                tvRequiredFuel = itemView.findViewById(R.id.tvRequiredFuel);
                tvProgress = itemView.findViewById(R.id.tvProgress);
                btnPlay = itemView.findViewById(R.id.btnPlay);
                tvStar1 = itemView.findViewById(R.id.tvStar1);
                tvStar2 = itemView.findViewById(R.id.tvStar2);
                tvStar3 = itemView.findViewById(R.id.tvStar3);
                progressPlanet = itemView.findViewById(R.id.progressPlanet);
            }
        }
    }
}
