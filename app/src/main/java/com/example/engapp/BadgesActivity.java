package com.example.engapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class BadgesActivity extends AppCompatActivity {

    private RecyclerView recyclerBadges;
    private TextView tvTotalBadges, tvTotalStars;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_badges);

        initViews();
        loadStats();
        setupBadges();
    }

    private void initViews() {
        recyclerBadges = findViewById(R.id.recyclerBadges);
        tvTotalBadges = findViewById(R.id.tvTotalBadges);
        tvTotalStars = findViewById(R.id.tvTotalStars);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
    }

    private void loadStats() {
        SharedPreferences prefs = getSharedPreferences("game_progress", MODE_PRIVATE);
        int totalStars = prefs.getInt("total_stars", 0);

        tvTotalStars.setText("⭐ " + totalStars);

        int earnedBadges = 0;
        if (totalStars >= 10) earnedBadges++;
        if (totalStars >= 50) earnedBadges++;
        if (totalStars >= 100) earnedBadges++;
        if (totalStars >= 200) earnedBadges++;
        if (totalStars >= 500) earnedBadges++;

        tvTotalBadges.setText("🏆 " + earnedBadges + "/10");
    }

    private void setupBadges() {
        SharedPreferences prefs = getSharedPreferences("game_progress", MODE_PRIVATE);
        int totalStars = prefs.getInt("total_stars", 0);

        List<Badge> badges = new ArrayList<>();
        badges.add(new Badge("🌟", "Người mới", "Thu thập 10 sao đầu tiên", 10, totalStars >= 10));
        badges.add(new Badge("🚀", "Phi hành gia tập sự", "Thu thập 50 sao", 50, totalStars >= 50));
        badges.add(new Badge("🌍", "Nhà thám hiểm", "Thu thập 100 sao", 100, totalStars >= 100));
        badges.add(new Badge("🏆", "Chiến binh", "Thu thập 200 sao", 200, totalStars >= 200));
        badges.add(new Badge("👑", "Vua vũ trụ", "Thu thập 500 sao", 500, totalStars >= 500));
        badges.add(new Badge("📚", "Học giả", "Học 100 từ vựng", 100, false));
        badges.add(new Badge("🎮", "Game thủ", "Hoàn thành 20 mini-game", 20, false));
        badges.add(new Badge("🎯", "Bách phát bách trúng", "Đạt 100% trong 1 game", 100, false));
        badges.add(new Badge("🔥", "Siêu sao", "Chơi 7 ngày liên tiếp", 7, false));
        badges.add(new Badge("🌈", "Đa tài", "Hoàn thành 3 hành tinh", 3, false));

        BadgeAdapter adapter = new BadgeAdapter(badges);
        recyclerBadges.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerBadges.setAdapter(adapter);
    }

    static class Badge {
        String emoji;
        String name;
        String description;
        int requirement;
        boolean isEarned;

        Badge(String emoji, String name, String description, int requirement, boolean isEarned) {
            this.emoji = emoji;
            this.name = name;
            this.description = description;
            this.requirement = requirement;
            this.isEarned = isEarned;
        }
    }

    class BadgeAdapter extends RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder> {
        private List<Badge> badges;

        BadgeAdapter(List<Badge> badges) {
            this.badges = badges;
        }

        @Override
        public BadgeViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            android.view.View view = getLayoutInflater().inflate(R.layout.item_badge, parent, false);
            return new BadgeViewHolder(view);
        }

        @Override
        public void onBindViewHolder(BadgeViewHolder holder, int position) {
            Badge badge = badges.get(position);
            holder.tvEmoji.setText(badge.emoji);
            holder.tvName.setText(badge.name);
            holder.tvDescription.setText(badge.description);

            if (badge.isEarned) {
                holder.itemView.setAlpha(1f);
                holder.tvStatus.setText("✅ Đã nhận");
                holder.tvStatus.setTextColor(getColor(R.color.correct_green));
            } else {
                holder.itemView.setAlpha(0.5f);
                holder.tvStatus.setText("🔒 Chưa đạt");
                holder.tvStatus.setTextColor(getColor(R.color.text_secondary));
            }
        }

        @Override
        public int getItemCount() {
            return badges.size();
        }

        class BadgeViewHolder extends RecyclerView.ViewHolder {
            TextView tvEmoji, tvName, tvDescription, tvStatus;

            BadgeViewHolder(android.view.View itemView) {
                super(itemView);
                tvEmoji = itemView.findViewById(R.id.tvBadgeEmoji);
                tvName = itemView.findViewById(R.id.tvBadgeName);
                tvDescription = itemView.findViewById(R.id.tvBadgeDescription);
                tvStatus = itemView.findViewById(R.id.tvBadgeStatus);
            }
        }
    }
}
