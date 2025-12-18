package com.example.engapp;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.engapp.database.GameDatabaseHelper;
import com.example.engapp.database.GameDatabaseHelper.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ExploreActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private TextView tvProgress, tvCrystals, tvInstruction;
    private ProgressBar progressBar;
    private GridLayout crystalGrid;
    private ImageView btnBack;

    private TextToSpeech tts;
    private GameDatabaseHelper dbHelper;
    private int planetId, sceneId;
    private List<WordData> words;
    private List<WordData> collectedWords = new ArrayList<>();
    private int totalCrystals = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);
        overridePendingTransition(R.anim.fade_scale_in, 0);

        planetId = getIntent().getIntExtra("planet_id", 1);
        sceneId = getIntent().getIntExtra("scene_id", 1);

        dbHelper = GameDatabaseHelper.getInstance(this);
        tts = new TextToSpeech(this, this);

        initViews();
        loadWords();
        setupCrystals();
    }

    private void initViews() {
        tvProgress = findViewById(R.id.tvProgress);
        tvCrystals = findViewById(R.id.tvCrystals);
        tvInstruction = findViewById(R.id.tvInstruction);
        progressBar = findViewById(R.id.progressBar);
        crystalGrid = findViewById(R.id.crystalGrid);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        tvInstruction.setText("🔮 Chạm vào các Word Crystal để thu thập từ vựng!");
    }

    private void loadWords() {
        words = dbHelper.getWordsForPlanet(planetId);
        if (words == null || words.isEmpty()) {
            Toast.makeText(this, "Không có từ vựng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Collections.shuffle(words);
        // Take max 8 words
        if (words.size() > 8) {
            words = words.subList(0, 8);
        }
    }

    private void setupCrystals() {
        crystalGrid.removeAllViews();

        // Set column count based on word count
        int columnCount = 4;
        crystalGrid.setColumnCount(columnCount);

        for (int i = 0; i < words.size(); i++) {
            WordData word = words.get(i);

            // Create a card for each crystal
            CardView card = new CardView(this);
            card.setCardBackgroundColor(getColor(R.color.card_bg));
            card.setRadius(24);
            card.setCardElevation(8);

            GridLayout.LayoutParams cardParams = new GridLayout.LayoutParams();
            cardParams.width = 0;
            cardParams.height = GridLayout.LayoutParams.WRAP_CONTENT;
            cardParams.columnSpec = GridLayout.spec(i % columnCount, 1f);
            cardParams.rowSpec = GridLayout.spec(i / columnCount);
            cardParams.setMargins(12, 12, 12, 12);
            card.setLayoutParams(cardParams);

            TextView crystal = new TextView(this);
            crystal.setText("💎");
            crystal.setTextSize(40);
            crystal.setPadding(20, 30, 20, 30);
            crystal.setGravity(Gravity.CENTER);

            card.addView(crystal);

            final int index = i;
            card.setOnClickListener(v -> collectCrystal(card, crystal, word, index));

            // Add appear animation with delay
            card.setAlpha(0f);
            card.animate()
                .alpha(1f)
                .setStartDelay(i * 100)
                .setDuration(300)
                .start();

            crystalGrid.addView(card);
        }

        updateProgress();
    }

    private void collectCrystal(CardView card, TextView crystal, WordData word, int index) {
        if (collectedWords.contains(word)) return;

        // Play sound
        if (tts != null) {
            tts.speak(word.english, TextToSpeech.QUEUE_FLUSH, null, "word");
        }

        // Show word dialog
        showWordDialog(word);

        // Mark as collected
        collectedWords.add(word);
        totalCrystals++;

        // Change crystal appearance
        crystal.setText(word.emoji);
        card.setCardBackgroundColor(getColor(R.color.correct_green));
        card.setAlpha(0.8f);

        // Update progress
        updateProgress();

        // Check completion
        if (collectedWords.size() >= words.size()) {
            completeScene();
        }
    }

    private void showWordDialog(WordData word) {
        String message = "Phiên âm: " + word.pronunciation + "\n\n" +
                        "Nghĩa: " + word.vietnamese + "\n\n" +
                        "Ví dụ: " + word.exampleSentence;
        SpaceDialog.showInfo(this, word.emoji, word.english, message, () -> {
            if (tts != null) tts.speak(word.english, TextToSpeech.QUEUE_FLUSH, null, "word");
        });
    }

    private void updateProgress() {
        int progress = words.isEmpty() ? 0 : (collectedWords.size() * 100) / words.size();
        progressBar.setProgress(progress);
        tvProgress.setText(collectedWords.size() + "/" + words.size());
        tvCrystals.setText("💎 " + totalCrystals);
    }

    private void completeScene() {
        // Save progress
        dbHelper.updateSceneProgress(sceneId, 3);
        dbHelper.addStars(3);

        String message = "Bạn đã thu thập tất cả Word Crystals!\n\n" +
                        "💎 +" + totalCrystals + " Crystals";
        SpaceDialog.showSuccess(this, message, 3, () -> finish());
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
