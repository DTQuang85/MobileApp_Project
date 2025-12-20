package com.example.engapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.engapp.database.GameDatabaseHelper;
import com.example.engapp.database.GameDatabaseHelper.PlanetData;
import com.example.engapp.database.GameDatabaseHelper.UserProgressData;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity hiển thị danh sách các hành tinh (planets) của một thiên hà (galaxy) được chọn.
 * Flow: GalaxyMapActivity -> GalaxyPlanetsActivity -> PlanetMapActivity
 */
public class GalaxyPlanetsActivity extends AppCompatActivity {

    private RecyclerView rvPlanets;
    private TextView tvGalaxyName, tvGalaxyDescription, tvGalaxyEmoji;
    private TextView tvStarCount, tvFuelCount;
    private ImageButton btnBack;

    private GameDatabaseHelper dbHelper;
    private List<PlanetData> planets;
    private UserProgressData userProgress;

    private int galaxyId;
    private String galaxyName, galaxyNameVi, galaxyEmoji;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_galaxy_planets);

        dbHelper = GameDatabaseHelper.getInstance(this);

        getIntentData();
        initViews();
        loadData();
        setupUI();
    }

    private void getIntentData() {
        galaxyId = getIntent().getIntExtra("galaxy_id", 1);
        galaxyName = getIntent().getStringExtra("galaxy_name");
        galaxyNameVi = getIntent().getStringExtra("galaxy_name_vi");
        galaxyEmoji = getIntent().getStringExtra("galaxy_emoji");

        if (galaxyName == null) galaxyName = "Galaxy";
        if (galaxyNameVi == null) galaxyNameVi = "Thiên hà";
        if (galaxyEmoji == null) galaxyEmoji = "🌌";
    }

    private void initViews() {
        rvPlanets = findViewById(R.id.rvPlanets);
        tvGalaxyName = findViewById(R.id.tvGalaxyName);
        tvGalaxyDescription = findViewById(R.id.tvGalaxyDescription);
        tvGalaxyEmoji = findViewById(R.id.tvGalaxyEmoji);
        tvStarCount = findViewById(R.id.tvStarCount);
        tvFuelCount = findViewById(R.id.tvFuelCount);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> onBackPressed());

        rvPlanets.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadData() {
        userProgress = dbHelper.getUserProgress();

        // Lấy danh sách planets theo galaxy_id
        List<PlanetData> allPlanets = dbHelper.getAllPlanets();
        planets = new ArrayList<>();

        // Filter planets theo galaxy
        // Galaxy 1: planet 1-3 (Beginner - Colors, Toys, Numbers)
        // Galaxy 2: planet 4-6 (Explorer - Food, Family, Nature)
        // Galaxy 3: planet 7-9 (Advanced - Body, School, Actions)
        int startPlanet = (galaxyId - 1) * 3 + 1;
        int endPlanet = galaxyId * 3;

        for (PlanetData planet : allPlanets) {
            if (planet.id >= startPlanet && planet.id <= endPlanet) {
                planets.add(planet);
            }
        }
    }

    private void setupUI() {
        tvGalaxyName.setText(galaxyName);
        tvGalaxyDescription.setText(galaxyNameVi);
        tvGalaxyEmoji.setText(galaxyEmoji);

        if (userProgress != null) {
            tvStarCount.setText(String.valueOf(userProgress.totalStars));
            tvFuelCount.setText(String.valueOf(userProgress.totalFuelCells));
        }

        rvPlanets.setAdapter(new PlanetAdapter());
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

        // Navigate to PlanetMapActivity
        Intent intent = new Intent(this, PlanetMapActivity.class);
        intent.putExtra("planet_id", planet.id);
        intent.putExtra("planet_name", planet.name);
        intent.putExtra("planet_name_vi", planet.nameVi);
        intent.putExtra("planet_emoji", planet.emoji);
        intent.putExtra("planet_color", planet.themeColor);
        startActivity(intent);
        overridePendingTransition(R.anim.warp_in, R.anim.warp_out);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
        if (rvPlanets.getAdapter() != null) {
            rvPlanets.getAdapter().notifyDataSetChanged();
        }
        if (userProgress != null) {
            tvStarCount.setText(String.valueOf(userProgress.totalStars));
            tvFuelCount.setText(String.valueOf(userProgress.totalFuelCells));
        }
    }

    // ============ PLANET ADAPTER ============

    class PlanetAdapter extends RecyclerView.Adapter<PlanetAdapter.PlanetViewHolder> {

        private int[] gradientColors = {
            Color.parseColor("#FF6B6B"), // Red/Pink
            Color.parseColor("#4ECDC4"), // Teal
            Color.parseColor("#45B7D1"), // Blue
            Color.parseColor("#96CEB4"), // Green
            Color.parseColor("#FFEAA7"), // Yellow
            Color.parseColor("#74B9FF"), // Light Blue
            Color.parseColor("#A29BFE"), // Purple
            Color.parseColor("#FD79A8"), // Pink
            Color.parseColor("#E17055"), // Orange
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
            int colorIndex = (planet.id - 1) % gradientColors.length;
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

            // Calculate progress from scenes
            List<GameDatabaseHelper.SceneData> scenes = dbHelper.getScenesForPlanet(planet.id);
            int completed = 0;
            for (GameDatabaseHelper.SceneData scene : scenes) {
                if (scene.isCompleted) completed++;
            }
            int progress = scenes.size() > 0 ? (completed * 100 / scenes.size()) : 0;
            holder.progressPlanet.setProgress(progress);
            holder.tvProgress.setText(progress + "%");

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

