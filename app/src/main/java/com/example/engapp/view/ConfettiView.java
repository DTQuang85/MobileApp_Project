package com.example.engapp.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Custom view for confetti animations.
 * Used for victory celebrations with non-violent sparkles and stars.
 */
public class ConfettiView extends View {

    private static final int PARTICLE_COUNT = 50;
    private static final int[] COLORS = {
        0xFFFFD93D, // Gold
        0xFF7C3AED, // Purple
        0xFF2563EB, // Blue
        0xFF22C55E, // Green
        0xFFF97316  // Orange
    };

    private List<Particle> particles;
    private Paint paint;
    private Random random;
    private ValueAnimator animator;
    private boolean isAnimating = false;

    public ConfettiView(Context context) {
        super(context);
        init();
    }

    public ConfettiView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ConfettiView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        random = new Random();
        particles = new ArrayList<>();
    }

    public void start() {
        if (isAnimating) {
            return;
        }

        particles.clear();
        int width = getWidth();
        int height = getHeight();

        if (width == 0 || height == 0) {
            post(this::start);
            return;
        }

        // Create particles
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            Particle p = new Particle();
            p.x = random.nextFloat() * width;
            p.y = -random.nextFloat() * height * 0.5f; // Start above screen
            p.velocityX = (random.nextFloat() - 0.5f) * 4f;
            p.velocityY = random.nextFloat() * 8f + 4f;
            p.rotation = random.nextFloat() * 360f;
            p.rotationSpeed = (random.nextFloat() - 0.5f) * 10f;
            p.size = random.nextFloat() * 12f + 8f;
            p.color = COLORS[random.nextInt(COLORS.length)];
            p.alpha = 1f;
            particles.add(p);
        }

        isAnimating = true;
        setVisibility(VISIBLE);

        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(3000);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(animation -> {
            updateParticles();
            invalidate();
        });
        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                isAnimating = false;
                setVisibility(GONE);
            }
        });
        animator.start();
    }

    public void stop() {
        if (animator != null) {
            animator.cancel();
        }
        isAnimating = false;
        setVisibility(GONE);
    }

    // Alias methods for better API naming
    public void startConfetti() {
        start();
    }

    public void stopConfetti() {
        stop();
    }

    private void updateParticles() {
        int width = getWidth();
        int height = getHeight();

        for (Particle p : particles) {
            // Update position
            p.x += p.velocityX;
            p.y += p.velocityY;

            // Update rotation
            p.rotation += p.rotationSpeed;

            // Apply gravity
            p.velocityY += 0.3f;

            // Fade out near bottom
            if (p.y > height * 0.7f) {
                p.alpha = Math.max(0f, 1f - (p.y - height * 0.7f) / (height * 0.3f));
            }

            // Reset if off screen
            if (p.y > height + 50) {
                p.y = -50;
                p.x = random.nextFloat() * width;
                p.alpha = 1f;
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!isAnimating || particles.isEmpty()) {
            return;
        }

        for (Particle p : particles) {
            paint.setColor(p.color);
            paint.setAlpha((int) (255 * p.alpha));

            canvas.save();
            canvas.translate(p.x, p.y);
            canvas.rotate(p.rotation);

            // Draw star shape (simple)
            float[] points = new float[10];
            for (int i = 0; i < 5; i++) {
                double angle = Math.PI * 2 * i / 5 - Math.PI / 2;
                points[i * 2] = (float) (Math.cos(angle) * p.size);
                points[i * 2 + 1] = (float) (Math.sin(angle) * p.size);
            }

            // Draw as circle for simplicity (or use Path for star)
            canvas.drawCircle(0, 0, p.size, paint);

            canvas.restore();
        }
    }

    private static class Particle {
        float x, y;
        float velocityX, velocityY;
        float rotation, rotationSpeed;
        float size;
        int color;
        float alpha;
    }
}

