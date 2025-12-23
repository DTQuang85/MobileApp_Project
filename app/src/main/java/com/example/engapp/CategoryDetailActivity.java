package com.example.engapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.android.material.tabs.TabLayout;

public class CategoryDetailActivity extends AppCompatActivity {
    private String categoryName;
    private TabLayout tabLayout;
    private View vocabularySection, videoSection, gameSection;
    private CardView btnVocabulary, btnVideo, btnGame;
    private ImageView btnBack;
    private TextView tvTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_detail);

        categoryName = getIntent().getStringExtra("category");
        
        initViews();
        setupTabs();
        showVocabularySection(); // Mặc định hiện vocabulary
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tvTitle);
        btnBack = findViewById(R.id.btnBack);
        tabLayout = findViewById(R.id.tabLayout);
        
        vocabularySection = findViewById(R.id.vocabularySection);
        videoSection = findViewById(R.id.videoSection);
        gameSection = findViewById(R.id.gameSection);
        
        btnVocabulary = findViewById(R.id.btnVocabulary);
        btnVideo = findViewById(R.id.btnVideo);
        btnGame = findViewById(R.id.btnGame);
        
        tvTitle.setText(categoryName);
        
        btnBack.setOnClickListener(v -> finish());
        
        // Click listeners
        btnVocabulary.setOnClickListener(v -> {
            Intent intent = new Intent(this, VocabularyActivity.class);
            intent.putExtra("category", categoryName);
            startActivity(intent);
        });
        
        btnVideo.setOnClickListener(v -> {
            Intent intent = new Intent(this, VideoListActivity.class);
            intent.putExtra("category", categoryName);
            startActivity(intent);
        });
        
        btnGame.setOnClickListener(v -> {
            Intent intent = new Intent(this, VocabularyGameActivity.class);
            startActivity(intent);
        });
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Vocabulary"));
        tabLayout.addTab(tabLayout.newTab().setText("Videos"));
        tabLayout.addTab(tabLayout.newTab().setText("Game"));
        
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        showVocabularySection();
                        break;
                    case 1:
                        showVideoSection();
                        break;
                    case 2:
                        showGameSection();
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void showVocabularySection() {
        vocabularySection.setVisibility(View.VISIBLE);
        videoSection.setVisibility(View.GONE);
        gameSection.setVisibility(View.GONE);
    }

    private void showVideoSection() {
        vocabularySection.setVisibility(View.GONE);
        videoSection.setVisibility(View.VISIBLE);
        gameSection.setVisibility(View.GONE);
    }

    private void showGameSection() {
        vocabularySection.setVisibility(View.GONE);
        videoSection.setVisibility(View.GONE);
        gameSection.setVisibility(View.VISIBLE);
    }
}
