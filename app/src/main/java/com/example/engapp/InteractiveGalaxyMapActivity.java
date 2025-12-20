package com.example.engapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.engapp.database.GameDatabaseHelper;
import com.example.engapp.view.GalaxyMapView;

/**
 * Interactive Galaxy Map - Hiển thị các galaxy dạng node trên bản đồ vũ trụ
 */
public class InteractiveGalaxyMapActivity extends AppCompatActivity {

    private GalaxyMapView galaxyMapView;
    private TextView tvStarCount, tvFuelCount;
    private ImageButton btnBack;
    private GameDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interactive_galaxy_map);

        dbHelper = GameDatabaseHelper.getInstance(this);

        initViews();
        loadGalaxyData();
    }

    private void initViews() {
        galaxyMapView = findViewById(R.id.galaxyMapView);
        tvStarCount = findViewById(R.id.tvStarCount);
        tvFuelCount = findViewById(R.id.tvFuelCount);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> onBackPressed());

        // Set galaxy click listener
        galaxyMapView.setOnGalaxyClickListener((galaxyId, galaxyName, galaxyEmoji) -> {
            // Navigate to Interactive Planet Map
            Intent intent = new Intent(this, InteractivePlanetMapActivity.class);
            intent.putExtra("galaxy_id", galaxyId);
            intent.putExtra("galaxy_name", galaxyName);
            intent.putExtra("galaxy_emoji", galaxyEmoji);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_scale_in, 0);
        });
    }

    private void loadGalaxyData() {
        // Get user progress
        GameDatabaseHelper.UserProgressData progress = dbHelper.getUserProgress();
        if (progress != null) {
            tvStarCount.setText(String.valueOf(progress.totalStars));
            tvFuelCount.setText(String.valueOf(progress.totalFuelCells));
        }

        // Load galaxies into map view
        galaxyMapView.loadGalaxies(progress != null ? progress.totalStars : 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGalaxyData();
    }
}

