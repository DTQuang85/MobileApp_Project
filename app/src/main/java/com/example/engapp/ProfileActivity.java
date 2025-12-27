package com.example.engapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.engapp.database.GameDatabaseHelper;
import com.example.engapp.database.GameDatabaseHelper.*;
import com.example.engapp.manager.ProgressionManager;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "game_prefs";
    private static final String KEY_AVATAR_INDEX = "avatar_index";

    private TextView tvAvatar, tvPlayerName, tvLevel, tvXP;
    private TextView tvTotalStars, tvTotalFuel, tvTotalCrystals;
    private TextView tvWordsLearned, tvGamesCompleted, tvStreak;
    private ProgressBar progressXP;
    private ImageView btnBack;
    private CardView cardAvatar;
    private LinearLayout planetProgressContainer;
    private TextView btnNotes;
    private TextView btnReminders;

    private TextView avatar1, avatar2, avatar3, avatar4, avatar5;
    private TextView[] avatarViews;
    private String[] avatarEmojis = {"👨‍🚀", "👩‍🚀", "🧑‍🚀", "🚀", "🌟"};
    private int currentAvatarIndex = 0;

    private GameDatabaseHelper dbHelper;
    private SharedPreferences prefs;
    private ProgressionManager progressionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        dbHelper = GameDatabaseHelper.getInstance(this);
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        progressionManager = ProgressionManager.getInstance(this);

        initViews();
        loadSavedAvatar();
        loadData();
        setupAvatarSelection();
    }

    private void loadSavedAvatar() {
        currentAvatarIndex = prefs.getInt(KEY_AVATAR_INDEX, 0);
        if (currentAvatarIndex >= 0 && currentAvatarIndex < avatarEmojis.length) {
            tvAvatar.setText(avatarEmojis[currentAvatarIndex]);
        }
    }

    private void initViews() {
        tvAvatar = findViewById(R.id.tvAvatar);
        tvPlayerName = findViewById(R.id.tvPlayerName);
        tvLevel = findViewById(R.id.tvLevel);
        tvXP = findViewById(R.id.tvXP);
        tvTotalStars = findViewById(R.id.tvTotalStars);
        tvTotalFuel = findViewById(R.id.tvTotalFuel);
        tvTotalCrystals = findViewById(R.id.tvTotalCrystals);
        tvWordsLearned = findViewById(R.id.tvWordsLearned);
        tvGamesCompleted = findViewById(R.id.tvGamesCompleted);
        tvStreak = findViewById(R.id.tvStreak);
        progressXP = findViewById(R.id.progressXP);
        btnBack = findViewById(R.id.btnBack);
        cardAvatar = findViewById(R.id.cardAvatar);
        planetProgressContainer = findViewById(R.id.planetProgressContainer);
        btnNotes = findViewById(R.id.btnNotes);
        btnReminders = findViewById(R.id.btnReminders);

        avatar1 = findViewById(R.id.avatar1);
        avatar2 = findViewById(R.id.avatar2);
        avatar3 = findViewById(R.id.avatar3);
        avatar4 = findViewById(R.id.avatar4);
        avatar5 = findViewById(R.id.avatar5);

        avatarViews = new TextView[]{avatar1, avatar2, avatar3, avatar4, avatar5};

        btnBack.setOnClickListener(v -> finish());
        if (btnNotes != null) {
            btnNotes.setOnClickListener(v ->
                startActivity(new android.content.Intent(this, NotesActivity.class)));
        }
        if (btnReminders != null) {
            btnReminders.setOnClickListener(v ->
                startActivity(new android.content.Intent(this, RemindersActivity.class)));
        }
    }

    private void loadData() {
        UserProgressData progress = dbHelper.getUserProgress();

        if (progress != null) {
            int level = progress.currentLevel;
            int xp = progress.experiencePoints;
            int xpInLevel = xp % 100;

            tvLevel.setText(String.valueOf(level));
            tvXP.setText(xpInLevel + "/100 XP");
            progressXP.setProgress(xpInLevel);

            tvTotalStars.setText(String.valueOf(progress.totalStars));
            tvTotalFuel.setText(String.valueOf(progress.totalFuelCells));
            tvTotalCrystals.setText(String.valueOf(progress.totalCrystals));
            tvWordsLearned.setText(String.valueOf(progressionManager.getWordsLearned()));
            tvGamesCompleted.setText(String.valueOf(progressionManager.getGamesCompleted()));
            tvStreak.setText(String.valueOf(progress.streakDays));

            currentAvatarIndex = progress.avatarId - 1;
            if (currentAvatarIndex >= 0 && currentAvatarIndex < avatarEmojis.length) {
                tvAvatar.setText(avatarEmojis[currentAvatarIndex]);
                updateAvatarSelection(currentAvatarIndex);
            }
        } else {
            tvLevel.setText("1");
            tvXP.setText("0/100 XP");
            progressXP.setProgress(0);
            tvTotalStars.setText("0");
            tvTotalFuel.setText("0");
            tvTotalCrystals.setText("0");
            tvWordsLearned.setText(String.valueOf(progressionManager.getWordsLearned()));
            tvGamesCompleted.setText(String.valueOf(progressionManager.getGamesCompleted()));
            tvStreak.setText("0");
        }
        loadPlanetProgress();
    }

    private void setupAvatarSelection() {
        for (int i = 0; i < avatarViews.length; i++) {
            final int index = i;
            avatarViews[i].setOnClickListener(v -> selectAvatar(index));
        }
    }

    private void loadPlanetProgress() {
        if (planetProgressContainer == null) {
            return;
        }
        planetProgressContainer.removeAllViews();
        List<PlanetData> planets = dbHelper.getAllPlanets();
        if (planets == null) {
            return;
        }
        for (PlanetData planet : planets) {
            List<SceneData> scenes = dbHelper.getScenesForPlanet(planet.id);
            int completed = 0;
            for (SceneData scene : scenes) {
                if (scene.isCompleted) completed++;
            }
            int percent = scenes.size() > 0 ? (completed * 100 / scenes.size()) : 0;
            TextView row = new TextView(this);
            String name = planet.nameVi != null && !planet.nameVi.isEmpty()
                ? planet.nameVi
                : planet.name;
            row.setText(name + ": " + percent + "%");
            row.setTextColor(getColor(R.color.text_white));
            row.setTextSize(14f);
            row.setPadding(0, 6, 0, 6);
            planetProgressContainer.addView(row);
        }
    }

    private void selectAvatar(int index) {
        currentAvatarIndex = index;
        tvAvatar.setText(avatarEmojis[index]);
        updateAvatarSelection(index);

        prefs.edit().putInt(KEY_AVATAR_INDEX, currentAvatarIndex).apply();

        Toast.makeText(this, "Đã chọn avatar mới! ✨", Toast.LENGTH_SHORT).show();
    }

    private void updateAvatarSelection(int selectedIndex) {
        for (int i = 0; i < avatarViews.length; i++) {
            if (i == selectedIndex) {
                avatarViews[i].setBackgroundResource(R.drawable.bg_avatar_selected);
            } else {
                avatarViews[i].setBackgroundResource(R.drawable.bg_avatar_normal);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }
}
