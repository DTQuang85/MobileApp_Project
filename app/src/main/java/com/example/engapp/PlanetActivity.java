package com.example.engapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.engapp.adapter.ZoneAdapter;
import com.example.engapp.data.GameDataProvider;
import com.example.engapp.model.Planet;
import com.example.engapp.model.Zone;

public class PlanetActivity extends AppCompatActivity {

    private RecyclerView recyclerZones;
    private ZoneAdapter zoneAdapter;
    private TextView tvPlanetName, tvPlanetNameVi, tvPlanetEmoji, tvProgress, tvStars;
    private ProgressBar progressPlanet;
    private ImageView btnBack;

    private Planet currentPlanet;
    private String planetId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planet);

        planetId = getIntent().getStringExtra("planet_id");
        if (planetId == null) {
            finish();
            return;
        }

        initViews();
        loadPlanet();
        setupZones();
    }

    private void initViews() {
        recyclerZones = findViewById(R.id.recyclerZones);
        tvPlanetName = findViewById(R.id.tvPlanetName);
        tvPlanetNameVi = findViewById(R.id.tvPlanetNameVi);
        tvPlanetEmoji = findViewById(R.id.tvPlanetEmoji);
        tvProgress = findViewById(R.id.tvProgress);
        tvStars = findViewById(R.id.tvStars);
        progressPlanet = findViewById(R.id.progressPlanet);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
    }

    private void loadPlanet() {
        currentPlanet = GameDataProvider.getPlanetById(planetId);

        if (currentPlanet == null) {
            finish();
            return;
        }

        tvPlanetName.setText(currentPlanet.getName());
        tvPlanetNameVi.setText(currentPlanet.getNameVi());
        tvPlanetEmoji.setText(currentPlanet.getEmoji());

        int progress = currentPlanet.getProgress();
        progressPlanet.setProgress(progress);
        tvProgress.setText(progress + "% hoàn thành");

        int totalStars = 0;
        int maxStars = 0;
        if (currentPlanet.getZones() != null) {
            maxStars = currentPlanet.getZones().size() * 3;
            for (Zone zone : currentPlanet.getZones()) {
                totalStars += zone.getStarsEarned();
            }
        }
        tvStars.setText(totalStars + "/" + maxStars);
    }

    private void setupZones() {
        recyclerZones.setLayoutManager(new LinearLayoutManager(this));

        if (currentPlanet.getZones() != null) {
            // Unlock first zone
            if (!currentPlanet.getZones().isEmpty()) {
                currentPlanet.getZones().get(0).setUnlocked(true);
            }

            zoneAdapter = new ZoneAdapter(this, currentPlanet.getZones(), (zone, position) -> {
                Intent intent = new Intent(PlanetActivity.this, ZoneActivity.class);
                intent.putExtra("planet_id", planetId);
                intent.putExtra("zone_index", position);
                startActivity(intent);
            });

            recyclerZones.setAdapter(zoneAdapter);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPlanet();
        if (zoneAdapter != null) {
            zoneAdapter.notifyDataSetChanged();
        }
    }
}

