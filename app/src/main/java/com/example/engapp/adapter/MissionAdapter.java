package com.example.engapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.engapp.DailyMissionsActivity.MissionData;
import com.example.engapp.R;
import java.util.List;

public class MissionAdapter extends RecyclerView.Adapter<MissionAdapter.MissionViewHolder> {

    private List<MissionData> missions;
    private OnMissionClaimListener listener;

    public interface OnMissionClaimListener {
        void onClaimReward(MissionData mission);
    }

    public MissionAdapter(List<MissionData> missions, OnMissionClaimListener listener) {
        this.missions = missions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MissionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mission, parent, false);
        return new MissionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MissionViewHolder holder, int position) {
        MissionData mission = missions.get(position);
        holder.bind(mission);
    }

    @Override
    public int getItemCount() {
        return missions.size();
    }

    class MissionViewHolder extends RecyclerView.ViewHolder {
        TextView tvMissionIcon, tvMissionTitle, tvMissionDescription;
        TextView tvMissionProgress, tvReward;
        ProgressBar progressMission;
        Button btnClaim;

        MissionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMissionIcon = itemView.findViewById(R.id.tvMissionIcon);
            tvMissionTitle = itemView.findViewById(R.id.tvMissionTitle);
            tvMissionDescription = itemView.findViewById(R.id.tvMissionDescription);
            tvMissionProgress = itemView.findViewById(R.id.tvMissionProgress);
            tvReward = itemView.findViewById(R.id.tvReward);
            progressMission = itemView.findViewById(R.id.progressMission);
            btnClaim = itemView.findViewById(R.id.btnClaim);
        }

        void bind(MissionData mission) {
            tvMissionIcon.setText(mission.icon);
            tvMissionTitle.setText(mission.title);
            tvMissionDescription.setText(mission.description);

            int progress = (mission.currentProgress * 100) / mission.targetProgress;
            progressMission.setProgress(Math.min(progress, 100));
            tvMissionProgress.setText(mission.currentProgress + "/" + mission.targetProgress);

            if (mission.isCompleted) {
                if (mission.isClaimed) {
                    tvReward.setText("✓ Claimed");
                    tvReward.setTextColor(0xFF4CAF50);
                    btnClaim.setVisibility(View.GONE);
                } else {
                    tvReward.setText("+" + mission.reward + " ⭐");
                    btnClaim.setVisibility(View.VISIBLE);
                    btnClaim.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onClaimReward(mission);
                        }
                    });
                }
                tvMissionTitle.setTextColor(0xFF4CAF50);
            } else {
                tvReward.setText("+" + mission.reward + " ⭐");
                tvReward.setTextColor(0xFFFFD700);
                btnClaim.setVisibility(View.GONE);
                tvMissionTitle.setTextColor(0xFFFFFFFF);
            }
        }
    }
}

