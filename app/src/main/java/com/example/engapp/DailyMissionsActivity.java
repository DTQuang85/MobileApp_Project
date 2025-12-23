package com.example.engapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.engapp.adapter.MissionAdapter;
import com.example.engapp.database.GameDatabaseHelper;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DailyMissionsActivity extends AppCompatActivity implements MissionAdapter.OnMissionClaimListener {

    private RecyclerView rvMissions;
    private ProgressBar progressDaily;
    private TextView tvMissionProgress, tvTodayRewards;
    private CardView cardDailyBonus;
    private TextView tvBonusDescription;
    private Button btnClaimBonus;
    private ImageButton btnBack;

    private GameDatabaseHelper dbHelper;
    private MissionAdapter adapter;
    private List<MissionData> missions;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_missions);

        dbHelper = GameDatabaseHelper.getInstance(this);
        prefs = getSharedPreferences("daily_missions", MODE_PRIVATE);

        initViews();
        checkNewDay();
        loadMissions();
        setupUI();
    }

    private void initViews() {
        rvMissions = findViewById(R.id.rvMissions);
        progressDaily = findViewById(R.id.progressDaily);
        tvMissionProgress = findViewById(R.id.tvMissionProgress);
        tvTodayRewards = findViewById(R.id.tvTodayRewards);
        cardDailyBonus = findViewById(R.id.cardDailyBonus);
        tvBonusDescription = findViewById(R.id.tvBonusDescription);
        btnClaimBonus = findViewById(R.id.btnClaimBonus);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> onBackPressed());

        rvMissions.setLayoutManager(new LinearLayoutManager(this));

        btnClaimBonus.setOnClickListener(v -> claimDailyBonus());
    }

    private void checkNewDay() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        String lastDay = prefs.getString("last_mission_day", "");

        if (!today.equals(lastDay)) {
            // New day - reset missions
            prefs.edit()
                    .putString("last_mission_day", today)
                    .putInt("words_learned_today", 0)
                    .putInt("games_played_today", 0)
                    .putInt("battles_won_today", 0)
                    .putInt("streak_today", 0)
                    .putBoolean("bonus_claimed", false)
                    .apply();
        }
    }

    private void loadMissions() {
        missions = new ArrayList<>();

        int wordsLearned = prefs.getInt("words_learned_today", 0);
        int gamesPlayed = prefs.getInt("games_played_today", 0);
        int battlesWon = prefs.getInt("battles_won_today", 0);
        int streak = prefs.getInt("streak_today", 0);

        missions.add(new MissionData(
                1, "📚", "Learn 10 Words",
                "Study new vocabulary today",
                wordsLearned, 10, 5, wordsLearned >= 10
        ));

        missions.add(new MissionData(
                2, "🎮", "Play 3 Games",
                "Complete any learning games",
                gamesPlayed, 3, 5, gamesPlayed >= 3
        ));

        missions.add(new MissionData(
                3, "⚔️", "Win 1 Battle",
                "Win a vocabulary battle",
                battlesWon, 1, 10, battlesWon >= 1
        ));

        missions.add(new MissionData(
                4, "🔥", "Keep Your Streak",
                "Study every day",
                streak > 0 ? 1 : 0, 1, 5, streak > 0
        ));

        missions.add(new MissionData(
                5, "🌟", "Earn 20 Stars",
                "Collect stars from activities",
                Math.min(getTodayStars(), 20), 20, 5, getTodayStars() >= 20
        ));
    }

    private int getTodayStars() {
        return prefs.getInt("stars_today", 0);
    }

    private void setupUI() {
        adapter = new MissionAdapter(missions, this);
        rvMissions.setAdapter(adapter);

        updateProgress();
    }

    private void updateProgress() {
        int completed = 0;
        for (MissionData mission : missions) {
            if (mission.isCompleted) completed++;
        }

        int progress = (completed * 100) / missions.size();
        progressDaily.setProgress(progress);
        tvMissionProgress.setText(completed + "/" + missions.size());

        if (completed == missions.size()) {
            boolean bonusClaimed = prefs.getBoolean("bonus_claimed", false);
            if (!bonusClaimed) {
                cardDailyBonus.setVisibility(View.VISIBLE);
            }
            tvTodayRewards.setText("🎉 All missions complete!");
        } else {
            tvTodayRewards.setText("Complete all for bonus: +20 ⭐");
            cardDailyBonus.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClaimReward(MissionData mission) {
        if (mission.isCompleted && !mission.isClaimed) {
            dbHelper.addStars(mission.reward);
            mission.isClaimed = true;
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "+" + mission.reward + " ⭐ claimed!", Toast.LENGTH_SHORT).show();
        }
    }

    private void claimDailyBonus() {
        dbHelper.addStars(20);
        prefs.edit().putBoolean("bonus_claimed", true).apply();
        cardDailyBonus.setVisibility(View.GONE);
        Toast.makeText(this, "🎁 +20 ⭐ Daily Bonus claimed!", Toast.LENGTH_SHORT).show();
    }

    // Mission data class
    public static class MissionData {
        public int id;
        public String icon;
        public String title;
        public String description;
        public int currentProgress;
        public int targetProgress;
        public int reward;
        public boolean isCompleted;
        public boolean isClaimed;

        public MissionData(int id, String icon, String title, String description,
                          int currentProgress, int targetProgress, int reward, boolean isCompleted) {
            this.id = id;
            this.icon = icon;
            this.title = title;
            this.description = description;
            this.currentProgress = currentProgress;
            this.targetProgress = targetProgress;
            this.reward = reward;
            this.isCompleted = isCompleted;
            this.isClaimed = false;
        }
    }
}

