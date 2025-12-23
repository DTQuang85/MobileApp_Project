package com.example.engapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.engapp.adapter.PlanetAdapter;
import com.example.engapp.data.GameDataProvider;
import com.example.engapp.model.Planet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import de.hdodenhof.circleimageview.CircleImageView;
import java.util.List;

public class SpaceMapActivity extends AppCompatActivity {

    private RecyclerView recyclerPlanets;
    private PlanetAdapter planetAdapter;
    private LinearLayout layoutEras;
    private TextView tvEraTitle, tvStars, tvEnergy, tvUsername, tvLevel;
    private CircleImageView ivAvatar;

    private String currentEra = GameDataProvider.ERA_PREHISTORIC;
    private int totalStars = 0;
    private int totalEnergy = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_space_map);

        initViews();
        loadUserInfo();
        loadUserProgress();
        setupEraButtons();
        setupPlanets();
        setupBottomNav();
    }

    private void initViews() {
        recyclerPlanets = findViewById(R.id.recyclerPlanets);
        layoutEras = findViewById(R.id.layoutEras);
        tvEraTitle = findViewById(R.id.tvEraTitle);
        tvStars = findViewById(R.id.tvStars);
        tvEnergy = findViewById(R.id.tvEnergy);
        tvUsername = findViewById(R.id.tvUsername);
        tvLevel = findViewById(R.id.tvLevel);
        ivAvatar = findViewById(R.id.ivAvatar);
    }

    private void loadUserInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String displayName = user.getDisplayName();
            tvUsername.setText(displayName != null && !displayName.isEmpty() ? displayName : "Phi hành gia nhí");

            String photoUrl = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null;
            if (photoUrl != null && !photoUrl.isEmpty()) {
                Glide.with(this)
                    .load(photoUrl)
                    .placeholder(R.drawable.avatar_astronaut)
                    .error(R.drawable.avatar_astronaut)
                    .into(ivAvatar);
            }
        }
    }

    private void loadUserProgress() {
        SharedPreferences prefs = getSharedPreferences("game_progress", MODE_PRIVATE);
        totalStars = prefs.getInt("total_stars", 0);
        totalEnergy = prefs.getInt("total_energy", 100);
        int level = (totalStars / 50) + 1;

        tvStars.setText(String.valueOf(totalStars));
        tvEnergy.setText(String.valueOf(totalEnergy));
        tvLevel.setText("Level " + level);
    }

    private void setupEraButtons() {
        List<GameDataProvider.TimeEra> eras = GameDataProvider.getAllTimeEras();

        for (GameDataProvider.TimeEra era : eras) {
            TextView eraButton = createEraButton(era);
            layoutEras.addView(eraButton);
        }

        // Select first era by default
        selectEra(eras.get(0));
    }

    private TextView createEraButton(GameDataProvider.TimeEra era) {
        TextView button = new TextView(this);
        button.setText(era.getEmoji() + " " + era.getNameVi());
        button.setTextColor(Color.WHITE);
        button.setTextSize(14);
        button.setPadding(32, 16, 32, 16);
        button.setBackgroundResource(R.drawable.bg_era_button);
        button.setTag(era);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(8, 0, 8, 0);
        button.setLayoutParams(params);
        button.setGravity(Gravity.CENTER);

        button.setOnClickListener(v -> selectEra(era));

        return button;
    }

    private void selectEra(GameDataProvider.TimeEra era) {
        currentEra = era.getId();
        tvEraTitle.setText(era.getEmoji() + " " + era.getNameVi());

        // Update button states
        for (int i = 0; i < layoutEras.getChildCount(); i++) {
            View child = layoutEras.getChildAt(i);
            if (child instanceof TextView) {
                GameDataProvider.TimeEra buttonEra = (GameDataProvider.TimeEra) child.getTag();
                if (buttonEra.getId().equals(era.getId())) {
                    child.setBackgroundResource(R.drawable.bg_era_button_selected);
                    ((TextView) child).setTextColor(Color.BLACK);
                } else {
                    child.setBackgroundResource(R.drawable.bg_era_button);
                    ((TextView) child).setTextColor(Color.WHITE);
                }
            }
        }

        // Update planets
        updatePlanetsForEra();
    }

    private void setupPlanets() {
        recyclerPlanets.setLayoutManager(new GridLayoutManager(this, 2));

        List<Planet> planets = GameDataProvider.getPlanetsByEra(currentEra);
        unlockPlanetsBasedOnStars(planets);

        planetAdapter = new PlanetAdapter(this, planets, planet -> {
            // Open planet detail/zone selection
            Intent intent = new Intent(SpaceMapActivity.this, PlanetActivity.class);
            intent.putExtra("planet_id", planet.getId());
            startActivity(intent);
        });

        recyclerPlanets.setAdapter(planetAdapter);
    }

    private void updatePlanetsForEra() {
        List<Planet> planets = GameDataProvider.getPlanetsByEra(currentEra);
        unlockPlanetsBasedOnStars(planets);

        if (planetAdapter != null) {
            planetAdapter.updatePlanets(planets);
        }
    }

    private void unlockPlanetsBasedOnStars(List<Planet> planets) {
        for (Planet planet : planets) {
            if (totalStars >= planet.getRequiredStars()) {
                planet.setUnlocked(true);
            }
        }
    }

    private void setupBottomNav() {
        findViewById(R.id.btnHome).setOnClickListener(v -> {
            // Already on home
        });

        findViewById(R.id.btnBadges).setOnClickListener(v -> {
            Intent intent = new Intent(this, BadgesActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btnVocab).setOnClickListener(v -> {
            Intent intent = new Intent(this, VocabularyActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btnProfile).setOnClickListener(v -> {
            showProfileDialog();
        });
    }

    private void showProfileDialog() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String name = user != null && user.getDisplayName() != null ? user.getDisplayName() : "Phi hành gia";
        String email = user != null && user.getEmail() != null ? user.getEmail() : "";

        new AlertDialog.Builder(this)
            .setTitle("👨‍🚀 " + name)
            .setMessage("Email: " + email + "\n\n⭐ Sao: " + totalStars + "\n⚡ Năng lượng: " + totalEnergy + "\n📊 Level: " + ((totalStars / 50) + 1))
            .setPositiveButton("Đóng", null)
            .setNegativeButton("Đăng xuất", (dialog, which) -> {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(SpaceMapActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            })
            .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProgress();
        updatePlanetsForEra();
    }

    @Override
    public void onBackPressed() {
        // Show exit confirmation
        new AlertDialog.Builder(this)
            .setTitle("Thoát ứng dụng?")
            .setMessage("Bạn có muốn thoát khỏi Space English không?")
            .setPositiveButton("Thoát", (dialog, which) -> {
                finishAffinity();
            })
            .setNegativeButton("Ở lại", null)
            .show();
    }
}

