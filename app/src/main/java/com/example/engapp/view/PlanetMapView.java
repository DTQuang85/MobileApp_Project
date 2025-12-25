package com.example.engapp.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import androidx.core.content.ContextCompat;
import com.example.engapp.database.GameDatabaseHelper;
import com.example.engapp.database.GameDatabaseHelper.PlanetData;
import com.example.engapp.manager.ProgressionManager;
import com.example.engapp.manager.TravelManager;
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
    private Drawable rocketDrawable;

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

        rocketDrawable = ContextCompat.getDrawable(context, com.example.engapp.R.drawable.ic_rocket);

        planetNodes = new ArrayList<>();
    }

    public void loadPlanets(int galaxyId, GameDatabaseHelper.UserProgressData progress) {
        planetNodes.clear();

        List<PlanetData> planets = dbHelper.getPlanetsForGalaxy(galaxyId);
        float[][] positions = buildPlanetPositions(planets.size());

        int[] colors = {
            Color.parseColor("#FF6B6B"),
            Color.parseColor("#4ECDC4"),
            Color.parseColor("#45B7D1"),
        };

        ProgressionManager progressionManager = ProgressionManager.getInstance(getContext());
        int posIndex = 0;
        for (PlanetData planet : planets) {
            if (posIndex < positions.length) {
                planet.isUnlocked = progressionManager.isPlanetUnlocked(planet.planetKey);
                planetNodes.add(new PlanetNode(
                    planet,
                    positions[posIndex][0],
                    positions[posIndex][1],
                    colors[posIndex % colors.length]
                ));
                posIndex++;
            }
        }

        // Initialize ship position at first unlocked planet
        if (!planetNodes.isEmpty()) {
            String currentPlanetKey = TravelManager.getInstance(getContext()).getCurrentPlanetId();
            for (int i = 0; i < planetNodes.size(); i++) {
                PlanetNode node = planetNodes.get(i);
                if (currentPlanetKey != null && currentPlanetKey.equals(node.planet.planetKey)
                    && node.planet.isUnlocked) {
                    currentPlanetIndex = i;
                    shipX = node.x;
                    shipY = node.y;
                    break;
                }
                if (node.planet.isUnlocked) {
                    currentPlanetIndex = i;
                    shipX = node.x;
                    shipY = node.y;
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

            ProgressionManager progressionManager = ProgressionManager.getInstance(getContext());
            boolean isUnlocked = progressionManager.isPlanetUnlocked(node.planet.planetKey);

            // Draw planet circle with gradient effect
            if (isUnlocked) {
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
            if (isUnlocked) {
                textPaint.setAlpha(255);
            } else {
                textPaint.setAlpha(100);
            }
            canvas.drawText(node.planet.emoji, x, y + 25, textPaint);

            // Draw lock overlay
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
            if (isUnlocked) {
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

            int size = (int) (getResources().getDisplayMetrics().density * 36);
            int left = (int) (actualShipX - size / 2f);
            int top = (int) (actualShipY - size - 80);
            if (rocketDrawable != null) {
                rocketDrawable.setBounds(left, top, left + size, top + size);
                rocketDrawable.draw(canvas);
            } else {
                Paint fallbackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                fallbackPaint.setColor(Color.parseColor("#FFD54F"));
                float shipSize = 12f * getResources().getDisplayMetrics().density;
                Path shipPath = new Path();
                shipPath.moveTo(actualShipX, actualShipY - 120 - shipSize);
                shipPath.lineTo(actualShipX - shipSize * 0.6f, actualShipY - 120 + shipSize * 0.6f);
                shipPath.lineTo(actualShipX + shipSize * 0.6f, actualShipY - 120 + shipSize * 0.6f);
                shipPath.close();
                canvas.drawPath(shipPath, fallbackPaint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float touchX = event.getX();
            float touchY = event.getY();
            ProgressionManager progressionManager = ProgressionManager.getInstance(getContext());

            for (int i = 0; i < planetNodes.size(); i++) {
                PlanetNode node = planetNodes.get(i);
                float nodeX = node.x * getWidth();
                float nodeY = node.y * getHeight();
                float distance = (float) Math.sqrt(
                    Math.pow(touchX - nodeX, 2) + Math.pow(touchY - nodeY, 2)
                );

                if (distance <= 90) {
                    if (!progressionManager.isPlanetUnlocked(node.planet.planetKey)) {
                        return true;
                    }
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

        TravelManager travelManager = TravelManager.getInstance(getContext());
        travelManager.setCurrentPlanetId(target.planet.planetKey);
        currentPlanetIndex = targetIndex;

        shipAnimator.addUpdateListener(animation -> {
            float fraction = (float) animation.getAnimatedValue();
            shipX = startX + (endX - startX) * fraction;
            shipY = startY + (endY - startY) * fraction;
            invalidate();
        });

        shipAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                shipX = endX;
                shipY = endY;
                invalidate();
            }
        });

        shipAnimator.start();
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

    private float[][] buildPlanetPositions(int count) {
        if (count <= 0) {
            return new float[0][0];
        }
        float[][] positions = new float[count][2];
        if (count == 1) {
            positions[0][0] = 0.5f;
            positions[0][1] = 0.35f;
            return positions;
        }

        float startY = 0.2f;
        float endY = 0.75f;
        float stepY = (endY - startY) / (count - 1);
        for (int i = 0; i < count; i++) {
            float y = startY + stepY * i;
            float wave = (float) Math.sin(i * Math.PI / 2f);
            float x = 0.5f + 0.3f * wave;
            positions[i][0] = Math.max(0.15f, Math.min(0.85f, x));
            positions[i][1] = y;
        }
        return positions;
    }
}


