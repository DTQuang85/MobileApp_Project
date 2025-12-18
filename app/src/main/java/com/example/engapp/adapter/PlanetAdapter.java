package com.example.engapp.adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.engapp.R;
import com.example.engapp.model.Planet;
import java.util.List;

public class PlanetAdapter extends RecyclerView.Adapter<PlanetAdapter.PlanetViewHolder> {

    private Context context;
    private List<Planet> planets;
    private OnPlanetClickListener listener;

    public interface OnPlanetClickListener {
        void onPlanetClick(Planet planet);
    }

    public PlanetAdapter(Context context, List<Planet> planets, OnPlanetClickListener listener) {
        this.context = context;
        this.planets = planets;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PlanetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_planet, parent, false);
        return new PlanetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlanetViewHolder holder, int position) {
        Planet planet = planets.get(position);
        holder.bind(planet);
    }

    @Override
    public int getItemCount() {
        return planets.size();
    }

    public void updatePlanets(List<Planet> newPlanets) {
        this.planets = newPlanets;
        notifyDataSetChanged();
    }

    class PlanetViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvPlanetEmoji, tvPlanetName, tvPlanetNameVi, tvProgress;
        TextView tvStar1, tvStar2, tvStar3, tvRequiredStars;
        ProgressBar progressBar;
        View lockOverlay;
        LinearLayout lockContainer;
        View bgView;

        public PlanetViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            tvPlanetEmoji = itemView.findViewById(R.id.tvPlanetEmoji);
            tvPlanetName = itemView.findViewById(R.id.tvPlanetName);
            tvPlanetNameVi = itemView.findViewById(R.id.tvPlanetNameVi);
            tvProgress = itemView.findViewById(R.id.tvProgress);
            tvStar1 = itemView.findViewById(R.id.tvStar1);
            tvStar2 = itemView.findViewById(R.id.tvStar2);
            tvStar3 = itemView.findViewById(R.id.tvStar3);
            tvRequiredStars = itemView.findViewById(R.id.tvRequiredStars);
            progressBar = itemView.findViewById(R.id.progressBar);
            lockOverlay = itemView.findViewById(R.id.lockOverlay);
            lockContainer = itemView.findViewById(R.id.lockContainer);
            bgView = ((ViewGroup) itemView).getChildAt(0);
        }

        public void bind(Planet planet) {
            tvPlanetEmoji.setText(planet.getEmoji());
            tvPlanetName.setText(planet.getName());
            tvPlanetNameVi.setText(planet.getNameVi());

            // Set background color
            if (bgView != null && bgView.getBackground() instanceof GradientDrawable) {
                GradientDrawable bg = (GradientDrawable) bgView.getBackground().mutate();
                int color = planet.getColor();
                int darkerColor = darkenColor(color);
                bg.setColors(new int[]{color, darkerColor});
            }

            // Progress
            int progress = planet.getProgress();
            progressBar.setProgress(progress);
            tvProgress.setText(progress + "%");

            // Stars
            int stars = planet.getStarsEarned();
            tvStar1.setAlpha(stars >= 1 ? 1f : 0.3f);
            tvStar2.setAlpha(stars >= 2 ? 1f : 0.3f);
            tvStar3.setAlpha(stars >= 3 ? 1f : 0.3f);

            // Lock state
            if (planet.isUnlocked()) {
                lockOverlay.setVisibility(View.GONE);
                lockContainer.setVisibility(View.GONE);
                tvPlanetEmoji.setVisibility(View.VISIBLE);
            } else {
                lockOverlay.setVisibility(View.VISIBLE);
                lockContainer.setVisibility(View.VISIBLE);
                tvRequiredStars.setText("⭐ " + planet.getRequiredStars());
            }

            // Click listener
            cardView.setOnClickListener(v -> {
                if (planet.isUnlocked() && listener != null) {
                    listener.onPlanetClick(planet);
                }
            });
        }

        private int darkenColor(int color) {
            float factor = 0.7f;
            int a = (color >> 24) & 0xFF;
            int r = (int) (((color >> 16) & 0xFF) * factor);
            int g = (int) (((color >> 8) & 0xFF) * factor);
            int b = (int) ((color & 0xFF) * factor);
            return (a << 24) | (r << 16) | (g << 8) | b;
        }
    }
}

