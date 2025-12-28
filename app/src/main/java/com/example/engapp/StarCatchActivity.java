package com.example.engapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.engapp.database.GameDatabaseHelper;
import java.util.Random;

public class StarCatchActivity extends AppCompatActivity {

    private FrameLayout gameArea;
    private TextView tvScore;
    private TextView tvTime;
    private Button btnRestart;
    private ImageButton btnBack;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();
    private CountDownTimer timer;
    private boolean running = false;
    private int score = 0;

    private GameDatabaseHelper dbHelper;

    private final Runnable spawnRunnable = new Runnable() {
        @Override
        public void run() {
            if (!running) {
                return;
            }
            spawnStar();
            handler.postDelayed(this, 700);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_star_catch);

        dbHelper = GameDatabaseHelper.getInstance(this);
        initViews();
        setupListeners();

        gameArea.post(this::startGame);
    }

    private void initViews() {
        gameArea = findViewById(R.id.gameArea);
        tvScore = findViewById(R.id.tvScore);
        tvTime = findViewById(R.id.tvTime);
        btnRestart = findViewById(R.id.btnRestart);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupListeners() {
        btnRestart.setOnClickListener(v -> startGame());
        btnBack.setOnClickListener(v -> finish());
    }

    private void startGame() {
        stopGame();
        clearStars();
        score = 0;
        updateScore();
        startTimer(30);
        running = true;
        handler.post(spawnRunnable);
    }

    private void stopGame() {
        running = false;
        handler.removeCallbacks(spawnRunnable);
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void startTimer(int seconds) {
        tvTime.setText(seconds + "s");
        timer = new CountDownTimer(seconds * 1000L, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                int remaining = (int) (millisUntilFinished / 1000L);
                tvTime.setText(remaining + "s");
            }

            @Override
            public void onFinish() {
                tvTime.setText("0s");
                endGame();
            }
        };
        timer.start();
    }

    private void endGame() {
        stopGame();
        clearStars();
        dbHelper.addExperience(Math.max(1, score / 2));

        new AlertDialog.Builder(this)
            .setTitle("Hoàn thành!")
            .setMessage("Bạn bắt được " + score + " sao.")
            .setPositiveButton("Chơi lại", (d, w) -> startGame())
            .setNegativeButton("Thoát", (d, w) -> finish())
            .setCancelable(false)
            .show();
    }

    private void spawnStar() {
        int size = dpToPx(36);
        int width = gameArea.getWidth();
        int height = gameArea.getHeight();
        if (width <= size || height <= size) {
            return;
        }

        int x = random.nextInt(width - size);
        ImageView star = new ImageView(this);
        star.setImageResource(R.drawable.ic_star);
        star.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(size, size);
        lp.leftMargin = x;
        lp.topMargin = -size;
        star.setLayoutParams(lp);
        star.setClickable(true);
        star.setOnClickListener(v -> {
            if (!running) {
                return;
            }
            score++;
            updateScore();
            gameArea.removeView(star);
        });

        gameArea.addView(star);

        long duration = 2000 + random.nextInt(1200);
        ObjectAnimator animator = ObjectAnimator.ofFloat(star, "translationY", height + size);
        animator.setDuration(duration);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                gameArea.removeView(star);
            }
        });
        animator.start();
    }

    private void updateScore() {
        tvScore.setText("Score: " + score);
    }

    private void clearStars() {
        gameArea.removeAllViews();
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    protected void onDestroy() {
        stopGame();
        super.onDestroy();
    }
}
