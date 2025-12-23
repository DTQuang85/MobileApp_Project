package com.example.engapp.view;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Constellation Galaxy Map View - Redesigned for kids (ages 5-12)
 * Features:
 * - Vertical constellation path
 * - Large glowing galaxy nodes
 * - Progress rings
 * - Space decorations (twinkling stars, particles)
 * - Unlock animations
 * - Player ship indicator
 */
public class ConstellationGalaxyMapView extends View {

    // Galaxy nodes
    private List<GalaxyNode> galaxyNodes;
    private int currentGalaxyId = 1;
    private int userStars = 0;
    
    // Animation
    private ValueAnimator twinkleAnimator;
    private float twinklePhase = 0f;
    private ObjectAnimator pulseAnimator;
    private float pulseScale = 1.0f;
    private ValueAnimator unlockAnimator;
    private GalaxyNode unlockingNode = null;
    private float unlockProgress = 0f;
    
    // Decorations
    private List<Star> backgroundStars;
    private List<Particle> particles;
    private Random random;
    
    // Paints
    private Paint galaxyPaint, glowPaint, textPaint, pathPaint, progressPaint;
    private Paint starPaint, particlePaint, lockPaint;
    
    // Listener
    private OnGalaxyClickListener listener;
    
    // Constants
    private static final float NODE_RADIUS = 80f; // Large enough for kids
    private static final float GLOW_RADIUS = NODE_RADIUS + 40f;
    private static final float PROGRESS_RING_WIDTH = 8f;
    private static final int STAR_COUNT = 100;
    private static final int PARTICLE_COUNT = 20;
    
    public interface OnGalaxyClickListener {
        void onGalaxyClick(int galaxyId, String galaxyName, String galaxyEmoji);
        void onLockedGalaxyClick(int galaxyId, int starsRequired);
    }
    
    public ConstellationGalaxyMapView(Context context) {
        super(context);
        init();
    }
    
    public ConstellationGalaxyMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        random = new Random();
        galaxyNodes = new ArrayList<>();
        backgroundStars = new ArrayList<>();
        particles = new ArrayList<>();
        
        initPaints();
        generateBackgroundStars();
        generateParticles();
        startAnimations();
        
