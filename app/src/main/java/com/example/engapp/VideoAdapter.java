package com.example.engapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {
    private Context context;
    private List<VideoInterview> videoList;
    private OnVideoClickListener listener;

    public interface OnVideoClickListener {
        void onVideoClick(VideoInterview video);
    }

    public VideoAdapter(Context context, List<VideoInterview> videoList, OnVideoClickListener listener) {
        this.context = context;
        this.videoList = videoList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        VideoInterview video = videoList.get(position);
        
        holder.tvTitle.setText(video.getTitle());
        holder.tvDescription.setText(video.getCategory());
        
        // Load thumbnail with Glide optimization for faster loading
        if (video.getThumbnail() != null && !video.getThumbnail().isEmpty()) {
            Glide.with(context)
                .load(video.getThumbnail())
                .placeholder(R.drawable.avatar_circle_bg)
                .error(R.drawable.avatar_circle_bg)
                .override(400, 225) // Resize to standard thumbnail size
                .centerCrop()
                .skipMemoryCache(false) // Use memory cache
                .into(holder.ivThumbnail);
        } else if (video.getVideoId() != null && !video.getVideoId().isEmpty()) {
            // Fallback: generate thumbnail from videoId field
            String thumbnailUrl = "https://img.youtube.com/vi/" + video.getVideoId() + "/mqdefault.jpg"; // Use mqdefault for faster loading
            Glide.with(context)
                .load(thumbnailUrl)
                .placeholder(R.drawable.avatar_circle_bg)
                .error(R.drawable.avatar_circle_bg)
                .override(400, 225)
                .centerCrop()
                .skipMemoryCache(false)
                .into(holder.ivThumbnail);
        } else {
            holder.ivThumbnail.setImageResource(R.drawable.avatar_circle_bg);
        }
        
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onVideoClick(video);
            }
        });
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    static class VideoViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView ivThumbnail, ivPlayIcon;
        TextView tvTitle, tvDescription;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            ivThumbnail = itemView.findViewById(R.id.ivThumbnail);
            ivPlayIcon = itemView.findViewById(R.id.ivPlayIcon);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
        }
    }
}
