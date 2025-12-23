package com.example.engapp;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.engapp.data.GameDataProvider;
import com.example.engapp.manager.ProgressionManager;
import com.example.engapp.model.Planet;
import com.example.engapp.model.Word;
import com.example.engapp.model.Zone;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ListenChooseGameActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private TextView tvScore, tvQuestion, tvLives;
    private TextView tvEmoji1, tvEmoji2, tvEmoji3, tvEmoji4;
    private CardView cardAnswer1, cardAnswer2, cardAnswer3, cardAnswer4;
    private LinearLayout btnListen;
    private ProgressBar progressBar;
    private ImageView btnBack;

    private TextToSpeech tts;
    private ProgressionManager progressionManager;
    private List<Word> words;
    private List<Word> questions;
    private Word currentWord;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private int lives = 3;
    private int correctAnswerIndex = 0;
    private int totalQuestions = 10;
    private boolean isAnswering = false;
    private boolean ttsReady = false;
    private int planetIdInt = -1;
    private int sceneId = -1;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listen_choose_game);

        // Get planet_id as Integer (consistent with PlanetMapActivity)
        planetIdInt = getIntent().getIntExtra("planet_id", -1);
        sceneId = getIntent().getIntExtra("scene_id", -1);
        String planetId = planetIdInt > 0 ? String.valueOf(planetIdInt) : getIntent().getStringExtra("planet_id");
        int zoneIndex = getIntent().getIntExtra("zone_index", 0);

        if (planetId == null) {
            finish();
            return;
        }

        progressionManager = ProgressionManager.getInstance(this);
        initViews();
        initTTS();
        loadWords(planetId, zoneIndex);
        setupClickListeners();
    }

    private void initViews() {
        tvScore = findViewById(R.id.tvScore);
        tvQuestion = findViewById(R.id.tvQuestion);
        tvLives = findViewById(R.id.tvLives);
        tvEmoji1 = findViewById(R.id.tvEmoji1);
        tvEmoji2 = findViewById(R.id.tvEmoji2);
        tvEmoji3 = findViewById(R.id.tvEmoji3);
        tvEmoji4 = findViewById(R.id.tvEmoji4);
        cardAnswer1 = findViewById(R.id.cardAnswer1);
        cardAnswer2 = findViewById(R.id.cardAnswer2);
        cardAnswer3 = findViewById(R.id.cardAnswer3);
        cardAnswer4 = findViewById(R.id.cardAnswer4);
        btnListen = findViewById(R.id.btnListen);
        progressBar = findViewById(R.id.progressBar);
        btnBack = findViewById(R.id.btnBack);
    }

    private void initTTS() {
        tts = new TextToSpeech(this, this);
    }

    private void loadWords(String planetId, int zoneIndex) {
        Planet planet = GameDataProvider.getPlanetById(planetId);
        if (planet != null && planet.getZones() != null && zoneIndex < planet.getZones().size()) {
            Zone zone = planet.getZones().get(zoneIndex);
            words = new ArrayList<>(zone.getWords());
        }

        if (words == null || words.size() < 4) {
            Toast.makeText(this, "Không đủ từ vựng để chơi", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> showExitConfirmation());

        btnListen.setOnClickListener(v -> speakCurrentWord());

        cardAnswer1.setOnClickListener(v -> checkAnswer(0));
        cardAnswer2.setOnClickListener(v -> checkAnswer(1));
        cardAnswer3.setOnClickListener(v -> checkAnswer(2));
        cardAnswer4.setOnClickListener(v -> checkAnswer(3));
    }

    private void startGame() {
        questions = new ArrayList<>(words);
        Collections.shuffle(questions);

        totalQuestions = Math.min(totalQuestions, questions.size());
        if (questions.size() > totalQuestions) {
            questions = questions.subList(0, totalQuestions);
        }

        currentQuestionIndex = 0;
        score = 0;
        lives = 3;

        updateUI();
        displayQuestion();
    }

    private void displayQuestion() {
        if (currentQuestionIndex >= questions.size()) {
            endGame();
            return;
        }

        isAnswering = false;
        currentWord = questions.get(currentQuestionIndex);

        // Generate 4 options with emojis
        List<Word> options = new ArrayList<>();
        options.add(currentWord);

        List<Word> wrongAnswers = new ArrayList<>(words);
        wrongAnswers.remove(currentWord);
        Collections.shuffle(wrongAnswers);

        for (int i = 0; i < Math.min(3, wrongAnswers.size()); i++) {
            options.add(wrongAnswers.get(i));
        }

        Collections.shuffle(options);
        correctAnswerIndex = options.indexOf(currentWord);

        // Set emojis
        tvEmoji1.setText(options.get(0).getImageUrl() != null ? options.get(0).getImageUrl() : "❓");
        tvEmoji2.setText(options.get(1).getImageUrl() != null ? options.get(1).getImageUrl() : "❓");
        tvEmoji3.setText(options.get(2).getImageUrl() != null ? options.get(2).getImageUrl() : "❓");
        tvEmoji4.setText(options.get(3).getImageUrl() != null ? options.get(3).getImageUrl() : "❓");

        // Store words in tags for later
        cardAnswer1.setTag(options.get(0));
        cardAnswer2.setTag(options.get(1));
        cardAnswer3.setTag(options.get(2));
        cardAnswer4.setTag(options.get(3));

        resetCardColors();
        updateUI();

        // Auto-speak after short delay
        handler.postDelayed(this::speakCurrentWord, 500);
    }

    private void speakCurrentWord() {
        if (tts != null && ttsReady && currentWord != null) {
            tts.speak(currentWord.getEnglish(), TextToSpeech.QUEUE_FLUSH, null, "word");
        }
    }

    private void checkAnswer(int selectedIndex) {
        if (isAnswering) return;
        isAnswering = true;

        CardView selectedCard = getCardByIndex(selectedIndex);
        CardView correctCard = getCardByIndex(correctAnswerIndex);

        if (selectedIndex == correctAnswerIndex) {
            score += 10;
            selectedCard.setCardBackgroundColor(getColor(R.color.correct_green));
            speakCurrentWord();

            Toast.makeText(this, "🎉 Đúng rồi! " + currentWord.getEnglish() + " = " + currentWord.getVietnamese(), Toast.LENGTH_SHORT).show();
        } else {
            lives--;
            selectedCard.setCardBackgroundColor(getColor(R.color.wrong_red));
            correctCard.setCardBackgroundColor(getColor(R.color.correct_green));

            Toast.makeText(this, "😢 Đáp án đúng: " + currentWord.getEnglish(), Toast.LENGTH_SHORT).show();

            if (lives <= 0) {
                handler.postDelayed(this::endGame, 1500);
                return;
            }
        }

        updateUI();
        handler.postDelayed(this::nextQuestion, 1500);
    }

    private void nextQuestion() {
        currentQuestionIndex++;
        displayQuestion();
    }

    private void endGame() {
        int percentage = (score * 100) / (totalQuestions * 10);
        int stars = percentage >= 90 ? 3 : percentage >= 70 ? 2 : percentage >= 50 ? 1 : 0;

        saveProgress(stars);
        
        // IMPORTANT: Record lesson completion to unlock next lesson
        if (planetIdInt > 0 && sceneId > 0 && stars > 0) {
            progressionManager.recordLessonCompleted(planetIdInt, sceneId, stars);
        }

        String message = "Điểm: " + score + "/" + (totalQuestions * 10) + "\n";
        for (int i = 0; i < 3; i++) {
            message += i < stars ? "⭐" : "☆";
        }

        new AlertDialog.Builder(this)
            .setTitle(stars >= 2 ? "🎉 Tuyệt vời!" : stars >= 1 ? "👍 Tốt lắm!" : "💪 Cố gắng lên!")
            .setMessage(message)
            .setPositiveButton("Chơi lại", (d, w) -> startGame())
            .setNegativeButton("Thoát", (d, w) -> finish())
            .setCancelable(false)
            .show();
    }

    private void saveProgress(int stars) {
        SharedPreferences prefs = getSharedPreferences("game_progress", MODE_PRIVATE);
        int totalStars = prefs.getInt("total_stars", 0);
        prefs.edit().putInt("total_stars", totalStars + stars).apply();
    }

    private void updateUI() {
        tvScore.setText("⭐ " + score);
        tvQuestion.setText("Câu " + (currentQuestionIndex + 1) + "/" + totalQuestions);
        progressBar.setProgress(((currentQuestionIndex + 1) * 100) / totalQuestions);

        StringBuilder livesText = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            livesText.append(i < lives ? "❤️" : "🖤");
        }
        tvLives.setText(livesText.toString());
    }

    private void resetCardColors() {
        int defaultColor = 0x60FFFFFF;
        cardAnswer1.setCardBackgroundColor(defaultColor);
        cardAnswer2.setCardBackgroundColor(defaultColor);
        cardAnswer3.setCardBackgroundColor(defaultColor);
        cardAnswer4.setCardBackgroundColor(defaultColor);
    }

    private CardView getCardByIndex(int index) {
        switch (index) {
            case 0: return cardAnswer1;
            case 1: return cardAnswer2;
            case 2: return cardAnswer3;
            case 3: return cardAnswer4;
            default: return cardAnswer1;
        }
    }

    private void showExitConfirmation() {
        new AlertDialog.Builder(this)
            .setTitle("Thoát game?")
            .setMessage("Bạn có chắc muốn thoát?")
            .setPositiveButton("Thoát", (d, w) -> finish())
            .setNegativeButton("Tiếp tục", null)
            .show();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.US);
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                ttsReady = true;
                tts.setSpeechRate(0.8f);
                startGame();
            }
        }
    }

    @Override
    @SuppressLint("MissingSuperCall")
    public void onBackPressed() {
        showExitConfirmation();
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}

