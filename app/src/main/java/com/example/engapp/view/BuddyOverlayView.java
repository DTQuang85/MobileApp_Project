package com.example.engapp.view;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.example.engapp.R;
import com.example.engapp.manager.BuddyManager;
import com.example.engapp.model.BuddyState;

/**
 * Custom view that displays the Buddy companion as an overlay.
 * Can be added to any activity layout to show Buddy across screens.
 */
public class BuddyOverlayView extends FrameLayout implements BuddyManager.BuddyEventListener {

    private TextView tvBuddyEmoji;
    private CardView cardBuddy;
    private CardView cardSpeechBubble;
    private TextView tvSpeech;
    private LinearLayout layoutSpeechBubble;

    private BuddyManager buddyManager;
    private Handler handler;
    private boolean isExpanded = false;
    private ObjectAnimator idleAnimator;

    // Display modes
    public static final int MODE_SMALL = 0;  // Bottom corner, small
    public static final int MODE_MEDIUM = 1; // Larger, for learning screens
    public static final int MODE_LARGE = 2;  // Full display for travel/celebration

    private int currentMode = MODE_SMALL;

    public BuddyOverlayView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public BuddyOverlayView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BuddyOverlayView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        handler = new Handler(Looper.getMainLooper());
        buddyManager = BuddyManager.getInstance(context);
        buddyManager.addListener(this);

