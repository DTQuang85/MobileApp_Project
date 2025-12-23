package com.example.engapp.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * Custom animated health bar with gradient and glow effects.
 * Used for both player and enemy health display.
 */
public class HealthBarView extends View {

    private Paint backgroundPaint;
    private Paint healthPaint;
    private Paint glowPaint;
    private RectF backgroundRect;
    private RectF healthRect;

    private int maxHealth = 100;
    private int currentHealth = 100;
    private float animatedHealth = 100f;
    private boolean isPlayer = true;

    // Colors
    private int backgroundColor = 0xFF333333;
    private int healthColorHigh = 0xFF22C55E; // Green (>50%)
    private int healthColorMid = 0xFFFBBF24;   // Yellow (30-50%)
    private int healthColorLow = 0xFFEF4444;  // Red (<30%)

    private ValueAnimator healthAnimator;

    public HealthBarView(Context context) {
        super(context);
        init();
    }

    public HealthBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HealthBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Background paint
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(backgroundColor);
        backgroundPaint.setStyle(Paint.Style.FILL);

        // Health paint
        healthPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        healthPaint.setStyle(Paint.Style.FILL);

        // Glow paint (for high health)
        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setStyle(Paint.Style.STROKE);
        glowPaint.setStrokeWidth(4f);
        glowPaint.setColor(0x80FFFFFF);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        backgroundRect = new RectF(0, 0, w, h);
        updateHealthRect();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw background
        canvas.drawRoundRect(backgroundRect, getHeight() / 2f, getHeight() / 2f, backgroundPaint);

        // Draw health bar
        if (healthRect != null && healthRect.width() > 0) {
            canvas.drawRoundRect(healthRect, getHeight() / 2f, getHeight() / 2f, healthPaint);
        }

        // Draw glow effect for high health
        if (animatedHealth / maxHealth > 0.5f) {
            canvas.drawRoundRect(healthRect, getHeight() / 2f, getHeight() / 2f, glowPaint);
        }
    }

    private void updateHealthRect() {
        if (backgroundRect == null) return;

        float healthPercent = animatedHealth / maxHealth;
        float healthWidth = backgroundRect.width() * healthPercent;

        healthRect = new RectF(
            0,
            0,
            healthWidth,
            backgroundRect.height()
        );

        // Update health color based on percentage
        int healthColor;
        if (healthPercent > 0.5f) {
            healthColor = healthColorHigh;
        } else if (healthPercent > 0.3f) {
            healthColor = healthColorMid;
        } else {
            healthColor = healthColorLow;
        }

        // Create gradient
        LinearGradient gradient = new LinearGradient(
            0, 0, healthWidth, 0,
            healthColor,
            adjustBrightness(healthColor, 0.8f),
            Shader.TileMode.CLAMP
        );
        healthPaint.setShader(gradient);
    }

    private int adjustBrightness(int color, float factor) {
        int a = (color >> 24) & 0xFF;
        int r = Math.min(255, (int) (((color >> 16) & 0xFF) * factor));
        int g = Math.min(255, (int) (((color >> 8) & 0xFF) * factor));
        int b = Math.min(255, (int) ((color & 0xFF) * factor));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
        invalidate();
    }

    public void setHealth(int health, boolean animate) {
        this.currentHealth = Math.max(0, Math.min(health, maxHealth));

        if (healthAnimator != null) {
            healthAnimator.cancel();
        }

        if (animate) {
            healthAnimator = ValueAnimator.ofFloat(animatedHealth, currentHealth);
            healthAnimator.setDuration(600);
            healthAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            healthAnimator.addUpdateListener(animation -> {
                animatedHealth = (float) animation.getAnimatedValue();
                updateHealthRect();
                invalidate();
            });
            healthAnimator.start();
        } else {
            animatedHealth = currentHealth;
            updateHealthRect();
            invalidate();
        }
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void setIsPlayer(boolean isPlayer) {
        this.isPlayer = isPlayer;
        // Player uses green, enemy uses orange/red
        if (!isPlayer) {
            healthColorHigh = 0xFFF97316; // Orange for enemy
            healthColorMid = 0xFFFBBF24;  // Yellow
            healthColorLow = 0xFFEF4444; // Red
        }
        invalidate();
    }
}

