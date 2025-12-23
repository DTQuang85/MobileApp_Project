package com.example.engapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class VocabularyAdapter extends RecyclerView.Adapter<VocabularyAdapter.VocabularyViewHolder> {
    private Context context;
    private List<Vocabulary> vocabularyList;
    private OnVocabularyClickListener listener;

    public interface OnVocabularyClickListener {
        void onVocabularyClick(Vocabulary vocabulary);
    }

    public VocabularyAdapter(Context context, List<Vocabulary> vocabularyList, OnVocabularyClickListener listener) {
        this.context = context;
        this.vocabularyList = vocabularyList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VocabularyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_vocabulary, parent, false);
        return new VocabularyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VocabularyViewHolder holder, int position) {
        Vocabulary vocab = vocabularyList.get(position);
        
        holder.tvTerm.setText(vocab.getTerm());
        holder.tvType.setText("(" + vocab.getType() + ")");
        holder.tvPronunciation.setText(vocab.getPronunciation());
        holder.tvDefinition.setText(vocab.getDefinition());
        
        // Load image từ URL nếu có
        if (vocab.getImage() != null && !vocab.getImage().isEmpty() && 
            (vocab.getImage().startsWith("http://") || vocab.getImage().startsWith("https://"))) {
            holder.cardImage.setVisibility(android.view.View.VISIBLE);
            com.bumptech.glide.Glide.with(context)
                .load(vocab.getImage())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .centerCrop()
                .into(holder.ivImage);
        } else {
            holder.cardImage.setVisibility(android.view.View.GONE);
        }
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onVocabularyClick(vocab);
            }
        });
    }

    @Override
    public int getItemCount() {
        return vocabularyList.size();
    }

    static class VocabularyViewHolder extends RecyclerView.ViewHolder {
        TextView tvTerm, tvType, tvPronunciation, tvDefinition;
        ImageView ivImage;
        androidx.cardview.widget.CardView cardImage;

        public VocabularyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTerm = itemView.findViewById(R.id.tvTerm);
            tvType = itemView.findViewById(R.id.tvType);
            tvPronunciation = itemView.findViewById(R.id.tvPronunciation);
            tvDefinition = itemView.findViewById(R.id.tvDefinition);
            ivImage = itemView.findViewById(R.id.ivVocabImage);
            cardImage = itemView.findViewById(R.id.cardImage);
        }
    }
}
