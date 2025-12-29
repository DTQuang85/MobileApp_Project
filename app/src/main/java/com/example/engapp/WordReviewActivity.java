package com.example.engapp;

import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.engapp.database.GameDatabaseHelper;
import com.example.engapp.database.GameDatabaseHelper.WordData;
import com.example.engapp.database.GameDatabaseHelper.UserProgressData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WordReviewActivity extends AppCompatActivity {

    public static final String EXTRA_REVIEW_MODE = "review_mode";
    public static final String MODE_STANDARD = "standard";
    public static final String MODE_ADAPTIVE = "adaptive";
    public static final String MODE_DUE = "due";
    public static final String MODE_DRILL = "drill";

    private CardView cardFlashcard, cardComplete;
    private LinearLayout frontCard, backCard, ratingButtons;
    private TextView tvReviewTitle, tvModeHint, tvWordEmoji, tvWordEnglish, tvPronunciation, tvTapHint;
    private TextView tvWordVietnamese, tvExample, tvExampleVi;
    private TextView tvProgress, tvStreak, tvReviewStats;
    private ProgressBar progressReview, progressMastery;
    private TextView tvMasteryValue;
    private Button btnHard, btnGood, btnEasy, btnFinish;
    private ImageButton btnBack;

    private GameDatabaseHelper dbHelper;
    private List<WordData> words;
    private int currentIndex = 0;
    private int streak = 0;
    private boolean isFlipped = false;
    private boolean isAdaptiveMode = false;
    private boolean isDueMode = false;
    private boolean isDrillMode = false;
    private boolean usedFallback = false;
    private final Map<Integer, Integer> drillRetryCount = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_review);

        dbHelper = GameDatabaseHelper.getInstance(this);

        initViews();
        loadWords();
        showWord();
    }

    private void initViews() {
        cardFlashcard = findViewById(R.id.cardFlashcard);
        cardComplete = findViewById(R.id.cardComplete);
        frontCard = findViewById(R.id.frontCard);
        backCard = findViewById(R.id.backCard);
        ratingButtons = findViewById(R.id.ratingButtons);
        tvReviewTitle = findViewById(R.id.tvReviewTitle);
        tvModeHint = findViewById(R.id.tvModeHint);
        tvWordEmoji = findViewById(R.id.tvWordEmoji);
        tvWordEnglish = findViewById(R.id.tvWordEnglish);
        tvPronunciation = findViewById(R.id.tvPronunciation);
        tvTapHint = findViewById(R.id.tvTapHint);
        tvWordVietnamese = findViewById(R.id.tvWordVietnamese);
        tvExample = findViewById(R.id.tvExample);
        tvExampleVi = findViewById(R.id.tvExampleVi);
        tvProgress = findViewById(R.id.tvProgress);
        tvStreak = findViewById(R.id.tvStreak);
        tvReviewStats = findViewById(R.id.tvReviewStats);
        progressReview = findViewById(R.id.progressReview);
        progressMastery = findViewById(R.id.progressMastery);
        tvMasteryValue = findViewById(R.id.tvMasteryValue);
        btnHard = findViewById(R.id.btnHard);
        btnGood = findViewById(R.id.btnGood);
        btnEasy = findViewById(R.id.btnEasy);
        btnFinish = findViewById(R.id.btnFinish);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> onBackPressed());

        cardFlashcard.setOnClickListener(v -> flipCard());

        btnHard.setOnClickListener(v -> rateWord(1));
        btnGood.setOnClickListener(v -> rateWord(2));
        btnEasy.setOnClickListener(v -> rateWord(3));
        btnFinish.setOnClickListener(v -> finish());
    }

    private void loadWords() {
        String mode = getIntent().getStringExtra(EXTRA_REVIEW_MODE);
        isAdaptiveMode = MODE_ADAPTIVE.equals(mode);
        isDueMode = MODE_DUE.equals(mode);
        isDrillMode = MODE_DRILL.equals(mode);

        if (tvReviewTitle != null) {
            tvReviewTitle.setText(getModeTitle());
        }

        usedFallback = false;
        if (isDueMode) {
            words = dbHelper.getDueWords(10);
        } else if (isDrillMode) {
            words = dbHelper.getErrorDrillWords(10);
        } else if (isAdaptiveMode) {
            words = buildAdaptiveList(10);
        } else {
            words = dbHelper.getLearnedWords();
        }

        if (words == null || words.isEmpty()) {
            UserProgressData progress = dbHelper.getUserProgress();
            int fallbackPlanetId = progress != null ? progress.currentPlanetId : 1;
            words = dbHelper.getWordsForPlanet(fallbackPlanetId);
            usedFallback = true;
        }

        if (words != null && words.size() > 10) {
            if (!isAdaptiveMode || usedFallback) {
                Collections.shuffle(words);
            }
            words = new ArrayList<>(words.subList(0, 10));
        } else if (words != null) {
            words = new ArrayList<>(words);
        }

        drillRetryCount.clear();
        updateModeHint();
    }

    private String getModeTitle() {
        if (isDueMode) {
            return "Due Review";
        }
        if (isDrillMode) {
            return "Error Drill";
        }
        if (isAdaptiveMode) {
            return "Adaptive Review";
        }
        return "Word Review";
    }

    private void updateModeHint() {
        if (tvModeHint == null) {
            return;
        }
        if (usedFallback) {
            tvModeHint.setText("No review data yet. Using current planet.");
            return;
        }
        if (isDueMode) {
            int dueCount = dbHelper.getDueWordCount();
            tvModeHint.setText("Due today: " + dueCount + " words");
            return;
        }
        if (isDrillMode) {
            int drillCount = dbHelper.getErrorDrillWordCount();
            tvModeHint.setText("Error drill: " + drillCount + " weak words");
            return;
        }
        if (isAdaptiveMode) {
            int adaptiveCount = dbHelper.getAdaptiveWordCount();
            tvModeHint.setText("Mix due and weak words (" + adaptiveCount + " total)");
            return;
        }
        tvModeHint.setText("Review learned words");
    }

    private List<WordData> buildAdaptiveList(int limit) {
        List<WordData> result = new ArrayList<>();
        Set<Integer> seen = new HashSet<>();

        List<WordData> dueWords = dbHelper.getDueWords(limit);
        if (dueWords != null) {
            for (WordData word : dueWords) {
                if (word != null && seen.add(word.id)) {
                    result.add(word);
                }
            }
        }

        if (result.size() < limit) {
            List<WordData> learned = dbHelper.getLearnedWords();
            if (learned != null) {
                learned.sort(Comparator.comparingInt(this::calculateMasteryScore));
                for (WordData word : learned) {
                    if (result.size() >= limit) {
                        break;
                    }
                    if (word != null && seen.add(word.id)) {
                        result.add(word);
                    }
                }
            }
        }
        return result;
    }

    private int calculateMasteryScore(WordData word) {
        if (word == null) {
            return 0;
        }
        int attempts = word.timesCorrect + word.timesWrong;
        double accuracy = attempts > 0 ? (double) word.timesCorrect / attempts : 0.0;
        double spacing = word.srIntervalDays > 0 ? Math.min(word.srIntervalDays / 10.0, 1.0) : 0.0;
        double ease = word.srEase > 0 ? Math.min((word.srEase - 1.3) / 1.7, 1.0) : 0.0;
        double duePenalty = (word.srNextDue > 0 && word.srNextDue <= System.currentTimeMillis()) ? 0.15 : 0.0;

        double score = (accuracy * 0.6) + (spacing * 0.3) + (ease * 0.1) - duePenalty;
        score = Math.max(0.0, Math.min(1.0, score));
        return (int) Math.round(score * 100.0);
    }

    private int calculateAverageMastery(int count) {
        if (words == null || words.isEmpty() || count <= 0) {
            return 0;
        }
        int limit = Math.min(count, words.size());
        int total = 0;
        for (int i = 0; i < limit; i++) {
            total += calculateMasteryScore(words.get(i));
        }
        return Math.round((float) total / limit);
    }

    private void showWord() {
        if (words == null || words.isEmpty()) {
            showComplete();
            return;
        }

        if (currentIndex >= words.size()) {
            showComplete();
            return;
        }

        WordData word = words.get(currentIndex);

        // Front card
        tvWordEmoji.setText(word.emoji != null ? word.emoji : "📝");
        tvWordEnglish.setText(word.english);
        tvPronunciation.setText(word.pronunciation != null ? word.pronunciation : "");

        // Back card
        tvWordVietnamese.setText(word.vietnamese);
        tvExample.setText(word.exampleSentence != null ? word.exampleSentence : "");
        tvExampleVi.setText(word.exampleTranslation != null ? word.exampleTranslation : "");

        // Progress
        tvProgress.setText((currentIndex + 1) + " / " + words.size());
        int progress = ((currentIndex + 1) * 100) / words.size();
        progressReview.setProgress(progress);

        int mastery = calculateMasteryScore(word);
        if (progressMastery != null) {
            progressMastery.setProgress(mastery);
        }
        if (tvMasteryValue != null) {
            tvMasteryValue.setText(mastery + "%");
        }

        // Reset card state
        frontCard.setVisibility(View.VISIBLE);
        backCard.setVisibility(View.GONE);
        ratingButtons.setVisibility(View.GONE);
        tvTapHint.setVisibility(View.VISIBLE);
        isFlipped = false;

        // Animate
        cardFlashcard.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_scale_in));
    }

    private void flipCard() {
        if (isFlipped) return;

        isFlipped = true;
        tvTapHint.setVisibility(View.GONE);

        // Flip animation
        cardFlashcard.animate()
                .rotationY(90)
                .setDuration(150)
                .withEndAction(() -> {
                    frontCard.setVisibility(View.GONE);
                    backCard.setVisibility(View.VISIBLE);
                    cardFlashcard.setRotationY(-90);
                    cardFlashcard.animate()
                            .rotationY(0)
                            .setDuration(150)
                            .start();
                })
                .start();

        ratingButtons.setVisibility(View.VISIBLE);
        ratingButtons.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_right));
    }

    private void rateWord(int rating) {
        // Rating: 1=hard, 2=good, 3=easy
        // Update review stats and schedule

        if (words == null || currentIndex >= words.size()) {
            return;
        }

        WordData word = words.get(currentIndex);
        dbHelper.applyReviewResult(word, rating);

        if (isDrillMode && rating <= 1) {
            int retries = drillRetryCount.getOrDefault(word.id, 0);
            if (retries < 1) {
                drillRetryCount.put(word.id, retries + 1);
                words.add(word);
            }
        }

        if (rating >= 2) {
            streak++;
        } else {
            streak = 0;
        }
        tvStreak.setText("Streak: " + streak);

        // Award XP based on rating
        int xp = rating * 2;
        dbHelper.addExperience(xp);

        currentIndex++;
        showWord();
    }

    private void showComplete() {
        cardFlashcard.setVisibility(View.GONE);
        ratingButtons.setVisibility(View.GONE);
        cardComplete.setVisibility(View.VISIBLE);

        int wordsReviewed = words != null ? Math.min(currentIndex, words.size()) : 0;
        int avgMastery = calculateAverageMastery(wordsReviewed);
        String modeLabel = isDueMode ? "Due review" : (isDrillMode ? "Error drill" : (isAdaptiveMode ? "Adaptive review" : "Review"));
        tvReviewStats.setText(modeLabel + " complete\nReviewed " + wordsReviewed + " words\nBest streak: " + streak + "  Avg mastery: " + avgMastery + "%");

        // Award completion bonus
        dbHelper.addStars(wordsReviewed);
        dbHelper.addExperience(wordsReviewed * 5);

        cardComplete.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_scale_in));
    }
}
