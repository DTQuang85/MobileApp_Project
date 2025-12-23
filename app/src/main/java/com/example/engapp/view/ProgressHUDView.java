package com.example.engapp.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.engapp.R;

/**
 * Global Progress HUD component showing:
 * - Total stars
 * - Next unlock requirement
 * - Progress bar toward next unlock
 * 
 * Usage:
 * progressHUD.setStars(42);
 * progressHUD.setNextUnlock("Toytopia", 50);
 */
public class ProgressHUDView extends LinearLayout {
    
    private TextView tvStars;
    private TextView tvNextUnlock;
    private ProgressBar progressBar;
    
    public ProgressHUDView(Context context) {
        super(context);
        init();
    }
    
    public ProgressHUDView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public ProgressHUDView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        setOrientation(VERTICAL);
        int padding = dpToPx(16);
        setPadding(padding, padding, padding, padding);
        
        // Stars display
        tvStars = new TextView(getContext());
        tvStars.setTextSize(18);
        tvStars.setTextColor(getResources().getColor(R.color.text_white, null));
        tvStars.setTypeface(null, android.graphics.Typeface.BOLD);
        addView(tvStars);
        
        // Next unlock display
        tvNextUnlock = new TextView(getContext());
        tvNextUnlock.setTextSize(14);
        tvNextUnlock.setTextColor(getResources().getColor(R.color.accent_orange, null));
        addView(tvNextUnlock);
        
        // Progress bar
        progressBar = new ProgressBar(getContext(), null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setLayoutParams(new LayoutParams(
            LayoutParams.MATCH_PARENT,
            dpToPx(8)
        ));
        progressBar.setMax(100);
        addView(progressBar);
    }
    
    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
    
    /**
     * Update stars display
     */
    public void setStars(int currentStars) {
        tvStars.setText("⭐ " + currentStars);
    }
    
    /**
     * Set next unlock target
     */
    public void setNextUnlock(String unlockName, int requiredStars) {
        tvNextUnlock.setText("→ Unlock " + unlockName + " at ⭐ " + requiredStars);
    }
    
    /**
     * Update progress toward next unlock
     */
    public void updateProgress(int currentStars, int requiredStars) {
        if (requiredStars <= 0) {
            progressBar.setProgress(100);
            tvNextUnlock.setText("All unlocked! 🎉");
            return;
        }
        
        int progress = (int) ((float) currentStars / requiredStars * 100);
        progressBar.setProgress(Math.min(progress, 100));
        
        int remaining = requiredStars - currentStars;
        if (remaining > 0) {
            tvNextUnlock.setText("→ Unlock next at ⭐ " + requiredStars + " (" + remaining + " more)");
        } else {
            tvNextUnlock.setText("Ready to unlock! 🚀");
        }
    }
    
    /**
     * Clear next unlock (all unlocked)
     */
    public void clearNextUnlock() {
        tvNextUnlock.setText("");
        progressBar.setProgress(100);
    }
}

