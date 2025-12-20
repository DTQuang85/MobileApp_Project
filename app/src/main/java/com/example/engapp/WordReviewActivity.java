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
import java.util.Collections;
import java.util.List;

public class WordReviewActivity extends AppCompatActivity {

    private CardView cardFlashcard, cardComplete;
    private LinearLayout frontCard, backCard, ratingButtons;
    private TextView tvWordEmoji, tvWordEnglish, tvPronunciation, tvTapHint;
    private TextView tvWordVietnamese, tvExample, tvExampleVi;
    private TextView tvProgress, tvStreak, tvReviewStats;
    private ProgressBar progressReview;
    private Button btnHard, btnGood, btnEasy, btnFinish;
    private ImageButton btnBack;

    private GameDatabaseHelper dbHelper;
    private List<WordData> words;
    private int currentIndex = 0;
    private int streak = 0;
    private boolean isFlipped = false;

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
        // Get words that need review (learned words)
        words = dbHelper.getLearnedWords();

        if (words == null || words.isEmpty()) {
            // If no learned words, get some from planet 1
            words = dbHelper.getWordsForPlanet(1);
        }

        if (words != null && words.size() > 10) {
            Collections.shuffle(words);
            words = words.subList(0, 10);
        }
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
        // Update spaced repetition data (simplified)

        if (rating >= 2) {
            streak++;
        } else {
            streak = 0;
        }
        tvStreak.setText("🔥 " + streak);

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
        tvReviewStats.setText("You reviewed " + wordsReviewed + " words today\n🔥 Best streak: " + streak);

        // Award completion bonus
        dbHelper.addStars(wordsReviewed);
        dbHelper.addExperience(wordsReviewed * 5);

        cardComplete.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_scale_in));
    }
}

