package com.example.engapp;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.engapp.data.GameDataProvider;
import com.example.engapp.model.Planet;
import com.example.engapp.model.Sentence;
import com.example.engapp.model.Zone;
import java.util.List;
import java.util.Locale;

public class SentenceActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private TextView tvProgress, tvSentenceEn, tvSentenceVi, tvKeywords;
    private ProgressBar progressBar;
    private Button btnPrevious, btnNext;
    private LinearLayout btnListen;
    private ImageView btnBack;

    private TextToSpeech tts;
    private List<Sentence> sentences;
    private int currentIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sentence);

        String planetId = getIntent().getStringExtra("planet_id");
        int zoneIndex = getIntent().getIntExtra("zone_index", 0);

        if (planetId == null) {
            finish();
            return;
        }

        initViews();
        initTTS();
        loadSentences(planetId, zoneIndex);
        setupClickListeners();
        displayCurrentSentence();
    }

    private void initViews() {
        tvProgress = findViewById(R.id.tvProgress);
        tvSentenceEn = findViewById(R.id.tvSentenceEn);
        tvSentenceVi = findViewById(R.id.tvSentenceVi);
        tvKeywords = findViewById(R.id.tvKeywords);
        progressBar = findViewById(R.id.progressBar);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnNext = findViewById(R.id.btnNext);
        btnListen = findViewById(R.id.btnListen);
        btnBack = findViewById(R.id.btnBack);
    }

    private void initTTS() {
        tts = new TextToSpeech(this, this);
    }

    private void loadSentences(String planetId, int zoneIndex) {
        Planet planet = GameDataProvider.getPlanetById(planetId);
        if (planet != null && planet.getZones() != null && zoneIndex < planet.getZones().size()) {
            Zone zone = planet.getZones().get(zoneIndex);
            sentences = zone.getSentences();
        }

        if (sentences == null || sentences.isEmpty()) {
            Toast.makeText(this, "Không có câu mẫu", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnListen.setOnClickListener(v -> speakSentence());

        btnPrevious.setOnClickListener(v -> {
            if (currentIndex > 0) {
                currentIndex--;
                displayCurrentSentence();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (currentIndex < sentences.size() - 1) {
                currentIndex++;
                displayCurrentSentence();
            } else {
                Toast.makeText(this, "🎉 Hoàn thành!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void displayCurrentSentence() {
        if (sentences == null || currentIndex >= sentences.size()) return;

        Sentence sentence = sentences.get(currentIndex);

        tvProgress.setText((currentIndex + 1) + "/" + sentences.size());
        progressBar.setProgress(((currentIndex + 1) * 100) / sentences.size());

        tvSentenceEn.setText(sentence.getEnglish());
        tvSentenceVi.setText(sentence.getVietnamese());

        if (sentence.getKeywords() != null && sentence.getKeywords().length > 0) {
            StringBuilder keywords = new StringBuilder("Từ khóa: ");
            for (String keyword : sentence.getKeywords()) {
                keywords.append(keyword).append(", ");
            }
            tvKeywords.setText(keywords.substring(0, keywords.length() - 2));
        } else {
            tvKeywords.setText("");
        }

        btnPrevious.setAlpha(currentIndex > 0 ? 1f : 0.5f);
        btnNext.setText(currentIndex < sentences.size() - 1 ? "Tiếp ▶" : "Hoàn thành ✓");

        speakSentence();
    }

    private void speakSentence() {
        if (tts != null && sentences != null && currentIndex < sentences.size()) {
            String text = sentences.get(currentIndex).getEnglish();
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "sentence");
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.US);
            tts.setSpeechRate(0.7f);
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
