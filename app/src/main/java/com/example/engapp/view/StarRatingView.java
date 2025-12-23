package com.example.engapp.view;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

/**
 * Custom view to display star ratings with bounce animations.
 * Shows 1-3 stars based on battle performance.
 */
public class StarRatingView extends LinearLayout {

    private TextView[] starViews;
    private int rating = 0;

    public StarRatingView(Context context) {
        super(context);
        init(context);
    }

    public StarRatingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public StarRatingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setOrientation(HORIZONTAL);
        setGravity(android.view.Gravity.CENTER);

        starViews = new TextView[3];
        for (int i = 0; i < 3; i++) {
            TextView star = new TextView(context);
            star.setText("☆");
            star.setTextSize(40);
            star.setTextColor(0xFFFFD93D);
            star.setPadding(dpToPx(4), 0, dpToPx(4), 0);
            starViews[i] = star;
            addView(star);
        }
    }

    public void setRating(int stars) {
        this.rating = Math.max(0, Math.min(3, stars));

        // Update star display
        for (int i = 0; i < 3; i++) {
            if (i < rating) {
                starViews[i].setText("⭐");
            } else {
                starViews[i].setText("☆");
            }
        }

        // Animate stars
        animateStars();
    }

    private void animateStars() {
        AnimatorSet set = new AnimatorSet();
        AnimatorSet.Builder builder = null;

        for (int i = 0; i < rating; i++) {
            View star = starViews[i];
            star.setScaleX(0f);
            star.setScaleY(0f);
            star.setAlpha(0f);

            ObjectAnimator scaleX = ObjectAnimator.ofFloat(star, "scaleX", 0f, 1.2f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(star, "scaleY", 0f, 1.2f, 1f);
            ObjectAnimator alpha = ObjectAnimator.ofFloat(star, "alpha", 0f, 1f);

            AnimatorSet starAnim = new AnimatorSet();
            starAnim.playTogether(scaleX, scaleY, alpha);
            starAnim.setDuration(400);
            starAnim.setStartDelay(i * 150L);
            starAnim.setInterpolator(new BounceInterpolator());

            if (builder == null) {
                builder = set.play(starAnim);
            } else {
                builder.with(starAnim);
            }
        }

        if (builder != null) {
            set.start();
        }
    }

    public int getRating() {
        return rating;
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}

