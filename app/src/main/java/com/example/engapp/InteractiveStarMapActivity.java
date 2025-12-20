package com.example.engapp;

import android.content.Intent;
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
import com.example.engapp.manager.BuddyManager;
import com.example.engapp.manager.ProgressionManager;
import com.example.engapp.manager.TravelManager;
import com.example.engapp.model.Planet;
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
    private ImageView btnClosePlanetInfo;

    // Managers
    private BuddyManager buddyManager;
    private ProgressionManager progressionManager;
    private TravelManager travelManager;

    // Data
    private List<Planet> planets;
    private Planet selectedPlanet;

    // Activity result launcher for travel
    private ActivityResultLauncher<Intent> travelLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interactive_star_map);

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

        findViewById(R.id.btnNavMap).setOnClickListener(v -> {
            // Already on map - do nothing or scroll to current location
            String currentPlanetId = travelManager.getCurrentPlanetId();
            if (currentPlanetId != null) {
                starMapView.focusOnPlanet(currentPlanetId);
            }
        });

        findViewById(R.id.btnNavAdventure).setOnClickListener(v -> {
            // Use old Battle system (ABCD + images) instead of WordBattle
            String currentPlanetId = travelManager.getCurrentPlanetId();
            Intent intent = new Intent(this, BattleActivity.class);
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
        planets = GameDataProvider.getAllPlanets();

        // Update unlock status
        for (Planet planet : planets) {
            planet.setUnlocked(progressionManager.isPlanetUnlocked(planet.getId()));
        }

        starMapView.setPlanets(planets, this);
        starMapView.setCurrentPlanetId(travelManager.getCurrentPlanetId());
    }

    private void setupListeners() {
        btnClosePlanetInfo.setOnClickListener(v -> hidePlanetInfo());

        btnTravel.setOnClickListener(v -> {
            if (selectedPlanet != null) {
                handleTravelRequest();
            }
        });
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
            } else if (travelManager.getFuelCells() >= fuelCost) {
                btnTravel.setText("🚀 Bay đến đây!");
                btnTravel.setEnabled(true);
            } else {
                btnTravel.setText("🔋 Không đủ nhiên liệu");
                btnTravel.setEnabled(false);
            }
        } else {
            // Show lock info
            layoutFuelCost.setVisibility(View.GONE);
            layoutLockInfo.setVisibility(View.VISIBLE);

            int required = progressionManager.getStarsRequiredForPlanet(planet.getId());
            int current = progressionManager.getTotalStars();
            int needed = required - current;

            tvStarsNeeded.setText("⭐ " + needed + " sao nữa");

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
        starMapView.setCurrentPlanetId(planetId);
        starMapView.focusOnPlanet(planetId);

        // Update stats
        updateStatsDisplay();

        // Find the planet and navigate to it
        for (Planet p : planets) {
            if (p.getId().equals(planetId)) {
                // Navigate to planet activity
                Intent intent = new Intent(this, PlanetActivity.class);
                intent.putExtra("planet", p);
                startActivity(intent);
                break;
            }
        }
    }


    // ProgressionManager.ProgressionEventListener
    @Override
    public void onStarsChanged(int totalStars, int addedStars) {
        runOnUiThread(() -> {
            tvStars.setText(String.valueOf(totalStars));
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

            // Update planet in list
            for (Planet p : planets) {
                if (p.getId().equals(planetId)) {
                    p.setUnlocked(true);
                    break;
                }
            }

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
            starMapView.setCurrentPlanetId(destination.getId());
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

