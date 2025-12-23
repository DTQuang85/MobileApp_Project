package com.example.engapp.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import java.util.Random;

/**
 * Custom view để vẽ tia sét từ Buddy/Player đến Enemy
 */
public class LightningBoltView extends View {

    private Paint lightningPaint;
    private Path lightningPath;
    private float startX, startY, endX, endY;
    private float progress = 0f;
    private ValueAnimator animator;
    private Random random = new Random();

    public LightningBoltView(Context context) {
        super(context);
        init();
    }

    public LightningBoltView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LightningBoltView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        lightningPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        lightningPaint.setColor(Color.parseColor("#FFD93D")); // Gold color
        lightningPaint.setStyle(Paint.Style.STROKE);
        lightningPaint.setStrokeWidth(8f);
        lightningPaint.setStrokeCap(Paint.Cap.ROUND);
        lightningPaint.setStrokeJoin(Paint.Join.ROUND);

        lightningPath = new Path();
        setBackgroundColor(Color.TRANSPARENT);
    }

    public void setStartPoint(float x, float y) {
        this.startX = x;
        this.startY = y;
    }

    public void setEndPoint(float x, float y) {
        this.endX = x;
        this.endY = y;
    }

    public void fire() {
        if (animator != null) {
            animator.cancel();
        }

        progress = 0f;
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(300);
        animator.addUpdateListener(animation -> {
            progress = (float) animation.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (startX == 0 && startY == 0) {
            // Default: từ bottom-left (Player/Buddy) đến top-center (Enemy)
            startX = getWidth() * 0.2f;
            startY = getHeight() * 0.8f;
            endX = getWidth() * 0.5f;
            endY = getHeight() * 0.2f;
        }

        if (progress <= 0) return;

        // Vẽ tia sét zigzag
        lightningPath.reset();
        lightningPath.moveTo(startX, startY);

        int segments = 8;
        float currentX = startX;
        float currentY = startY;
        float deltaX = (endX - startX) / segments;
        float deltaY = (endY - startY) / segments;

        for (int i = 1; i <= segments; i++) {
            float targetX = startX + deltaX * i;
            float targetY = startY + deltaY * i;

            // Thêm zigzag ngẫu nhiên
            if (i < segments) {
                targetX += (random.nextFloat() - 0.5f) * 30f * (1f - progress);
                targetY += (random.nextFloat() - 0.5f) * 20f * (1f - progress);
            }

            // Chỉ vẽ đến progress hiện tại
            if (i / (float) segments <= progress) {
                lightningPath.lineTo(targetX, targetY);
            } else {
                break;
            }
        }

        // Vẽ glow effect
        Paint glowPaint = new Paint(lightningPaint);
        glowPaint.setColor(Color.parseColor("#80FFD93D"));
        glowPaint.setStrokeWidth(16f);
        canvas.drawPath(lightningPath, glowPaint);

        // Vẽ tia sét chính
        canvas.drawPath(lightningPath, lightningPaint);

        // Vẽ sparkles
        if (progress > 0.5f) {
            Paint sparklePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            sparklePaint.setColor(Color.WHITE);
            float sparkleX = startX + (endX - startX) * progress;
            float sparkleY = startY + (endY - startY) * progress;
            canvas.drawCircle(sparkleX, sparkleY, 12f * progress, sparklePaint);
        }
    }

    public void stop() {
        if (animator != null) {
            animator.cancel();
        }
        progress = 0f;
        invalidate();
    }
}

