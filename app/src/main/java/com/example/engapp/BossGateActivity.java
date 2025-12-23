package com.example.engapp;

import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.engapp.database.GameDatabaseHelper;
import com.example.engapp.database.GameDatabaseHelper.*;
import com.example.engapp.manager.ProgressionManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class BossGateActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private TextView tvBossEmoji, tvBossName, tvBossMessage;
    private TextView tvHealth, tvProgress;
    private ProgressBar progressBoss;
    private LinearLayout optionsContainer;
    private Button btnListen;
    private ImageView btnBack;

    private TextToSpeech tts;
    private GameDatabaseHelper dbHelper;
    private ProgressionManager progressionManager;
    private int planetId, sceneId;
    private List<WordData> words;
    private int currentIndex = 0;
    private int bossHealth = 100;
    private int score = 0;
    private int correctAnswer = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boss_gate);
        overridePendingTransition(R.anim.fade_scale_in, 0);

        planetId = getIntent().getIntExtra("planet_id", 1);
        sceneId = getIntent().getIntExtra("scene_id", 1);

        dbHelper = GameDatabaseHelper.getInstance(this);
        progressionManager = ProgressionManager.getInstance(this);
        tts = new TextToSpeech(this, this);

        initViews();
        loadWords();
        showBossIntro();
    }

    private void initViews() {
        tvBossEmoji = findViewById(R.id.tvBossEmoji);
        tvBossName = findViewById(R.id.tvBossName);
        tvBossMessage = findViewById(R.id.tvBossMessage);
        tvHealth = findViewById(R.id.tvHealth);
        tvProgress = findViewById(R.id.tvProgress);
        progressBoss = findViewById(R.id.progressBoss);
        optionsContainer = findViewById(R.id.optionsContainer);
        btnListen = findViewById(R.id.btnListen);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
        btnListen.setOnClickListener(v -> speakCurrentWord());
    }

    private void loadWords() {
        words = dbHelper.getWordsForPlanet(planetId);
        if (words == null || words.isEmpty()) {
            Toast.makeText(this, "Không có từ vựng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Collections.shuffle(words);
        if (words.size() > 5) {
            words = words.subList(0, 5);
        }
    }

    private void showBossIntro() {
        tvBossEmoji.setText("👾");
        tvBossName.setText("Nhiễu Sóng Boss");
        tvBossMessage.setText("Grr! Tôi đang chặn cổng Portal!\n\nHãy nghe và chọn đúng từ để đánh bại tôi!");

        // Shake animation for boss
        tvBossEmoji.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_scale_in));

        new Handler().postDelayed(this::showCurrentChallenge, 2000);
    }

    private void showCurrentChallenge() {
        if (bossHealth <= 0 || currentIndex >= words.size()) {
            endBattle();
            return;
        }

        WordData word = words.get(currentIndex);

        tvBossMessage.setText("Nghe và chọn đúng từ!\n\n🔊 Nhấn nút nghe bên dưới");

        // Generate options
        List<String> options = new ArrayList<>();
        options.add(word.english);

        for (WordData w : words) {
            if (!w.english.equals(word.english) && options.size() < 4) {
                options.add(w.english);
            }
        }

        while (options.size() < 4) {
            options.add("unknown");
        }

        Collections.shuffle(options);
        correctAnswer = options.indexOf(word.english);

        setupOptions(options, word);
        updateProgress();
    }

    private void setupOptions(List<String> options, WordData correctWord) {
        optionsContainer.removeAllViews();

        for (int i = 0; i < options.size(); i++) {
            final int index = i;
            final String option = options.get(i);

            CardView card = new CardView(this);
            card.setCardBackgroundColor(getColor(R.color.card_bg));
            card.setRadius(32);
            card.setCardElevation(8);

            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(0, 0, 0, 16);
            card.setLayoutParams(cardParams);

            TextView tv = new TextView(this);
            tv.setText(option);
            tv.setTextColor(getColor(R.color.text_primary));
            tv.setTextSize(18);
            tv.setPadding(32, 24, 32, 24);
            tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

            card.addView(tv);

            card.setOnClickListener(v -> checkAnswer(index, card, correctWord));

            optionsContainer.addView(card);
        }
    }

    private void checkAnswer(int selected, CardView card, WordData correctWord) {
        // Disable all options
        for (int i = 0; i < optionsContainer.getChildCount(); i++) {
            optionsContainer.getChildAt(i).setClickable(false);
        }

        if (selected == correctAnswer) {
            // Correct - damage boss
            card.setCardBackgroundColor(getColor(R.color.correct_green));
            bossHealth -= 20;
            score += 20;

            Toast.makeText(this, "💥 Đánh trúng Boss!", Toast.LENGTH_SHORT).show();

            // Boss react
            tvBossMessage.setText("Argh! Bạn đúng rồi! 💢");
            tvBossEmoji.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_scale_in));
        } else {
            // Wrong
            card.setCardBackgroundColor(getColor(R.color.wrong_red));

            // Show correct answer
            CardView correctCard = (CardView) optionsContainer.getChildAt(correctAnswer);
            correctCard.setCardBackgroundColor(getColor(R.color.correct_green));

            Toast.makeText(this, "❌ Sai rồi!", Toast.LENGTH_SHORT).show();
            tvBossMessage.setText("Haha! Sai rồi! 😈");
        }

        progressBoss.setProgress(bossHealth);
        tvHealth.setText("❤️ " + bossHealth + "%");

        new Handler().postDelayed(() -> {
            currentIndex++;
            showCurrentChallenge();
        }, 1500);
    }

    private void speakCurrentWord() {
        if (currentIndex < words.size() && tts != null) {
            WordData word = words.get(currentIndex);
            tts.speak(word.english, TextToSpeech.QUEUE_FLUSH, null, "word");
        }
    }

    private void updateProgress() {
        tvProgress.setText("Câu " + (currentIndex + 1) + "/" + words.size());
    }

    private void endBattle() {
        boolean victory = bossHealth <= 0;
        int stars = victory ? 3 : score >= 40 ? 2 : 1;

        // Save progress
        dbHelper.updateSceneProgress(sceneId, stars);
        dbHelper.addStars(stars);
        
        // IMPORTANT: Record lesson completion to unlock next lesson
        if (planetId > 0 && sceneId > 0) {
            progressionManager.recordLessonCompleted(planetId, sceneId, stars);
        }

        // Award fuel cell for completing boss
        if (victory) {
            dbHelper.addFuelCells(1);
        }

        String icon = victory ? "🏆" : "💪";
        String title = victory ? "CHIẾN THẮNG!" : "Cố gắng thêm!";
        String message = victory ?
            "Bạn đã đánh bại Nhiễu Sóng Boss!\n\n🔋 +1 Fuel Cell" :
            "Lần sau sẽ tốt hơn!";

        SpaceDialog.showResult(this, icon, title, message, stars, "Hoàn thành", () -> finish());
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

