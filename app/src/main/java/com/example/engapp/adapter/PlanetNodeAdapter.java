package com.example.engapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.engapp.R;
import com.example.engapp.database.GameDatabaseHelper.SceneData;
import java.util.List;

public class PlanetNodeAdapter extends RecyclerView.Adapter<PlanetNodeAdapter.NodeViewHolder> {

    private List<SceneData> nodes;
    private OnNodeClickListener listener;

    public interface OnNodeClickListener {
        void onNodeClick(SceneData node, int position);
    }

    public PlanetNodeAdapter(List<SceneData> nodes, OnNodeClickListener listener) {
        this.nodes = nodes;
        this.listener = listener;
    }
    
    public void updateNodes(List<SceneData> newNodes) {
        this.nodes = newNodes;
    }

    @NonNull
    @Override
    public NodeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_planet_node, parent, false);
        return new NodeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NodeViewHolder holder, int position) {
        SceneData node = nodes.get(position);
        holder.bind(node, position);
    }

    @Override
    public int getItemCount() {
        return nodes.size();
    }

    class NodeViewHolder extends RecyclerView.ViewHolder {
        LinearLayout nodeContainer;
        TextView tvNodeEmoji, tvNodeName, tvNodeNameVi, tvNodeType;
        TextView tvStars, tvCompleted, btnPlay, tvLockMessage;
        FrameLayout lockOverlay;
        View connectionLine;

        NodeViewHolder(@NonNull View itemView) {
            super(itemView);
            nodeContainer = itemView.findViewById(R.id.nodeContainer);
            tvNodeEmoji = itemView.findViewById(R.id.tvNodeEmoji);
            tvNodeName = itemView.findViewById(R.id.tvNodeName);
            tvNodeNameVi = itemView.findViewById(R.id.tvNodeNameVi);
            tvNodeType = itemView.findViewById(R.id.tvNodeType);
            tvStars = itemView.findViewById(R.id.tvStars);
            tvCompleted = itemView.findViewById(R.id.tvCompleted);
            btnPlay = itemView.findViewById(R.id.btnPlay);
            tvLockMessage = itemView.findViewById(R.id.tvLockMessage);
            lockOverlay = itemView.findViewById(R.id.lockOverlay);
            connectionLine = itemView.findViewById(R.id.connectionLine);
        }

        void bind(SceneData node, int position) {
            tvNodeName.setText(node.name);
            tvNodeNameVi.setText(node.nameVi != null ? node.nameVi : "");

            // Use emoji from database if available, otherwise use type-based emoji
            String emoji = (node.emoji != null && !node.emoji.isEmpty()) 
                ? node.emoji 
                : getEmojiForType(node.sceneType);
            tvNodeEmoji.setText(emoji);

            String typeLabel = getTypeLabelForType(node.sceneType);
            tvNodeType.setText(typeLabel);

            int stars = node.starsEarned;
            String starDisplay = "";
            for (int i = 0; i < 3; i++) {
                starDisplay += (i < stars) ? "⭐" : "☆";
            }
            tvStars.setText(starDisplay);

            if (node.isCompleted) {
                tvCompleted.setVisibility(View.VISIBLE);
                btnPlay.setText("REPLAY");
            } else {
                tvCompleted.setVisibility(View.GONE);
                btnPlay.setText("PLAY");
            }

            // Use LessonUnlockManager to check unlock status
            android.content.Context context = itemView.getContext();
            com.example.engapp.manager.LessonUnlockManager unlockManager = 
                com.example.engapp.manager.LessonUnlockManager.getInstance(context);
            
            boolean isLocked = !unlockManager.isLessonUnlocked(node.planetId, node.id);
            lockOverlay.setVisibility(isLocked ? View.VISIBLE : View.GONE);
            itemView.setAlpha(isLocked ? 0.7f : 1.0f);

            if (position < nodes.size() - 1) {
                connectionLine.setVisibility(View.VISIBLE);
            } else {
                connectionLine.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null && !isLocked) {
                    listener.onNodeClick(node, position);
                }
            });

            btnPlay.setOnClickListener(v -> {
                if (listener != null && !isLocked) {
                    listener.onNodeClick(node, position);
                }
            });
        }

        private String getEmojiForType(String type) {
            if (type == null) return "📚";
            
            // Map database scene types to emojis
            switch (type) {
                // Standard types
                case "learn": return "📚";
                case "guess_name": return "🎯";
                case "listen_choose": return "🎧";
                case "match": return "🔗";
                case "sentence": return "✍️";
                case "boss": return "👾";
                case "battle": return "⚔️";
                
                // Database scene_key types (mapped to standard types)
                case "landing_zone": return "📚"; // Learn/Introduction
                case "explore_area": return "🎯"; // Quiz/Explore
                case "dialogue_dock": return "💬"; // Dialogue/Conversation
                case "puzzle_zone": return "🧩"; // Puzzle
                case "boss_gate": return "👾"; // Boss/Battle
                case "mini_game": return "M"; // Mini game
                
                default: return "📚";
            }
        }

        private String getTypeLabelForType(String type) {
            if (type == null) return "LEARN";
            
            // Map database scene types to labels
            switch (type) {
                // Standard types
                case "learn": return "📖 LEARN";
                case "guess_name": return "🎯 QUIZ";
                case "listen_choose": return "🎧 LISTEN";
                case "match": return "🔗 MATCH";
                case "sentence": return "✍️ SENTENCE";
                case "boss": return "👾 BOSS";
                case "battle": return "⚔️ BATTLE";
                
                // Database scene_key types
                case "landing_zone": return "📖 LEARN";
                case "explore_area": return "🎯 EXPLORE";
                case "dialogue_dock": return "💬 DIALOGUE";
                case "puzzle_zone": return "🧩 PUZZLE";
                case "boss_gate": return "👾 BOSS";
                case "mini_game": return "MINI GAME";
                
                default: return "📖 LEARN";
            }
        }
    }
}
