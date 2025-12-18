package com.example.engapp;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
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

public class DialogueActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private TextView tvNpcEmoji, tvNpcName, tvNpcMessage;
    private TextView tvOption1, tvOption2, tvOption3;
    private CardView cardOption1, cardOption2, cardOption3;
    private ProgressBar progressBar;
    private TextView tvProgress, tvScore;
    private ImageView btnBack;

    private TextToSpeech tts;
    private GameDatabaseHelper dbHelper;
    private int planetId, sceneId;
    private List<SentenceData> sentences;
    private int currentIndex = 0;
    private int score = 0;
    private int correctAnswer = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialogue);
        overridePendingTransition(R.anim.fade_scale_in, 0);

        planetId = getIntent().getIntExtra("planet_id", 1);
        sceneId = getIntent().getIntExtra("scene_id", 1);

        dbHelper = GameDatabaseHelper.getInstance(this);
        tts = new TextToSpeech(this, this);

        initViews();
        loadSentences();
        showCurrentDialogue();
    }

    private void initViews() {
        tvNpcEmoji = findViewById(R.id.tvNpcEmoji);
        tvNpcName = findViewById(R.id.tvNpcName);
        tvNpcMessage = findViewById(R.id.tvNpcMessage);
        tvOption1 = findViewById(R.id.tvOption1);
        tvOption2 = findViewById(R.id.tvOption2);
        tvOption3 = findViewById(R.id.tvOption3);
        cardOption1 = findViewById(R.id.cardOption1);
        cardOption2 = findViewById(R.id.cardOption2);
        cardOption3 = findViewById(R.id.cardOption3);
        progressBar = findViewById(R.id.progressBar);
        tvProgress = findViewById(R.id.tvProgress);
        tvScore = findViewById(R.id.tvScore);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        cardOption1.setOnClickListener(v -> checkAnswer(0));
        cardOption2.setOnClickListener(v -> checkAnswer(1));
        cardOption3.setOnClickListener(v -> checkAnswer(2));
    }

    private void loadSentences() {
        sentences = dbHelper.getSentencesForPlanet(planetId);
        if (sentences == null || sentences.isEmpty()) {
            Toast.makeText(this, "Không có hội thoại", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Collections.shuffle(sentences);
        if (sentences.size() > 5) {
            sentences = sentences.subList(0, 5);
        }
    }

    private void showCurrentDialogue() {
        if (currentIndex >= sentences.size()) {
            completeScene();
            return;
        }

        SentenceData sentence = sentences.get(currentIndex);

        // NPC asks question
        tvNpcEmoji.setText("🤖");
        tvNpcName.setText("Robo-Guide");
        tvNpcMessage.setText("Câu nào dưới đây có nghĩa là:\n\"" + sentence.vietnamese + "\"?");

        // Generate options
        List<String> options = new ArrayList<>();
        options.add(sentence.english); // Correct answer

        // Add wrong options
        for (SentenceData s : sentences) {
            if (!s.english.equals(sentence.english) && options.size() < 3) {
                options.add(s.english);
            }
        }

        // Fill remaining with dummy options
        while (options.size() < 3) {
            options.add("I don't know.");
        }

        Collections.shuffle(options);
        correctAnswer = options.indexOf(sentence.english);

        tvOption1.setText(options.get(0));
        tvOption2.setText(options.get(1));
        tvOption3.setText(options.get(2));

        // Reset colors
        cardOption1.setCardBackgroundColor(getColor(R.color.card_bg));
        cardOption2.setCardBackgroundColor(getColor(R.color.card_bg));
        cardOption3.setCardBackgroundColor(getColor(R.color.card_bg));

        updateProgress();

        // Speak the Vietnamese
        if (tts != null) {
            tts.setLanguage(new Locale("vi", "VN"));
            tts.speak(sentence.vietnamese, TextToSpeech.QUEUE_FLUSH, null, "question");
        }
    }

    private void checkAnswer(int selected) {
        CardView[] cards = {cardOption1, cardOption2, cardOption3};

        if (selected == correctAnswer) {
            // Correct!
            score += 20;
            cards[selected].setCardBackgroundColor(getColor(R.color.correct_green));
            Toast.makeText(this, "🎉 Đúng rồi!", Toast.LENGTH_SHORT).show();

            // Speak the correct sentence
            if (tts != null) {
                tts.setLanguage(Locale.US);
                tts.speak(sentences.get(currentIndex).english, TextToSpeech.QUEUE_FLUSH, null, "answer");
            }
        } else {
            // Wrong
            cards[selected].setCardBackgroundColor(getColor(R.color.wrong_red));
            cards[correctAnswer].setCardBackgroundColor(getColor(R.color.correct_green));
            Toast.makeText(this, "❌ Sai rồi!", Toast.LENGTH_SHORT).show();
        }

        tvScore.setText("⭐ " + score);

        // Next question after delay
        cards[0].postDelayed(() -> {
            currentIndex++;
            showCurrentDialogue();
        }, 1500);
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
        SpaceDialog.showSuccess(this, message, stars, () -> finish());
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

