package com.example.engapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BehavioralAdapter extends RecyclerView.Adapter<BehavioralAdapter.ViewHolder> {

    private Context context;
    private List<BehavioralQuestion> questions;

    public BehavioralAdapter(Context context, List<BehavioralQuestion> questions) {
        this.context = context;
        this.questions = questions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_behavioral, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BehavioralQuestion question = questions.get(position);

        if (question == null) return;

        holder.tvQuestion.setText(question.getQuestion() != null ? question.getQuestion() : "");
        holder.tvCategory.setText(question.getCategory() != null ? question.getCategory() : "");
        holder.tvDifficulty.setText(question.getDifficulty() != null ? question.getDifficulty().toUpperCase() : "");

        // Count keywords
        int keywordCount = 0;
        if (question.getKeywords() != null && !question.getKeywords().isEmpty()) {
            keywordCount = question.getKeywords().size();
        }
        holder.tvKeywords.setText(keywordCount + " key points");

        // Set difficulty icon and color
        String difficulty = question.getDifficulty() != null ? question.getDifficulty().toLowerCase() : "easy";
        switch (difficulty) {
            case "easy":
                holder.ivDifficulty.setImageResource(R.drawable.ic_difficulty_easy);
                holder.tvDifficulty.setTextColor(context.getResources().getColor(R.color.difficulty_easy));
                break;
            case "medium":
                holder.ivDifficulty.setImageResource(R.drawable.ic_difficulty_medium);
                holder.tvDifficulty.setTextColor(context.getResources().getColor(R.color.difficulty_medium));
                break;
            case "hard":
                holder.ivDifficulty.setImageResource(R.drawable.ic_difficulty_hard);
                holder.tvDifficulty.setTextColor(context.getResources().getColor(R.color.difficulty_hard));
                break;
        }

        // Click listener to open detail
        holder.cardView.setOnClickListener(v -> {
            Intent intent = new Intent(context, BehavioralDetailActivity.class);
            intent.putExtra("questionId", question.getId());
            intent.putExtra("question", question.getQuestion());
            intent.putExtra("category", question.getCategory());
            intent.putExtra("difficulty", question.getDifficulty());
            intent.putExtra("sample_basic", question.getSample_basic());
            intent.putExtra("sample_intermediate", question.getSample_intermediate());
            intent.putExtra("sample_advanced", question.getSample_advanced());
            intent.putExtra("keywords", question.getKeywordsAsString()); // Convert List to String
            intent.putExtra("explanation", question.getExplanation());
            intent.putExtra("practice_template", question.getPractice_template());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return questions != null ? questions.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvCategory, tvDifficulty, tvQuestion, tvKeywords;
        ImageView ivDifficulty;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDifficulty = itemView.findViewById(R.id.tvDifficulty);
            tvQuestion = itemView.findViewById(R.id.tvQuestion);
            tvKeywords = itemView.findViewById(R.id.tvKeywords);
            ivDifficulty = itemView.findViewById(R.id.ivDifficulty);
        }
    }
}
