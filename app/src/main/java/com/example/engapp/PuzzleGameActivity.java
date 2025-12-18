package com.example.engapp;

import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.DragEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.engapp.database.GameDatabaseHelper;
import com.example.engapp.database.GameDatabaseHelper.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class PuzzleGameActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private TextView tvQuestion, tvProgress, tvScore;
    private LinearLayout wordContainer, answerContainer;
    private ProgressBar progressBar;
    private ImageView btnBack;

    private TextToSpeech tts;
    private GameDatabaseHelper dbHelper;
    private int planetId, sceneId;
    private List<SentenceData> sentences;
    private int currentIndex = 0;
    private int score = 0;
    private List<String> currentWords = new ArrayList<>();
    private List<String> selectedWords = new ArrayList<>();
    private String correctSentence = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle_game);
        overridePendingTransition(R.anim.fade_scale_in, 0);

        planetId = getIntent().getIntExtra("planet_id", 1);
        sceneId = getIntent().getIntExtra("scene_id", 1);

        dbHelper = GameDatabaseHelper.getInstance(this);
        tts = new TextToSpeech(this, this);

        initViews();
        loadSentences();
        showCurrentPuzzle();
    }

    private void initViews() {
        tvQuestion = findViewById(R.id.tvQuestion);
        tvProgress = findViewById(R.id.tvProgress);
        tvScore = findViewById(R.id.tvScore);
        wordContainer = findViewById(R.id.wordContainer);
        answerContainer = findViewById(R.id.answerContainer);
        progressBar = findViewById(R.id.progressBar);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
    }

    private void loadSentences() {
        sentences = dbHelper.getSentencesForPlanet(planetId);
        if (sentences == null || sentences.isEmpty()) {
            Toast.makeText(this, "Không có câu đố", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Collections.shuffle(sentences);
        if (sentences.size() > 5) {
            sentences = sentences.subList(0, 5);
        }
    }

    private void showCurrentPuzzle() {
        if (currentIndex >= sentences.size()) {
            completeScene();
            return;
        }

        SentenceData sentence = sentences.get(currentIndex);
        correctSentence = sentence.english;

        tvQuestion.setText("📝 Sắp xếp thành câu:\n\"" + sentence.vietnamese + "\"");

        // Split sentence into words
        currentWords = new ArrayList<>(Arrays.asList(sentence.english.split(" ")));
        Collections.shuffle(currentWords);

        selectedWords.clear();

        setupWordButtons();
        updateAnswerDisplay();
        updateProgress();
    }

    private void setupWordButtons() {
        wordContainer.removeAllViews();

        for (int i = 0; i < currentWords.size(); i++) {
            final String word = currentWords.get(i);
            final int index = i;

            TextView wordBtn = new TextView(this);
            wordBtn.setText(word);
            wordBtn.setTextColor(getColor(R.color.text_white));
            wordBtn.setTextSize(16);
            wordBtn.setBackgroundResource(R.drawable.bg_word_button);
            wordBtn.setPadding(24, 16, 24, 16);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 8, 8, 8);
            wordBtn.setLayoutParams(params);

            wordBtn.setOnClickListener(v -> {
                if (!selectedWords.contains(word)) {
                    selectedWords.add(word);
                    wordBtn.setAlpha(0.3f);
                    wordBtn.setClickable(false);
                    updateAnswerDisplay();
                    checkAnswer();
                }
            });

            wordContainer.addView(wordBtn);
        }
    }

    private void updateAnswerDisplay() {
        answerContainer.removeAllViews();

        for (String word : selectedWords) {
            TextView wordView = new TextView(this);
            wordView.setText(word);
            wordView.setTextColor(getColor(R.color.text_white));
            wordView.setTextSize(18);
            wordView.setBackgroundResource(R.drawable.bg_word_selected);
            wordView.setPadding(16, 12, 16, 12);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(4, 4, 4, 4);
            wordView.setLayoutParams(params);

            // Click to remove
            wordView.setOnClickListener(v -> {
                selectedWords.remove(word);
                updateAnswerDisplay();
                // Re-enable the word button
                for (int i = 0; i < wordContainer.getChildCount(); i++) {
                    TextView btn = (TextView) wordContainer.getChildAt(i);
                    if (btn.getText().toString().equals(word)) {
                        btn.setAlpha(1f);
                        btn.setClickable(true);
                        break;
                    }
                }
            });

            answerContainer.addView(wordView);
        }
    }

    private void checkAnswer() {
        if (selectedWords.size() != currentWords.size()) return;

        String answer = String.join(" ", selectedWords);

        if (answer.equals(correctSentence)) {
            // Correct!
            score += 20;
            tvScore.setText("⭐ " + score);
            Toast.makeText(this, "🎉 Đúng rồi!", Toast.LENGTH_SHORT).show();

            if (tts != null) {
                tts.speak(correctSentence, TextToSpeech.QUEUE_FLUSH, null, "sentence");
            }

            new Handler().postDelayed(() -> {
                currentIndex++;
                showCurrentPuzzle();
            }, 1500);
        } else {
            // Wrong - let user try again
            Toast.makeText(this, "❌ Chưa đúng, thử lại!", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateProgress() {
        int progress = sentences.isEmpty() ? 0 : ((currentIndex + 1) * 100) / sentences.size();
        progressBar.setProgress(progress);
        tvProgress.setText((currentIndex + 1) + "/" + sentences.size());
    }

    private void completeScene() {
        int stars = score >= 80 ? 3 : score >= 50 ? 2 : 1;

        dbHelper.updateSceneProgress(sceneId, stars);
        dbHelper.addStars(stars);

        String message = "Điểm số: " + score;
        SpaceDialog.showResult(this, "🧩", "Hoàn thành Puzzle!", message, stars, "Tiếp tục", () -> finish());
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.US);
            tts.setSpeechRate(0.8f);
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}

