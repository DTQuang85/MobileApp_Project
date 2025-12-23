package com.example.engapp;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.engapp.manager.BuddyManager;
import com.example.engapp.manager.ProgressionManager;
import com.example.engapp.view.BuddyOverlayView;

/**
 * Base Activity class that includes Buddy companion overlay.
 * Extend this class to add Buddy to any learning activity.
 */
public abstract class BaseBuddyActivity extends AppCompatActivity implements BuddyManager.BuddyEventListener {

    protected BuddyOverlayView buddyOverlay;
    protected BuddyManager buddyManager;
    protected ProgressionManager progressionManager;

    private boolean buddyEnabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        buddyManager = BuddyManager.getInstance(this);
        progressionManager = ProgressionManager.getInstance(this);
        buddyManager.addListener(this);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        if (buddyEnabled) {
            addBuddyOverlay();
        }
    }

    /**
     * Override this to disable Buddy on specific screens
     */
    protected void setBuddyEnabled(boolean enabled) {
        this.buddyEnabled = enabled;
    }

    private void addBuddyOverlay() {
        // Find root view and add buddy overlay
        ViewGroup rootView = findViewById(android.R.id.content);
        if (rootView != null && rootView.getChildCount() > 0) {
            // Get the first child (main content)
            ViewGroup mainContent = (ViewGroup) rootView.getChildAt(0);

            // If main content is not a FrameLayout, we need to wrap it
            if (!(mainContent instanceof FrameLayout)) {
                // Create wrapper
                FrameLayout wrapper = new FrameLayout(this);
                wrapper.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));

                // Move main content to wrapper
                rootView.removeView(mainContent);
                wrapper.addView(mainContent);

                // Add buddy overlay to wrapper
                buddyOverlay = new BuddyOverlayView(this);
                wrapper.addView(buddyOverlay);

                // Add wrapper to root
                rootView.addView(wrapper);
            } else {
                // Already a FrameLayout, just add buddy
                buddyOverlay = new BuddyOverlayView(this);
                mainContent.addView(buddyOverlay);
            }
        }
    }

    /**
     * Call when user answers correctly
     */
    protected void onCorrectAnswer() {
        if (buddyManager != null) {
            buddyManager.onCorrectAnswer();
        }
    }

    /**
     * Call when user answers incorrectly
     */
    protected void onWrongAnswer() {
        if (buddyManager != null) {
            buddyManager.onWrongAnswer();
        }
    }

    /**
     * Call when user requests a hint
     */
    protected void onHintRequested() {
        if (buddyManager != null) {
            buddyManager.onHintRequested();
        }
    }

    /**
     * Call when user completes a zone/level
     */
    protected void onZoneCompleted() {
        if (buddyManager != null) {
            buddyManager.onZoneComplete();
        }
    }

    /**
     * Record that user learned a new word
     */
    protected void recordWordLearned(String word, String wordVi, String planetId) {
        if (progressionManager != null) {
            progressionManager.recordWordLearned(word, wordVi, planetId);
        }
    }

    /**
     * Record that user completed a game
     */
    protected void recordGameCompleted(String gameType, int starsEarned) {
        if (progressionManager != null) {
            progressionManager.recordGameCompleted(gameType, starsEarned);
        }
    }

    /**
     * Add stars to user's progress
     */
    protected void addStars(int amount, String source) {
        if (progressionManager != null) {
            progressionManager.addStars(amount, source);
        }
    }

    // BuddyEventListener implementation
    @Override
    public void onStateChanged(String newState, String previousState) {
        // Override in subclass if needed
    }

    @Override
    public void onBuddySpeak(String message) {
        // Override in subclass if needed
    }

    @Override
    public void onMoodChanged(int newMood) {
        // Override in subclass if needed
    }

    @Override
    public void onBuddyLevelUp(int newLevel) {
        // Override in subclass if needed
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (buddyManager != null) {
            buddyManager.removeListener(this);
        }
        if (buddyOverlay != null) {
            buddyOverlay.cleanup();
        }
    }
}

