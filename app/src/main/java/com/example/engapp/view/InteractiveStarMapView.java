package com.example.engapp.view;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.engapp.manager.ProgressionManager;
import com.example.engapp.model.Planet;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Interactive zoomable/pannable star map view.
 * Displays planets as interactive nodes that can be tapped.
 */
public class InteractiveStarMapView extends View {

    // Map state
    private float scaleFactor = 1.0f;
    private float translateX = 0f;
    private float translateY = 0f;
    private float minScale = 0.5f;
    private float maxScale = 3.0f;

    // Gesture detectors
    private ScaleGestureDetector scaleDetector;
    private GestureDetector gestureDetector;

    // Drawing
    private Paint starPaint;
    private Paint nebulaPaint;
    private Paint planetPaint;
    private Paint planetGlowPaint;
    private Paint lockedPaint;
    private Paint textPaint;
    private Paint pathPaint;
    private Paint progressPaint;
    private Paint shipTextPaint;
    private Drawable shipDrawable;

    // Data
    private Context context;
    private List<PlanetNode> planetNodes;
    private List<Star> backgroundStars;
    private String currentPlanetId;
    private PlanetNode selectedPlanet;

    // Animation
    private float starTwinklePhase = 0f;
    private ObjectAnimator twinkleAnimator;
    private ValueAnimator shipAnimator;
    private float shipX;
    private float shipY;
    private float shipRadius;
    private boolean hasShipPosition;

    private static final String SHIP_EMOJI = "🚀";

    // Callbacks
    private OnPlanetSelectedListener planetSelectedListener;

    public interface OnPlanetSelectedListener {
        void onPlanetSelected(Planet planet, PlanetNode node);
        void onPlanetLongPressed(Planet planet, PlanetNode node);
    }

    // Planet node class
    public static class PlanetNode {
        public Planet planet;
        public float x, y;
        public float radius;
        public int baseColor;
        public int glowColor;
        public float orbitPhase;
        public boolean isUnlocked;
        public float unlockProgress;

        public PlanetNode(Planet planet, float x, float y, float radius, int color) {
            this.planet = planet;
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.baseColor = color;
            this.glowColor = adjustAlpha(color, 100);
            this.orbitPhase = (float) (Math.random() * Math.PI * 2);
            this.isUnlocked = planet.isUnlocked();
            this.unlockProgress = 0f;
        }

        private static int adjustAlpha(int color, int alpha) {
            return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
        }
    }

    // Background star class
    private static class Star {
        float x, y;
        float size;
        float brightness;
        float twinkleSpeed;
        float twinklePhase;

        Star(float x, float y) {
            this.x = x;
            this.y = y;
            this.size = (float) (Math.random() * 3 + 1);
            this.brightness = (float) (Math.random() * 0.5 + 0.5);
            this.twinkleSpeed = (float) (Math.random() * 2 + 1);
            this.twinklePhase = (float) (Math.random() * Math.PI * 2);
        }
    }

    public InteractiveStarMapView(Context context) {
        super(context);
        init(context);
    }

    public InteractiveStarMapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public InteractiveStarMapView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        planetNodes = new ArrayList<>();
        backgroundStars = new ArrayList<>();

