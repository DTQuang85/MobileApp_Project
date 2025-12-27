package com.example.engapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.example.engapp.data.GameDataProvider;
import com.example.engapp.database.GameDatabaseHelper;
import com.example.engapp.manager.BuddyManager;
import com.example.engapp.manager.ProgressionManager;
import com.example.engapp.manager.TravelManager;
import com.example.engapp.model.Planet;
import com.example.engapp.model.Zone;
import com.example.engapp.view.BuddyOverlayView;
import com.example.engapp.view.InteractiveStarMapView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Interactive Star Map Activity with pan/zoom and visual planet selection.
 */
public class InteractiveStarMapActivity extends AppCompatActivity
        implements InteractiveStarMapView.OnPlanetSelectedListener,
                   ProgressionManager.ProgressionEventListener,
                   TravelManager.TravelEventListener {

    private InteractiveStarMapView starMapView;
    private BuddyOverlayView buddyOverlay;

    // Top bar
    private CircleImageView ivAvatar;
    private TextView tvUsername, tvLevel, tvStars, tvFuel;

    // Planet info card
    private CardView cardPlanetInfo;
    private TextView tvPlanetEmoji, tvPlanetName, tvPlanetNameVi;
    private TextView tvPlanetProgress, tvFuelCost, tvStarsNeeded;
    private ProgressBar progressPlanet;
    private LinearLayout layoutFuelCost, layoutLockInfo;
    private Button btnTravel;
    private Button btnEnterPlanet;
    private ImageView btnClosePlanetInfo;

    // Managers
    private BuddyManager buddyManager;
    private ProgressionManager progressionManager;
    private TravelManager travelManager;

    // Data
    private List<Planet> planets;
    private Planet selectedPlanet;
    private int galaxyId = 1; // Current galaxy ID
    private String galaxyName = "Beginner Galaxy";
    private String galaxyEmoji = "🌌";

    // Activity result launcher for travel
    private ActivityResultLauncher<Intent> travelLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interactive_star_map);

        // Get galaxy info from intent
        galaxyId = getIntent().getIntExtra("galaxy_id", 1);
        galaxyName = getIntent().getStringExtra("galaxy_name");
        galaxyEmoji = getIntent().getStringExtra("galaxy_emoji");

        if (galaxyName == null) galaxyName = "Beginner Galaxy";
        if (galaxyEmoji == null) galaxyEmoji = "🌌";

        initManagers();
        initViews();
        loadUserInfo();
        loadPlanets();
        setupListeners();

        // Register travel activity launcher
        travelLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String arrivedPlanetId = result.getData().getStringExtra("arrived_planet_id");
                    if (arrivedPlanetId != null) {
                        onArrivedAtPlanet(arrivedPlanetId);
                    }
                }
            }
        );

        // Welcome message from buddy
        buddyManager.onAppOpen();

        // Record daily login for streak
        progressionManager.recordDailyLogin();
    }

    private void initManagers() {
        buddyManager = BuddyManager.getInstance(this);
        progressionManager = ProgressionManager.getInstance(this);
        travelManager = TravelManager.getInstance(this);

        progressionManager.addListener(this);
        travelManager.addListener(this);
    }

    private void initViews() {
        starMapView = findViewById(R.id.interactiveStarMap);
        buddyOverlay = findViewById(R.id.buddyOverlay);

        // Top bar
        ivAvatar = findViewById(R.id.ivAvatar);
        tvUsername = findViewById(R.id.tvUsername);
        tvLevel = findViewById(R.id.tvLevel);
        tvStars = findViewById(R.id.tvStars);
        tvFuel = findViewById(R.id.tvFuel);

        // Planet info card
        cardPlanetInfo = findViewById(R.id.cardPlanetInfo);
        tvPlanetEmoji = findViewById(R.id.tvPlanetEmoji);
        tvPlanetName = findViewById(R.id.tvPlanetName);
        tvPlanetNameVi = findViewById(R.id.tvPlanetNameVi);
        tvPlanetProgress = findViewById(R.id.tvPlanetProgress);
        tvFuelCost = findViewById(R.id.tvFuelCost);
        tvStarsNeeded = findViewById(R.id.tvStarsNeeded);
        progressPlanet = findViewById(R.id.progressPlanet);
        layoutFuelCost = findViewById(R.id.layoutFuelCost);
        layoutLockInfo = findViewById(R.id.layoutLockInfo);
        btnTravel = findViewById(R.id.btnTravel);
        btnEnterPlanet = findViewById(R.id.btnEnterPlanet);
        btnClosePlanetInfo = findViewById(R.id.btnClosePlanetInfo);

        // Set listener for star map
        starMapView.setOnPlanetSelectedListener(this);

        // Setup bottom navigation
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

        // FAB Galaxy Map - Navigate back to Galaxy selection
        findViewById(R.id.fabGalaxyMap).setOnClickListener(v -> {
            startActivity(new Intent(this, InteractiveGalaxyMapActivity.class));
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        findViewById(R.id.btnNavAdventure).setOnClickListener(v -> {
            // Use old Battle system (ABCD + images) instead of WordBattle
            String currentPlanetId = travelManager.getCurrentPlanetId();
            Intent intent = new Intent(this, WordBattleActivity.class);
            if (currentPlanetId != null) {
                // Try to convert planet ID to int (assuming format like "planet_1" -> 1)
                try {
                    int planetId = Integer.parseInt(currentPlanetId.replace("planet_", ""));
                    intent.putExtra("planet_id", planetId);
                } catch (NumberFormatException e) {
                    intent.putExtra("planet_id", 1);
                }
            } else {
                intent.putExtra("planet_id", 1);
            }
            startActivity(intent);
            overridePendingTransition(R.anim.fade_scale_in, 0);
        });

        findViewById(R.id.btnNavBuddy).setOnClickListener(v -> {
            startActivity(new Intent(this, BuddyRoomActivity.class));
        });
    }

    private void loadUserInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String displayName = user.getDisplayName();
            tvUsername.setText(displayName != null && !displayName.isEmpty() ?
                displayName : "Phi hành gia nhí");

            String photoUrl = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null;
            if (photoUrl != null && !photoUrl.isEmpty()) {
                Glide.with(this)
                    .load(photoUrl)
                    .placeholder(R.drawable.avatar_astronaut)
                    .error(R.drawable.avatar_astronaut)
                    .into(ivAvatar);
            }
        }

        updateStatsDisplay();
    }

    private void updateStatsDisplay() {
        int totalStars = progressionManager.getTotalStars();
        int level = progressionManager.getCurrentLevel();
        int fuel = travelManager.getFuelCells();

        tvStars.setText(String.valueOf(totalStars));
        tvLevel.setText("Level " + level);
        tvFuel.setText(String.valueOf(fuel));
    }
    private void loadPlanets() {
        // Ki???m tra vA? m??Y khA3a cA?c hA?nh tinh ?`??i???u ki???n tr????>c
        progressionManager.checkForNewUnlocks();

        GameDatabaseHelper dbHelper = GameDatabaseHelper.getInstance(this);
        List<GameDatabaseHelper.PlanetData> planetDataList = dbHelper.getAllPlanets();
        planets = new java.util.ArrayList<>();

        if (planetDataList == null || planetDataList.isEmpty()) {
            List<Planet> allPlanets = GameDataProvider.getAllPlanets();
            for (Planet planet : allPlanets) {
                planet.setUnlocked(progressionManager.isPlanetUnlocked(planet.getId()));
                planets.add(planet);
            }
        } else {
            for (GameDatabaseHelper.PlanetData planetData : planetDataList) {
                int color = 0xFF4ADE80;
                if (planetData.themeColor != null && !planetData.themeColor.isEmpty()) {
                    try {
                        color = Color.parseColor(planetData.themeColor);
                    } catch (IllegalArgumentException ignored) {
                    }
                }

                Planet planet = new Planet(
                    planetData.planetKey,
                    planetData.name,
                    planetData.nameVi,
                    planetData.emoji,
                    color,
                    ""
                );
                planet.setRequiredStars(progressionManager.getStarsRequiredForPlanet(planetData.planetKey));
                planet.setUnlocked(progressionManager.isPlanetUnlocked(planetData.planetKey));

                List<GameDatabaseHelper.SceneData> scenes = dbHelper.getScenesForPlanet(planetData.id);
                List<Zone> zones = new java.util.ArrayList<>();
                if (scenes != null) {
                    for (GameDatabaseHelper.SceneData scene : scenes) {
                        String sceneKey = scene.sceneKey != null ? scene.sceneKey : "scene_" + scene.id;
                        String sceneName = scene.name != null ? scene.name : "Scene";
                        String sceneNameVi = scene.nameVi != null ? scene.nameVi : "";
                        String sceneEmoji = scene.emoji != null ? scene.emoji : "";
                        Zone zone = new Zone(sceneKey, sceneName, sceneNameVi, sceneEmoji);
                        zone.setCompleted(scene.isCompleted);
                        zone.setStarsEarned(scene.starsEarned);
                        zones.add(zone);
                    }
                }
                planet.setZones(zones);

                planets.add(planet);
            }
        }

        starMapView.setPlanets(planets, this);
        String currentPlanetId = resolveCurrentPlanetKey(travelManager.getCurrentPlanetId());
        if (currentPlanetId != null && !currentPlanetId.isEmpty()) {
            travelManager.setCurrentPlanetId(currentPlanetId);
        }
        starMapView.setCurrentPlanetId(currentPlanetId);
        if (currentPlanetId != null && !currentPlanetId.isEmpty()) {
            starMapView.post(() -> starMapView.focusOnPlanet(currentPlanetId));
        }
    }

    private String resolveCurrentPlanetKey(String planetId) {
        if (planetId == null || planetId.isEmpty()) {
            return planetId;
        }
        String normalized = progressionManager.normalizePlanetKey(planetId);
        if (normalized != null && !normalized.equals(planetId)) {
            return normalized;
        }
        String numericId = planetId;
        if (numericId.startsWith("planet_")) {
            numericId = numericId.substring("planet_".length());
        }
        try {
            int id = Integer.parseInt(numericId);
            GameDatabaseHelper.PlanetData planetData =
                GameDatabaseHelper.getInstance(this).getPlanetById(id);
            if (planetData != null && planetData.planetKey != null) {
                return planetData.planetKey;
            }
        } catch (NumberFormatException ignored) {
        }
        return planetId;
    }

    private void setupListeners() {
        btnClosePlanetInfo.setOnClickListener(v -> hidePlanetInfo());

        btnTravel.setOnClickListener(v -> {
            if (selectedPlanet != null) {
                handleTravelRequest();
            }
        });
        btnEnterPlanet.setOnClickListener(v -> openSelectedPlanetMiniGame());
    }

    // InteractiveStarMapView.OnPlanetSelectedListener
    @Override
    public void onPlanetSelected(Planet planet, InteractiveStarMapView.PlanetNode node) {
        selectedPlanet = planet;
        showPlanetInfo(planet);
    }

    @Override
    public void onPlanetLongPressed(Planet planet, InteractiveStarMapView.PlanetNode node) {
        // Show detailed planet info or quick travel option
        if (planet.isUnlocked() && travelManager.canTravelTo(planet)) {
            handleTravelRequest();
        } else {
            Toast.makeText(this, "🔒 Hành tinh này chưa được mở khóa", Toast.LENGTH_SHORT).show();
        }
    }

    private void showPlanetInfo(Planet planet) {
        tvPlanetEmoji.setText(planet.getEmoji());
        tvPlanetName.setText(planet.getName());
        tvPlanetNameVi.setText(planet.getNameVi());

        int progress = planet.getProgress();
        tvPlanetProgress.setText(progress + "%");
        progressPlanet.setProgress(progress);

        if (planet.isUnlocked()) {
            // Show travel info
            layoutLockInfo.setVisibility(View.GONE);
            layoutFuelCost.setVisibility(View.VISIBLE);

            int fuelCost = travelManager.calculateFuelCost(null, planet);
            tvFuelCost.setText("🔋 " + fuelCost);

            boolean isCurrentPlanet = planet.getId().equals(travelManager.getCurrentPlanetId());
            if (isCurrentPlanet) {
                btnTravel.setText("📍 Bạn đang ở đây");
                btnTravel.setEnabled(false);
                btnEnterPlanet.setVisibility(View.VISIBLE);
                btnEnterPlanet.setEnabled(true);
            } else if (travelManager.getFuelCells() >= fuelCost) {
                btnTravel.setText("🚀 Bay đến đây!");
                btnTravel.setEnabled(true);
                btnEnterPlanet.setVisibility(View.GONE);
                btnEnterPlanet.setEnabled(false);
            } else {
                btnTravel.setText("🔋 Không đủ nhiên liệu");
                btnTravel.setEnabled(false);
                btnEnterPlanet.setVisibility(View.GONE);
                btnEnterPlanet.setEnabled(false);
            }
        } else {
            // Show lock info
            layoutFuelCost.setVisibility(View.GONE);
            layoutLockInfo.setVisibility(View.VISIBLE);
            btnEnterPlanet.setVisibility(View.GONE);
            btnEnterPlanet.setEnabled(false);

            int required = progressionManager.getStarsRequiredForPlanet(planet.getId());
            int current = progressionManager.getTotalStars();
            int needed = Math.max(0, required - current); // Không hiển thị số âm

            if (required == 0) {
                // Planet luôn mở khóa (như animal planet)
                tvStarsNeeded.setText("⭐ Sẵn sàng mở khóa!");
            } else if (needed == 0) {
                tvStarsNeeded.setText("⭐ Đã đủ sao! Sẵn sàng mở khóa!");
            } else {
                tvStarsNeeded.setText("⭐ Cần thêm " + needed + " sao nữa");
            }

            btnTravel.setText("🔒 Chưa mở khóa");
            btnTravel.setEnabled(false);
        }

        // Animate card in
        cardPlanetInfo.setVisibility(View.VISIBLE);
        cardPlanetInfo.setAlpha(0f);
        cardPlanetInfo.setTranslationY(200f);
        cardPlanetInfo.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(300)
            .start();
    }

    private void hidePlanetInfo() {
        cardPlanetInfo.animate()
            .alpha(0f)
            .translationY(200f)
            .setDuration(200)
            .withEndAction(() -> {
                cardPlanetInfo.setVisibility(View.GONE);
                selectedPlanet = null;
            })
            .start();
    }

    private void openSelectedPlanetMiniGame() {
        if (selectedPlanet == null) return;
        GameDatabaseHelper dbHelper = GameDatabaseHelper.getInstance(this);
        GameDatabaseHelper.PlanetData planetData = dbHelper.getPlanetByKey(selectedPlanet.getId());
        if (planetData == null) {
            Toast.makeText(this, "Khong tim thay hanh tinh", Toast.LENGTH_SHORT).show();
            return;
        }
        int sceneId = getMiniGameSceneId(dbHelper, planetData.id);
        if (sceneId <= 0) {
            Toast.makeText(this, "Chua co mini-game", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, SignalDecodeActivity.class);
        intent.putExtra("planet_id", planetData.id);
        intent.putExtra("scene_id", sceneId);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_scale_in, 0);
        hidePlanetInfo();
    }

    private int getMiniGameSceneId(GameDatabaseHelper dbHelper, int planetId) {
        List<GameDatabaseHelper.SceneData> scenes = dbHelper.getScenesForPlanet(planetId);
        if (scenes == null) {
            return -1;
        }
        for (GameDatabaseHelper.SceneData scene : scenes) {
            if ("mini_game".equals(scene.sceneType) || "mini_game".equals(scene.sceneKey)) {
                return scene.id;
            }
        }
        return -1;
    }

    private void handleTravelRequest() {
        if (selectedPlanet == null) return;

        if (!selectedPlanet.isUnlocked()) {
            Toast.makeText(this, "🔒 Hành tinh này chưa được mở khóa!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!travelManager.canTravelTo(selectedPlanet)) {
            Toast.makeText(this, "🔋 Không đủ nhiên liệu!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Start travel animation
        Intent intent = new Intent(this, SpaceTravelActivity.class);
        intent.putExtra(SpaceTravelActivity.EXTRA_DESTINATION_PLANET, selectedPlanet);
        intent.putExtra(SpaceTravelActivity.EXTRA_FROM_PLANET_NAME,
            getCurrentPlanetName());
        travelLauncher.launch(intent);

        hidePlanetInfo();
    }

    private String getCurrentPlanetName() {
        String currentId = travelManager.getCurrentPlanetId();
        for (Planet p : planets) {
            if (p.getId().equals(currentId)) {
                return p.getName();
            }
        }
        return "Space";
    }

    private void onArrivedAtPlanet(String planetId) {
        // Update current location on map
        String resolvedPlanetId = resolveCurrentPlanetKey(planetId);
        travelManager.setCurrentPlanetId(resolvedPlanetId);
        starMapView.setCurrentPlanetId(resolvedPlanetId);
        if (resolvedPlanetId != null && !resolvedPlanetId.isEmpty()) {
            starMapView.focusOnPlanet(resolvedPlanetId);
        }

        // Update stats
        updateStatsDisplay();

        // Get planet_id from database using planet key
        GameDatabaseHelper dbHelper = GameDatabaseHelper.getInstance(this);
        GameDatabaseHelper.PlanetData planetData = dbHelper.getPlanetByKey(resolvedPlanetId);
        
        if (planetData != null) {
            // Navigate to PlanetMapActivity with correct planet_id
            // Sử dụng FLAG_ACTIVITY_CLEAR_TOP và FLAG_ACTIVITY_NEW_TASK để đảm bảo activity mới
            Intent intent = new Intent(this, PlanetMapActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("planet_id", planetData.id);
            intent.putExtra("planet_name", planetData.name);
            intent.putExtra("planet_name_vi", planetData.nameVi);
            intent.putExtra("planet_emoji", planetData.emoji);
            intent.putExtra("planet_color", normalizeColor(planetData.themeColor, "#4ADE80"));
            startActivity(intent);
        } else {
            Toast.makeText(this, "Không tìm thấy hành tinh: " + planetId, Toast.LENGTH_SHORT).show();
        }
    }


    private String normalizeColor(String color, String fallback) {
        if (color == null) {
            return fallback;
        }
        String trimmed = color.trim();
        if (trimmed.isEmpty()) {
            return fallback;
        }
        if (!trimmed.startsWith("#")) {
            trimmed = "#" + trimmed;
        }
        return trimmed;
    }

    // ProgressionManager.ProgressionEventListener
    @Override
    public void onStarsChanged(int totalStars, int addedStars) {
        runOnUiThread(() -> {
            tvStars.setText(String.valueOf(totalStars));
            // Kiểm tra và mở khóa các hành tinh mới khi có sao mới
            progressionManager.checkForNewUnlocks();
            // Reload planets để cập nhật unlock status
            loadPlanets();
            starMapView.refreshUnlockStatus(this);
        });
    }

    @Override
    public void onLevelUp(int newLevel) {
        runOnUiThread(() -> {
            tvLevel.setText("Level " + newLevel);
            Toast.makeText(this, "🎉 Level Up! Level " + newLevel, Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void onPlanetUnlocked(String planetId, String planetName) {
        runOnUiThread(() -> {
            Toast.makeText(this, "🌟 Mở khóa hành tinh mới: " + planetName + "!",
                Toast.LENGTH_LONG).show();

            // Update planet in list - kiểm tra null trước
            if (planets != null) {
                for (Planet p : planets) {
                    if (p.getId().equals(planetId)) {
                        p.setUnlocked(true);
                        break;
                    }
                }
            }

            // Reload planets để đảm bảo sync
            loadPlanets();
            
            starMapView.refreshUnlockStatus(this);
            starMapView.focusOnPlanet(planetId);

            // Buddy celebrates
            buddyManager.onPlanetUnlock(planetName);
        });
    }

    @Override
    public void onBadgeEarned(com.example.engapp.model.Collectible badge) {
        runOnUiThread(() -> {
            Toast.makeText(this, "🏅 Huy hiệu mới: " + badge.getNameVi(),
                Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onCollectibleAdded(com.example.engapp.model.Collectible collectible) {
        // Could show animation
    }

    @Override
    public void onMilestoneReached(String milestoneType, int value) {
        runOnUiThread(() -> {
            String message = "";
            if ("words".equals(milestoneType)) {
                message = "📚 Đã học " + value + " từ!";
            } else if ("games".equals(milestoneType)) {
                message = "🎮 Đã hoàn thành " + value + " trò chơi!";
            }
            if (!message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // TravelManager.TravelEventListener
    @Override
    public void onTravelPhaseChanged(int phase, String phaseName) {
        // Handled by SpaceTravelActivity
    }

    @Override
    public void onTravelProgress(float progress) {
        // Handled by SpaceTravelActivity
    }

    @Override
    public void onTravelComplete(Planet destination) {
        runOnUiThread(() -> {
            starMapView.setCurrentPlanetId(resolveCurrentPlanetKey(destination.getId()));
            updateStatsDisplay();
        });
    }

    @Override
    public void onTravelCancelled() {
        // Nothing to do
    }

    @Override
    public void onFuelChanged(int currentFuel, int maxFuel) {
        runOnUiThread(() -> tvFuel.setText(String.valueOf(currentFuel)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Kiểm tra và mở khóa các hành tinh đủ điều kiện
        progressionManager.checkForNewUnlocks();
        // Reload planets để cập nhật unlock status
        loadPlanets();
        updateStatsDisplay();
        starMapView.refreshUnlockStatus(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        progressionManager.removeListener(this);
        travelManager.removeListener(this);
        if (buddyOverlay != null) {
            buddyOverlay.cleanup();
        }
    }
}


