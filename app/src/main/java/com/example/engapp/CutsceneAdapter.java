package com.example.engapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class CutsceneAdapter extends RecyclerView.Adapter<CutsceneAdapter.CutsceneViewHolder> {

    private CutsceneScene[] scenes;

    public CutsceneAdapter(CutsceneScene[] scenes) {
        this.scenes = scenes;
    }

    @NonNull
    @Override
    public CutsceneViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cutscene, parent, false);
        return new CutsceneViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CutsceneViewHolder holder, int position) {
        CutsceneScene scene = scenes[position];
        holder.bind(scene);
    }

    @Override
    public int getItemCount() {
        return scenes.length;
    }

    static class CutsceneViewHolder extends RecyclerView.ViewHolder {
        TextView tvSceneEmoji;
        TextView tvSceneTitle;
        TextView tvSceneDescription;
        CardView speechBubble;
        TextView tvBuddySpeech;

        CutsceneViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSceneEmoji = itemView.findViewById(R.id.tvSceneEmoji);
            tvSceneTitle = itemView.findViewById(R.id.tvSceneTitle);
            tvSceneDescription = itemView.findViewById(R.id.tvSceneDescription);
            speechBubble = itemView.findViewById(R.id.speechBubble);
            tvBuddySpeech = itemView.findViewById(R.id.tvBuddySpeech);
        }

        void bind(CutsceneScene scene) {
            tvSceneEmoji.setText(scene.emoji);
            tvSceneTitle.setText(scene.title);
            tvSceneDescription.setText(scene.description);

            if (scene.buddySpeech != null && !scene.buddySpeech.isEmpty()) {
                speechBubble.setVisibility(View.VISIBLE);
                tvBuddySpeech.setText(scene.buddySpeech);
            } else {
                speechBubble.setVisibility(View.GONE);
            }
        }
    }

    public static class CutsceneScene {
        String emoji;
        String title;
        String description;
        String buddySpeech;

        public CutsceneScene(String emoji, String title, String description, String buddySpeech) {
            this.emoji = emoji;
            this.title = title;
            this.description = description;
            this.buddySpeech = buddySpeech;
        }
    }
}

