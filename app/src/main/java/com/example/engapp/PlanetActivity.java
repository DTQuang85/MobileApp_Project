package com.example.engapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.engapp.database.GameDatabaseHelper;
import com.example.engapp.database.GameDatabaseHelper.*;
import java.util.List;

public class PlanetActivity extends AppCompatActivity {

    private RecyclerView recyclerZones;
    private TextView tvPlanetName, tvPlanetNameVi, tvPlanetEmoji, tvProgress, tvStars;
    private TextView tvGrammarFocus, tvCollectibles;
    private ProgressBar progressPlanet;
    private ImageView btnBack;
    private View planetHeader;

    private GameDatabaseHelper dbHelper;
    private int planetId;
    private PlanetData currentPlanet;
    private List<SceneData> scenes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planet);

        // Animation chuyển cảnh kiểu warp
        overridePendingTransition(R.anim.warp_in, R.anim.warp_out);

        planetId = getIntent().getIntExtra("planet_id", -1);
        if (planetId == -1) {
            finish();
            return;
        }

        dbHelper = GameDatabaseHelper.getInstance(this);

        initViews();
        loadPlanet();
        setupScenes();
    }

    private void initViews() {
        recyclerZones = findViewById(R.id.recyclerZones);
        tvPlanetName = findViewById(R.id.tvPlanetName);
        tvPlanetNameVi = findViewById(R.id.tvPlanetNameVi);
        tvPlanetEmoji = findViewById(R.id.tvPlanetEmoji);
        tvProgress = findViewById(R.id.tvProgress);
        tvStars = findViewById(R.id.tvStars);
        progressPlanet = findViewById(R.id.progressPlanet);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });
    }

    private void loadPlanet() {
        currentPlanet = dbHelper.getPlanetById(planetId);

        if (currentPlanet == null) {
            finish();
            return;
        }

        tvPlanetName.setText(currentPlanet.name);
        tvPlanetNameVi.setText(currentPlanet.nameVi);
        tvPlanetEmoji.setText(currentPlanet.emoji);

        dbHelper.ensurePlanetsSeededNow();
        // Load scenes
        scenes = dbHelper.getScenesForPlanet(planetId);

        // Calculate progress
        int completed = 0;
        int totalStars = 0;
        for (SceneData scene : scenes) {
            if (scene.isCompleted) completed++;
            totalStars += scene.starsEarned;
        }

        int progress = scenes.isEmpty() ? 0 : (completed * 100) / scenes.size();
        progressPlanet.setProgress(progress);
        tvProgress.setText(progress + "% hoàn thành");
        tvStars.setText("⭐ " + totalStars + "/" + (scenes.size() * 3));
    }

    private void setupScenes() {
        recyclerZones.setLayoutManager(new LinearLayoutManager(this));
        recyclerZones.setAdapter(new SceneAdapter());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPlanet();
        if (recyclerZones.getAdapter() != null) {
            recyclerZones.getAdapter().notifyDataSetChanged();
        }
    }

    // ============ SCENE ADAPTER ============

    class SceneAdapter extends RecyclerView.Adapter<SceneAdapter.SceneViewHolder> {

        private String[] sceneColors = {
            "#4ECDC4", "#FF6B6B", "#45B7D1", "#96CEB4", "#A29BFE"
        };

        @NonNull
        @Override
        public SceneViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_scene, parent, false);
            return new SceneViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SceneViewHolder holder, int position) {
            SceneData scene = scenes.get(position);

            holder.tvSceneName.setText(scene.name);
            holder.tvSceneNameVi.setText(scene.nameVi);
            holder.tvSceneEmoji.setText(scene.emoji);
            holder.tvSceneDescription.setText(scene.description);

            // Set color
            int colorIndex = position % sceneColors.length;
            holder.sceneContainer.setBackgroundColor(Color.parseColor(sceneColors[colorIndex]));

            // Stars
            holder.tvStars.setText(scene.starsEarned > 0 ?
                "⭐".repeat(scene.starsEarned) + "☆".repeat(3 - scene.starsEarned) :
                "☆☆☆");

            // Lock state - first scene always unlocked, others need previous completed
            boolean isUnlocked = position == 0 || (position > 0 && scenes.get(position - 1).isCompleted);

            if (isUnlocked) {
                holder.lockOverlay.setVisibility(View.GONE);
                holder.itemView.setAlpha(1f);
            } else {
                holder.lockOverlay.setVisibility(View.VISIBLE);
                holder.itemView.setAlpha(0.7f);
            }

            // Completed badge
            if (scene.isCompleted) {
                holder.tvCompleted.setVisibility(View.VISIBLE);
            } else {
                holder.tvCompleted.setVisibility(View.GONE);
            }

            // Click listener
            final boolean finalUnlocked = isUnlocked;
            holder.itemView.setOnClickListener(v -> {
                if (finalUnlocked) {
                    openScene(scene, position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return scenes != null ? scenes.size() : 0;
        }

        class SceneViewHolder extends RecyclerView.ViewHolder {
            View sceneContainer, lockOverlay;
            TextView tvSceneName, tvSceneNameVi, tvSceneEmoji;
            TextView tvSceneDescription, tvStars, tvCompleted;

            SceneViewHolder(@NonNull View itemView) {
                super(itemView);
                sceneContainer = itemView.findViewById(R.id.sceneContainer);
                lockOverlay = itemView.findViewById(R.id.lockOverlay);
                tvSceneName = itemView.findViewById(R.id.tvSceneName);
                tvSceneNameVi = itemView.findViewById(R.id.tvSceneNameVi);
                tvSceneEmoji = itemView.findViewById(R.id.tvSceneEmoji);
                tvSceneDescription = itemView.findViewById(R.id.tvSceneDescription);
                tvStars = itemView.findViewById(R.id.tvStars);
                tvCompleted = itemView.findViewById(R.id.tvCompleted);
            }
        }
    }

    private void openScene(SceneData scene, int position) {
        Intent intent;

        switch (scene.sceneType) {
            case "landing_zone":
                intent = new Intent(this, LearnWordsActivity.class);
                break;
            case "explore_area":
                intent = new Intent(this, ExploreActivity.class);
                break;
            case "dialogue_dock":
                intent = new Intent(this, DialogueActivity.class);
                break;
            case "puzzle_zone":
                intent = new Intent(this, PuzzleGameActivity.class);
                break;
            case "boss_gate":
                intent = new Intent(this, BossGateActivity.class);
                break;
            case "mini_game":
                intent = new Intent(this, SignalDecodeActivity.class);
                break;
            default:
                intent = new Intent(this, LearnWordsActivity.class);
        }

        intent.putExtra("planet_id", planetId);
        intent.putExtra("scene_id", scene.id);
        intent.putExtra("scene_type", scene.sceneType);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}
