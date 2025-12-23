package com.example.engapp.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import com.example.engapp.database.GameDatabaseHelper;
import com.example.engapp.database.GameDatabaseHelper.PlanetData;
import com.example.engapp.manager.ProgressionManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom View hiển thị Planet Map với các hành tinh tròn và animation tàu vũ trụ
 */
public class PlanetMapView extends View {

    private Paint planetPaint, textPaint, linePaint, lockPaint, shipPaint;
    private List<PlanetNode> planetNodes;
    private OnPlanetClickListener listener;
    private GameDatabaseHelper dbHelper;

    // Spaceship animation
    private float shipX, shipY;
    private int currentPlanetIndex = 0;
    private ValueAnimator shipAnimator;

    public interface OnPlanetClickListener {
        void onPlanetClick(PlanetData planet);
    }

    public PlanetMapView(Context context) {
        super(context);
        init(context);
    }

    public PlanetMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        dbHelper = GameDatabaseHelper.getInstance(context);

        planetPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        planetPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(70);
        textPaint.setTextAlign(Paint.Align.CENTER);

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.parseColor("#4A5568"));
        linePaint.setStrokeWidth(5);
        linePaint.setStyle(Paint.Style.STROKE);

        lockPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        lockPaint.setColor(Color.parseColor("#1A202C"));
        lockPaint.setAlpha(180);

        shipPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shipPaint.setTextSize(50);
        shipPaint.setTextAlign(Paint.Align.CENTER);

        planetNodes = new ArrayList<>();
    }

    public void loadPlanets(int galaxyId, GameDatabaseHelper.UserProgressData progress) {
        planetNodes.clear();

        // Get planets for this galaxy
        List<PlanetData> allPlanets = dbHelper.getAllPlanets();

        // Filter planets by galaxy
        int startPlanet = (galaxyId - 1) * 3 + 1;
        int endPlanet = galaxyId * 3;

        // Define planet positions in a path
        float[][] positions = {
            {0.2f, 0.3f},  // Planet 1
            {0.5f, 0.2f},  // Planet 2
            {0.8f, 0.4f},  // Planet 3
        };

        int[] colors = {
            Color.parseColor("#FF6B6B"),
            Color.parseColor("#4ECDC4"),
            Color.parseColor("#45B7D1"),
        };

        int posIndex = 0;
        for (PlanetData planet : allPlanets) {
            if (planet.id >= startPlanet && planet.id <= endPlanet) {
                if (posIndex < positions.length) {
                    planetNodes.add(new PlanetNode(
                        planet,
                        positions[posIndex][0],
                        positions[posIndex][1],
                        colors[posIndex]
                    ));
                    posIndex++;
                }
            }
        }

        // Initialize ship position at first unlocked planet
        if (!planetNodes.isEmpty()) {
            for (int i = 0; i < planetNodes.size(); i++) {
                if (planetNodes.get(i).planet.isUnlocked) {
                    currentPlanetIndex = i;
                    shipX = planetNodes.get(i).x;
                    shipY = planetNodes.get(i).y;
                    break;
                }
            }
        }

        invalidate();
    }

    public void setOnPlanetClickListener(OnPlanetClickListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        // Draw connection lines
        for (int i = 0; i < planetNodes.size() - 1; i++) {
            PlanetNode from = planetNodes.get(i);
            PlanetNode to = planetNodes.get(i + 1);

            float startX = from.x * width;
            float startY = from.y * height;
            float endX = to.x * width;
            float endY = to.y * height;

            // Draw curved line
            Path path = new Path();
            path.moveTo(startX, startY);

            float controlX = (startX + endX) / 2;
            float controlY = Math.min(startY, endY) - 100;
            path.quadTo(controlX, controlY, endX, endY);

            linePaint.setPathEffect(new android.graphics.DashPathEffect(new float[]{15, 10}, 0));
            canvas.drawPath(path, linePaint);
        }

        // Draw planets
        for (int i = 0; i < planetNodes.size(); i++) {
            PlanetNode node = planetNodes.get(i);
            float x = node.x * width;
            float y = node.y * height;
            float radius = 90;

            // Draw planet circle with gradient effect
            if (node.planet.isUnlocked) {
                planetPaint.setColor(node.color);
            } else {
                planetPaint.setColor(Color.parseColor("#2D3748"));
            }

            // Draw outer glow
            planetPaint.setAlpha(50);
            canvas.drawCircle(x, y, radius + 20, planetPaint);
            planetPaint.setAlpha(255);

            // Draw planet
            canvas.drawCircle(x, y, radius, planetPaint);

            // Draw emoji
            if (node.planet.isUnlocked) {
                textPaint.setAlpha(255);
            } else {
                textPaint.setAlpha(100);
            }
            canvas.drawText(node.planet.emoji, x, y + 25, textPaint);

            // Draw lock overlay
            ProgressionManager progressionManager = ProgressionManager.getInstance(getContext());
            boolean isUnlocked = progressionManager.isPlanetUnlocked(node.planet.planetKey);
            
            if (!isUnlocked) {
                canvas.drawCircle(x, y, radius, lockPaint);
                textPaint.setTextSize(45);
                canvas.drawText("🔒", x, y + 15, textPaint);
                textPaint.setTextSize(70);

                // Draw stars requirement thay vì fuel cells
                int starsRequired = progressionManager.getStarsRequiredForPlanet(node.planet.planetKey);
                int currentStars = progressionManager.getTotalStars();
                int needed = Math.max(0, starsRequired - currentStars);
                textPaint.setTextSize(30);
                if (starsRequired == 0) {
                    canvas.drawText("⭐ Sẵn sàng!", x, y + radius + 40, textPaint);
                } else {
                    canvas.drawText("⭐ " + needed, x, y + radius + 40, textPaint);
                }
                textPaint.setTextSize(70);
            }

            // Draw planet name
            textPaint.setTextSize(32);
            textPaint.setAlpha(255);
            canvas.drawText(node.planet.nameVi, x, y + radius + 90, textPaint);
            textPaint.setTextSize(70);

            // Draw progress indicator
            if (node.planet.isUnlocked) {
                List<GameDatabaseHelper.SceneData> scenes = dbHelper.getScenesForPlanet(node.planet.id);
                int completed = 0;
                for (GameDatabaseHelper.SceneData scene : scenes) {
                    if (scene.isCompleted) completed++;
                }
                int progress = scenes.size() > 0 ? (completed * 100 / scenes.size()) : 0;

                textPaint.setTextSize(24);
                canvas.drawText(progress + "%", x, y - radius - 20, textPaint);
                textPaint.setTextSize(70);
            }
        }

        // Draw spaceship
        if (!planetNodes.isEmpty() && currentPlanetIndex >= 0 && currentPlanetIndex < planetNodes.size()) {
            float actualShipX = shipX * width;
            float actualShipY = shipY * height;

            shipPaint.setColor(Color.WHITE);
            canvas.drawText("🚀", actualShipX, actualShipY - 120, shipPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float touchX = event.getX();
            float touchY = event.getY();

            for (int i = 0; i < planetNodes.size(); i++) {
                PlanetNode node = planetNodes.get(i);
                float nodeX = node.x * getWidth();
                float nodeY = node.y * getHeight();
                float distance = (float) Math.sqrt(
                    Math.pow(touchX - nodeX, 2) + Math.pow(touchY - nodeY, 2)
                );

                if (distance <= 90) {
                    // Animate ship to this planet
                    animateShipToPlanet(i);

                    if (listener != null) {
                        listener.onPlanetClick(node.planet);
                    }
                    return true;
                }
            }
        }
        return super.onTouchEvent(event);
    }

    private void animateShipToPlanet(int targetIndex) {
        if (targetIndex < 0 || targetIndex >= planetNodes.size()) return;
        if (targetIndex == currentPlanetIndex) return;

        PlanetNode target = planetNodes.get(targetIndex);

        // Cancel previous animation
        if (shipAnimator != null && shipAnimator.isRunning()) {
            shipAnimator.cancel();
        }

        // Create ship movement animation
        shipAnimator = ValueAnimator.ofFloat(0f, 1f);
        shipAnimator.setDuration(800);
        shipAnimator.setInterpolator(new DecelerateInterpolator());

        float startX = shipX;
        float startY = shipY;
        float endX = target.x;
        float endY = target.y;

        shipAnimator.addUpdateListener(animation -> {
            float fraction = (float) animation.getAnimatedValue();
            shipX = startX + (endX - startX) * fraction;
            shipY = startY + (endY - startY) * fraction;
            invalidate();
        });

        shipAnimator.start();
        currentPlanetIndex = targetIndex;
    }

    private static class PlanetNode {
        PlanetData planet;
        float x, y; // Position as ratio (0-1)
        int color;

        PlanetNode(PlanetData planet, float x, float y, int color) {
            this.planet = planet;
            this.x = x;
            this.y = y;
            this.color = color;
        }
    }
}