        setLayerType(LAYER_TYPE_HARDWARE, null);
    }
    
    private void initPaints() {
        // Galaxy paint
        galaxyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        galaxyPaint.setStyle(Paint.Style.FILL);
        
        // Glow paint
        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setStyle(Paint.Style.FILL);
        
        // Text paint
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(android.graphics.Typeface.create(
            android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD));
        
        // Path paint (constellation lines)
        pathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pathPaint.setStyle(Paint.Style.STROKE);
        pathPaint.setStrokeWidth(4f);
        pathPaint.setColor(Color.parseColor("#6366F1"));
        pathPaint.setPathEffect(new DashPathEffect(new float[]{15f, 10f}, 0f));
        
        // Progress ring paint
        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(PROGRESS_RING_WIDTH);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setColor(Color.parseColor("#FFD700")); // Gold
        
        // Star paint
        starPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        starPaint.setColor(Color.WHITE);
        
        // Particle paint
        particlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        particlePaint.setColor(Color.parseColor("#FFD700"));
        
        // Lock paint
        lockPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        lockPaint.setColor(Color.parseColor("#80000000"));
        lockPaint.setStyle(Paint.Style.FILL);
    }
    
    public void loadGalaxies(int stars, int currentGalaxyId) {
        int previousStars = this.userStars;
        this.userStars = stars;
        this.currentGalaxyId = currentGalaxyId;
        
        int width = getWidth() > 0 ? getWidth() : 1080;
        int height = getHeight() > 0 ? getHeight() : 1920;
        
        // Vertical constellation path (zig-zag for visual interest)
        float centerX = width * 0.5f;
        float spacing = height * 0.25f;
        float startY = height * 0.15f;
        
        // Check if any galaxy just unlocked
        boolean galaxy2Unlocked = stars >= 30 && previousStars < 30;
        boolean galaxy3Unlocked = stars >= 60 && previousStars < 60;
        
        galaxyNodes.clear();
        
        // Galaxy 1: Beginner (always unlocked)
        galaxyNodes.add(new GalaxyNode(
            1, "🌌", "Beginner Galaxy", "Thiên hà Khởi đầu",
            centerX, startY, 0, true, Color.parseColor("#6366F1"), 100
        ));
        
        // Galaxy 2: Explorer
        float x2 = centerX + (width * 0.15f); // Slight zig-zag
        galaxyNodes.add(new GalaxyNode(
            2, "🌠", "Explorer Galaxy", "Thiên hà Khám phá",
            x2, startY + spacing, 30, stars >= 30, Color.parseColor("#8B5CF6"), 0
        ));
        
        // Galaxy 3: Advanced
        float x3 = centerX - (width * 0.15f); // Zig-zag back
        galaxyNodes.add(new GalaxyNode(
            3, "✨", "Advanced Galaxy", "Thiên hà Nâng cao",
            x3, startY + spacing * 2, 60, stars >= 60, Color.parseColor("#EC4899"), 0
        ));
        
        // Calculate progress for each galaxy
        for (GalaxyNode node : galaxyNodes) {
            if (!node.isUnlocked && node.starsRequired > 0) {
                node.progress = Math.min(100, (int)((float)stars / node.starsRequired * 100));
            } else if (node.isUnlocked) {
                node.progress = 100;
            }
        }
        
        // Trigger unlock animation if needed
        if (galaxy2Unlocked) {
            triggerUnlockAnimation(galaxyNodes.get(1));
        } else if (galaxy3Unlocked) {
            triggerUnlockAnimation(galaxyNodes.get(2));
        }
        
        invalidate();
    }
    
    private void triggerUnlockAnimation(GalaxyNode node) {
        unlockingNode = node;
        unlockProgress = 0f;
        
        unlockAnimator = ValueAnimator.ofFloat(0f, 1f);
        unlockAnimator.setDuration(1500);
        unlockAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        unlockAnimator.addUpdateListener(animation -> {
            unlockProgress = (float) animation.getAnimatedValue();
            invalidate();
        });
        unlockAnimator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                unlockingNode = null;
                unlockProgress = 0f;
            }
        });
        unlockAnimator.start();
    }
    
    private void generateBackgroundStars() {
        backgroundStars.clear();
        for (int i = 0; i < STAR_COUNT; i++) {
            float x = random.nextFloat() * 2000 - 500;
            float y = random.nextFloat() * 3000 - 500;
            float size = 1 + random.nextFloat() * 2;
            float twinkleSpeed = 0.5f + random.nextFloat() * 1.5f;
            backgroundStars.add(new Star(x, y, size, twinkleSpeed));
        }
    }
    
    private void generateParticles() {
        particles.clear();
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            float x = random.nextFloat() * 2000 - 500;
            float y = random.nextFloat() * 3000 - 500;
            float vx = (random.nextFloat() - 0.5f) * 0.5f;
            float vy = (random.nextFloat() - 0.5f) * 0.5f;
            particles.add(new Particle(x, y, vx, vy));
        }
    }
    
    private void startAnimations() {
        // Twinkle animation
        twinkleAnimator = ValueAnimator.ofFloat(0f, (float)(Math.PI * 2));
        twinkleAnimator.setDuration(3000);
        twinkleAnimator.setRepeatCount(ValueAnimator.INFINITE);
        twinkleAnimator.addUpdateListener(animation -> {
            twinklePhase = (float) animation.getAnimatedValue();
            invalidate();
        });
        twinkleAnimator.start();
        
        // Pulse animation for current galaxy
        pulseAnimator = ObjectAnimator.ofFloat(this, "pulseScale", 1.0f, 1.15f);
        pulseAnimator.setDuration(1500);
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.setRepeatMode(ValueAnimator.REVERSE);
        pulseAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        pulseAnimator.start();
    }
    
    public void setPulseScale(float scale) {
        this.pulseScale = scale;
        invalidate();
    }
    
    public float getPulseScale() {
        return pulseScale;
    }
    
    public void setOnGalaxyClickListener(OnGalaxyClickListener listener) {
        this.listener = listener;
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        int width = getWidth();
        int height = getHeight();
        
        // Draw deep space background gradient
        drawSpaceBackground(canvas, width, height);
        
        // Draw twinkling stars
        drawBackgroundStars(canvas);
        
        // Draw constellation path (connecting lines)
        drawConstellationPath(canvas);
        
        // Draw particles
        drawParticles(canvas);
        
        // Draw galaxy nodes
        drawGalaxyNodes(canvas);
    }
    
    private void drawSpaceBackground(Canvas canvas, int width, int height) {
        Paint bgPaint = new Paint();
        RadialGradient gradient = new RadialGradient(
            width * 0.5f, height * 0.3f, height * 0.8f,
            new int[]{
                Color.parseColor("#0A0A1A"),
                Color.parseColor("#1A1A2E"),
                Color.parseColor("#0D1B2A")
            },
            null,
            Shader.TileMode.CLAMP
        );
        bgPaint.setShader(gradient);
        canvas.drawRect(0, 0, width, height, bgPaint);
    }
    
    private void drawBackgroundStars(Canvas canvas) {
        for (Star star : backgroundStars) {
            float alpha = 100 + (float)(Math.sin(twinklePhase * star.twinkleSpeed) * 100);
            starPaint.setAlpha((int)alpha);
            canvas.drawCircle(star.x, star.y, star.size, starPaint);
        }
    }
    
    private void drawConstellationPath(Canvas canvas) {
        for (int i = 0; i < galaxyNodes.size() - 1; i++) {
            GalaxyNode from = galaxyNodes.get(i);
            GalaxyNode to = galaxyNodes.get(i + 1);
            
            // Only draw path if at least one galaxy is unlocked
            if (from.isUnlocked || to.isUnlocked) {
                Path path = new Path();
                path.moveTo(from.x, from.y);
                path.lineTo(to.x, to.y);
                
                // Animate path if next galaxy is unlocked
                if (to.isUnlocked) {
                    pathPaint.setAlpha(200);
                    pathPaint.setColor(Color.parseColor("#6366F1"));
                } else {
                    pathPaint.setAlpha(80);
                    pathPaint.setColor(Color.parseColor("#4A5568"));
                }
                
                canvas.drawPath(path, pathPaint);
            }
        }
    }
    
    private void drawParticles(Canvas canvas) {
        for (Particle particle : particles) {
            particlePaint.setAlpha((int)(particle.alpha * 255));
            canvas.drawCircle(particle.x, particle.y, 2f, particlePaint);
        }
    }
    
    private void drawGalaxyNodes(Canvas canvas) {
        for (GalaxyNode node : galaxyNodes) {
            boolean isCurrent = node.id == currentGalaxyId;
            boolean isNextUnlock = !node.isUnlocked && 
                (node.id == currentGalaxyId + 1 || 
                 (currentGalaxyId == 1 && node.id == 2));
            
            // Draw glow effect
            drawGlow(canvas, node, isCurrent);
            
            // Draw progress ring (for locked galaxies)
            if (!node.isUnlocked && node.progress > 0) {
                drawProgressRing(canvas, node);
            }
            
            // Draw galaxy circle
            drawGalaxyCircle(canvas, node, isCurrent);
            
            // Draw lock overlay if locked
            if (!node.isUnlocked) {
                drawLockOverlay(canvas, node);
            }
            
            // Draw player ship on current galaxy
            if (isCurrent) {
                drawPlayerShip(canvas, node);
            }
            
            // Draw galaxy name and info
            drawGalaxyInfo(canvas, node, isNextUnlock);
        }
    }
    
    private void drawGlow(Canvas canvas, GalaxyNode node, boolean isCurrent) {
        if (!node.isUnlocked && !isCurrent) return;
        
        float glowRadius = GLOW_RADIUS * (isCurrent ? pulseScale : 1.0f);
        float[] radii = {glowRadius, glowRadius * 0.7f, glowRadius * 0.4f};
        int[] alphas = {30, 60, 100};
        
        for (int i = 0; i < radii.length; i++) {
            glowPaint.setColor(node.color);
            glowPaint.setAlpha(alphas[i]);
            canvas.drawCircle(node.x, node.y, radii[i], glowPaint);
        }
    }
    
    private void drawProgressRing(Canvas canvas, GalaxyNode node) {
        RectF rect = new RectF(
            node.x - NODE_RADIUS - PROGRESS_RING_WIDTH,
            node.y - NODE_RADIUS - PROGRESS_RING_WIDTH,
            node.x + NODE_RADIUS + PROGRESS_RING_WIDTH,
            node.y + NODE_RADIUS + PROGRESS_RING_WIDTH
        );
        
        float sweepAngle = (node.progress / 100f) * 360f;
        progressPaint.setAlpha(200);
        canvas.drawArc(rect, -90f, sweepAngle, false, progressPaint);
        
        // Draw "Almost there!" effect if close
        if (node.progress >= 80) {
            progressPaint.setAlpha(100 + (int)(Math.sin(twinklePhase * 3) * 100));
            canvas.drawArc(rect, -90f, sweepAngle, false, progressPaint);
        }
    }
    
    private void drawGalaxyCircle(Canvas canvas, GalaxyNode node, boolean isCurrent) {
        float radius = NODE_RADIUS * (isCurrent ? pulseScale : 1.0f);
        
        // Unlock animation effect
        if (unlockingNode != null && unlockingNode.id == node.id) {
            radius *= (1.0f + unlockProgress * 0.3f);
            float alpha = 150 + (int)(unlockProgress * 105);
            galaxyPaint.setColor(node.color);
            galaxyPaint.setAlpha((int)alpha);
        } else if (node.isUnlocked || isCurrent) {
            galaxyPaint.setColor(node.color);
            galaxyPaint.setAlpha(255);
        } else {
            galaxyPaint.setColor(Color.parseColor("#2D3748"));
            galaxyPaint.setAlpha(150);
        }
        
        canvas.drawCircle(node.x, node.y, radius, galaxyPaint);
        
        // Draw emoji with unlock animation
        textPaint.setTextSize(radius * 0.7f);
        if (unlockingNode != null && unlockingNode.id == node.id) {
            textPaint.setAlpha(120 + (int)(unlockProgress * 135));
        } else {
            textPaint.setAlpha(node.isUnlocked ? 255 : 120);
        }
        canvas.drawText(node.emoji, node.x, node.y + radius * 0.3f, textPaint);
        
        // Draw star particles during unlock
        if (unlockingNode != null && unlockingNode.id == node.id) {
            drawUnlockParticles(canvas, node);
        }
    }
    
    private void drawUnlockParticles(Canvas canvas, GalaxyNode node) {
        int particleCount = 12;
        for (int i = 0; i < particleCount; i++) {
            float angle = (float)(Math.PI * 2 * i / particleCount);
            float distance = NODE_RADIUS * 2 * unlockProgress;
            float px = node.x + (float)(Math.cos(angle) * distance);
            float py = node.y + (float)(Math.sin(angle) * distance);
            
            particlePaint.setAlpha((int)(255 * (1f - unlockProgress)));
            canvas.drawCircle(px, py, 4f, particlePaint);
        }
    }
    
    private void drawLockOverlay(Canvas canvas, GalaxyNode node) {
        // Fade out lock during unlock animation
        int alpha = 180;
        if (unlockingNode != null && unlockingNode.id == node.id) {
            alpha = (int)(180 * (1f - unlockProgress));
        }
        
        // Dark overlay
        lockPaint.setAlpha(alpha);
        canvas.drawCircle(node.x, node.y, NODE_RADIUS, lockPaint);
        
        // Lock icon
        textPaint.setTextSize(40f);
        textPaint.setAlpha(alpha);
        canvas.drawText("🔒", node.x, node.y, textPaint);
    }
    
    private void drawPlayerShip(Canvas canvas, GalaxyNode node) {
        // Draw small spaceship above galaxy
        Paint shipPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shipPaint.setColor(Color.parseColor("#4ECDC4"));
        shipPaint.setStyle(Paint.Style.FILL);
        
        float shipY = node.y - NODE_RADIUS - 30f;
        float shipSize = 20f;
        
        // Simple triangle ship
        Path shipPath = new Path();
        shipPath.moveTo(node.x, shipY - shipSize);
        shipPath.lineTo(node.x - shipSize * 0.7f, shipY + shipSize * 0.5f);
        shipPath.lineTo(node.x, shipY);
        shipPath.lineTo(node.x + shipSize * 0.7f, shipY + shipSize * 0.5f);
        shipPath.close();
        
        canvas.drawPath(shipPath, shipPaint);
        
        // Ship glow
        shipPaint.setAlpha(100);
        shipPaint.setStyle(Paint.Style.STROKE);
        shipPaint.setStrokeWidth(3f);
        canvas.drawPath(shipPath, shipPaint);
    }
    
    private void drawGalaxyInfo(Canvas canvas, GalaxyNode node, boolean isNextUnlock) {
        float textY = node.y + NODE_RADIUS + 50f;
        
        // Galaxy name (Vietnamese)
        textPaint.setTextSize(24f);
        textPaint.setColor(Color.WHITE);
        textPaint.setAlpha(255);
        canvas.drawText(node.nameVi, node.x, textY, textPaint);
        
        // Status text
        textY += 35f;
        textPaint.setTextSize(18f);
        
        if (node.isUnlocked) {
            textPaint.setColor(Color.parseColor("#4CAF50"));
            canvas.drawText("🔓 Đã mở khóa", node.x, textY, textPaint);
        } else {
            if (isNextUnlock && node.progress >= 80) {
                textPaint.setColor(Color.parseColor("#FFD700"));
                textPaint.setTextSize(20f);
                canvas.drawText("Sắp mở rồi! ⭐", node.x, textY, textPaint);
            } else {
                textPaint.setColor(Color.parseColor("#FFA500"));
                canvas.drawText("Cần " + node.starsRequired + " ⭐", node.x, textY, textPaint);
            }
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float touchX = event.getX();
            float touchY = event.getY();
            
            for (GalaxyNode node : galaxyNodes) {
                float distance = (float) Math.sqrt(
                    Math.pow(touchX - node.x, 2) + Math.pow(touchY - node.y, 2)
                );
                
                if (distance <= NODE_RADIUS + 20f) {
                    // Haptic feedback
                    performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
                    
                    if (node.isUnlocked && listener != null) {
                        // Scale animation
                        animateTap(node);
                        listener.onGalaxyClick(node.id, node.name, node.emoji);
                    } else if (!node.isUnlocked && listener != null) {
                        // Shake animation
                        animateShake();
                        listener.onLockedGalaxyClick(node.id, node.starsRequired);
                    }
                    return true;
                }
            }
        }
        return super.onTouchEvent(event);
    }
    
    private void animateTap(GalaxyNode node) {
        // Simple scale animation
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(this, "scaleX", 1.0f, 0.95f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(this, "scaleY", 1.0f, 0.95f, 1.0f);
        scaleX.setDuration(200);
        scaleY.setDuration(200);
        scaleX.start();
        scaleY.start();
    }
    
    private void animateShake() {
        // Shake animation for locked galaxies
        ObjectAnimator shakeX = ObjectAnimator.ofFloat(this, "translationX", 0f, -10f, 10f, -10f, 10f, 0f);
        shakeX.setDuration(300);
        shakeX.setInterpolator(new OvershootInterpolator());
        shakeX.start();
    }
    
    // Data classes
    private static class GalaxyNode {
        int id;
        String emoji;
        String name;
        String nameVi;
        float x, y;
        int starsRequired;
        boolean isUnlocked;
        int color;
        int progress; // 0-100
        
        GalaxyNode(int id, String emoji, String name, String nameVi,
                  float x, float y, int starsRequired, boolean isUnlocked,
                  int color, int progress) {
            this.id = id;
            this.emoji = emoji;
            this.name = name;
            this.nameVi = nameVi;
            this.x = x;
            this.y = y;
            this.starsRequired = starsRequired;
            this.isUnlocked = isUnlocked;
            this.color = color;
            this.progress = progress;
        }
    }
    
    private static class Star {
        float x, y, size, twinkleSpeed;
        
        Star(float x, float y, float size, float twinkleSpeed) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.twinkleSpeed = twinkleSpeed;
        }
    }
    
    private static class Particle {
        float x, y, vx, vy, alpha;
        
        Particle(float x, float y, float vx, float vy) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.alpha = 0.3f + (float)Math.random() * 0.7f;
        }
    }
}

