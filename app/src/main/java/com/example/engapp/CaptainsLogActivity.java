package com.example.engapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.engapp.manager.ProgressionManager;
import com.example.engapp.manager.TravelManager;
import com.example.engapp.model.Collectible;
import com.example.engapp.model.TravelLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Captain's Log Activity - Shows travel history and achievements
 */
public class CaptainsLogActivity extends AppCompatActivity {

    private RecyclerView recyclerTravelLogs;
    private RecyclerView recyclerAchievements;
    private TextView tvTotalTrips, tvTotalWords, tvTotalStars, tvStreak;
    private ImageView btnBack;

    private TravelManager travelManager;
    private ProgressionManager progressionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_captains_log);

        travelManager = TravelManager.getInstance(this);
        progressionManager = ProgressionManager.getInstance(this);

        initViews();
        loadData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        recyclerTravelLogs = findViewById(R.id.recyclerTravelLogs);
        recyclerAchievements = findViewById(R.id.recyclerAchievements);
        tvTotalTrips = findViewById(R.id.tvTotalTrips);
        tvTotalWords = findViewById(R.id.tvTotalWords);
        tvTotalStars = findViewById(R.id.tvTotalStars);
        tvStreak = findViewById(R.id.tvStreak);

        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        recyclerTravelLogs.setLayoutManager(new LinearLayoutManager(this));
        recyclerAchievements.setLayoutManager(
            new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    }

    private void loadData() {
        // Load stats
        tvTotalTrips.setText(String.valueOf(travelManager.getSpaceshipData().getTotalTrips()));
        tvTotalWords.setText(String.valueOf(progressionManager.getWordsLearned()));
        tvTotalStars.setText(String.valueOf(progressionManager.getTotalStars()));
        tvStreak.setText(progressionManager.getStreakDays() + " ngày");

        // Load travel logs
        List<TravelLog> travelLogs = travelManager.getTravelLogs();
        TravelLogAdapter logAdapter = new TravelLogAdapter(travelLogs);
        recyclerTravelLogs.setAdapter(logAdapter);

        // Load achievements/badges
        List<Collectible> badges = progressionManager.getCollectiblesByCategory(Collectible.CATEGORY_BADGE);
        BadgeAdapter badgeAdapter = new BadgeAdapter(badges);
        recyclerAchievements.setAdapter(badgeAdapter);
    }

    // Travel Log Adapter
    class TravelLogAdapter extends RecyclerView.Adapter<TravelLogAdapter.ViewHolder> {
        private List<TravelLog> logs;

        TravelLogAdapter(List<TravelLog> logs) {
            this.logs = logs != null ? logs : new ArrayList<>();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_travel_log, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            TravelLog log = logs.get(position);
            holder.bind(log);
        }

        @Override
        public int getItemCount() {
            return logs.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvEmoji, tvPlanetName, tvDate, tvStats;

            ViewHolder(View itemView) {
                super(itemView);
                tvEmoji = itemView.findViewById(R.id.tvLogEmoji);
                tvPlanetName = itemView.findViewById(R.id.tvLogPlanetName);
                tvDate = itemView.findViewById(R.id.tvLogDate);
                tvStats = itemView.findViewById(R.id.tvLogStats);
            }

            void bind(TravelLog log) {
                tvEmoji.setText(log.getPlanetEmoji());
                tvPlanetName.setText(log.getToPlanetName());
                tvDate.setText(log.getFormattedDate());

                StringBuilder stats = new StringBuilder();
                if (log.getWordsLearnedDuringVisit() > 0) {
                    stats.append("📚 ").append(log.getWordsLearnedDuringVisit()).append(" từ  ");
                }
                if (log.getStarsEarnedDuringVisit() > 0) {
                    stats.append("⭐ ").append(log.getStarsEarnedDuringVisit());
                }
                tvStats.setText(stats.toString());
            }
        }
    }

    // Badge Adapter
    class BadgeAdapter extends RecyclerView.Adapter<BadgeAdapter.ViewHolder> {
        private List<Collectible> badges;

        BadgeAdapter(List<Collectible> badges) {
            this.badges = badges != null ? badges : new ArrayList<>();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_badge_small, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Collectible badge = badges.get(position);
            holder.bind(badge);
        }

        @Override
        public int getItemCount() {
            return badges.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvEmoji, tvName;

            ViewHolder(View itemView) {
                super(itemView);
                tvEmoji = itemView.findViewById(R.id.tvBadgeEmoji);
                tvName = itemView.findViewById(R.id.tvBadgeName);
            }

            void bind(Collectible badge) {
                tvEmoji.setText(badge.getEmoji());
                tvName.setText(badge.getNameVi());
            }
        }
    }
}

