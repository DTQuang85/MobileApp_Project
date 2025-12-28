package com.example.engapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.engapp.database.GameDatabaseHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ConstellationMemoryActivity extends AppCompatActivity {

    private ImageView[] stars = new ImageView[6];
    private TextView tvRound;
    private TextView tvScore;
    private TextView tvStatus;
    private Button btnReplay;
    private Button btnRestart;
    private ImageButton btnBack;

    private final List<Integer> sequence = new ArrayList<>();
    private final Random random = new Random();
    private final Handler handler = new Handler(Looper.getMainLooper());

    private int inputIndex = 0;
    private int score = 0;
    private boolean isShowing = false;

    private GameDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_constellation_memory);

        dbHelper = GameDatabaseHelper.getInstance(this);
        initViews();
        setupListeners();
        startNewGame();
    }

    private void initViews() {
        stars[0] = findViewById(R.id.star0);
        stars[1] = findViewById(R.id.star1);
        stars[2] = findViewById(R.id.star2);
        stars[3] = findViewById(R.id.star3);
        stars[4] = findViewById(R.id.star4);
        stars[5] = findViewById(R.id.star5);
        tvRound = findViewById(R.id.tvRound);
        tvScore = findViewById(R.id.tvScore);
        tvStatus = findViewById(R.id.tvStatus);
        btnReplay = findViewById(R.id.btnReplay);
        btnRestart = findViewById(R.id.btnRestart);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupListeners() {
        for (int i = 0; i < stars.length; i++) {
            final int index = i;
            stars[i].setOnClickListener(v -> handleStarClick(index));
        }

        btnReplay.setOnClickListener(v -> {
            if (!sequence.isEmpty()) {
                playSequence();
            }
        });

        btnRestart.setOnClickListener(v -> startNewGame());
        btnBack.setOnClickListener(v -> finish());
    }

    private void startNewGame() {
        handler.removeCallbacksAndMessages(null);
        sequence.clear();
        score = 0;
        inputIndex = 0;
        updateScore();
        nextRound();
    }

    private void nextRound() {
        sequence.add(random.nextInt(stars.length));
        inputIndex = 0;
        tvRound.setText("Vòng " + sequence.size());
        playSequence();
    }

    private void playSequence() {
        isShowing = true;
        tvStatus.setText("Quan sát các sao");

        int stepDelay = 600;
        for (int i = 0; i < sequence.size(); i++) {
            int starIndex = sequence.get(i);
            int startDelay = i * stepDelay;
            handler.postDelayed(() -> setStarActive(starIndex, true), startDelay);
            handler.postDelayed(() -> setStarActive(starIndex, false), startDelay + 350);
        }

        handler.postDelayed(() -> {
            isShowing = false;
            tvStatus.setText("Đến lượt bạn");
        }, sequence.size() * stepDelay + 100);
    }

    private void handleStarClick(int index) {
        if (isShowing || sequence.isEmpty()) {
            return;
        }

        if (sequence.get(inputIndex) == index) {
            flashStar(index);
            inputIndex++;
            if (inputIndex >= sequence.size()) {
                score++;
                dbHelper.addExperience(2);
                updateScore();
                handler.postDelayed(this::nextRound, 500);
            }
        } else {
            Toast.makeText(this, "Sai rồi, thử lại nhé!", Toast.LENGTH_SHORT).show();
            startNewGame();
        }
    }

    private void flashStar(int index) {
        setStarActive(index, true);
        handler.postDelayed(() -> setStarActive(index, false), 200);
    }

    private void setStarActive(int index, boolean active) {
        int resId = active ? R.drawable.bg_star_node_active : R.drawable.bg_star_node;
        stars[index].setBackgroundResource(resId);
    }

    private void updateScore() {
        tvScore.setText("Score: " + score);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
