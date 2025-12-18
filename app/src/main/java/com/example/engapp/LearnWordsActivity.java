package com.example.engapp;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
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
import java.util.List;
import java.util.Locale;

public class LearnWordsActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private TextView tvProgress, tvWordEmoji, tvEnglish, tvPronunciation, tvVietnamese;
    private TextView tvExample, tvExampleVi;
    private ProgressBar progressBar;
    private Button btnPrevious, btnNext;
    private LinearLayout btnListen;
    private ImageView btnBack;
    private CardView cardWord;

    private TextToSpeech tts;
    private GameDatabaseHelper dbHelper;
    private List<WordData> words;
    private int currentIndex = 0;
    private int planetId;
    private int sceneId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn_words);
        overridePendingTransition(R.anim.fade_scale_in, 0);

        planetId = getIntent().getIntExtra("planet_id", 1);
        sceneId = getIntent().getIntExtra("scene_id", 1);

        dbHelper = GameDatabaseHelper.getInstance(this);

        initViews();
        initTTS();
        loadWords();
        setupClickListeners();
        displayCurrentWord();
    }

    private void initViews() {
        tvProgress = findViewById(R.id.tvProgress);
        tvWordEmoji = findViewById(R.id.tvWordEmoji);
        tvEnglish = findViewById(R.id.tvEnglish);
        tvPronunciation = findViewById(R.id.tvPronunciation);
        tvVietnamese = findViewById(R.id.tvVietnamese);
        tvExample = findViewById(R.id.tvExample);
        tvExampleVi = findViewById(R.id.tvExampleVi);
        progressBar = findViewById(R.id.progressBar);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnNext = findViewById(R.id.btnNext);
        btnListen = findViewById(R.id.btnListen);
        btnBack = findViewById(R.id.btnBack);
        cardWord = findViewById(R.id.cardWord);
    }

    private void initTTS() {
        tts = new TextToSpeech(this, this);
    }

    private void loadWords() {
        words = dbHelper.getWordsForPlanet(planetId);

        if (words == null || words.isEmpty()) {
            Toast.makeText(this, "Không có từ vựng!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        btnPrevious.setOnClickListener(v -> {
            if (currentIndex > 0) {
                currentIndex--;
                displayCurrentWord();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (currentIndex < words.size() - 1) {
                currentIndex++;
                displayCurrentWord();
            } else {
                // Completed all words
                completeScene();
            }
        });

        btnListen.setOnClickListener(v -> speakCurrentWord());

        if (cardWord != null) {
            cardWord.setOnClickListener(v -> speakCurrentWord());
        }
    }

    private void displayCurrentWord() {
        if (words == null || words.isEmpty() || currentIndex >= words.size()) {
            return;
        }

        WordData word = words.get(currentIndex);

        tvWordEmoji.setText(word.emoji);
        tvEnglish.setText(word.english);
        tvPronunciation.setText(word.pronunciation);
        tvVietnamese.setText(word.vietnamese);
        tvExample.setText(word.exampleSentence);

        if (tvExampleVi != null) {
            tvExampleVi.setText(word.exampleTranslation);
        }

        // Update progress
        int progress = ((currentIndex + 1) * 100) / words.size();
        progressBar.setProgress(progress);
        tvProgress.setText((currentIndex + 1) + "/" + words.size());

        // Update button states
        btnPrevious.setEnabled(currentIndex > 0);
        btnPrevious.setAlpha(currentIndex > 0 ? 1f : 0.5f);

        if (currentIndex >= words.size() - 1) {
            btnNext.setText("Hoàn thành ✅");
        } else {
            btnNext.setText("Tiếp theo →");
        }

        // Auto speak
        speakCurrentWord();
    }

    private void speakCurrentWord() {
        if (tts != null && words != null && currentIndex < words.size()) {
            WordData word = words.get(currentIndex);
            tts.speak(word.english, TextToSpeech.QUEUE_FLUSH, null, "word");
        }
    }

    private void completeScene() {
        // Mark words as learned
        for (WordData word : words) {
            dbHelper.markWordAsLearned(word.id);
        }

        // Update scene progress
        dbHelper.updateSceneProgress(sceneId, 3);
        dbHelper.addStars(3);

        String message = "Bạn đã học " + words.size() + " từ mới!";
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
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
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

