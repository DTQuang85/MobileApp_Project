package com.example.engapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.engapp.R;
import com.example.engapp.model.Zone;
import java.util.List;

public class ZoneAdapter extends RecyclerView.Adapter<ZoneAdapter.ZoneViewHolder> {

    private Context context;
    private List<Zone> zones;
    private OnZoneClickListener listener;

    public interface OnZoneClickListener {
        void onZoneClick(Zone zone, int position);
    }

    public ZoneAdapter(Context context, List<Zone> zones, OnZoneClickListener listener) {
        this.context = context;
        this.zones = zones;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ZoneViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_zone, parent, false);
        return new ZoneViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ZoneViewHolder holder, int position) {
        Zone zone = zones.get(position);
        holder.bind(zone, position);
    }

    @Override
    public int getItemCount() {
        return zones.size();
    }

    class ZoneViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvZoneEmoji, tvZoneName, tvZoneNameVi, tvWordCount, tvGameCount;
        TextView tvStar1, tvStar2, tvStar3, tvLock;

        public ZoneViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            tvZoneEmoji = itemView.findViewById(R.id.tvZoneEmoji);
            tvZoneName = itemView.findViewById(R.id.tvZoneName);
            tvZoneNameVi = itemView.findViewById(R.id.tvZoneNameVi);
            tvWordCount = itemView.findViewById(R.id.tvWordCount);
            tvGameCount = itemView.findViewById(R.id.tvGameCount);
            tvStar1 = itemView.findViewById(R.id.tvStar1);
            tvStar2 = itemView.findViewById(R.id.tvStar2);
            tvStar3 = itemView.findViewById(R.id.tvStar3);
            tvLock = itemView.findViewById(R.id.tvLock);
        }

        public void bind(Zone zone, int position) {
            tvZoneEmoji.setText(zone.getEmoji());
            tvZoneName.setText(zone.getName());
            tvZoneNameVi.setText(zone.getNameVi());

            int wordCount = zone.getWords() != null ? zone.getWords().size() : 0;
            tvWordCount.setText("📚 " + wordCount + " từ");
            tvGameCount.setText("🎮 4 trò chơi");

            // Stars
            int stars = zone.getStarsEarned();
            tvStar1.setAlpha(stars >= 1 ? 1f : 0.3f);
            tvStar2.setAlpha(stars >= 2 ? 1f : 0.3f);
            tvStar3.setAlpha(stars >= 3 ? 1f : 0.3f);

            // Lock state - first zone always unlocked, others based on previous completion
            boolean isUnlocked = position == 0 || zone.isUnlocked();
            if (position > 0 && zones.get(position - 1).isCompleted()) {
                isUnlocked = true;
            }
            zone.setUnlocked(isUnlocked);

            final boolean finalIsUnlocked = isUnlocked;

            if (finalIsUnlocked) {
                tvLock.setVisibility(View.GONE);
                cardView.setAlpha(1f);
            } else {
                tvLock.setVisibility(View.VISIBLE);
                cardView.setAlpha(0.6f);
            }

            cardView.setOnClickListener(v -> {
                if (finalIsUnlocked && listener != null) {
                    listener.onZoneClick(zone, position);
                }
            });
        }
    }
}

