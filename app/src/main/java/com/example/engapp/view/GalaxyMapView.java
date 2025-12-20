package com.example.engapp.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom View hiển thị Galaxy Map với các galaxy nodes
 */
public class GalaxyMapView extends View {

    private Paint galaxyPaint, textPaint, linePaint, lockPaint;
    private List<GalaxyNode> galaxyNodes;
    private OnGalaxyClickListener listener;
    private int userStars = 0;

    public interface OnGalaxyClickListener {
        void onGalaxyClick(int galaxyId, String galaxyName, String galaxyEmoji);
    }

    public GalaxyMapView(Context context) {
        super(context);
        init();
    }

    public GalaxyMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        galaxyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        galaxyPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(60);
        textPaint.setTextAlign(Paint.Align.CENTER);

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.parseColor("#4A5568"));
        linePaint.setStrokeWidth(4);
        linePaint.setStyle(Paint.Style.STROKE);

        lockPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        lockPaint.setColor(Color.parseColor("#1A202C"));
        lockPaint.setAlpha(200);

        galaxyNodes = new ArrayList<>();

        // Enable hardware acceleration for smooth animations
        setLayerType(LAYER_TYPE_HARDWARE, null);
    }

    public void loadGalaxies(int stars) {
        this.userStars = stars;
        galaxyNodes.clear();

        // Define galaxy positions in a more realistic arrangement
        // Center of screen with spiral/circular arrangement

        // Galaxy 1: Beginner - Center-left position
        galaxyNodes.add(new GalaxyNode(1, "🌌", "Beginner Galaxy",
            0.25f, 0.5f, 0, true, Color.parseColor("#6366F1")));

        // Galaxy 2: Explorer - Top-right position
        galaxyNodes.add(new GalaxyNode(2, "🌠", "Explorer Galaxy",
            0.65f, 0.3f, 30, stars >= 30, Color.parseColor("#8B5CF6")));

        // Galaxy 3: Advanced - Bottom-right position
        galaxyNodes.add(new GalaxyNode(3, "✨", "Advanced Galaxy",
            0.65f, 0.7f, 60, stars >= 60, Color.parseColor("#EC4899")));

        invalidate();
    }

    public void setOnGalaxyClickListener(OnGalaxyClickListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        // Draw random stars in background
        Paint starPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        starPaint.setColor(Color.WHITE);
        starPaint.setAlpha(150);
        for (int i = 0; i < 50; i++) {
            float starX = (i * 137.5f) % width;
            float starY = (i * 211.3f) % height;
            float starRadius = (i % 3) + 1;
            canvas.drawCircle(starX, starY, starRadius, starPaint);
        }

        // Draw connection lines between galaxies
        for (int i = 0; i < galaxyNodes.size() - 1; i++) {
            GalaxyNode from = galaxyNodes.get(i);
            GalaxyNode to = galaxyNodes.get(i + 1);

            if (to.isUnlocked || from.isUnlocked) {
                float startX = from.x * width;
                float startY = from.y * height;
                float endX = to.x * width;
                float endY = to.y * height;

                // Draw curved line with glow
                linePaint.setColor(Color.parseColor("#6366F1"));
                linePaint.setStrokeWidth(3);
                linePaint.setAlpha(to.isUnlocked ? 200 : 80);
                linePaint.setPathEffect(new android.graphics.DashPathEffect(new float[]{20, 10}, 0));
                canvas.drawLine(startX, startY, endX, endY, linePaint);
            }
        }

        // Draw galaxy nodes
        for (GalaxyNode node : galaxyNodes) {
            float x = node.x * width;
            float y = node.y * height;
            float radius = 100;

            // Draw glow effect for unlocked galaxies
            if (node.isUnlocked) {
                Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                glowPaint.setStyle(Paint.Style.FILL);
                glowPaint.setColor(node.color);

                // Outer glow
                glowPaint.setAlpha(30);
                canvas.drawCircle(x, y, radius + 30, glowPaint);
                glowPaint.setAlpha(60);
                canvas.drawCircle(x, y, radius + 15, glowPaint);

                // Main circle
                galaxyPaint.setColor(node.color);
                galaxyPaint.setAlpha(255);
            } else {
                galaxyPaint.setColor(Color.parseColor("#2D3748"));
                galaxyPaint.setAlpha(200);
            }
            canvas.drawCircle(x, y, radius, galaxyPaint);

            // Draw emoji
            textPaint.setTextSize(70);
            if (node.isUnlocked) {
                textPaint.setAlpha(255);
            } else {
                textPaint.setAlpha(100);
            }
            canvas.drawText(node.emoji, x, y + 25, textPaint);

            // Draw lock if not unlocked
            if (!node.isUnlocked) {
                canvas.drawCircle(x, y, radius, lockPaint);
                textPaint.setTextSize(50);
                canvas.drawText("🔒", x, y + 20, textPaint);

                // Draw required stars
                textPaint.setTextSize(32);
                textPaint.setAlpha(255);
                canvas.drawText(node.starsRequired + " ⭐", x, y + radius + 50, textPaint);
            }

            // Draw galaxy name below
            textPaint.setTextSize(26);
            textPaint.setAlpha(255);
            textPaint.setColor(Color.WHITE);
            canvas.drawText(node.name, x, y + radius + 90, textPaint);

            // Reset text size
            textPaint.setTextSize(60);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float touchX = event.getX();
            float touchY = event.getY();

            for (GalaxyNode node : galaxyNodes) {
                float nodeX = node.x * getWidth();
                float nodeY = node.y * getHeight();
                float distance = (float) Math.sqrt(
                    Math.pow(touchX - nodeX, 2) + Math.pow(touchY - nodeY, 2)
                );

                // Match the radius used in onDraw (100)
                if (distance <= 100) {
                    if (node.isUnlocked && listener != null) {
                        listener.onGalaxyClick(node.id, node.name, node.emoji);
                    } else if (!node.isUnlocked) {
                        // Show toast about stars required
                        android.widget.Toast.makeText(getContext(),
                            "Cần " + node.starsRequired + " ⭐ để mở khóa!",
                            android.widget.Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            }
        }
        return super.onTouchEvent(event);
    }

    private static class GalaxyNode {
        int id;
        String emoji;
        String name;
        float x, y; // Position as ratio (0-1)
        int starsRequired;
        boolean isUnlocked;
        int color;

        GalaxyNode(int id, String emoji, String name, float x, float y,
                   int starsRequired, boolean isUnlocked, int color) {
            this.id = id;
            this.emoji = emoji;
            this.name = name;
            this.x = x;
            this.y = y;
            this.starsRequired = starsRequired;
            this.isUnlocked = isUnlocked;
            this.color = color;
        }
    }
}

