package com.example.engapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class IntroActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private Button btnNext;
    private TextView btnSkip;
    private IntroAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        initViews();
        setupIntro();
        setupListeners();
    }

    private void initViews() {
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        btnNext = findViewById(R.id.btnNext);
        btnSkip = findViewById(R.id.btnSkip);
    }

    private void setupIntro() {
        adapter = new IntroAdapter(getIntroSlides());
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
        }).attach();

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (position == adapter.getItemCount() - 1) {
                    btnNext.setText("Get Started");
                } else {
                    btnNext.setText("Next");
                }
            }
        });
    }

    private void setupListeners() {
        btnNext.setOnClickListener(v -> {
            int currentItem = viewPager.getCurrentItem();
            if (currentItem < adapter.getItemCount() - 1) {
                viewPager.setCurrentItem(currentItem + 1);
            } else {
                finishIntro();
            }
        });

        btnSkip.setOnClickListener(v -> finishIntro());
    }

    private void finishIntro() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        prefs.edit().putBoolean("intro_seen", true).apply();

        // Navigate to new main flow
        Intent intent = new Intent(this, InteractiveGalaxyMapActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_scale_in, 0);
        finish();
    }

    private IntroAdapter.IntroSlide[] getIntroSlides() {
        return new IntroAdapter.IntroSlide[] {
            new IntroAdapter.IntroSlide(
                "🌌",
                "Explore the Galaxy",
                "Travel through different eras and discover new planets!"
            ),
            new IntroAdapter.IntroSlide(
                "📚",
                "Learn English Words",
                "Master vocabulary through fun and engaging games!"
            ),
            new IntroAdapter.IntroSlide(
                "🎯",
                "Complete Missions",
                "Unlock achievements and earn rewards as you progress!"
            ),
            new IntroAdapter.IntroSlide(
                "🤝",
                "Meet Your Buddy",
                "Your AI companion will guide you through your learning journey!"
            )
        };
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() > 0) {
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        } else {
            super.onBackPressed();
        }
    }
}
