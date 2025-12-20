package com.example.engapp;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.engapp.database.GameDatabaseHelper;
import com.example.engapp.database.GameDatabaseHelper.WordData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class BattleActivity extends AppCompatActivity {

    private TextView tvQuestionCount, tvQuestionType, tvQuestionEmoji, tvQuestion;
    private TextView tvFeedback, tvPlayerEmoji, tvEnemyEmoji, tvEnemyName;
    private ProgressBar progressPlayerHealth, progressEnemyHealth;
    private Button btnAnswer1, btnAnswer2, btnAnswer3, btnAnswer4;
    private Button btnRetry, btnContinue;
    private CardView resultCard;
    private TextView tvResultEmoji, tvResultTitle, tvResultStars, tvResultScore, tvResultDetails;
    private ImageButton btnBack;
    private View playerContainer;

    private GameDatabaseHelper dbHelper;
    private List<WordData> words;
    private List<BattleQuestion> questions;
    private int currentQuestionIndex = 0;
    private int playerHealth = 100;
    private int enemyHealth = 100;
    private int correctAnswers = 0;
    private int totalQuestions = 10;
    private boolean isAnswering = true;

    private int planetId;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle);

        dbHelper = GameDatabaseHelper.getInstance(this);
        planetId = getIntent().getIntExtra("planet_id", 1);

        initViews();
        loadWords();
        generateQuestions();
        showQuestion();
    }

    private void initViews() {
        tvQuestionCount = findViewById(R.id.tvQuestionCount);
        tvQuestionType = findViewById(R.id.tvQuestionType);
        tvQuestionEmoji = findViewById(R.id.tvQuestionEmoji);
        tvQuestion = findViewById(R.id.tvQuestion);
        tvFeedback = findViewById(R.id.tvFeedback);
        tvPlayerEmoji = findViewById(R.id.tvPlayerEmoji);
        tvEnemyEmoji = findViewById(R.id.tvEnemyEmoji);
        tvEnemyName = findViewById(R.id.tvEnemyName);
        progressPlayerHealth = findViewById(R.id.progressPlayerHealth);
        progressEnemyHealth = findViewById(R.id.progressEnemyHealth);
        btnAnswer1 = findViewById(R.id.btnAnswer1);
        btnAnswer2 = findViewById(R.id.btnAnswer2);
        btnAnswer3 = findViewById(R.id.btnAnswer3);
        btnAnswer4 = findViewById(R.id.btnAnswer4);
        btnRetry = findViewById(R.id.btnRetry);
        btnContinue = findViewById(R.id.btnContinue);
        resultCard = findViewById(R.id.resultCard);
        tvResultEmoji = findViewById(R.id.tvResultEmoji);
        tvResultTitle = findViewById(R.id.tvResultTitle);
        tvResultStars = findViewById(R.id.tvResultStars);
        tvResultScore = findViewById(R.id.tvResultScore);
        tvResultDetails = findViewById(R.id.tvResultDetails);
        btnBack = findViewById(R.id.btnBack);
        playerContainer = findViewById(R.id.playerContainer);

        btnBack.setOnClickListener(v -> onBackPressed());

        View.OnClickListener answerListener = v -> {
            if (!isAnswering) return;
            Button btn = (Button) v;
            checkAnswer(btn);
        };

        btnAnswer1.setOnClickListener(answerListener);
        btnAnswer2.setOnClickListener(answerListener);
        btnAnswer3.setOnClickListener(answerListener);
        btnAnswer4.setOnClickListener(answerListener);

        btnRetry.setOnClickListener(v -> restartBattle());
        btnContinue.setOnClickListener(v -> finish());

        // Random enemy
        String[] enemies = {"👾", "👹", "🤖", "👻", "🦖"};
        String[] enemyNames = {"Word Monster", "Vocab Beast", "Robo Quiz", "Ghost Learner", "Dino Word"};
        int enemyIndex = new Random().nextInt(enemies.length);
        tvEnemyEmoji.setText(enemies[enemyIndex]);
        tvEnemyName.setText(enemyNames[enemyIndex]);
    }

    private void loadWords() {
        words = dbHelper.getWordsForPlanet(planetId);
        if (words == null || words.size() < 4) {
            // Load default words if not enough
            words = dbHelper.getWordsForPlanet(1);
        }
    }

    private void generateQuestions() {
        questions = new ArrayList<>();
        Collections.shuffle(words);

        int questionCount = Math.min(totalQuestions, words.size());
        for (int i = 0; i < questionCount; i++) {
            WordData word = words.get(i);

            // Generate wrong answers
            List<String> wrongAnswers = new ArrayList<>();
            for (WordData w : words) {
                if (!w.vietnamese.equals(word.vietnamese)) {
                    wrongAnswers.add(w.vietnamese);
                }
            }
            Collections.shuffle(wrongAnswers);

            List<String> options = new ArrayList<>();
            options.add(word.vietnamese); // Correct answer
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

    private void showQuestion() {
        if (currentQuestionIndex >= questions.size()) {
            showResult();
            return;
        }

        BattleQuestion question = questions.get(currentQuestionIndex);

        tvQuestionCount.setText((currentQuestionIndex + 1) + "/" + totalQuestions);
        tvQuestionType.setText("What does this mean?");
        tvQuestionEmoji.setText(question.emoji);
        tvQuestion.setText(question.english);

        btnAnswer1.setText(question.options.get(0));
        btnAnswer2.setText(question.options.get(1));
        btnAnswer3.setText(question.options.size() > 2 ? question.options.get(2) : "");
        btnAnswer4.setText(question.options.size() > 3 ? question.options.get(3) : "");

        btnAnswer3.setVisibility(question.options.size() > 2 ? View.VISIBLE : View.GONE);
        btnAnswer4.setVisibility(question.options.size() > 3 ? View.VISIBLE : View.GONE);

        resetButtonStyles();
        tvFeedback.setVisibility(View.INVISIBLE);
        isAnswering = true;
    }

    private void checkAnswer(Button selectedButton) {
        isAnswering = false;
        BattleQuestion question = questions.get(currentQuestionIndex);
        String selectedAnswer = selectedButton.getText().toString();

        if (selectedAnswer.equals(question.correctAnswer)) {
            // Correct!
            correctAnswers++;
            enemyHealth -= 10;
            progressEnemyHealth.setProgress(enemyHealth);

            selectedButton.setBackgroundResource(R.drawable.button_answer_correct);
            tvFeedback.setText("✓ Correct!");
            tvFeedback.setTextColor(0xFF4CAF50);
            tvFeedback.setVisibility(View.VISIBLE);

            // Attack animation on enemy
            View enemyContainer = findViewById(R.id.enemyContainer);
            enemyContainer.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake));
        } else {
            // Wrong!
            playerHealth -= 10;
            progressPlayerHealth.setProgress(playerHealth);

            selectedButton.setBackgroundResource(R.drawable.button_answer_wrong);
            tvFeedback.setText("✗ Wrong! It's: " + question.correctAnswer);
            tvFeedback.setTextColor(0xFFF44336);
            tvFeedback.setVisibility(View.VISIBLE);

            // Show correct answer
            highlightCorrectAnswer(question.correctAnswer);

            // Shake player
            playerContainer.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake));
        }

        // Check if battle ended
        if (playerHealth <= 0 || enemyHealth <= 0) {
            handler.postDelayed(this::showResult, 1500);
        } else {
            currentQuestionIndex++;
            handler.postDelayed(this::showQuestion, 1500);
        }
    }

    private void highlightCorrectAnswer(String correctAnswer) {
        Button[] buttons = {btnAnswer1, btnAnswer2, btnAnswer3, btnAnswer4};
        for (Button btn : buttons) {
            if (btn.getText().toString().equals(correctAnswer)) {
                btn.setBackgroundResource(R.drawable.button_answer_correct);
                break;
            }
        }
    }

    private void resetButtonStyles() {
        btnAnswer1.setBackgroundResource(R.drawable.button_answer_background);
        btnAnswer2.setBackgroundResource(R.drawable.button_answer_background);
        btnAnswer3.setBackgroundResource(R.drawable.button_answer_background);
        btnAnswer4.setBackgroundResource(R.drawable.button_answer_background);
    }

    private void showResult() {
        resultCard.setVisibility(View.VISIBLE);

        boolean isVictory = enemyHealth <= 0 || correctAnswers > totalQuestions / 2;

        if (isVictory) {
            tvResultEmoji.setText("🏆");
            tvResultTitle.setText("Victory!");
            tvResultTitle.setTextColor(0xFFFFD700);
        } else {
            tvResultEmoji.setText("💔");
            tvResultTitle.setText("Defeated");
            tvResultTitle.setTextColor(0xFFFF6B6B);
        }

        // Calculate stars
        int stars = 0;
        float accuracy = (float) correctAnswers / totalQuestions;
        if (accuracy >= 0.9f) stars = 3;
        else if (accuracy >= 0.7f) stars = 2;
        else if (accuracy >= 0.5f) stars = 1;

        String starDisplay = "";
        for (int i = 0; i < 3; i++) {
            starDisplay += (i < stars) ? "⭐" : "☆";
        }
        tvResultStars.setText(starDisplay);

        int score = correctAnswers * 10 + (isVictory ? 50 : 0);
        tvResultScore.setText("Score: " + score);
        tvResultDetails.setText(correctAnswers + "/" + totalQuestions + " correct answers");

        // Save rewards
        if (isVictory && stars > 0) {
            dbHelper.addStars(stars * 2);
            dbHelper.addExperience(score);
        }
    }

    private void restartBattle() {
        currentQuestionIndex = 0;
        playerHealth = 100;
        enemyHealth = 100;
        correctAnswers = 0;

        progressPlayerHealth.setProgress(100);
        progressEnemyHealth.setProgress(100);
        resultCard.setVisibility(View.GONE);

        generateQuestions();
        showQuestion();
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

