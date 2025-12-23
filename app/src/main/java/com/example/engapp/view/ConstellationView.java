package com.example.engapp.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.example.engapp.database.GameDatabaseHelper.WordData;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Custom View cho Word Constellation Game
 * Người chơi nối các điểm theo thứ tự để tạo thành chòm sao và học từ vựng
 */
public class ConstellationView extends View {

    private Paint dotPaint, linePaint, textPaint, glowPaint, backgroundStarPaint, wordLabelPaint;
    private List<StarPoint> starPoints;
    private List<Integer> connectedOrder;
    private int currentConnectingIndex = -1;
    private PointF currentTouchPoint;
    private OnConstellationCompleteListener listener;
    private int dotRadius = 50;
    private int glowRadius = 80;
    private boolean isCompleted = false;
    
    // Animation
    private ValueAnimator pulseAnimator;
    private float pulseScale = 1.0f;
    private List<BackgroundStar> backgroundStars;
    private float twinklePhase = 0f;
    private ValueAnimator twinkleAnimator;
    private Random random = new Random();
    
    // Particle effects
    private List<Particle> particles;
    private ValueAnimator particleAnimator;

    public interface OnConstellationCompleteListener {
        void onStarConnected(WordData word, int order);
        void onConstellationComplete();
    }

    private static class StarPoint {
        float x, y;
        WordData word;
        int order;
        boolean isConnected;
        boolean isHighlighted;
        float pulsePhase = 0f;
        float scale = 1.0f;

        StarPoint(float x, float y, WordData word, int order) {
            this.x = x;
            this.y = y;
            this.word = word;
            this.order = order;
            this.isConnected = false;
            this.isHighlighted = false;
        }
    }
    
    private static class BackgroundStar {
        float x, y;
        float size;
        float brightness;
        float twinkleSpeed;
        float twinklePhase;
        
