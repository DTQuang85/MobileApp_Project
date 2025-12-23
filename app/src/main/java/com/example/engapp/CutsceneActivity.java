package com.example.engapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class CutsceneActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TabLayout tabIndicator;
    private Button btnContinue;
    private TextView btnSkip;
    private CardView goalCard;
    private Button btnStartAdventure;
    private CutsceneAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cutscene);

        initViews();
        setupCutscene();
        setupListeners();
    }

    private void initViews() {
        viewPager = findViewById(R.id.cutsceneViewPager);
        tabIndicator = findViewById(R.id.tabIndicator);
        btnContinue = findViewById(R.id.btnContinue);
        btnSkip = findViewById(R.id.btnSkip);
        goalCard = findViewById(R.id.goalCard);
        btnStartAdventure = findViewById(R.id.btnStartAdventure);
    }

    private void setupCutscene() {
        adapter = new CutsceneAdapter(getCutsceneScenes());
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabIndicator, viewPager, (tab, position) -> {
        }).attach();

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (position == adapter.getItemCount() - 1) {
                    btnContinue.setText("Finish");
                } else {
                    btnContinue.setText("Continue");
                }
            }
        });
    }

    private void setupListeners() {
        btnContinue.setOnClickListener(v -> {
            int currentItem = viewPager.getCurrentItem();
            if (currentItem < adapter.getItemCount() - 1) {
                viewPager.setCurrentItem(currentItem + 1);
            } else {
                showGoalCard();
            }
        });

        btnSkip.setOnClickListener(v -> showGoalCard());

        btnStartAdventure.setOnClickListener(v -> finishCutscene());
    }

    private void showGoalCard() {
        viewPager.setVisibility(View.GONE);
        tabIndicator.setVisibility(View.GONE);
        btnContinue.setVisibility(View.GONE);
        btnSkip.setVisibility(View.GONE);
        goalCard.setVisibility(View.VISIBLE);
    }

    private void finishCutscene() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        prefs.edit().putBoolean("cutscene_seen", true).apply();

        Intent intent = new Intent(this, IntroActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_scale_in, 0);
        finish();
    }

    private CutsceneAdapter.CutsceneScene[] getCutsceneScenes() {
        return new CutsceneAdapter.CutsceneScene[] {
            new CutsceneAdapter.CutsceneScene(
                "🚀",
                "Welcome, Space Explorer!",
                "The universe is waiting for you...",
                null
            ),
            new CutsceneAdapter.CutsceneScene(
                "🌍",
                "Discover New Worlds",
                "Each planet holds amazing English words to learn!",
                null
            ),
            new CutsceneAdapter.CutsceneScene(
                "🤖",
                "Meet Your Buddy",
                "Cosmo will help you on your journey!",
                "Hi! I'm Cosmo! Let's explore together!"
            ),
            new CutsceneAdapter.CutsceneScene(
                "⭐",
                "Collect Stars",
                "Complete challenges to earn stars and unlock new planets!",
                null
            ),
            new CutsceneAdapter.CutsceneScene(
                "🎮",
                "Play & Learn",
                "Fun games will help you remember words forever!",
                "Are you ready for the adventure?"
            )
        };
    }

    @Override
    public void onBackPressed() {
        if (goalCard.getVisibility() == View.VISIBLE) {
            goalCard.setVisibility(View.GONE);
            viewPager.setVisibility(View.VISIBLE);
            tabIndicator.setVisibility(View.VISIBLE);
            btnContinue.setVisibility(View.VISIBLE);
            btnSkip.setVisibility(View.VISIBLE);
        } else if (viewPager.getCurrentItem() > 0) {
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        } else {
            super.onBackPressed();
        }
    }
}

