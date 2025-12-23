package com.example.engapp;

import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.engapp.database.GameDatabaseHelper;
import com.example.engapp.database.GameDatabaseHelper.WordData;
import com.example.engapp.manager.BuddyManager;
import com.example.engapp.manager.ProgressionManager;
import com.example.engapp.manager.LessonUnlockManager;
import com.example.engapp.view.BreadcrumbView;
import com.example.engapp.view.BuddyOverlayView;
import com.example.engapp.view.ConfettiView;
import com.example.engapp.view.HealthBarView;
import com.example.engapp.view.LightningBoltView;
import com.example.engapp.view.StarRatingView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Redesigned Word Battle Activity with improved UI/UX for kids.
 * Features: Animated health bars, buddy integration, sparkle effects, combo system.
 */
public class BattleActivity extends AppCompatActivity implements TextToSpeech.OnInitListener, BuddyManager.BuddyEventListener {

    // UI Views - Top Bar
    private ImageButton btnBack;
    private TextView tvBattleTitle, tvQuestionCount;

    // UI Views - Enemy Zone
    private TextView tvEnemyEmoji, tvEnemyName;
    private HealthBarView healthBarEnemy;

    // UI Views - Arena Zone
    private TextView tvPlayerEmoji, tvDamagePopup, tvCombo;
    private HealthBarView healthBarPlayer;
    private View hitFXOverlay, playerContainer, enemyContainer;
    private LightningBoltView lightningBolt;

    // UI Views - Command Panel
    private View commandPanel;
    private TextView tvQuestionType, tvQuestion;
    private ImageButton btnSpeaker;
    private Button btnAnswerCorrect; // Primary action (correct answer)
    private LinearLayout layoutWrongAnswers;
    private Button btnAnswer1, btnAnswer2, btnAnswer3, btnAnswer4; // Secondary actions

    // UI Views - Result
    private CardView resultCard;
    private TextView tvResultEmoji, tvResultTitle, tvResultScore, tvResultDetails;
    private StarRatingView starRating;
    private Button btnRetry, btnContinue;
    private ConfettiView confettiView;

    // Buddy
    private BuddyOverlayView buddyOverlay;
    private BuddyManager buddyManager;

    // Game State
    private GameDatabaseHelper dbHelper;
    private ProgressionManager progressionManager;
    private LessonUnlockManager lessonUnlockManager;
    private TextToSpeech tts;
    private List<WordData> words;
    private List<BattleQuestion> questions;
    private int currentQuestionIndex = 0;
    private int playerHealth = 100;
    private int enemyHealth = 100;
    private int correctAnswers = 0;
    private int totalQuestions = 10;
    private boolean isAnswering = true;
    private int combo = 0;
    private int planetId;
    private String planetName;
    private String galaxyName;
    
    // Breadcrumb
    private BreadcrumbView breadcrumbView;

    private Handler handler = new Handler();
    private Random random = new Random();

    // Enemy data
    private String[] enemyEmojis = {"🤖", "👾", "👽", "🦖", "🐉", "👹"};
    private String[] enemyNames = {"Nebula Bot", "Word Monster", "Alien Quiz", "Dino Word", "Story Dragon", "Vocab Beast"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle_v2);

        dbHelper = GameDatabaseHelper.getInstance(this);
        progressionManager = ProgressionManager.getInstance(this);
        lessonUnlockManager = LessonUnlockManager.getInstance(this);
        planetId = getIntent().getIntExtra("planet_id", 1);
        
        // Get planet and galaxy info for context
        loadPlanetContext();
        
        tts = new TextToSpeech(this, this);
        buddyManager = BuddyManager.getInstance(this);
        buddyManager.addListener(this);

