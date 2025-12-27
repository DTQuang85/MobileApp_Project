package com.example.engapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.engapp.database.GameDatabaseHelper;
import com.example.engapp.database.GameDatabaseHelper.WordData;
import com.example.engapp.manager.ProgressionManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SignalDecodeActivity extends AppCompatActivity {

    private TextView tvProgress;
    private TextView tvPrompt;
    private TextView tvEmoji;
    private TextView tvHint;
    private TextView tvFeedback;
    private TextView tvScore;
    private ProgressBar progressBar;
    private EditText etAnswer;
    private Button btnCheck;
    private Button btnSkip;
    private ImageView btnBack;

    private GameDatabaseHelper dbHelper;
    private ProgressionManager progressionManager;
    private List<WordData> words = new ArrayList<>();
    private int planetId;
    private int sceneId;
    private int currentIndex = 0;
    private int correctCount = 0;
    private int score = 0;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signal_decode);
        overridePendingTransition(R.anim.fade_scale_in, 0);

        planetId = getIntent().getIntExtra("planet_id", 1);
        sceneId = getIntent().getIntExtra("scene_id", 1);

        dbHelper = GameDatabaseHelper.getInstance(this);
        progressionManager = ProgressionManager.getInstance(this);

        initViews();
        loadWords();
        showCurrentSignal();
    }

    private void initViews() {
        tvProgress = findViewById(R.id.tvProgress);
        tvPrompt = findViewById(R.id.tvPrompt);
        tvEmoji = findViewById(R.id.tvEmoji);
        tvHint = findViewById(R.id.tvHint);
        tvFeedback = findViewById(R.id.tvFeedback);
        tvScore = findViewById(R.id.tvScore);
        progressBar = findViewById(R.id.progressBar);
        etAnswer = findViewById(R.id.etAnswer);
        btnCheck = findViewById(R.id.btnCheck);
        btnSkip = findViewById(R.id.btnSkip);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
        btnCheck.setOnClickListener(v -> checkAnswer());
        btnSkip.setOnClickListener(v -> skipSignal());
    }

    private void loadWords() {
        words = dbHelper.getWordsForPlanet(planetId);
        if (words == null || words.isEmpty()) {
            words = dbHelper.getWordsForPlanet(1);
        }
        if (words == null) {
            words = new ArrayList<>();
        }
        Collections.shuffle(words);
        if (words.size() > 5) {
            words = new ArrayList<>(words.subList(0, 5));
        }
    }

    private void showCurrentSignal() {
        if (currentIndex >= words.size()) {
            finishGame();
            return;
        }
        WordData word = words.get(currentIndex);
        tvPrompt.setText("Decode the signal");
        tvEmoji.setText(word.emoji != null ? word.emoji : "?");
        tvHint.setText(word.vietnamese != null ? word.vietnamese : "");
        tvFeedback.setText("");
        etAnswer.setText("");
        updateProgress();
        updateScore();
    }

    private void updateProgress() {
        int total = words.size();
        int progress = total == 0 ? 0 : ((currentIndex + 1) * 100) / total;
        progressBar.setProgress(progress);
        tvProgress.setText((currentIndex + 1) + "/" + total);
    }

    private void updateScore() {
        tvScore.setText("Score: " + score);
    }

    private void checkAnswer() {
        if (currentIndex >= words.size()) {
            return;
        }
        WordData word = words.get(currentIndex);
        String answer = etAnswer.getText().toString().trim().toLowerCase();
        String expected = word.english != null ? word.english.trim().toLowerCase() : "";

        if (answer.equals(expected)) {
            correctCount++;
            score += 20;
            tvFeedback.setText("Correct!");
        } else {
            tvFeedback.setText("Correct answer: " + (word.english != null ? word.english : ""));
        }

        updateScore();
        btnCheck.setEnabled(false);
        btnSkip.setEnabled(false);
        handler.postDelayed(() -> {
            btnCheck.setEnabled(true);
            btnSkip.setEnabled(true);
            currentIndex++;
            showCurrentSignal();
        }, 700);
    }

    private void skipSignal() {
        if (currentIndex >= words.size()) {
            return;
        }
        Toast.makeText(this, "Skipped", Toast.LENGTH_SHORT).show();
        currentIndex++;
        showCurrentSignal();
    }

    private void finishGame() {
        int total = words.size();
        int percent = total == 0 ? 0 : (correctCount * 100) / total;
        int stars = percent >= 80 ? 3 : percent >= 60 ? 2 : percent >= 40 ? 1 : 0;

        dbHelper.updateSceneProgress(sceneId, stars);
        dbHelper.addStars(stars);

        int starsEarned = stars * 10;
        progressionManager.recordGameCompleted("mini_game", starsEarned);
        if (planetId > 0 && sceneId > 0 && stars > 0) {
            progressionManager.recordLessonCompleted(planetId, sceneId, stars);
        }

        String message = "Score: " + score + "\nCorrect: " + correctCount + "/" + total;
        SpaceDialog.showResult(this, "!", "Signal Decoder", message, stars, "Continue", this::finish);
    }
}
