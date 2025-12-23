package com.example.engapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import com.example.engapp.data.GameDataProvider;
import com.example.engapp.database.GameDatabaseHelper;
import com.example.engapp.manager.ProgressionManager;
import com.example.engapp.model.Planet;
import com.example.engapp.model.Word;
import com.example.engapp.model.Zone;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class GuessNameGameActivity extends BaseBuddyActivity implements TextToSpeech.OnInitListener {

    private TextView tvScore, tvQuestion, tvEmoji;
    private TextView tvAnswer1, tvAnswer2, tvAnswer3, tvAnswer4;
    private CardView cardAnswer1, cardAnswer2, cardAnswer3, cardAnswer4;
    private ProgressBar progressBar;
    private ImageView btnBack;
    private FrameLayout resultOverlay;
    private TextView tvResultEmoji, tvResultText, tvCorrectAnswer, tvLives;

    private TextToSpeech tts;
    private ProgressionManager progressionManager;
    private GameDatabaseHelper dbHelper;
    private List<Word> words;
    private List<Word> questions;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private int lives = 3;
    private int correctAnswerIndex = 0;
    private int totalQuestions = 10;
    private boolean isAnswering = false;
    private int planetIdInt = -1;
    private int sceneId = -1;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guess_name_game);

        // Get planet_id as Integer (consistent with PlanetMapActivity)
        planetIdInt = getIntent().getIntExtra("planet_id", -1);
        sceneId = getIntent().getIntExtra("scene_id", -1);
        String planetId = planetIdInt > 0 ? String.valueOf(planetIdInt) : null;
        int zoneIndex = getIntent().getIntExtra("zone_index", 0);

        if (planetId == null || planetIdInt <= 0) {
            // Fallback: try to get as String (for backward compatibility)
            planetId = getIntent().getStringExtra("planet_id");
            if (planetId == null) {
                finish();
                return;
            }
        }

        progressionManager = ProgressionManager.getInstance(this);
        dbHelper = GameDatabaseHelper.getInstance(this);

        initViews();
        initTTS();
        loadWords(planetId, zoneIndex);
        setupClickListeners();
        
        // Check if words loaded successfully
        if (words == null || words.isEmpty()) {
            Toast.makeText(this, "Không thể tải dữ liệu từ vựng. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        startGame();
    }

    private void initViews() {
        tvScore = findViewById(R.id.tvScore);
        tvQuestion = findViewById(R.id.tvQuestion);
        tvEmoji = findViewById(R.id.tvEmoji);
        tvAnswer1 = findViewById(R.id.tvAnswer1);
        tvAnswer2 = findViewById(R.id.tvAnswer2);
        tvAnswer3 = findViewById(R.id.tvAnswer3);
        tvAnswer4 = findViewById(R.id.tvAnswer4);
        cardAnswer1 = findViewById(R.id.cardAnswer1);
        cardAnswer2 = findViewById(R.id.cardAnswer2);
        cardAnswer3 = findViewById(R.id.cardAnswer3);
        cardAnswer4 = findViewById(R.id.cardAnswer4);
        progressBar = findViewById(R.id.progressBar);
        btnBack = findViewById(R.id.btnBack);
        resultOverlay = findViewById(R.id.resultOverlay);
        tvResultEmoji = findViewById(R.id.tvResultEmoji);
        tvResultText = findViewById(R.id.tvResultText);
        tvCorrectAnswer = findViewById(R.id.tvCorrectAnswer);
        tvLives = findViewById(R.id.tvLives);
    }

    private void initTTS() {
        tts = new TextToSpeech(this, this);
    }

    private void loadWords(String planetId, int zoneIndex) {
        words = new ArrayList<>();
        
        // Try to load from database first (preferred method)
        if (planetIdInt > 0) {
            List<GameDatabaseHelper.WordData> wordDataList = dbHelper.getWordsForPlanet(planetIdInt);
            if (wordDataList != null && !wordDataList.isEmpty()) {
                // Convert WordData to Word model
                for (GameDatabaseHelper.WordData wordData : wordDataList) {
                    Word word = new Word(wordData.english, wordData.vietnamese, wordData.emoji);
                    if (wordData.exampleSentence != null) {
                        word.setExampleSentence(wordData.exampleSentence);
                    }
                    if (wordData.exampleTranslation != null) {
                        word.setExampleTranslation(wordData.exampleTranslation);
                    }
                    words.add(word);
                }
            }
        }
        
        // Fallback: Try GameDataProvider if database doesn't have words
        if (words.isEmpty()) {
            Planet planet = GameDataProvider.getPlanetById(planetId);
            if (planet != null && planet.getZones() != null && zoneIndex < planet.getZones().size()) {
                Zone zone = planet.getZones().get(zoneIndex);
                if (zone.getWords() != null) {
                    words = new ArrayList<>(zone.getWords());
                }
            }
        }

        // Final check
        if (words == null || words.size() < 4) {
            // Don't finish here, let onCreate handle it
            words = new ArrayList<>(); // Ensure it's not null
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> showExitConfirmation());

        cardAnswer1.setOnClickListener(v -> checkAnswer(0));
        cardAnswer2.setOnClickListener(v -> checkAnswer(1));
        cardAnswer3.setOnClickListener(v -> checkAnswer(2));
        cardAnswer4.setOnClickListener(v -> checkAnswer(3));

        resultOverlay.setOnClickListener(v -> {
            resultOverlay.setVisibility(View.GONE);
            nextQuestion();
        });
    }

    private void startGame() {
        questions = new ArrayList<>(words);
        Collections.shuffle(questions);

        // Limit to totalQuestions or available words
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

        Word currentWord = questions.get(currentQuestionIndex);

        // Display emoji/image
        tvEmoji.setText(currentWord.getImageUrl() != null ? currentWord.getImageUrl() : "❓");

        // Generate answer options
        List<String> options = new ArrayList<>();
        options.add(currentWord.getEnglish());

        // Add 3 wrong answers
        List<Word> wrongAnswers = new ArrayList<>(words);
        wrongAnswers.remove(currentWord);
        Collections.shuffle(wrongAnswers);

        for (int i = 0; i < Math.min(3, wrongAnswers.size()); i++) {
            options.add(wrongAnswers.get(i).getEnglish());
        }

        // Shuffle and set answers
        Collections.shuffle(options);
        correctAnswerIndex = options.indexOf(currentWord.getEnglish());

        tvAnswer1.setText(capitalizeFirst(options.get(0)));
        tvAnswer2.setText(capitalizeFirst(options.get(1)));
        tvAnswer3.setText(capitalizeFirst(options.get(2)));
        tvAnswer4.setText(capitalizeFirst(options.get(3)));

        // Reset card colors
        resetCardColors();

        updateUI();
    }

    private void checkAnswer(int selectedIndex) {
        if (isAnswering) return;
        isAnswering = true;

        Word currentWord = questions.get(currentQuestionIndex);
        CardView selectedCard = getCardByIndex(selectedIndex);
        CardView correctCard = getCardByIndex(correctAnswerIndex);

        if (selectedIndex == correctAnswerIndex) {
            // Correct answer
            score += 10;
            selectedCard.setCardBackgroundColor(getColor(R.color.correct_green));

            showResult(true, currentWord);
            speakWord(currentWord.getEnglish());

            // Buddy celebrates correct answer
            onCorrectAnswer();
        } else {
            // Wrong answer
            lives--;
            selectedCard.setCardBackgroundColor(getColor(R.color.wrong_red));
            correctCard.setCardBackgroundColor(getColor(R.color.correct_green));

            showResult(false, currentWord);

            // Buddy encourages after wrong answer
            onWrongAnswer();

            if (lives <= 0) {
                handler.postDelayed(this::endGame, 1500);
                return;
            }
        }

        updateUI();
    }

    private void showResult(boolean isCorrect, Word word) {
        resultOverlay.setVisibility(View.VISIBLE);

        if (isCorrect) {
            tvResultEmoji.setText("🎉");
            tvResultText.setText("Đúng rồi!");
            tvResultText.setTextColor(getColor(R.color.correct_green));
        } else {
            tvResultEmoji.setText("😢");
            tvResultText.setText("Chưa đúng!");
            tvResultText.setTextColor(getColor(R.color.wrong_red));
        }

        tvCorrectAnswer.setText(capitalizeFirst(word.getEnglish()) + " = " + word.getVietnamese());

        // Auto-hide after delay
        handler.postDelayed(() -> {
            if (resultOverlay.getVisibility() == View.VISIBLE) {
                resultOverlay.setVisibility(View.GONE);
                nextQuestion();
            }
        }, 1500);
    }

    private void nextQuestion() {
        currentQuestionIndex++;
        displayQuestion();
    }

    private void endGame() {
        // #region agent log
        try {
            java.io.FileWriter fw = new java.io.FileWriter("c:\\Users\\ADMIN\\Downloads\\MobileApp_Project-main (2)\\MobileApp_Project-main\\.cursor\\debug.log", true);
            fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"A\",\"location\":\"GuessNameGameActivity.endGame:296\",\"message\":\"endGame entry\",\"data\":{\"score\":" + score + ",\"totalQuestions\":" + totalQuestions + ",\"planetIdInt\":" + planetIdInt + ",\"sceneId\":" + sceneId + "},\"timestamp\":" + System.currentTimeMillis() + "}\n");
            fw.close();
        } catch (Exception e) {}
        // #endregion
        // Calculate stars
        int percentage = (score * 100) / (totalQuestions * 10);
        int stars = percentage >= 90 ? 3 : percentage >= 70 ? 2 : percentage >= 50 ? 1 : 0;
        
        // #region agent log
        try {
            java.io.FileWriter fw = new java.io.FileWriter("c:\\Users\\ADMIN\\Downloads\\MobileApp_Project-main (2)\\MobileApp_Project-main\\.cursor\\debug.log", true);
            fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"A\",\"location\":\"GuessNameGameActivity.endGame:300\",\"message\":\"Stars calculated\",\"data\":{\"percentage\":" + percentage + ",\"stars\":" + stars + "},\"timestamp\":" + System.currentTimeMillis() + "}\n");
            fw.close();
        } catch (Exception e) {}
        // #endregion

        // Save progress using both old and new systems
        saveProgress(stars);

        int starsEarned = stars * 10;
        
        // Record in new progression system
        recordGameCompleted("guess_name", starsEarned);
        
        // Record lesson completion to unlock next lesson
        // #region agent log
        try {
            java.io.FileWriter fw = new java.io.FileWriter("c:\\Users\\ADMIN\\Downloads\\MobileApp_Project-main (2)\\MobileApp_Project-main\\.cursor\\debug.log", true);
            fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"B\",\"location\":\"GuessNameGameActivity.endGame:312\",\"message\":\"Checking recordLessonCompleted condition\",\"data\":{\"planetIdInt\":" + planetIdInt + ",\"sceneId\":" + sceneId + ",\"stars\":" + stars + ",\"willRecord\":" + (planetIdInt > 0 && sceneId > 0 && stars > 0) + "},\"timestamp\":" + System.currentTimeMillis() + "}\n");
            fw.close();
        } catch (Exception e) {}
        // #endregion
        if (planetIdInt > 0 && sceneId > 0 && stars > 0) {
            // #region agent log
            try {
                java.io.FileWriter fw = new java.io.FileWriter("c:\\Users\\ADMIN\\Downloads\\MobileApp_Project-main (2)\\MobileApp_Project-main\\.cursor\\debug.log", true);
                fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"B\",\"location\":\"GuessNameGameActivity.endGame:315\",\"message\":\"Calling recordLessonCompleted\",\"data\":{\"planetIdInt\":" + planetIdInt + ",\"sceneId\":" + sceneId + ",\"stars\":" + stars + "},\"timestamp\":" + System.currentTimeMillis() + "}\n");
                fw.close();
            } catch (Exception e) {}
            // #endregion
            progressionManager.recordLessonCompleted(planetIdInt, sceneId, stars);
        } else {
            // #region agent log
            try {
                java.io.FileWriter fw = new java.io.FileWriter("c:\\Users\\ADMIN\\Downloads\\MobileApp_Project-main (2)\\MobileApp_Project-main\\.cursor\\debug.log", true);
                fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"B\",\"location\":\"GuessNameGameActivity.endGame:322\",\"message\":\"NOT calling recordLessonCompleted\",\"data\":{\"reason\":\"condition not met\"},\"timestamp\":" + System.currentTimeMillis() + "}\n");
                fw.close();
            } catch (Exception e) {}
            // #endregion
        }

        // Trigger Buddy celebration if good performance
        if (stars >= 2) {
            onZoneCompleted();
        }

        String message = "Điểm: " + score + "/" + (totalQuestions * 10) + "\n";
        message += "⭐".repeat(stars) + (stars < 3 ? "☆".repeat(3 - stars) : "");

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

        // Update lives
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

    private void speakWord(String text) {
        if (tts != null) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "word");
        }
    }

    private String capitalizeFirst(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    private void showExitConfirmation() {
        new AlertDialog.Builder(this)
            .setTitle("Thoát game?")
            .setMessage("Bạn có chắc muốn thoát? Tiến trình sẽ không được lưu.")
            .setPositiveButton("Thoát", (d, w) -> finish())
            .setNegativeButton("Tiếp tục", null)
            .show();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.US);
            tts.setSpeechRate(0.8f);
        }
    }

    @Override
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

