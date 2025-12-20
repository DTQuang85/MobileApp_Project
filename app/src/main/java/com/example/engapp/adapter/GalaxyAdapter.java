package com.example.engapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    public interface OnGalaxyClickListener {
        void onGalaxyClick(GalaxyData galaxy);
    }

    public GalaxyAdapter(List<GalaxyData> galaxies, OnGalaxyClickListener listener) {
        this.galaxies = galaxies;
        this.listener = listener;
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
    }

    @Override
    public int getItemCount() {
        return galaxies.size();
    }

    class GalaxyViewHolder extends RecyclerView.ViewHolder {
        TextView tvGalaxyEmoji, tvGalaxyName, tvGalaxyNameVi;
        TextView tvLockStatus, tvProgress, tvRequirement, tvLockMessage;
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
            progressGalaxy = itemView.findViewById(R.id.progressGalaxy);
            planetsPreview = itemView.findViewById(R.id.planetsPreview);
            requirementLayout = itemView.findViewById(R.id.requirementLayout);
            lockOverlay = itemView.findViewById(R.id.lockOverlay);
        }

        void bind(GalaxyData galaxy) {
            tvGalaxyEmoji.setText(galaxy.emoji);
            tvGalaxyName.setText(galaxy.name);
            tvGalaxyNameVi.setText(galaxy.nameVi);

            progressGalaxy.setProgress(galaxy.progress);
            tvProgress.setText(galaxy.progress + "%");

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
                tvLockStatus.setText("🔓 Unlocked");
                tvLockStatus.setTextColor(0xFF4CAF50);
                lockOverlay.setVisibility(View.GONE);
                requirementLayout.setVisibility(View.GONE);
            } else {
                tvLockStatus.setText("🔒 Locked");
                tvLockStatus.setTextColor(0xFFFF5722);
                lockOverlay.setVisibility(View.VISIBLE);
                requirementLayout.setVisibility(View.VISIBLE);
                tvRequirement.setText("Requires " + galaxy.starsRequired + " ⭐ to unlock");
                tvLockMessage.setText("Need " + galaxy.starsRequired + " stars");
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onGalaxyClick(galaxy);
                }
            });
        }
    }
}

