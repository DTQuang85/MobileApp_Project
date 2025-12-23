package com.example.engapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.engapp.GalaxyMapActivity.GalaxyData;
import com.example.engapp.R;
import java.util.List;

public class GalaxyAdapter extends RecyclerView.Adapter<GalaxyAdapter.GalaxyViewHolder> {

    private List<GalaxyData> galaxies;
    private OnGalaxyClickListener listener;
    private int userStars;

    public interface OnGalaxyClickListener {
        void onGalaxyClick(GalaxyData galaxy);
    }

    public GalaxyAdapter(List<GalaxyData> galaxies, OnGalaxyClickListener listener, int userStars) {
        this.galaxies = galaxies;
        this.listener = listener;
        this.userStars = userStars;
    }

    @NonNull
    @Override
    public GalaxyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_galaxy, parent, false);
        return new GalaxyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GalaxyViewHolder holder, int position) {
        GalaxyData galaxy = galaxies.get(position);
        holder.bind(galaxy);
        
        // Add fade-in animation
        Animation animation = AnimationUtils.loadAnimation(holder.itemView.getContext(), 
            android.R.anim.fade_in);
        animation.setDuration(300);
        animation.setStartOffset(position * 100); // Stagger animation
        holder.itemView.startAnimation(animation);
    }

    @Override
    public int getItemCount() {
        return galaxies.size();
    }

    class GalaxyViewHolder extends RecyclerView.ViewHolder {
        TextView tvGalaxyEmoji, tvGalaxyName, tvGalaxyNameVi;
        TextView tvLockStatus, tvProgress, tvRequirement, tvLockMessage, tvRemainingStars;
        ProgressBar progressGalaxy;
        LinearLayout planetsPreview, requirementLayout;
        FrameLayout lockOverlay;

        GalaxyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGalaxyEmoji = itemView.findViewById(R.id.tvGalaxyEmoji);
            tvGalaxyName = itemView.findViewById(R.id.tvGalaxyName);
            tvGalaxyNameVi = itemView.findViewById(R.id.tvGalaxyNameVi);
            tvLockStatus = itemView.findViewById(R.id.tvLockStatus);
            tvProgress = itemView.findViewById(R.id.tvProgress);
            tvRequirement = itemView.findViewById(R.id.tvRequirement);
            tvLockMessage = itemView.findViewById(R.id.tvLockMessage);
            tvRemainingStars = itemView.findViewById(R.id.tvRemainingStars);
            progressGalaxy = itemView.findViewById(R.id.progressGalaxy);
            planetsPreview = itemView.findViewById(R.id.planetsPreview);
            requirementLayout = itemView.findViewById(R.id.requirementLayout);
            lockOverlay = itemView.findViewById(R.id.lockOverlay);
        }

        void bind(GalaxyData galaxy) {
            tvGalaxyEmoji.setText(galaxy.emoji);
            tvGalaxyName.setText(galaxy.name);
            tvGalaxyNameVi.setText(galaxy.nameVi);

            // Planet preview
            planetsPreview.removeAllViews();
            if (galaxy.planetEmojis != null) {
                for (String emoji : galaxy.planetEmojis) {
                    TextView planetView = new TextView(itemView.getContext());
                    planetView.setText(emoji);
                    planetView.setTextSize(24);
                    planetView.setPadding(8, 0, 8, 0);
                    planetsPreview.addView(planetView);
                }
            }

            if (galaxy.isUnlocked) {
                tvLockStatus.setText("🔓 Đã mở khóa");
                tvLockStatus.setTextColor(0xFF4CAF50);
                lockOverlay.setVisibility(View.GONE);
                requirementLayout.setVisibility(View.GONE);
                
                // Show progress for unlocked galaxies
                progressGalaxy.setProgress(galaxy.progress);
                tvProgress.setText(galaxy.progress + "%");
            } else {
                tvLockStatus.setText("🔒 Đã khóa");
                tvLockStatus.setTextColor(0xFFFF5722);
                lockOverlay.setVisibility(View.VISIBLE);
                requirementLayout.setVisibility(View.VISIBLE);
                
                // Calculate unlock progress
                int progressPercent = 0;
                int remaining = galaxy.starsRequired - userStars;
                if (galaxy.starsRequired > 0) {
                    progressPercent = (int) ((float) userStars / galaxy.starsRequired * 100);
                    progressPercent = Math.min(100, Math.max(0, progressPercent));
                }
                
                progressGalaxy.setProgress(progressPercent);
                tvProgress.setText(progressPercent + "%");
                
                if (remaining > 0) {
                    tvRequirement.setText("Cần " + galaxy.starsRequired + " ⭐ để mở khóa");
                    tvRemainingStars.setText("Còn thiếu " + remaining + " ⭐");
                    tvRemainingStars.setVisibility(View.VISIBLE);
                    tvLockMessage.setText("Cần thêm " + remaining + " ⭐ nữa");
                } else {
                    tvRequirement.setText("Sẵn sàng mở khóa! 🎉");
                    tvRemainingStars.setVisibility(View.GONE);
                    tvLockMessage.setText("Chạm để mở khóa");
                }
            }

            // Add click animation
            itemView.setOnClickListener(v -> {
                // Scale animation
                v.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        v.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(100)
                            .start();
                        if (listener != null) {
                            listener.onGalaxyClick(galaxy);
                        }
                    })
                    .start();
            });
        }
    }
}

