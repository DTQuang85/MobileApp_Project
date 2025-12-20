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
        lockPaint.setAlpha(180);

        galaxyNodes = new ArrayList<>();
    }

    public void loadGalaxies(int stars) {
        this.userStars = stars;
        galaxyNodes.clear();

        // Define galaxy positions and data
        // Galaxy 1: Beginner (0,0,0)
        galaxyNodes.add(new GalaxyNode(1, "🌌", "Beginner Galaxy",
            0.3f, 0.3f, 0, true, Color.parseColor("#6366F1")));

        // Galaxy 2: Explorer (30 stars)
        galaxyNodes.add(new GalaxyNode(2, "🌠", "Explorer Galaxy",
            0.5f, 0.5f, 30, stars >= 30, Color.parseColor("#8B5CF6")));

        // Galaxy 3: Advanced (60 stars)
        galaxyNodes.add(new GalaxyNode(3, "✨", "Advanced Galaxy",
            0.7f, 0.7f, 60, stars >= 60, Color.parseColor("#EC4899")));

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

        // Draw connection lines between galaxies
        for (int i = 0; i < galaxyNodes.size() - 1; i++) {
            GalaxyNode from = galaxyNodes.get(i);
            GalaxyNode to = galaxyNodes.get(i + 1);

            float startX = from.x * width;
            float startY = from.y * height;
            float endX = to.x * width;
            float endY = to.y * height;

            // Draw dashed line
            linePaint.setPathEffect(new android.graphics.DashPathEffect(new float[]{20, 10}, 0));
            canvas.drawLine(startX, startY, endX, endY, linePaint);
        }

        // Draw galaxy nodes
        for (GalaxyNode node : galaxyNodes) {
            float x = node.x * width;
            float y = node.y * height;
            float radius = 80;

            // Draw galaxy circle
            if (node.isUnlocked) {
                galaxyPaint.setColor(node.color);
            } else {
                galaxyPaint.setColor(Color.parseColor("#2D3748"));
            }
            canvas.drawCircle(x, y, radius, galaxyPaint);

            // Draw emoji
            if (node.isUnlocked) {
                textPaint.setAlpha(255);
            } else {
                textPaint.setAlpha(100);
            }
            canvas.drawText(node.emoji, x, y + 20, textPaint);

            // Draw lock if not unlocked
            if (!node.isUnlocked) {
                canvas.drawCircle(x, y, radius, lockPaint);
                textPaint.setTextSize(40);
                canvas.drawText("🔒", x, y + 15, textPaint);
                textPaint.setTextSize(60);

                // Draw required stars
                textPaint.setTextSize(30);
                canvas.drawText(node.starsRequired + "⭐", x, y + radius + 40, textPaint);
                textPaint.setTextSize(60);
            }

            // Draw galaxy name below
            textPaint.setTextSize(28);
            textPaint.setAlpha(255);
            canvas.drawText(node.name, x, y + radius + 80, textPaint);
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

                if (distance <= 80) {
                    if (node.isUnlocked && listener != null) {
                        listener.onGalaxyClick(node.id, node.name, node.emoji);
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

