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
import com.example.engapp.data.GameDataProvider;
import com.example.engapp.model.Planet;
import com.example.engapp.model.Word;
import com.example.engapp.model.Zone;
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
    private List<Word> words;
    private int currentIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn_words);

        String planetId = getIntent().getStringExtra("planet_id");
        int zoneIndex = getIntent().getIntExtra("zone_index", 0);

        if (planetId == null) {
            finish();
            return;
        }

        initViews();
        initTTS();
        loadWords(planetId, zoneIndex);
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

    private void loadWords(String planetId, int zoneIndex) {
        Planet planet = GameDataProvider.getPlanetById(planetId);
        if (planet != null && planet.getZones() != null && zoneIndex < planet.getZones().size()) {
            Zone zone = planet.getZones().get(zoneIndex);
            words = zone.getWords();
        }

        if (words == null || words.isEmpty()) {
            Toast.makeText(this, "Không có từ vựng", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnListen.setOnClickListener(v -> speakWord());

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
                // Finished learning all words
                Toast.makeText(this, "🎉 Hoàn thành! Bạn đã học xong " + words.size() + " từ!", Toast.LENGTH_LONG).show();
                finish();
            }
        });

        // Card animation on touch
        cardWord.setOnClickListener(v -> speakWord());
    }

    private void displayCurrentWord() {
        if (words == null || currentIndex >= words.size()) return;

        Word word = words.get(currentIndex);

        // Update progress
        tvProgress.setText((currentIndex + 1) + "/" + words.size());
        int progress = ((currentIndex + 1) * 100) / words.size();
        progressBar.setProgress(progress);

        // Display word
        tvWordEmoji.setText(word.getImageUrl() != null ? word.getImageUrl() : "📖");
        tvEnglish.setText(capitalizeFirst(word.getEnglish()));
        tvVietnamese.setText(word.getVietnamese());

        // Pronunciation (if available)
        if (word.getPronunciation() != null && !word.getPronunciation().isEmpty()) {
            tvPronunciation.setText(word.getPronunciation());
            tvPronunciation.setVisibility(View.VISIBLE);
        } else {
            tvPronunciation.setVisibility(View.GONE);
        }

        // Example sentence
        if (word.getExampleSentence() != null) {
            tvExample.setText(word.getExampleSentence());
            tvExampleVi.setText(word.getExampleTranslation());
        } else {
            tvExample.setText("This is a " + word.getEnglish() + ".");
            tvExampleVi.setText("Đây là " + word.getVietnamese() + ".");
        }

        // Update button states
        btnPrevious.setAlpha(currentIndex > 0 ? 1f : 0.5f);
        btnNext.setText(currentIndex < words.size() - 1 ? "Tiếp ▶" : "Hoàn thành ✓");

        // Auto-speak word
        speakWord();
    }

    private void speakWord() {
        if (tts != null && words != null && currentIndex < words.size()) {
            String text = words.get(currentIndex).getEnglish();
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "word_" + currentIndex);
        }
    }

    private String capitalizeFirst(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "Ngôn ngữ không hỗ trợ", Toast.LENGTH_SHORT).show();
            } else {
                tts.setSpeechRate(0.8f); // Slower for kids
            }
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

