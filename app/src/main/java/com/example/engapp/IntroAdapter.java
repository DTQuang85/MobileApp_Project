package com.example.engapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class IntroAdapter extends RecyclerView.Adapter<IntroAdapter.IntroViewHolder> {
    private IntroActivity context;

    public IntroAdapter(IntroActivity context) {
        this.context = context;
    }

    @NonNull
    @Override
    public IntroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_intro, parent, false);
        return new IntroViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IntroViewHolder holder, int position) {
        switch (position) {
            case 0:
                holder.icon.setText("📚");
                holder.title.setText("Learn IT Vocabulary");
                holder.description.setText("Master technical terms across 10 different IT categories");
                break;
            case 1:
                holder.icon.setText("🎯");
                holder.title.setText("Interactive Learning");
                holder.description.setText("Practice with examples and hear correct pronunciations");
                break;
            case 2:
                holder.icon.setText("📊");
                holder.title.setText("Track Your Progress");
                holder.description.setText("Monitor your learning journey and improve your tech vocabulary");
                break;
            case 3:
                holder.icon.setText("🚀");
                holder.title.setText("Get Started");
                holder.description.setText("Join thousands of learners improving their IT vocabulary today!");
                break;
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }

    static class IntroViewHolder extends RecyclerView.ViewHolder {
        TextView icon, title, description;

        public IntroViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.tvIntroIcon);
            title = itemView.findViewById(R.id.tvIntroTitle);
            description = itemView.findViewById(R.id.tvIntroDescription);
        }
    }
}