        initViews();
        setupBreadcrumb();
        setupBuddy();
        loadWords();
        generateQuestions();
        spawnEnemy();
        showQuestion();
    }
    
    private void loadPlanetContext() {
        // Get planet name from database
        GameDatabaseHelper.PlanetData planet = dbHelper.getPlanetById(planetId);
        if (planet != null) {
            planetName = planet.nameVi != null ? planet.nameVi : planet.name;
        } else {
            planetName = "Planet " + planetId;
        }
        
        // Determine galaxy based on planet ID
        // Galaxy 1: planets 1-4, Galaxy 2: planets 5-8, Galaxy 3: planets 9-12
        int galaxyId = ((planetId - 1) / 4) + 1;
        String[] galaxyNames = {"", "Beginner Galaxy", "Explorer Galaxy", "Advanced Galaxy"};
        String[] galaxyNamesVi = {"", "Thiên hà Khởi đầu", "Thiên hà Khám phá", "Thiên hà Nâng cao"};
        galaxyName = galaxyNamesVi[galaxyId];
    }
    
    private void setupBreadcrumb() {
        breadcrumbView = findViewById(R.id.breadcrumbView);
        if (breadcrumbView != null) {
            // Galaxy segment (clickable - goes back to galaxy map)
            breadcrumbView.addSegment("🌌", galaxyName, v -> {
                // Navigate back to galaxy map
                finish();
            });
            
            // Planet segment (clickable - goes back to planet map)
            breadcrumbView.addSegment("🪐", planetName, v -> {
                // Navigate back to planet map
                finish();
            });
            
            // Battle segment (not clickable - current location)
            breadcrumbView.addSegment("⚔️", "Battle", null);
        }
    }

    private void initViews() {
        // Top Bar
        btnBack = findViewById(R.id.btnBack);
        tvBattleTitle = findViewById(R.id.tvBattleTitle);
        tvQuestionCount = findViewById(R.id.tvQuestionCount);

        // Enemy Zone
        tvEnemyEmoji = findViewById(R.id.tvEnemyEmoji);
        tvEnemyName = findViewById(R.id.tvEnemyName);
        healthBarEnemy = findViewById(R.id.healthBarEnemy);
        healthBarEnemy.setMaxHealth(100);
        healthBarEnemy.setHealth(100, false);
        healthBarEnemy.setIsPlayer(false);

        // Arena Zone
        tvPlayerEmoji = findViewById(R.id.tvPlayerEmoji);
        tvDamagePopup = findViewById(R.id.tvDamagePopup);
        tvCombo = findViewById(R.id.tvCombo);
        healthBarPlayer = findViewById(R.id.healthBarPlayer);
        healthBarPlayer.setMaxHealth(100);
        healthBarPlayer.setHealth(100, false);
        healthBarPlayer.setIsPlayer(true);
        hitFXOverlay = findViewById(R.id.hitFXOverlay);
        playerContainer = findViewById(R.id.playerContainer);
        if (playerContainer != null) {
            playerContainer.setVisibility(View.GONE); // Ẩn player
        }
        enemyContainer = findViewById(R.id.cardEnemy);
        lightningBolt = findViewById(R.id.lightningBolt);

        // Command Panel
        commandPanel = findViewById(R.id.commandPanel);
        tvQuestionType = findViewById(R.id.tvQuestionType);
        tvQuestion = findViewById(R.id.tvQuestion);
        btnSpeaker = findViewById(R.id.btnSpeaker);
        
        // Answer Buttons (Actions)
        btnAnswerCorrect = findViewById(R.id.btnAnswerCorrect);
        layoutWrongAnswers = findViewById(R.id.layoutWrongAnswers);
        btnAnswer1 = findViewById(R.id.btnAnswer1);
        btnAnswer2 = findViewById(R.id.btnAnswer2);
        btnAnswer3 = findViewById(R.id.btnAnswer3);
        btnAnswer4 = findViewById(R.id.btnAnswer4);

        // Result
        resultCard = findViewById(R.id.resultCard);
        tvResultEmoji = findViewById(R.id.tvResultEmoji);
        tvResultTitle = findViewById(R.id.tvResultTitle);
        tvResultScore = findViewById(R.id.tvResultScore);
        tvResultDetails = findViewById(R.id.tvResultDetails);
        starRating = findViewById(R.id.starRating);
        btnRetry = findViewById(R.id.btnRetry);
        btnContinue = findViewById(R.id.btnContinue);
        confettiView = findViewById(R.id.confettiView);

        // Setup listeners
        btnBack.setOnClickListener(v -> finish());
        btnSpeaker.setOnClickListener(v -> speakQuestion());
        
        // Answer listeners will be set in showQuestion() for each question

        btnRetry.setOnClickListener(v -> restartBattle());
        btnContinue.setOnClickListener(v -> finish());
    }

    private void setupBuddy() {
        buddyOverlay = findViewById(R.id.buddyOverlay);
        if (buddyOverlay != null) {
            buddyOverlay.setVisibility(View.GONE); // Ẩn buddy
            buddyManager.transitionToState(BuddyManager.CONTEXT_ENCOURAGEMENT);
        }
    }

    private void loadWords() {
        words = dbHelper.getWordsForPlanet(planetId);
        if (words == null || words.size() < 4) {
            words = dbHelper.getWordsForPlanet(1);
        }
    }

    private void generateQuestions() {
        questions = new ArrayList<>();
        Collections.shuffle(words);

        int questionCount = Math.min(totalQuestions, words.size());
        for (int i = 0; i < questionCount; i++) {
            WordData word = words.get(i);

            List<String> wrongAnswers = new ArrayList<>();
            for (WordData w : words) {
                if (!w.vietnamese.equals(word.vietnamese)) {
                    wrongAnswers.add(w.vietnamese);
                }
            }
            Collections.shuffle(wrongAnswers);

            List<String> options = new ArrayList<>();
            options.add(word.vietnamese);
            for (int j = 0; j < 3 && j < wrongAnswers.size(); j++) {
                options.add(wrongAnswers.get(j));
            }
            Collections.shuffle(options);

            questions.add(new BattleQuestion(
                word.english,
                word.emoji,
                word.vietnamese,
                options
            ));
        }

        totalQuestions = questions.size();
    }

    private void spawnEnemy() {
        int enemyIndex = random.nextInt(enemyEmojis.length);
        tvEnemyEmoji.setText(enemyEmojis[enemyIndex]);
        tvEnemyName.setText(enemyNames[enemyIndex]);

        // Spawn animation
        Animation spawnAnim = AnimationUtils.loadAnimation(this, R.anim.fade_scale_in);
        enemyContainer.startAnimation(spawnAnim);
    }

    private void showQuestion() {
        if (currentQuestionIndex >= questions.size()) {
            showResult();
            return;
        }

        BattleQuestion question = questions.get(currentQuestionIndex);

        tvQuestionCount.setText((currentQuestionIndex + 1) + "/" + totalQuestions);
        tvQuestionType.setText("Chọn nghĩa đúng nhé!");
        
        // Display word với emoji
        String wordDisplay = question.emoji + " " + question.english.toUpperCase();
        tvQuestion.setText(wordDisplay);

        // Shuffle options to randomize correct answer position
        List<String> shuffledOptions = new ArrayList<>(question.options);
        Collections.shuffle(shuffledOptions);
        
        // Hiển thị tất cả 4 đáp án bằng nhau (không có button lớn riêng)
        Button[] answerButtons = {btnAnswer1, btnAnswer2, btnAnswer3, btnAnswer4};
        final BattleQuestion finalQuestion = question; // Make final for lambda
        
        // Ẩn button lớn (correct answer button)
        btnAnswerCorrect.setVisibility(View.GONE);
        
        // Hiển thị 4 buttons bằng nhau
        for (int i = 0; i < Math.min(shuffledOptions.size(), 4); i++) {
            Button btn = answerButtons[i];
            String answer = shuffledOptions.get(i);
            btn.setText(answer);
            btn.setVisibility(View.VISIBLE);
            
            final Button finalBtn = btn; // Make final for lambda
            final String finalAnswer = answer;
            btn.setOnClickListener(v -> {
                if (!isAnswering) return;
                checkAnswer(finalBtn, finalQuestion);
            });
        }
        
        // Hide unused buttons nếu có ít hơn 4 options
        for (int i = shuffledOptions.size(); i < 4; i++) {
            answerButtons[i].setVisibility(View.GONE);
        }

        resetButtonStyles();
        isAnswering = true;

        // Command panel animation
        Animation slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
        commandPanel.startAnimation(slideIn);
    }

    private void checkAnswer(Button selectedButton, BattleQuestion question) {
        isAnswering = false;
        String selectedAnswer = selectedButton.getText().toString();

        // Lock all buttons
        lockButtons();

        if (selectedAnswer.equals(question.correctAnswer)) {
            handleCorrectAnswer(selectedButton, question);
        } else {
            handleWrongAnswer(selectedButton, question);
        }
    }

    private void handleCorrectAnswer(Button selectedButton, BattleQuestion question) {
        correctAnswers++;
        combo++;
        enemyHealth -= 10;
        if (enemyHealth < 0) enemyHealth = 0;

        // Visual feedback
        selectedButton.setBackgroundResource(R.drawable.button_answer_correct_v2);
        playSparkleAnimation(selectedButton);
        
        // Highlight correct answer button
        selectedButton.animate().scaleX(1.1f).scaleY(1.1f).setDuration(200).start();

        // Health bar animation
        healthBarEnemy.setHealth(enemyHealth, true);

        // Damage popup
        showDamagePopup(-10, true);

        // Enemy hit animation
        Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
        enemyContainer.startAnimation(shake);

        // Combo indicator
        if (combo >= 3) {
            showCombo(combo);
        }

        // Buddy reaction
        buddyManager.onCorrectAnswer();

        // Star flies to progress
        animateStarToProgress();

        // Check battle end
        if (enemyHealth <= 0) {
            handler.postDelayed(this::showResult, 1500);
        } else {
            currentQuestionIndex++;
            handler.postDelayed(this::showQuestion, 1200);
        }
    }

    private void handleWrongAnswer(Button selectedButton, BattleQuestion question) {
        combo = 0; // Reset combo
        playerHealth -= 10;
        if (playerHealth < 0) playerHealth = 0;

        // Visual feedback
        selectedButton.setBackgroundResource(R.drawable.button_answer_wrong_v2);
        Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
        selectedButton.startAnimation(shake);
        
        // Highlight correct answer trong các buttons
        Button[] buttons = {btnAnswer1, btnAnswer2, btnAnswer3, btnAnswer4};
        for (Button btn : buttons) {
            if (btn.getText().toString().equals(question.correctAnswer)) {
                btn.setBackgroundResource(R.drawable.button_answer_correct_v2);
                btn.animate().scaleX(1.15f).scaleY(1.15f).setDuration(300).start();
                playSparkleAnimation(btn);
                break;
            }
        }

        // Health bar animation
        healthBarPlayer.setHealth(playerHealth, true);

        // Show correct answer
        highlightCorrectAnswer(question.correctAnswer);

        // Player hit animation
        Animation playerShake = AnimationUtils.loadAnimation(this, R.anim.shake);
        playerContainer.startAnimation(playerShake);

        // Shield ripple effect
        playShieldRipple();

        // Buddy reaction
        buddyManager.onWrongAnswer();

        // Check battle end
        if (playerHealth <= 0) {
            handler.postDelayed(this::showResult, 1500);
        } else {
            currentQuestionIndex++;
            handler.postDelayed(this::showQuestion, 1500);
        }
    }

    private void playSparkleAnimation(View view) {
        Animation sparkle = AnimationUtils.loadAnimation(this, R.anim.sparkle_burst);
        view.startAnimation(sparkle);
    }

    // Lightning bolt đã bị xóa

    private void playEnergyBeam() {
        // Simple animation for energy beam effect
        Animation beam = AnimationUtils.loadAnimation(this, R.anim.energy_beam);
        hitFXOverlay.setVisibility(View.VISIBLE);
        hitFXOverlay.startAnimation(beam);
        handler.postDelayed(() -> hitFXOverlay.setVisibility(View.GONE), 500);
    }

    private void playShieldRipple() {
        Animation ripple = AnimationUtils.loadAnimation(this, R.anim.shield_ripple);
        playerContainer.startAnimation(ripple);
    }

    private void showDamagePopup(int damage, boolean isEnemy) {
        tvDamagePopup.setText((isEnemy ? "-" : "+") + Math.abs(damage));
        tvDamagePopup.setTextColor(isEnemy ? 0xFFFFD93D : 0xFFEF4444);
        tvDamagePopup.setVisibility(View.VISIBLE);

        Animation popup = AnimationUtils.loadAnimation(this, R.anim.damage_popup_float);
        popup.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                tvDamagePopup.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        tvDamagePopup.startAnimation(popup);
    }

    private void showCombo(int comboCount) {
        tvCombo.setText("✨ Combo x" + comboCount);
        tvCombo.setVisibility(View.VISIBLE);

        Animation comboAnim = AnimationUtils.loadAnimation(this, R.anim.combo_appear);
        tvCombo.startAnimation(comboAnim);

        handler.postDelayed(() -> {
            Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_scale_in);
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    tvCombo.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            tvCombo.startAnimation(fadeOut);
        }, 2000);
    }

    private void animateStarToProgress() {
        // Simple star animation - could be enhanced with custom view
        // For now, just animate the star emoji in progress container
    }

    private void highlightCorrectAnswer(String correctAnswer) {
        Button[] buttons = {btnAnswer1, btnAnswer2, btnAnswer3, btnAnswer4};
        for (Button btn : buttons) {
            if (btn.getText().toString().equals(correctAnswer)) {
                btn.setBackgroundResource(R.drawable.button_answer_correct_v2);
                playSparkleAnimation(btn);
                break;
            }
        }
    }

    private void lockButtons() {
        btnAnswer1.setEnabled(false);
        btnAnswer2.setEnabled(false);
        btnAnswer3.setEnabled(false);
        btnAnswer4.setEnabled(false);
    }

    private void unlockButtons() {
        btnAnswer1.setEnabled(true);
        btnAnswer2.setEnabled(true);
        btnAnswer3.setEnabled(true);
        btnAnswer4.setEnabled(true);
    }

    private void resetButtonStyles() {
        btnAnswer1.setBackgroundResource(R.drawable.button_answer_selector);
        btnAnswer2.setBackgroundResource(R.drawable.button_answer_selector);
        btnAnswer3.setBackgroundResource(R.drawable.button_answer_selector);
        btnAnswer4.setBackgroundResource(R.drawable.button_answer_selector);
        unlockButtons();
    }

    // showFeedback đã bị xóa

    private void showResult() {
        resultCard.setVisibility(View.VISIBLE);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        resultCard.startAnimation(slideUp);

        boolean isVictory = enemyHealth <= 0 || correctAnswers > totalQuestions / 2;

        if (isVictory) {
            tvResultEmoji.setText("🏆");
            tvResultTitle.setText("You Win! 🌟");
            tvResultTitle.setTextColor(0xFFFFD93D);
            
            // Confetti
            confettiView.setVisibility(View.VISIBLE);
            confettiView.startConfetti();
            
            // Buddy celebration
            buddyManager.transitionToState(BuddyManager.CONTEXT_CELEBRATION);
        } else {
            tvResultEmoji.setText("💔");
            tvResultTitle.setText("So close! Try again 🚀");
            tvResultTitle.setTextColor(0xFFFF6B6B);
            
            // Buddy encouragement
            buddyManager.transitionToState(BuddyManager.CONTEXT_ENCOURAGEMENT);
        }

        // Calculate stars
        int stars = 0;
        float accuracy = (float) correctAnswers / totalQuestions;
        if (accuracy >= 0.9f) stars = 3;
        else if (accuracy >= 0.7f) stars = 2;
        else if (accuracy >= 0.5f) stars = 1;

        starRating.setRating(stars);

        int score = correctAnswers * 10 + (isVictory ? 50 : 0);
        tvResultScore.setText("Score: " + score);
        tvResultDetails.setText(correctAnswers + "/" + totalQuestions + " correct answers");

        // Save rewards using ProgressionManager (unified system)
        if (isVictory && stars > 0) {
            int starsEarned = stars * 2;
            
            // Get current scene/lesson info if available
            int sceneId = getIntent().getIntExtra("scene_id", -1);
            
            // Use ProgressionManager to add stars and trigger unlock checks
            progressionManager.recordGameCompleted("battle", starsEarned);
            dbHelper.addExperience(score);
            
            // If this is a lesson completion, record it
            if (sceneId > 0) {
                progressionManager.recordLessonCompleted(planetId, sceneId, starsEarned);
            }
            
            // Check for new unlocks and show notification
            progressionManager.checkForNewUnlocks();
            
            // Show unlock notification if planet/galaxy unlocked
            int totalStars = progressionManager.getUserProgress().getTotalStars();
            // This will be handled by ProgressionManager listeners
        }
    }

    private void restartBattle() {
        currentQuestionIndex = 0;
        playerHealth = 100;
        enemyHealth = 100;
        correctAnswers = 0;
        combo = 0;

        healthBarPlayer.setHealth(100, false);
        healthBarEnemy.setHealth(100, false);
        resultCard.setVisibility(View.GONE);
        confettiView.stopConfetti();
        confettiView.setVisibility(View.GONE);

        generateQuestions();
        spawnEnemy();
        showQuestion();
    }

    private void speakQuestion() {
        if (tts != null && tvQuestion != null) {
            String text = tvQuestion.getText().toString();
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "question");
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "TTS not supported", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Buddy Event Listener
    @Override
    public void onStateChanged(String newState, String previousState) {
        // Buddy state changed
    }

    @Override
    public void onBuddySpeak(String message) {
        // Buddy speaks
    }

    @Override
    public void onMoodChanged(int newMood) {
        // Buddy mood changed
    }

    @Override
    public void onBuddyLevelUp(int newLevel) {
        // Buddy leveled up
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        // BuddyManager cleanup handled automatically
        super.onDestroy();
    }

    // Battle question data class
    static class BattleQuestion {
        String english;
        String emoji;
        String correctAnswer;
        List<String> options;

        BattleQuestion(String english, String emoji, String correctAnswer, List<String> options) {
            this.english = english;
            this.emoji = emoji;
            this.correctAnswer = correctAnswer;
            this.options = options;
        }
    }
}

