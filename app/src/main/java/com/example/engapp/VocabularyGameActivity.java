package com.example.engapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class VocabularyGameActivity extends AppCompatActivity {
    private TextView tvQuestion, tvScore, tvProgress;
    private CardView cardOption1, cardOption2, cardOption3, cardOption4;
    private ImageView ivOption1, ivOption2, ivOption3, ivOption4;
    private View correctOverlay1, correctOverlay2, correctOverlay3, correctOverlay4;
    private View wrongOverlay1, wrongOverlay2, wrongOverlay3, wrongOverlay4;
    private ProgressBar progressBar;
    
    private TextToSpeech tts;
    private boolean ttsReady = false;
    
    private FirebaseFirestore db;
    private List<Vocabulary> allVocabulary = new ArrayList<>();
    private List<Vocabulary> gameQuestions = new ArrayList<>();
    
    private int currentQuestionIndex = 0;
    private int score = 0;
    private int totalQuestions = 10;
    private Vocabulary currentCorrectAnswer;
    private boolean isAnswered = false;
    private List<Vocabulary> currentOptions = new ArrayList<>();
    
    private CardView[] cardOptions;
    private ImageView[] imageOptions;
    private View[] correctOverlays;
    private View[] wrongOverlays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vocabulary_game);
        
        initViews();
        initTTS();
        initFirestore();
        loadVocabularyData();
    }
    
    private void initViews() {
        tvQuestion = findViewById(R.id.tvQuestion);
        tvScore = findViewById(R.id.tvScore);
        tvProgress = findViewById(R.id.tvProgress);
        progressBar = findViewById(R.id.progressBar);
        
        cardOption1 = findViewById(R.id.cardOption1);
        cardOption2 = findViewById(R.id.cardOption2);
        cardOption3 = findViewById(R.id.cardOption3);
        cardOption4 = findViewById(R.id.cardOption4);
        
        ivOption1 = findViewById(R.id.ivOption1);
        ivOption2 = findViewById(R.id.ivOption2);
        ivOption3 = findViewById(R.id.ivOption3);
        ivOption4 = findViewById(R.id.ivOption4);
        
        correctOverlay1 = findViewById(R.id.correctOverlay1);
        correctOverlay2 = findViewById(R.id.correctOverlay2);
        correctOverlay3 = findViewById(R.id.correctOverlay3);
        correctOverlay4 = findViewById(R.id.correctOverlay4);
        
        wrongOverlay1 = findViewById(R.id.wrongOverlay1);
        wrongOverlay2 = findViewById(R.id.wrongOverlay2);
        wrongOverlay3 = findViewById(R.id.wrongOverlay3);
        wrongOverlay4 = findViewById(R.id.wrongOverlay4);
        
        cardOptions = new CardView[]{cardOption1, cardOption2, cardOption3, cardOption4};
        imageOptions = new ImageView[]{ivOption1, ivOption2, ivOption3, ivOption4};
        correctOverlays = new View[]{correctOverlay1, correctOverlay2, correctOverlay3, correctOverlay4};
        wrongOverlays = new View[]{wrongOverlay1, wrongOverlay2, wrongOverlay3, wrongOverlay4};
        
        // Set click listeners
        for (int i = 0; i < cardOptions.length; i++) {
            final int index = i;
            cardOptions[i].setOnClickListener(v -> onOptionClicked(index));
        }
        
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
    
    private void initTTS() {
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(Locale.US);
                ttsReady = (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED);
            }
        });
    }
    
    private void initFirestore() {
        db = FirebaseFirestore.getInstance();
    }
    
    private void loadVocabularyData() {
        db.collection("vocabulary")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                allVocabulary.clear();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Vocabulary vocab = document.toObject(Vocabulary.class);
                    // Chỉ lấy từ có ảnh
                    if (vocab.getImage() != null && !vocab.getImage().isEmpty()) {
                        allVocabulary.add(vocab);
                    }
                }
                
                if (allVocabulary.size() >= 4) {
                    prepareGameQuestions();
                    showQuestion();
                } else {
                    Toast.makeText(this, "Không đủ dữ liệu để chơi game", Toast.LENGTH_SHORT).show();
                    finish();
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            });
    }
    
    private void prepareGameQuestions() {
        gameQuestions.clear();
        List<Vocabulary> shuffled = new ArrayList<>(allVocabulary);
        Collections.shuffle(shuffled);
        
        // Lấy số câu hỏi tối đa có thể
        int questionsCount = Math.min(totalQuestions, shuffled.size());
        for (int i = 0; i < questionsCount; i++) {
            gameQuestions.add(shuffled.get(i));
        }
        totalQuestions = questionsCount;
    }
    
    private void showQuestion() {
        if (currentQuestionIndex >= gameQuestions.size()) {
            showGameResult();
            return;
        }
        
        isAnswered = false;
        currentCorrectAnswer = gameQuestions.get(currentQuestionIndex);
        
        // Reset overlays
        for (View overlay : correctOverlays) {
            overlay.setVisibility(View.GONE);
        }
        for (View overlay : wrongOverlays) {
            overlay.setVisibility(View.GONE);
        }
        
        // Enable all cards
        for (CardView card : cardOptions) {
            card.setEnabled(true);
            card.setAlpha(1.0f);
        }
        
        // Update UI
        tvQuestion.setText(currentCorrectAnswer.getTerm());
        tvProgress.setText((currentQuestionIndex + 1) + "/" + totalQuestions);
        progressBar.setMax(totalQuestions);
        progressBar.setProgress(currentQuestionIndex + 1);
        
        // Entrance animation for question
        animateQuestionEntrance();
        
        // Prepare options
        currentOptions = prepareOptions();
        Collections.shuffle(currentOptions);
        
        // Load images
        for (int i = 0; i < 4; i++) {
            Vocabulary vocab = currentOptions.get(i);
            Glide.with(this)
                .load(vocab.getImage())
                .placeholder(R.drawable.avatar_circle_bg)
                .error(R.drawable.avatar_circle_bg)
                .centerCrop()
                .into(imageOptions[i]);
            
            // Entrance animation for cards
            animateCardEntrance(cardOptions[i], i);
        }
    }
    
    private List<Vocabulary> prepareOptions() {
        List<Vocabulary> options = new ArrayList<>();
        options.add(currentCorrectAnswer);
        
        // Lấy 3 đáp án sai khác category để tránh nhầm lẫn
        List<Vocabulary> otherVocabs = new ArrayList<>(allVocabulary);
        otherVocabs.remove(currentCorrectAnswer);
        Collections.shuffle(otherVocabs);
        
        int added = 0;
        for (Vocabulary vocab : otherVocabs) {
            if (added >= 3) break;
            if (vocab.getId() != currentCorrectAnswer.getId()) {
                options.add(vocab);
                added++;
            }
        }
        
        // Nếu không đủ, lấy bất kỳ
        while (options.size() < 4 && otherVocabs.size() > 0) {
            options.add(otherVocabs.get(0));
            otherVocabs.remove(0);
        }
        
        return options;
    }
    
    private void onOptionClicked(int optionIndex) {
        if (isAnswered) return;
        
        isAnswered = true;
        
        // Disable all cards
        for (CardView card : cardOptions) {
            card.setEnabled(false);
        }
        
        // Get clicked vocabulary
        Vocabulary clickedVocab = getVocabularyFromImage(imageOptions[optionIndex]);
        
        // Speak the word
        if (ttsReady) {
            tts.speak(currentCorrectAnswer.getTerm(), TextToSpeech.QUEUE_FLUSH, null, null);
        }
        
        // Check answer
        boolean isCorrect = clickedVocab != null && clickedVocab.getId() == currentCorrectAnswer.getId();
        
        if (isCorrect) {
            handleCorrectAnswer(optionIndex);
        } else {
            handleWrongAnswer(optionIndex);
        }
    }
    
    private void handleCorrectAnswer(int optionIndex) {
        score += 10;
        tvScore.setText("Score: " + score);
        
        // Show correct overlay with animation
        correctOverlays[optionIndex].setVisibility(View.VISIBLE);
        correctOverlays[optionIndex].setAlpha(0f);
        correctOverlays[optionIndex].animate()
            .alpha(1f)
            .setDuration(300)
            .start();
        
        // Scale animation
        ScaleAnimation scaleAnimation = new ScaleAnimation(
            1.0f, 1.1f, 1.0f, 1.1f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleAnimation.setDuration(200);
        scaleAnimation.setRepeatCount(1);
        scaleAnimation.setRepeatMode(Animation.REVERSE);
        cardOptions[optionIndex].startAnimation(scaleAnimation);
        
        // Vibrate effect (visual)
        cardOptions[optionIndex].animate()
            .scaleX(1.05f)
            .scaleY(1.05f)
            .setDuration(150)
            .withEndAction(() -> 
                cardOptions[optionIndex].animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(150)
                    .start()
            ).start();
        
        // Next question after delay
        new Handler().postDelayed(() -> {
            currentQuestionIndex++;
            showQuestion();
        }, 1500);
    }
    
    private void handleWrongAnswer(int wrongIndex) {
        // Show wrong overlay
        wrongOverlays[wrongIndex].setVisibility(View.VISIBLE);
        wrongOverlays[wrongIndex].setAlpha(0f);
        wrongOverlays[wrongIndex].animate()
            .alpha(1f)
            .setDuration(300)
            .start();
        
        // Shake animation
        ObjectAnimator animator = ObjectAnimator.ofFloat(cardOptions[wrongIndex], "translationX", 0, 25, -25, 25, -25, 15, -15, 6, -6, 0);
        animator.setDuration(500);
        animator.start();
        
        // Fade out wrong option
        cardOptions[wrongIndex].animate()
            .alpha(0.3f)
            .setDuration(300)
            .start();
        
        // Show correct answer
        new Handler().postDelayed(() -> {
            for (int i = 0; i < imageOptions.length; i++) {
                Vocabulary vocab = getVocabularyFromImage(imageOptions[i]);
                if (vocab != null && vocab.getId() == currentCorrectAnswer.getId()) {
                    final int correctIndex = i;
                    correctOverlays[correctIndex].setVisibility(View.VISIBLE);
                    correctOverlays[correctIndex].setAlpha(0f);
                    correctOverlays[correctIndex].animate()
                        .alpha(1f)
                        .setDuration(300)
                        .start();
                    
                    // Pulse animation for correct answer
                    cardOptions[correctIndex].animate()
                        .scaleX(1.1f)
                        .scaleY(1.1f)
                        .setDuration(300)
                        .withEndAction(() -> 
                            cardOptions[correctIndex].animate()
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .setDuration(300)
                                .start()
                        ).start();
                    break;
                }
            }
            
            // Next question after delay
            new Handler().postDelayed(() -> {
                currentQuestionIndex++;
                showQuestion();
            }, 1500);
        }, 800);
    }
    
    private Vocabulary getVocabularyFromImage(ImageView imageView) {
        int index = -1;
        for (int i = 0; i < imageOptions.length; i++) {
            if (imageOptions[i] == imageView) {
                index = i;
                break;
            }
        }
        return index >= 0 && index < currentOptions.size() ? currentOptions.get(index) : null;
    }
    
    private void animateQuestionEntrance() {
        tvQuestion.setAlpha(0f);
        tvQuestion.setTranslationY(-50f);
        tvQuestion.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(400)
            .setInterpolator(new AccelerateDecelerateInterpolator())
            .start();
    }
    
    private void animateCardEntrance(CardView card, int position) {
        card.setAlpha(0f);
        card.setScaleX(0.8f);
        card.setScaleY(0.8f);
        
        new Handler().postDelayed(() -> {
            card.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
        }, position * 100);
    }
    
    private void showGameResult() {
        Toast.makeText(this, "Game Over! Score: " + score + "/" + (totalQuestions * 10), Toast.LENGTH_LONG).show();
        finish();
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
