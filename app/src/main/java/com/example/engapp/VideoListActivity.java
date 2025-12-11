package com.example.engapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class VideoListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private VideoAdapter adapter;
    private List<VideoInterview> videoList;
    private FirebaseFirestore db;
    private ProgressBar progressBar;
    private TextView tvEmpty, tvTitle;
    private ImageView btnBack;
    private String category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);

        category = getIntent().getStringExtra("category");
        
        initViews();
        loadVideos();
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tvTitle);
        btnBack = findViewById(R.id.btnBack);
        recyclerView = findViewById(R.id.recyclerViewVideos);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        
        tvTitle.setText(category + " - Videos");
        btnBack.setOnClickListener(v -> finish());
        
        videoList = new ArrayList<>();
        adapter = new VideoAdapter(this, videoList, video -> {
            // Pass all video data to player
            Intent intent = new Intent(this, VideoPlayerActivity.class);
            intent.putExtra("videoId", video.getVideoId());
            intent.putExtra("videoTitle", video.getTitle());
            intent.putExtra("platform", video.getPlatform());
            intent.putExtra("streamUrl", video.getStreamUrl());
            startActivity(intent);
        });
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true); // Performance optimization
        recyclerView.setItemViewCacheSize(20); // Cache more items for smooth scrolling
        
        db = FirebaseFirestore.getInstance();
    }

    private void loadVideos() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        
        // Query directly by category for faster loading
        db.collection("videoInterviewIT")
            .whereEqualTo("category", category)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                progressBar.setVisibility(View.GONE);
                videoList.clear();
                
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    VideoInterview video = document.toObject(VideoInterview.class);
                    // No need to filter, already filtered by query
                    {
                        videoList.add(video);
                    }
                }
                
                if (videoList.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("No videos available for " + category);
                } else {
                    adapter.notifyDataSetChanged();
                }
            })
            .addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText("Error loading videos: " + e.getMessage());
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void openYouTubeVideo(String videoUrl) {
        if (videoUrl == null || videoUrl.isEmpty()) {
            Toast.makeText(this, "Video link not available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            // Extract video ID from YouTube URL
            String videoId = extractVideoId(videoUrl);
            
            if (videoId != null) {
                // Try to open in YouTube app
                Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + videoId));
                Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl));
                
                try {
                    startActivity(appIntent);
                } catch (Exception e) {
                    // If YouTube app not installed, open in browser
                    startActivity(webIntent);
                }
            } else {
                // Open direct URL
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl));
                startActivity(intent);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Cannot open video: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String extractVideoId(String url) {
        // Extract YouTube video ID from various URL formats
        // https://www.youtube.com/watch?v=VIDEO_ID
        // https://youtu.be/VIDEO_ID
        try {
            if (url.contains("youtube.com/watch?v=")) {
                return url.split("v=")[1].split("&")[0];
            } else if (url.contains("youtu.be/")) {
                return url.split("youtu.be/")[1].split("\\?")[0];
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
