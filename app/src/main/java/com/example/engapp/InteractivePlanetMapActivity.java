package com.example.engapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.engapp.database.GameDatabaseHelper;
import com.example.engapp.view.PlanetMapView;

/**
 * Interactive Planet Map - Hiển thị các planet dạng hành tinh tròn với animation tàu vũ trụ
 */
public class InteractivePlanetMapActivity extends AppCompatActivity {

    private PlanetMapView planetMapView;
    private TextView tvGalaxyName, tvStarCount, tvFuelCount;
    private ImageButton btnBack;
    private GameDatabaseHelper dbHelper;

    private int galaxyId;
    private String galaxyName, galaxyEmoji;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interactive_planet_map);

        dbHelper = GameDatabaseHelper.getInstance(this);

        getIntentData();
        initViews();
        loadPlanetData();
    }

    private void getIntentData() {
        galaxyId = getIntent().getIntExtra("galaxy_id", 1);
        galaxyName = getIntent().getStringExtra("galaxy_name");
        galaxyEmoji = getIntent().getStringExtra("galaxy_emoji");

        if (galaxyName == null) galaxyName = "Galaxy";
        if (galaxyEmoji == null) galaxyEmoji = "🌌";
    }

    private void initViews() {
        planetMapView = findViewById(R.id.planetMapView);
        tvGalaxyName = findViewById(R.id.tvGalaxyName);
        tvStarCount = findViewById(R.id.tvStarCount);
        tvFuelCount = findViewById(R.id.tvFuelCount);
        btnBack = findViewById(R.id.btnBack);

        tvGalaxyName.setText(galaxyEmoji + " " + galaxyName);
        btnBack.setOnClickListener(v -> onBackPressed());

        // Set planet click listener
        planetMapView.setOnPlanetClickListener(planet -> {
            if (!planet.isUnlocked) {
                // Show unlock dialog or toast
                return;
            }

            // Navigate to PlanetMapActivity (scenes/missions)
            Intent intent = new Intent(this, PlanetMapActivity.class);
            intent.putExtra("planet_id", planet.id);
            intent.putExtra("planet_name", planet.name);
            intent.putExtra("planet_name_vi", planet.nameVi);
            intent.putExtra("planet_emoji", planet.emoji);
            intent.putExtra("planet_color", planet.themeColor);
            startActivity(intent);
            overridePendingTransition(R.anim.warp_in, R.anim.warp_out);
        });
    }

    private void loadPlanetData() {
        // Get user progress
        GameDatabaseHelper.UserProgressData progress = dbHelper.getUserProgress();
        if (progress != null) {
            tvStarCount.setText(String.valueOf(progress.totalStars));
            tvFuelCount.setText(String.valueOf(progress.totalFuelCells));
        }

        // Load planets for this galaxy
        planetMapView.loadPlanets(galaxyId, progress);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPlanetData();
    }
}