        BackgroundStar(float x, float y, float size, float brightness) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.brightness = brightness;
            this.twinkleSpeed = (float) (Math.random() * 0.02 + 0.01);
            this.twinklePhase = (float) (Math.random() * Math.PI * 2);
        }
    }
    
    private static class Particle {
        float x, y;
        float velocityX, velocityY;
        float life = 1.0f;
        float size;
        int color;
    }

    public ConstellationView(Context context) {
        super(context);
        init();
    }

    public ConstellationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        starPoints = new ArrayList<>();
        connectedOrder = new ArrayList<>();
        backgroundStars = new ArrayList<>();
        particles = new ArrayList<>();

        // Paint for dots
        dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotPaint.setStyle(Paint.Style.FILL);

        // Paint for lines with gradient
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.parseColor("#60A5FA"));
        linePaint.setStrokeWidth(8);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeJoin(Paint.Join.ROUND);

        // Paint for glow effect
        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setStyle(Paint.Style.FILL);

        // Paint for background stars
        backgroundStarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundStarPaint.setColor(Color.WHITE);
        backgroundStarPaint.setStyle(Paint.Style.FILL);

        // Paint for text
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(36);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);

        // Paint for word labels
        wordLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        wordLabelPaint.setColor(Color.parseColor("#E0E7FF"));
        wordLabelPaint.setTextSize(24);
        wordLabelPaint.setTextAlign(Paint.Align.CENTER);
        wordLabelPaint.setFakeBoldText(true);

        setLayerType(LAYER_TYPE_HARDWARE, null);
        
        // Start animations
        startPulseAnimation();
        startTwinkleAnimation();
    }

    public void setWords(List<WordData> words) {
        starPoints.clear();
        connectedOrder.clear();
        isCompleted = false;
        currentConnectingIndex = -1;

        if (words == null || words.isEmpty()) return;

        // Tạo các điểm sao với vị trí ngẫu nhiên nhưng hợp lý
        int count = Math.min(words.size(), 6); // Tối đa 6 điểm để không quá phức tạp
        
        for (int i = 0; i < count; i++) {
            WordData word = words.get(i);
            // Vị trí sẽ được tính trong onSizeChanged
            starPoints.add(new StarPoint(0, 0, word, i));
        }

        requestLayout();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        // Generate background stars
        generateBackgroundStars(w, h);
        
        if (starPoints.isEmpty()) return;

        // Tính toán vị trí các điểm sao theo pattern constellation
        float centerX = w / 2f;
        float centerY = h / 2f;
        float radius = Math.min(w, h) * 0.35f;

        // Sắp xếp theo pattern hình sao hoặc vòng tròn
        int count = starPoints.size();
        for (int i = 0; i < count; i++) {
            StarPoint point = starPoints.get(i);
            double angle = (2 * Math.PI * i) / count - Math.PI / 2; // Bắt đầu từ trên
            
            // Thêm một chút random để tự nhiên hơn
            double randomOffset = (random.nextDouble() - 0.5) * 0.4;
            angle += randomOffset;
            
            point.x = centerX + (float) (radius * Math.cos(angle));
            point.y = centerY + (float) (radius * Math.sin(angle));
        }
    }
    
    private void generateBackgroundStars(int width, int height) {
        backgroundStars.clear();
        int starCount = 80;
        for (int i = 0; i < starCount; i++) {
            float x = random.nextFloat() * width;
            float y = random.nextFloat() * height;
            float size = random.nextFloat() * 3 + 1;
            float brightness = random.nextFloat() * 0.5f + 0.3f;
            backgroundStars.add(new BackgroundStar(x, y, size, brightness));
        }
    }
    
    private void startPulseAnimation() {
        pulseAnimator = ValueAnimator.ofFloat(0.9f, 1.1f);
        pulseAnimator.setDuration(2000);
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        pulseAnimator.addUpdateListener(animation -> {
            pulseScale = (float) animation.getAnimatedValue();
            invalidate();
        });
        pulseAnimator.start();
    }
    
    private void startTwinkleAnimation() {
        twinkleAnimator = ValueAnimator.ofFloat(0f, (float) (Math.PI * 2));
        twinkleAnimator.setDuration(3000);
        twinkleAnimator.setRepeatCount(ValueAnimator.INFINITE);
        twinkleAnimator.addUpdateListener(animation -> {
            twinklePhase = (float) animation.getAnimatedValue();
            invalidate();
        });
        twinkleAnimator.start();
    }

    public void setOnConstellationCompleteListener(OnConstellationCompleteListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        int width = getWidth();
        int height = getHeight();

        // Vẽ background stars
        drawBackgroundStars(canvas);

        // Vẽ các đường nối đã kết nối với gradient
        for (int i = 0; i < connectedOrder.size() - 1; i++) {
            int fromIndex = connectedOrder.get(i);
            int toIndex = connectedOrder.get(i + 1);
            
            StarPoint from = starPoints.get(fromIndex);
            StarPoint to = starPoints.get(toIndex);
            
            drawGradientLine(canvas, from.x, from.y, to.x, to.y);
        }

        // Vẽ đường nối đang kéo
        if (currentConnectingIndex >= 0 && currentTouchPoint != null && !connectedOrder.isEmpty()) {
            StarPoint from = starPoints.get(connectedOrder.get(connectedOrder.size() - 1));
            drawGradientLine(canvas, from.x, from.y, currentTouchPoint.x, currentTouchPoint.y);
        }

        // Vẽ particles
        drawParticles(canvas);

        // Vẽ các điểm sao
        for (int i = 0; i < starPoints.size(); i++) {
            StarPoint point = starPoints.get(i);
            drawStarPoint(canvas, point, i);
        }
    }
    
    private void drawBackgroundStars(Canvas canvas) {
        for (BackgroundStar star : backgroundStars) {
            float twinkle = (float) (0.5 + 0.5 * Math.sin(twinklePhase * star.twinkleSpeed + star.twinklePhase));
            int alpha = (int) (star.brightness * twinkle * 200);
            backgroundStarPaint.setAlpha(alpha);
            canvas.drawCircle(star.x, star.y, star.size, backgroundStarPaint);
        }
    }
    
    private void drawGradientLine(Canvas canvas, float x1, float y1, float x2, float y2) {
        // Create gradient line
        LinearGradient gradient = new LinearGradient(
            x1, y1, x2, y2,
            new int[]{Color.parseColor("#60A5FA"), Color.parseColor("#A78BFA"), Color.parseColor("#60A5FA")},
            null,
            Shader.TileMode.CLAMP
        );
        
        Paint gradientPaint = new Paint(linePaint);
        gradientPaint.setShader(gradient);
        
        // Draw glow
        Paint glowPaint = new Paint(gradientPaint);
        glowPaint.setStrokeWidth(16);
        glowPaint.setAlpha(60);
        canvas.drawLine(x1, y1, x2, y2, glowPaint);
        
        // Draw main line
        canvas.drawLine(x1, y1, x2, y2, gradientPaint);
    }
    
    private void drawStarPoint(Canvas canvas, StarPoint point, int index) {
        float currentRadius = dotRadius * point.scale * (point.isHighlighted ? pulseScale : 1.0f);
        
        // Vẽ outer glow nếu đang highlight
        if (point.isHighlighted) {
            RadialGradient glowGradient = new RadialGradient(
                point.x, point.y, glowRadius * 1.5f,
                new int[]{Color.parseColor("#8060A5FA"), Color.TRANSPARENT},
                null,
                Shader.TileMode.CLAMP
            );
            glowPaint.setShader(glowGradient);
            canvas.drawCircle(point.x, point.y, glowRadius * 1.5f, glowPaint);
            glowPaint.setShader(null);
        }
        
        // Vẽ inner glow
        if (point.isConnected || connectedOrder.contains(index)) {
            RadialGradient innerGlow = new RadialGradient(
                point.x, point.y, currentRadius * 1.2f,
                new int[]{Color.parseColor("#40FFFFFF"), Color.TRANSPARENT},
                null,
                Shader.TileMode.CLAMP
            );
            glowPaint.setShader(innerGlow);
            canvas.drawCircle(point.x, point.y, currentRadius * 1.2f, glowPaint);
            glowPaint.setShader(null);
        }

        // Vẽ dot với gradient
        RadialGradient dotGradient;
        if (point.isConnected) {
            dotGradient = new RadialGradient(
                point.x, point.y, currentRadius,
                new int[]{Color.parseColor("#10B981"), Color.parseColor("#059669")},
                null,
                Shader.TileMode.CLAMP
            );
        } else if (connectedOrder.contains(index)) {
            dotGradient = new RadialGradient(
                point.x, point.y, currentRadius,
                new int[]{Color.parseColor("#3B82F6"), Color.parseColor("#2563EB")},
                null,
                Shader.TileMode.CLAMP
            );
        } else {
            dotGradient = new RadialGradient(
                point.x, point.y, currentRadius,
                new int[]{Color.parseColor("#6B7280"), Color.parseColor("#4B5563")},
                null,
                Shader.TileMode.CLAMP
            );
        }
        dotPaint.setShader(dotGradient);
        canvas.drawCircle(point.x, point.y, currentRadius, dotPaint);
        dotPaint.setShader(null);

        // Vẽ border với glow
        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        if (point.isConnected || connectedOrder.contains(index)) {
            // Outer glow border (vẽ nhiều lớp để tạo glow effect)
            borderPaint.setColor(Color.WHITE);
            borderPaint.setAlpha(100);
            borderPaint.setStrokeWidth(12);
            canvas.drawCircle(point.x, point.y, currentRadius, borderPaint);
            
            borderPaint.setAlpha(150);
            borderPaint.setStrokeWidth(8);
            canvas.drawCircle(point.x, point.y, currentRadius, borderPaint);
        }
        borderPaint.setColor(point.isConnected ? Color.parseColor("#10B981") : 
                           connectedOrder.contains(index) ? Color.parseColor("#3B82F6") : 
                           Color.parseColor("#9CA3AF"));
        borderPaint.setAlpha(255);
        borderPaint.setStrokeWidth(5);
        canvas.drawCircle(point.x, point.y, currentRadius, borderPaint);

        // Vẽ emoji hoặc số
        if (point.isConnected) {
            textPaint.setTextSize(42);
            textPaint.setColor(Color.WHITE);
            canvas.drawText(point.word.emoji, point.x, point.y + 14, textPaint);
            
            // Vẽ từ vựng bên dưới
            wordLabelPaint.setColor(Color.parseColor("#E0E7FF"));
            canvas.drawText(point.word.english, point.x, point.y + currentRadius + 35, wordLabelPaint);
        } else {
            textPaint.setTextSize(32);
            textPaint.setColor(Color.WHITE);
            canvas.drawText(String.valueOf(point.order + 1), point.x, point.y + 12, textPaint);
        }
    }
    
    private void drawParticles(Canvas canvas) {
        for (Particle particle : particles) {
            if (particle.life > 0) {
                Paint particlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                particlePaint.setColor(particle.color);
                particlePaint.setAlpha((int) (particle.life * 255));
                canvas.drawCircle(particle.x, particle.y, particle.size, particlePaint);
            }
        }
    }
    
    private void createParticles(float x, float y) {
        int particleCount = 15;
        for (int i = 0; i < particleCount; i++) {
            Particle p = new Particle();
            p.x = x;
            p.y = y;
            float angle = (float) (Math.PI * 2 * i / particleCount);
            float speed = random.nextFloat() * 8 + 4;
            p.velocityX = (float) (Math.cos(angle) * speed);
            p.velocityY = (float) (Math.sin(angle) * speed);
            p.life = 1.0f;
            p.size = random.nextFloat() * 6 + 3;
            int[] colors = {Color.parseColor("#60A5FA"), Color.parseColor("#A78BFA"), Color.parseColor("#FCD34D")};
            p.color = colors[random.nextInt(colors.length)];
            particles.add(p);
        }
        
        if (particleAnimator != null) {
            particleAnimator.cancel();
        }
        
        particleAnimator = ValueAnimator.ofFloat(0f, 1f);
        particleAnimator.setDuration(800);
        particleAnimator.addUpdateListener(animation -> {
            float progress = (float) animation.getAnimatedValue();
            for (Particle p : particles) {
                p.x += p.velocityX * (1 - progress);
                p.y += p.velocityY * (1 - progress);
                p.life = 1.0f - progress;
            }
            invalidate();
        });
        particleAnimator.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isCompleted) return false;

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                handleTouchDown(x, y);
                return true;

            case MotionEvent.ACTION_MOVE:
                handleTouchMove(x, y);
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                handleTouchUp(x, y);
                return true;
        }

        return super.onTouchEvent(event);
    }

    private void handleTouchDown(float x, float y) {
        // Tìm điểm gần nhất
        int nearestIndex = findNearestPoint(x, y);
        
        if (nearestIndex >= 0) {
            StarPoint point = starPoints.get(nearestIndex);
            
            // Nếu là điểm đầu tiên
            if (connectedOrder.isEmpty()) {
                // Bắt đầu từ điểm này
                connectedOrder.add(nearestIndex);
                point.isConnected = true;
                point.isHighlighted = true;
                currentConnectingIndex = nearestIndex;
                
                // Animation
                animatePointConnect(point);
                createParticles(point.x, point.y);
                
                invalidate();
                
                if (listener != null) {
                    listener.onStarConnected(point.word, 0);
                }
            } else if (!connectedOrder.contains(nearestIndex)) {
                // Nối từ điểm cuối cùng đến điểm mới
                connectedOrder.add(nearestIndex);
                point.isConnected = true;
                point.isHighlighted = true;
                currentConnectingIndex = nearestIndex;
                
                // Animation
                animatePointConnect(point);
                createParticles(point.x, point.y);
                
                invalidate();
                
                if (listener != null) {
                    listener.onStarConnected(point.word, connectedOrder.size() - 1);
                }
                
                // Kiểm tra hoàn thành
                if (connectedOrder.size() >= starPoints.size()) {
                    completeConstellation();
                }
            }
        }
    }
    
    private void animatePointConnect(StarPoint point) {
        ValueAnimator scaleAnim = ValueAnimator.ofFloat(1.0f, 1.5f, 1.0f);
        scaleAnim.setDuration(400);
        scaleAnim.addUpdateListener(animation -> {
            point.scale = (float) animation.getAnimatedValue();
            invalidate();
        });
        scaleAnim.start();
    }

    private void handleTouchMove(float x, float y) {
        if (currentConnectingIndex >= 0) {
            currentTouchPoint = new PointF(x, y);
            invalidate();
        }
    }

    private void handleTouchUp(float x, float y) {
        currentTouchPoint = null;
        currentConnectingIndex = -1;
        
        // Bỏ highlight tất cả
        for (StarPoint point : starPoints) {
            point.isHighlighted = false;
        }
        
        invalidate();
    }

    private int findNearestPoint(float x, float y) {
        float minDistance = Float.MAX_VALUE;
        int nearestIndex = -1;
        float touchRadius = dotRadius * 2.5f; // Vùng chạm rộng hơn

        for (int i = 0; i < starPoints.size(); i++) {
            StarPoint point = starPoints.get(i);
            float distance = (float) Math.sqrt(
                Math.pow(x - point.x, 2) + Math.pow(y - point.y, 2)
            );

            if (distance < touchRadius && distance < minDistance) {
                minDistance = distance;
                nearestIndex = i;
            }
        }

        return nearestIndex;
    }

    private void completeConstellation() {
        isCompleted = true;
        
        // Animation: làm sáng tất cả các điểm với pulse effect
        for (StarPoint point : starPoints) {
            point.isHighlighted = true;
            createParticles(point.x, point.y);
        }
        
        // Celebration animation
        ValueAnimator celebrationAnim = ValueAnimator.ofFloat(1.0f, 1.3f, 1.0f);
        celebrationAnim.setDuration(600);
        celebrationAnim.setRepeatCount(2);
        celebrationAnim.addUpdateListener(animation -> {
            float scale = (float) animation.getAnimatedValue();
            for (StarPoint point : starPoints) {
                point.scale = scale;
            }
            invalidate();
        });
        celebrationAnim.start();
        
        invalidate();
        
        if (listener != null) {
            listener.onConstellationComplete();
        }
    }

    public void reset() {
        connectedOrder.clear();
        isCompleted = false;
        currentConnectingIndex = -1;
        currentTouchPoint = null;
        
        for (StarPoint point : starPoints) {
            point.isConnected = false;
            point.isHighlighted = false;
        }
        
        invalidate();
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public int getConnectedCount() {
        return connectedOrder.size();
    }

    public int getTotalCount() {
        return starPoints.size();
    }
}