        initPaints();
        initGestureDetectors(context);
        generateBackgroundStars();
        startTwinkleAnimation();
        shipDrawable = ContextCompat.getDrawable(context, com.example.engapp.R.drawable.ic_rocket);
    }

    private void initPaints() {
        starPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        starPaint.setColor(Color.WHITE);
        starPaint.setStyle(Paint.Style.FILL);

        nebulaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        nebulaPaint.setStyle(Paint.Style.FILL);

        planetPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        planetPaint.setStyle(Paint.Style.FILL);

        planetGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        planetGlowPaint.setStyle(Paint.Style.FILL);

        lockedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        lockedPaint.setColor(Color.parseColor("#80808080"));
        lockedPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(32f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        pathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pathPaint.setColor(Color.parseColor("#40FFFFFF"));
        pathPaint.setStyle(Paint.Style.STROKE);
        pathPaint.setStrokeWidth(3f);
        pathPaint.setPathEffect(new android.graphics.DashPathEffect(new float[]{10, 10}, 0));

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(6f);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);

        shipTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shipTextPaint.setColor(Color.WHITE);
        shipTextPaint.setTextAlign(Paint.Align.CENTER);
        float shipTextSize = 28f * getResources().getDisplayMetrics().scaledDensity;
        shipTextPaint.setTextSize(shipTextSize);
    }

    private void initGestureDetectors(Context context) {
        scaleDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float oldScale = scaleFactor;
                scaleFactor *= detector.getScaleFactor();
                scaleFactor = Math.max(minScale, Math.min(maxScale, scaleFactor));

                // Adjust translation to zoom toward the focus point
                float focusX = detector.getFocusX();
                float focusY = detector.getFocusY();
                translateX = focusX - (focusX - translateX) * (scaleFactor / oldScale);
                translateY = focusY - (focusY - translateY) * (scaleFactor / oldScale);

                invalidate();
                return true;
            }
        });

        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                translateX -= distanceX;
                translateY -= distanceY;
                invalidate();
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                handleTap(e.getX(), e.getY());
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                handleLongPress(e.getX(), e.getY());
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                // Double tap to zoom in/out
                if (scaleFactor < 1.5f) {
                    animateScaleTo(2.0f, e.getX(), e.getY());
                } else {
                    animateScaleTo(1.0f, e.getX(), e.getY());
                }
                return true;
            }
        });
    }

    private void generateBackgroundStars() {
        Random random = new Random();
        int starCount = 200;

        for (int i = 0; i < starCount; i++) {
            float x = random.nextFloat() * 3000 - 500; // Spread across larger area
            float y = random.nextFloat() * 3000 - 500;
            backgroundStars.add(new Star(x, y));
        }
    }

    private void startTwinkleAnimation() {
        twinkleAnimator = ObjectAnimator.ofFloat(this, "starTwinklePhase", 0f, (float)(Math.PI * 2));
        twinkleAnimator.setDuration(3000);
        twinkleAnimator.setRepeatCount(ValueAnimator.INFINITE);
        twinkleAnimator.setRepeatMode(ValueAnimator.RESTART);
        twinkleAnimator.start();
    }

    public void setStarTwinklePhase(float phase) {
        this.starTwinklePhase = phase;
        invalidate();
    }

    public float getStarTwinklePhase() {
        return starTwinklePhase;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean handled = scaleDetector.onTouchEvent(event);
        handled = gestureDetector.onTouchEvent(event) || handled;
        return handled || super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw space background
        canvas.drawColor(Color.parseColor("#0A0A1A"));

        canvas.save();
        canvas.translate(translateX, translateY);
        canvas.scale(scaleFactor, scaleFactor);

        // Draw background stars with twinkle effect
        drawBackgroundStars(canvas);

        // Draw nebulae
        drawNebulae(canvas);

        // Draw paths between planets
        drawPlanetPaths(canvas);

        // Draw planets
        drawPlanets(canvas);
        drawSpaceship(canvas);

        canvas.restore();
    }

    private void drawBackgroundStars(Canvas canvas) {
        for (Star star : backgroundStars) {
            float twinkle = (float) Math.sin(starTwinklePhase * star.twinkleSpeed + star.twinklePhase);
            float alpha = star.brightness * (0.7f + 0.3f * twinkle);

            starPaint.setAlpha((int)(alpha * 255));
            canvas.drawCircle(star.x, star.y, star.size, starPaint);
        }
    }

    private void drawNebulae(Canvas canvas) {
        // Draw some nebula clouds for atmosphere
        float[] nebulaPositions = {500, 300, 1200, 800, 800, 1500};
        int[] nebulaColors = {
            Color.parseColor("#20FF6B6B"),
            Color.parseColor("#204ECDC4"),
            Color.parseColor("#20A855F7")
        };

        for (int i = 0; i < nebulaPositions.length; i += 2) {
            float x = nebulaPositions[i];
            float y = nebulaPositions[i + 1];
            int color = nebulaColors[i / 2];

            RadialGradient gradient = new RadialGradient(
                x, y, 300,
                color, Color.TRANSPARENT,
                Shader.TileMode.CLAMP
            );
            nebulaPaint.setShader(gradient);
            canvas.drawCircle(x, y, 300, nebulaPaint);
        }
        nebulaPaint.setShader(null);
    }

    private void drawPlanetPaths(Canvas canvas) {
        // Draw enhanced constellation paths between planets
        for (int i = 0; i < planetNodes.size() - 1; i++) {
            PlanetNode current = planetNodes.get(i);
            PlanetNode next = planetNodes.get(i + 1);

            if (current.isUnlocked || next.isUnlocked) {
                Path path = new Path();
                path.moveTo(current.x, current.y);

                // Create smooth curved path (bezier)
                float midX = (current.x + next.x) / 2;
                float midY = (current.y + next.y) / 2 - 80;
                path.quadTo(midX, midY, next.x, next.y);

                // Enhanced path styling
                if (next.isUnlocked && current.isUnlocked) {
                    // Fully unlocked path - bright and animated
                    pathPaint.setColor(Color.parseColor("#80FFFFFF"));
                    pathPaint.setStrokeWidth(4f);
                    pathPaint.setPathEffect(new DashPathEffect(new float[]{20f, 10f}, 
                        starTwinklePhase * 50f)); // Animated dashes
                } else if (current.isUnlocked) {
                    // Partially unlocked - dimmer
                    pathPaint.setColor(Color.parseColor("#40FFFFFF"));
                    pathPaint.setStrokeWidth(3f);
                    pathPaint.setPathEffect(new DashPathEffect(new float[]{15f, 10f}, 0f));
                } else {
                    // Both locked - very dim
                    pathPaint.setColor(Color.parseColor("#20FFFFFF"));
                    pathPaint.setStrokeWidth(2f);
                    pathPaint.setPathEffect(new DashPathEffect(new float[]{10f, 10f}, 0f));
                }
                
                canvas.drawPath(path, pathPaint);
                
                // Draw glow effect for unlocked paths
                if (next.isUnlocked && current.isUnlocked) {
                    Paint glowPathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                    glowPathPaint.setStyle(Paint.Style.STROKE);
                    glowPathPaint.setColor(Color.parseColor("#20FFFFFF"));
                    glowPathPaint.setStrokeWidth(8f);
                    canvas.drawPath(path, glowPathPaint);
                }
            }
        }
    }

    private void drawPlanets(Canvas canvas) {
        for (PlanetNode node : planetNodes) {
            drawPlanet(canvas, node);
        }
    }

    private void drawPlanet(Canvas canvas, PlanetNode node) {
        float x = node.x;
        float y = node.y;
        float radius = node.radius;
        boolean isCurrent = node.planet.getId().equals(currentPlanetId);
        boolean isSelected = selectedPlanet == node;
        
        // Enhanced glow for unlocked planets
        if (node.isUnlocked) {
            // Multi-layer glow effect
            float[] glowRadii = {radius * 2.5f, radius * 2f, radius * 1.5f};
            int[] glowAlphas = {30, 60, 100};
            
            for (int i = 0; i < glowRadii.length; i++) {
                RadialGradient glowGradient = new RadialGradient(
                    x, y, glowRadii[i],
                    adjustAlpha(node.glowColor, glowAlphas[i]), Color.TRANSPARENT,
                    Shader.TileMode.CLAMP
                );
                planetGlowPaint.setShader(glowGradient);
                canvas.drawCircle(x, y, glowRadii[i], planetGlowPaint);
            }
            planetGlowPaint.setShader(null);
            
            // Pulse effect for current planet
            if (isCurrent) {
                float pulseScale = 1.0f + (float)(Math.sin(starTwinklePhase * 2) * 0.1f);
                radius *= pulseScale;
            }
        }

        // Draw progress ring for locked planets (outside planet)
        if (!node.isUnlocked && node.unlockProgress > 0) {
            float ringRadius = radius + 12f;
            RectF progressRect = new RectF(
                x - ringRadius, y - ringRadius,
                x + ringRadius, y + ringRadius
            );
            
            // Background ring (grey)
            progressPaint.setColor(Color.parseColor("#404040"));
            progressPaint.setStrokeWidth(6f);
            progressPaint.setStyle(Paint.Style.STROKE);
            canvas.drawArc(progressRect, -90, 360, false, progressPaint);
            
            // Progress ring (colored)
            progressPaint.setColor(node.baseColor);
            progressPaint.setAlpha(200);
            float sweepAngle = 360 * node.unlockProgress;
            canvas.drawArc(progressRect, -90, sweepAngle, false, progressPaint);
            
            // "Almost there!" pulse effect
            if (node.unlockProgress >= 0.8f) {
                float pulseAlpha = 100 + (float)(Math.sin(starTwinklePhase * 3) * 100);
                progressPaint.setAlpha((int)pulseAlpha);
                canvas.drawArc(progressRect, -90, sweepAngle, false, progressPaint);
            }
        }

        // Draw planet base
        if (node.isUnlocked) {
            // Enhanced gradient for 3D effect
            RadialGradient planetGradient = new RadialGradient(
                x - radius * 0.3f, y - radius * 0.3f, radius * 1.5f,
                lightenColor(node.baseColor), darkenColor(node.baseColor),
                Shader.TileMode.CLAMP
            );
            planetPaint.setShader(planetGradient);
            planetPaint.setColor(node.baseColor);
            canvas.drawCircle(x, y, radius, planetPaint);
            planetPaint.setShader(null);

            // Draw selection ring if selected (enhanced)
            if (isSelected) {
                Paint selectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                selectPaint.setColor(Color.WHITE);
                selectPaint.setStyle(Paint.Style.STROKE);
                selectPaint.setStrokeWidth(5f);
                selectPaint.setPathEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
                canvas.drawCircle(x, y, radius + 12, selectPaint);
            }

            // Draw current location indicator (enhanced)
            if (isCurrent) {
                // Gold ring with pulse
                Paint locationPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                locationPaint.setColor(Color.parseColor("#FFD700"));
                locationPaint.setStyle(Paint.Style.STROKE);
                locationPaint.setStrokeWidth(4f);
                float pulseRadius = radius + 18 + (float)(Math.sin(starTwinklePhase * 2) * 3);
                canvas.drawCircle(x, y, pulseRadius, locationPaint);
                
                // Outer glow ring
                locationPaint.setAlpha(100);
                locationPaint.setStrokeWidth(2f);
                canvas.drawCircle(x, y, pulseRadius + 5, locationPaint);
            }
        } else {
            // Draw locked planet (enhanced greyed out)
            lockedPaint.setColor(Color.parseColor("#2D3748"));
            lockedPaint.setAlpha(180);
            canvas.drawCircle(x, y, radius, lockedPaint);
            
            // Dark overlay
            Paint overlayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            overlayPaint.setColor(Color.parseColor("#80000000"));
            canvas.drawCircle(x, y, radius, overlayPaint);

            // Draw lock icon (larger, more visible)
            textPaint.setTextSize(radius * 0.4f);
            textPaint.setColor(Color.WHITE);
            textPaint.setAlpha(200);
            canvas.drawText("🔒", x, y + radius * 0.15f, textPaint);
        }

        // Draw planet emoji (enhanced)
        if (node.isUnlocked) {
            textPaint.setTextSize(radius * 0.9f);
            textPaint.setColor(Color.WHITE);
            textPaint.setAlpha(255);
            canvas.drawText(node.planet.getEmoji(), x, y + radius * 0.25f, textPaint);
        }

        // Draw planet name with better styling
        textPaint.setTextSize(22f);
        textPaint.setFakeBoldText(true);
        textPaint.setColor(node.isUnlocked ? Color.WHITE : Color.parseColor("#888888"));
        textPaint.setAlpha(node.isUnlocked ? 255 : 150);
        
        // Name with shadow for readability
        Paint shadowPaint = new Paint();
        shadowPaint.setTextSize(22f);
        shadowPaint.setColor(Color.BLACK);
        shadowPaint.setAlpha(100);
        shadowPaint.setStyle(Paint.Style.STROKE);
        shadowPaint.setStrokeWidth(3f);
        shadowPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(node.planet.getName(), x, y + radius + 35, shadowPaint);
        canvas.drawText(node.planet.getName(), x, y + radius + 35, textPaint);
        
        // Draw unlock requirement for locked planets
        if (!node.isUnlocked) {
            textPaint.setTextSize(18f);
            textPaint.setColor(Color.parseColor("#FFD700"));
            textPaint.setAlpha(255);
            textPaint.setFakeBoldText(false);
            
            // Get stars needed
            int starsNeeded = getStarsNeededForPlanet(node.planet);
            if (starsNeeded > 0) {
                String requirement = "⭐ " + starsNeeded;
                canvas.drawText(requirement, x, y + radius + 60, textPaint);
            }
        }
    }
    
    private int adjustAlpha(int color, int alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }
    
    
    private int getStarsNeededForPlanet(Planet planet) {
        // Use ProgressionManager to get the actual stars required
        if (context != null && planet != null) {
            ProgressionManager progressionManager = ProgressionManager.getInstance(context);
            return progressionManager.getStarsRequiredForPlanet(planet.getId());
        }
        return 15; // Default stars needed (updated from 20 to 15)
    }

    private void handleTap(float screenX, float screenY) {
        // Convert screen coordinates to map coordinates
        float mapX = (screenX - translateX) / scaleFactor;
        float mapY = (screenY - translateY) / scaleFactor;

        for (PlanetNode node : planetNodes) {
            float distance = (float) Math.sqrt(Math.pow(mapX - node.x, 2) + Math.pow(mapY - node.y, 2));
            if (distance <= node.radius * 1.5f) {
                selectedPlanet = node;
                invalidate();

                if (planetSelectedListener != null) {
                    planetSelectedListener.onPlanetSelected(node.planet, node);
                }
                return;
            }
        }

        // Deselect if tapped empty space
        selectedPlanet = null;
        invalidate();
    }

    private void handleLongPress(float screenX, float screenY) {
        float mapX = (screenX - translateX) / scaleFactor;
        float mapY = (screenY - translateY) / scaleFactor;

        for (PlanetNode node : planetNodes) {
            float distance = (float) Math.sqrt(Math.pow(mapX - node.x, 2) + Math.pow(mapY - node.y, 2));
            if (distance <= node.radius * 1.5f) {
                if (planetSelectedListener != null) {
                    planetSelectedListener.onPlanetLongPressed(node.planet, node);
                }
                return;
            }
        }
    }

    private void animateScaleTo(float targetScale, float focusX, float focusY) {
        ValueAnimator animator = ValueAnimator.ofFloat(scaleFactor, targetScale);
        animator.setDuration(300);
        animator.addUpdateListener(animation -> {
            float oldScale = scaleFactor;
            scaleFactor = (float) animation.getAnimatedValue();
            translateX = focusX - (focusX - translateX) * (scaleFactor / oldScale);
            translateY = focusY - (focusY - translateY) * (scaleFactor / oldScale);
            invalidate();
        });
        animator.start();
    }

    // Public methods
    public void setPlanets(List<Planet> planets, Context context) {
        planetNodes.clear();
        ProgressionManager progressionManager = ProgressionManager.getInstance(context);

        // Arrange planets in a spiral or grid pattern
        float startX = 400;
        float startY = 300;
        float spacingX = 350;
        float spacingY = 400;

        int[] planetColors = {
            Color.parseColor("#4ADE80"), // Green
            Color.parseColor("#F472B6"), // Pink
            Color.parseColor("#60A5FA"), // Blue
            Color.parseColor("#FBBF24"), // Yellow
            Color.parseColor("#A855F7"), // Purple
            Color.parseColor("#FB923C"), // Orange
            Color.parseColor("#34D399"), // Teal
            Color.parseColor("#F87171"), // Red
            Color.parseColor("#818CF8"), // Indigo
            Color.parseColor("#2DD4BF"), // Cyan
            Color.parseColor("#E879F9"), // Fuchsia
            Color.parseColor("#FCD34D"), // Amber
        };

        for (int i = 0; i < planets.size(); i++) {
            Planet planet = planets.get(i);

            // Calculate position (spiral layout)
            int row = i / 3;
            int col = i % 3;
            float x = startX + col * spacingX + (row % 2 == 1 ? spacingX / 2 : 0);
            float y = startY + row * spacingY;

            float radius = 60 + (i % 3) * 10;
            int color = planetColors[i % planetColors.length];

            PlanetNode node = new PlanetNode(planet, x, y, radius, color);
            node.isUnlocked = progressionManager.isPlanetUnlocked(planet.getId());
            node.unlockProgress = progressionManager.getPlanetUnlockProgress(planet.getId());

            planetNodes.add(node);
        }

        if (currentPlanetId != null) {
            PlanetNode currentNode = findNodeById(currentPlanetId);
            if (currentNode != null) {
                shipX = currentNode.x;
                shipY = currentNode.y;
                shipRadius = currentNode.radius;
                hasShipPosition = true;
            } else {
                hasShipPosition = false;
            }
        } else {
            hasShipPosition = false;
        }

        invalidate();
    }

    public void setCurrentPlanetId(String planetId) {
        if (planetId == null || planetId.isEmpty()) {
            currentPlanetId = planetId;
            hasShipPosition = false;
            invalidate();
            return;
        }
        this.currentPlanetId = planetId;
        PlanetNode targetNode = findNodeById(planetId);
        if (targetNode == null && planetId != null) {
            String normalized = ProgressionManager.getInstance(getContext())
                .normalizePlanetKey(planetId);
            if (normalized != null && !normalized.equals(planetId)) {
                this.currentPlanetId = normalized;
                targetNode = findNodeById(normalized);
            }
        }
        if (targetNode != null) {
            moveShipToNode(targetNode, hasShipPosition);
        } else {
            invalidate();
        }
    }

    private PlanetNode findNodeById(String planetId) {
        if (planetId == null) {
            return null;
        }
        for (PlanetNode node : planetNodes) {
            if (planetId.equals(node.planet.getId())) {
                return node;
            }
        }
        return null;
    }

    private void moveShipToNode(PlanetNode target, boolean animate) {
        if (target == null) {
            return;
        }
        if (!animate) {
            shipX = target.x;
            shipY = target.y;
            shipRadius = target.radius;
            hasShipPosition = true;
            invalidate();
            return;
        }
        if (shipAnimator != null) {
            shipAnimator.cancel();
        }

        float startX = shipX;
        float startY = shipY;
        float startRadius = shipRadius;
        float endX = target.x;
        float endY = target.y;
        float endRadius = target.radius;

        shipAnimator = ValueAnimator.ofFloat(0f, 1f);
        shipAnimator.setDuration(700);
        shipAnimator.setInterpolator(new DecelerateInterpolator());
        shipAnimator.addUpdateListener(animation -> {
            float fraction = (float) animation.getAnimatedValue();
            shipX = startX + (endX - startX) * fraction;
            shipY = startY + (endY - startY) * fraction;
            shipRadius = startRadius + (endRadius - startRadius) * fraction;
            hasShipPosition = true;
            invalidate();
        });
        shipAnimator.start();
    }

    public void setOnPlanetSelectedListener(OnPlanetSelectedListener listener) {
        this.planetSelectedListener = listener;
    }

    public void focusOnPlanet(String planetId) {
        for (PlanetNode node : planetNodes) {
            if (node.planet.getId().equals(planetId)) {
                // Animate to focus on this planet
                float targetTranslateX = getWidth() / 2f - node.x * scaleFactor;
                float targetTranslateY = getHeight() / 2f - node.y * scaleFactor;

                ValueAnimator animX = ValueAnimator.ofFloat(translateX, targetTranslateX);
                ValueAnimator animY = ValueAnimator.ofFloat(translateY, targetTranslateY);

                animX.setDuration(500);
                animY.setDuration(500);

                animX.addUpdateListener(a -> {
                    translateX = (float) a.getAnimatedValue();
                    invalidate();
                });

                animY.addUpdateListener(a -> {
                    translateY = (float) a.getAnimatedValue();
                    invalidate();
                });

                animX.start();
                animY.start();
                break;
            }
        }
    }

    public void refreshUnlockStatus(Context context) {
        ProgressionManager progressionManager = ProgressionManager.getInstance(context);

        for (PlanetNode node : planetNodes) {
            node.isUnlocked = progressionManager.isPlanetUnlocked(node.planet.getId());
            node.unlockProgress = progressionManager.getPlanetUnlockProgress(node.planet.getId());
        }

        invalidate();
    }

    private void drawSpaceship(Canvas canvas) {
        if (!hasShipPosition) {
            return;
        }
        float shipCenterX = shipX;
        float shipCenterY = shipY;
        boolean isTraveling = shipAnimator != null && shipAnimator.isRunning();
        if (!isTraveling) {
            shipCenterY = shipY - shipRadius - 18f;
        }
        float shipSize = Math.max(24f, Math.min(64f, shipRadius * 0.7f));

        if (shipDrawable != null) {
            int half = Math.round(shipSize / 2f);
            int left = Math.round(shipCenterX) - half;
            int top = Math.round(shipCenterY) - half;
            int right = left + Math.round(shipSize);
            int bottom = top + Math.round(shipSize);

            canvas.save();
            canvas.rotate(-45f, shipCenterX, shipCenterY);
            shipDrawable.setBounds(left, top, right, bottom);
            shipDrawable.draw(canvas);
            canvas.restore();
        } else {
            float textHeight = shipTextPaint.descent() - shipTextPaint.ascent();
            float baseline = shipCenterY + textHeight / 2f - shipTextPaint.descent();
            canvas.save();
            canvas.rotate(-45f, shipCenterX, shipCenterY);
            canvas.drawText(SHIP_EMOJI, shipCenterX, baseline, shipTextPaint);
            canvas.restore();
        }
    }

    // Color utility methods
    private int lightenColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] = Math.min(1f, hsv[2] * 1.3f);
        return Color.HSVToColor(hsv);
    }

    private int darkenColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.7f;
        return Color.HSVToColor(hsv);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (twinkleAnimator != null) {
            twinkleAnimator.cancel();
        }
        if (shipAnimator != null) {
            shipAnimator.cancel();
        }
    }
}