        createViews(context);
        setupTouchListeners();
        updateBuddyAppearance();
        startIdleAnimation();
    }

    private void createViews(Context context) {
        // Main buddy container
        cardBuddy = new CardView(context);
        cardBuddy.setCardBackgroundColor(Color.parseColor("#40FFFFFF"));
        cardBuddy.setRadius(100f);
        cardBuddy.setCardElevation(12f);

        int buddySize = dpToPx(80);
        LayoutParams buddyParams = new LayoutParams(buddySize, buddySize);
        buddyParams.gravity = Gravity.BOTTOM | Gravity.START;
        buddyParams.setMargins(dpToPx(16), 0, 0, dpToPx(80));
        cardBuddy.setLayoutParams(buddyParams);

        // Buddy emoji
        tvBuddyEmoji = new TextView(context);
        tvBuddyEmoji.setTextSize(40);
        tvBuddyEmoji.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams emojiParams = new FrameLayout.LayoutParams(
            LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        tvBuddyEmoji.setLayoutParams(emojiParams);
        cardBuddy.addView(tvBuddyEmoji);

        // Speech bubble container
        layoutSpeechBubble = new LinearLayout(context);
        layoutSpeechBubble.setOrientation(LinearLayout.VERTICAL);
        layoutSpeechBubble.setVisibility(GONE);

        LayoutParams bubbleContainerParams = new LayoutParams(
            LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        bubbleContainerParams.gravity = Gravity.BOTTOM | Gravity.START;
        bubbleContainerParams.setMargins(dpToPx(16), 0, dpToPx(16), dpToPx(170));
        layoutSpeechBubble.setLayoutParams(bubbleContainerParams);

        // Speech bubble card
        cardSpeechBubble = new CardView(context);
        cardSpeechBubble.setCardBackgroundColor(Color.WHITE);
        cardSpeechBubble.setRadius(dpToPx(16));
        cardSpeechBubble.setCardElevation(8f);
        cardSpeechBubble.setMaxCardElevation(8f);

        // Speech text
        tvSpeech = new TextView(context);
        tvSpeech.setTextColor(Color.parseColor("#2C3E50"));
        tvSpeech.setTextSize(14);
        tvSpeech.setPadding(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(12));
        tvSpeech.setMaxWidth(dpToPx(220));
        cardSpeechBubble.addView(tvSpeech);

        layoutSpeechBubble.addView(cardSpeechBubble);

        // Add speech bubble triangle
        View triangle = createTriangle(context);
        layoutSpeechBubble.addView(triangle);

        // Add views to layout
        addView(layoutSpeechBubble);
        addView(cardBuddy);
    }

    private View createTriangle(Context context) {
        View triangle = new View(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dpToPx(20), dpToPx(10));
        params.setMargins(dpToPx(30), 0, 0, 0);
        triangle.setLayoutParams(params);

        // Create triangle shape using gradient drawable
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(Color.WHITE);
        triangle.setBackground(drawable);
        triangle.setRotation(45);

        return triangle;
    }

    private void setupTouchListeners() {
        cardBuddy.setOnClickListener(v -> {
            animateBuddyTap();
            buddyManager.onUserTapBuddy();
        });

        cardBuddy.setOnLongClickListener(v -> {
            toggleExpanded();
            return true;
        });

        // Make speech bubble dismissible
        layoutSpeechBubble.setOnClickListener(v -> hideSpeechBubble());
    }

    public void updateBuddyAppearance() {
        String emoji = buddyManager.getCurrentBuddyEmoji();
        tvBuddyEmoji.setText(emoji);
    }

    // Display mode management
    public void setDisplayMode(int mode) {
        currentMode = mode;

        int size;
        int margin;
        float textSize;

        switch (mode) {
            case MODE_MEDIUM:
                size = dpToPx(100);
                margin = dpToPx(16);
                textSize = 50;
                break;
            case MODE_LARGE:
                size = dpToPx(150);
                margin = dpToPx(24);
                textSize = 80;
                break;
            default: // MODE_SMALL
                size = dpToPx(80);
                margin = dpToPx(16);
                textSize = 40;
                break;
        }

        LayoutParams params = (LayoutParams) cardBuddy.getLayoutParams();
        params.width = size;
        params.height = size;
        params.setMargins(margin, 0, 0, dpToPx(80));
        cardBuddy.setLayoutParams(params);
        cardBuddy.setRadius(size / 2f);

        tvBuddyEmoji.setTextSize(textSize);
    }

    public void setPosition(int gravity, int marginLeft, int marginBottom) {
        LayoutParams params = (LayoutParams) cardBuddy.getLayoutParams();
        params.gravity = gravity;
        params.setMargins(dpToPx(marginLeft), 0, dpToPx(marginLeft), dpToPx(marginBottom));
        cardBuddy.setLayoutParams(params);

        // Update speech bubble position
        LayoutParams bubbleParams = (LayoutParams) layoutSpeechBubble.getLayoutParams();
        bubbleParams.gravity = gravity;
        bubbleParams.setMargins(dpToPx(marginLeft), 0, dpToPx(16),
            dpToPx((int)(marginBottom + params.height / getDensity() + 10)));
        layoutSpeechBubble.setLayoutParams(bubbleParams);
    }

    private float getDensity() {
        return getResources().getDisplayMetrics().density;
    }

    // Animation methods
    private void startIdleAnimation() {
        if (idleAnimator != null && idleAnimator.isRunning()) {
            return;
        }

        idleAnimator = ObjectAnimator.ofFloat(cardBuddy, "translationY", 0f, -15f, 0f);
        idleAnimator.setDuration(2000);
        idleAnimator.setRepeatCount(ValueAnimator.INFINITE);
        idleAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        idleAnimator.start();
    }

    private void stopIdleAnimation() {
        if (idleAnimator != null) {
            idleAnimator.cancel();
        }
    }

    private void animateBuddyTap() {
        AnimatorSet set = new AnimatorSet();

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(cardBuddy, "scaleX", 1f, 1.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(cardBuddy, "scaleY", 1f, 1.2f, 1f);
        ObjectAnimator rotate = ObjectAnimator.ofFloat(cardBuddy, "rotation", 0f, 15f, -15f, 0f);

        set.playTogether(scaleX, scaleY, rotate);
        set.setDuration(400);
        set.setInterpolator(new BounceInterpolator());
        set.start();
    }

    public void playHappyAnimation() {
        stopIdleAnimation();

        AnimatorSet set = new AnimatorSet();

        ObjectAnimator jump1 = ObjectAnimator.ofFloat(cardBuddy, "translationY", 0f, -50f);
        jump1.setDuration(150);

        ObjectAnimator jump2 = ObjectAnimator.ofFloat(cardBuddy, "translationY", -50f, 0f);
        jump2.setDuration(150);
        jump2.setInterpolator(new BounceInterpolator());

        ObjectAnimator rotate = ObjectAnimator.ofFloat(cardBuddy, "rotation", 0f, 360f);
        rotate.setDuration(300);

        AnimatorSet jumpSet = new AnimatorSet();
        jumpSet.playSequentially(jump1, jump2);

        set.playTogether(jumpSet, rotate);
        set.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                cardBuddy.setRotation(0f);
                startIdleAnimation();
            }
        });
        set.start();
    }

    public void playEncouragingAnimation() {
        AnimatorSet set = new AnimatorSet();

        ObjectAnimator nod1 = ObjectAnimator.ofFloat(cardBuddy, "rotation", 0f, -10f);
        nod1.setDuration(200);

        ObjectAnimator nod2 = ObjectAnimator.ofFloat(cardBuddy, "rotation", -10f, 10f);
        nod2.setDuration(200);

        ObjectAnimator nod3 = ObjectAnimator.ofFloat(cardBuddy, "rotation", 10f, 0f);
        nod3.setDuration(200);

        set.playSequentially(nod1, nod2, nod3);
        set.start();
    }

    public void playCelebrateAnimation() {
        stopIdleAnimation();

        AnimatorSet set = new AnimatorSet();

        // Multiple jumps
        for (int i = 0; i < 3; i++) {
            ObjectAnimator jump = ObjectAnimator.ofFloat(cardBuddy, "translationY", 0f, -60f, 0f);
            jump.setDuration(300);
            jump.setStartDelay(i * 300L);
            jump.setInterpolator(new BounceInterpolator());
            set.play(jump);
        }

        ObjectAnimator scale = ObjectAnimator.ofFloat(cardBuddy, "scaleX", 1f, 1.3f, 1f);
        scale.setDuration(900);

        ObjectAnimator scaleY = ObjectAnimator.ofFloat(cardBuddy, "scaleY", 1f, 1.3f, 1f);
        scaleY.setDuration(900);

        set.playTogether(scale, scaleY);
        set.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                startIdleAnimation();
            }
        });
        set.start();
    }

    public void playTravelingAnimation() {
        stopIdleAnimation();

        // Continuous flying motion
        ObjectAnimator fly = ObjectAnimator.ofFloat(cardBuddy, "translationY", 0f, -20f, 0f, -20f, 0f);
        fly.setDuration(1000);
        fly.setRepeatCount(ValueAnimator.INFINITE);
        fly.start();

        // Slight rotation to simulate flight
        ObjectAnimator tilt = ObjectAnimator.ofFloat(cardBuddy, "rotation", -5f, 5f, -5f);
        tilt.setDuration(500);
        tilt.setRepeatCount(ValueAnimator.INFINITE);
        tilt.start();
    }

    public void playThinkingAnimation() {
        ObjectAnimator think = ObjectAnimator.ofFloat(cardBuddy, "rotation", 0f, 15f, -15f, 0f);
        think.setDuration(1500);
        think.setRepeatCount(2);
        think.start();
    }

    // Speech bubble management
    public void showSpeech(String message) {
        tvSpeech.setText(message);
        layoutSpeechBubble.setVisibility(VISIBLE);

        // Animate in
        layoutSpeechBubble.setAlpha(0f);
        layoutSpeechBubble.setScaleX(0.5f);
        layoutSpeechBubble.setScaleY(0.5f);

        AnimatorSet set = new AnimatorSet();
        ObjectAnimator alpha = ObjectAnimator.ofFloat(layoutSpeechBubble, "alpha", 0f, 1f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(layoutSpeechBubble, "scaleX", 0.5f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(layoutSpeechBubble, "scaleY", 0.5f, 1f);

        set.playTogether(alpha, scaleX, scaleY);
        set.setDuration(300);
        set.setInterpolator(new DecelerateInterpolator());
        set.start();

        // Auto-hide after delay
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(this::hideSpeechBubble, 4000);
    }

    public void hideSpeechBubble() {
        AnimatorSet set = new AnimatorSet();
        ObjectAnimator alpha = ObjectAnimator.ofFloat(layoutSpeechBubble, "alpha", 1f, 0f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(layoutSpeechBubble, "scaleX", 1f, 0.5f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(layoutSpeechBubble, "scaleY", 1f, 0.5f);

        set.playTogether(alpha, scaleX, scaleY);
        set.setDuration(200);
        set.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                layoutSpeechBubble.setVisibility(GONE);
            }
        });
        set.start();
    }

    private void toggleExpanded() {
        isExpanded = !isExpanded;

        int targetSize = isExpanded ? dpToPx(120) : dpToPx(80);
        float targetTextSize = isExpanded ? 60 : 40;

        ValueAnimator sizeAnim = ValueAnimator.ofInt(cardBuddy.getWidth(), targetSize);
        sizeAnim.setDuration(300);
        sizeAnim.addUpdateListener(animation -> {
            int val = (int) animation.getAnimatedValue();
            LayoutParams params = (LayoutParams) cardBuddy.getLayoutParams();
            params.width = val;
            params.height = val;
            cardBuddy.setLayoutParams(params);
            cardBuddy.setRadius(val / 2f);
        });
        sizeAnim.start();

        ObjectAnimator textAnim = ObjectAnimator.ofFloat(tvBuddyEmoji, "textSize",
            tvBuddyEmoji.getTextSize() / getDensity(), targetTextSize);
        textAnim.setDuration(300);
        textAnim.start();
    }

    // BuddyEventListener implementation
    @Override
    public void onStateChanged(String newState, String previousState) {
        handler.post(() -> {
            switch (newState) {
                case BuddyState.STATE_HAPPY:
                    playHappyAnimation();
                    break;
                case BuddyState.STATE_ENCOURAGING:
                    playEncouragingAnimation();
                    break;
                case BuddyState.STATE_TRAVELING:
                    playTravelingAnimation();
                    break;
                case BuddyState.STATE_CELEBRATING:
                    playCelebrateAnimation();
                    break;
                case BuddyState.STATE_THINKING:
                    playThinkingAnimation();
                    break;
                case BuddyState.STATE_IDLE:
                default:
                    startIdleAnimation();
                    break;
            }
        });
    }

    @Override
    public void onBuddySpeak(String message) {
        handler.post(() -> showSpeech(message));
    }

    @Override
    public void onMoodChanged(int newMood) {
        // Could update buddy appearance based on mood
    }

    @Override
    public void onBuddyLevelUp(int newLevel) {
        handler.post(() -> {
            playCelebrateAnimation();
            showSpeech("Level UP! Level " + newLevel + "! 🎉");
        });
    }

    // Utility
    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    // Cleanup
    public void cleanup() {
        stopIdleAnimation();
        handler.removeCallbacksAndMessages(null);
        buddyManager.removeListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cleanup();
    }
}

