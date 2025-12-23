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
import com.example.engapp.database.GameDatabaseHelper;
import com.example.engapp.manager.ProgressionManager;
import com.example.engapp.model.Planet;
import com.example.engapp.model.Sentence;
import com.example.engapp.model.Zone;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SentenceActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private TextView tvProgress, tvSentenceEn, tvSentenceVi, tvKeywords;
    private ProgressBar progressBar;
    private Button btnPrevious, btnNext;
    private LinearLayout btnListen;
    private ImageView btnBack;

    private TextToSpeech tts;
    private GameDatabaseHelper dbHelper;
    private ProgressionManager progressionManager;
    private List<Sentence> sentences;
    private int currentIndex = 0;
    private int planetIdInt = -1;
    private int sceneId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sentence);

        // Get planet_id as Integer (consistent with PlanetMapActivity)
        planetIdInt = getIntent().getIntExtra("planet_id", -1);
        sceneId = getIntent().getIntExtra("scene_id", -1);
        String planetId = planetIdInt > 0 ? String.valueOf(planetIdInt) : getIntent().getStringExtra("planet_id");
        int zoneIndex = getIntent().getIntExtra("zone_index", 0);
        
        // #region agent log
        try {
            java.io.FileWriter fw = new java.io.FileWriter("c:\\Users\\ADMIN\\Downloads\\MobileApp_Project-main (2)\\MobileApp_Project-main\\.cursor\\debug.log", true);
            fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"A\",\"location\":\"SentenceActivity.onCreate:46\",\"message\":\"Intent data\",\"data\":{\"planetIdInt\":" + planetIdInt + ",\"sceneId\":" + sceneId + ",\"planetId\":\"" + (planetId != null ? planetId : "null") + "\",\"zoneIndex\":" + zoneIndex + "},\"timestamp\":" + System.currentTimeMillis() + "}\n");
            fw.close();
        } catch (Exception e) {}
        // #endregion

        if (planetId == null || planetIdInt <= 0) {
            // #region agent log
            try {
                java.io.FileWriter fw = new java.io.FileWriter("c:\\Users\\ADMIN\\Downloads\\MobileApp_Project-main (2)\\MobileApp_Project-main\\.cursor\\debug.log", true);
                fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"A\",\"location\":\"SentenceActivity.onCreate:52\",\"message\":\"Invalid intent data, finishing\",\"data\":{\"planetId\":\"" + (planetId != null ? planetId : "null") + "\",\"planetIdInt\":" + planetIdInt + "},\"timestamp\":" + System.currentTimeMillis() + "}\n");
                fw.close();
            } catch (Exception e) {}
            // #endregion
            finish();
            return;
        }

        dbHelper = GameDatabaseHelper.getInstance(this);
        progressionManager = ProgressionManager.getInstance(this);

        initViews();
        initTTS();
        loadSentences(planetId, zoneIndex);
        
        // Check if sentences loaded successfully
        if (sentences == null || sentences.isEmpty()) {
            Toast.makeText(this, "Không có câu mẫu", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
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
        // #region agent log
        try {
            java.io.FileWriter fw = new java.io.FileWriter("c:\\Users\\ADMIN\\Downloads\\MobileApp_Project-main (2)\\MobileApp_Project-main\\.cursor\\debug.log", true);
            fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"A\",\"location\":\"SentenceActivity.loadSentences:88\",\"message\":\"loadSentences entry\",\"data\":{\"planetId\":\"" + planetId + "\",\"planetIdInt\":" + planetIdInt + ",\"sceneId\":" + sceneId + ",\"zoneIndex\":" + zoneIndex + "},\"timestamp\":" + System.currentTimeMillis() + "}\n");
            fw.close();
        } catch (Exception e) {}
        // #endregion
        sentences = new ArrayList<>();
        
        // Try to load from database first (preferred method)
        if (planetIdInt > 0) {
            // #region agent log
            try {
                java.io.FileWriter fw = new java.io.FileWriter("c:\\Users\\ADMIN\\Downloads\\MobileApp_Project-main (2)\\MobileApp_Project-main\\.cursor\\debug.log", true);
                fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"A\",\"location\":\"SentenceActivity.loadSentences:92\",\"message\":\"planetIdInt valid\",\"data\":{\"planetIdInt\":" + planetIdInt + "},\"timestamp\":" + System.currentTimeMillis() + "}\n");
                fw.close();
            } catch (Exception e) {}
            // #endregion
            // Try to load by scene_id first (more specific)
            List<GameDatabaseHelper.SentenceData> sentenceDataList = null;
            if (sceneId > 0) {
                // #region agent log
                try {
                    java.io.FileWriter fw = new java.io.FileWriter("c:\\Users\\ADMIN\\Downloads\\MobileApp_Project-main (2)\\MobileApp_Project-main\\.cursor\\debug.log", true);
                    fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"B\",\"location\":\"SentenceActivity.loadSentences:96\",\"message\":\"Loading by sceneId\",\"data\":{\"sceneId\":" + sceneId + "},\"timestamp\":" + System.currentTimeMillis() + "}\n");
                    fw.close();
                } catch (Exception e) {}
                // #endregion
                sentenceDataList = dbHelper.getSentencesForScene(sceneId);
                // #region agent log
                try {
                    java.io.FileWriter fw = new java.io.FileWriter("c:\\Users\\ADMIN\\Downloads\\MobileApp_Project-main (2)\\MobileApp_Project-main\\.cursor\\debug.log", true);
                    fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"B\",\"location\":\"SentenceActivity.loadSentences:97\",\"message\":\"getSentencesForScene result\",\"data\":{\"sceneId\":" + sceneId + ",\"count\":" + (sentenceDataList != null ? sentenceDataList.size() : -1) + "},\"timestamp\":" + System.currentTimeMillis() + "}\n");
                    fw.close();
                } catch (Exception e) {}
                // #endregion
            }
            
            // Fallback to planet_id if no scene-specific sentences
            if (sentenceDataList == null || sentenceDataList.isEmpty()) {
                // #region agent log
                try {
                    java.io.FileWriter fw = new java.io.FileWriter("c:\\Users\\ADMIN\\Downloads\\MobileApp_Project-main (2)\\MobileApp_Project-main\\.cursor\\debug.log", true);
                    fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"C\",\"location\":\"SentenceActivity.loadSentences:100\",\"message\":\"Fallback to planet_id\",\"data\":{\"planetIdInt\":" + planetIdInt + "},\"timestamp\":" + System.currentTimeMillis() + "}\n");
                    fw.close();
                } catch (Exception e) {}
                // #endregion
                sentenceDataList = dbHelper.getSentencesForPlanet(planetIdInt);
                // #region agent log
                try {
                    java.io.FileWriter fw = new java.io.FileWriter("c:\\Users\\ADMIN\\Downloads\\MobileApp_Project-main (2)\\MobileApp_Project-main\\.cursor\\debug.log", true);
                    fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"C\",\"location\":\"SentenceActivity.loadSentences:101\",\"message\":\"getSentencesForPlanet result\",\"data\":{\"planetIdInt\":" + planetIdInt + ",\"count\":" + (sentenceDataList != null ? sentenceDataList.size() : -1) + "},\"timestamp\":" + System.currentTimeMillis() + "}\n");
                    fw.close();
                } catch (Exception e) {}
                // #endregion
            }
            
            if (sentenceDataList != null && !sentenceDataList.isEmpty()) {
                // #region agent log
                try {
                    java.io.FileWriter fw = new java.io.FileWriter("c:\\Users\\ADMIN\\Downloads\\MobileApp_Project-main (2)\\MobileApp_Project-main\\.cursor\\debug.log", true);
                    fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"D\",\"location\":\"SentenceActivity.loadSentences:104\",\"message\":\"Converting SentenceData to Sentence\",\"data\":{\"count\":" + sentenceDataList.size() + "},\"timestamp\":" + System.currentTimeMillis() + "}\n");
                    fw.close();
                } catch (Exception e) {}
                // #endregion
                // Convert SentenceData to Sentence model
                for (GameDatabaseHelper.SentenceData sentenceData : sentenceDataList) {
                    Sentence sentence = new Sentence(sentenceData.english, sentenceData.vietnamese);
                    
                    // Parse keywords if available
                    if (sentenceData.keywords != null && !sentenceData.keywords.isEmpty()) {
                        String[] keywords = sentenceData.keywords.split(",");
                        sentence.setKeywords(keywords);
                    }
                    
                    sentences.add(sentence);
                }
            } else {
                // #region agent log
                try {
                    java.io.FileWriter fw = new java.io.FileWriter("c:\\Users\\ADMIN\\Downloads\\MobileApp_Project-main (2)\\MobileApp_Project-main\\.cursor\\debug.log", true);
                    fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"D\",\"location\":\"SentenceActivity.loadSentences:117\",\"message\":\"No sentences from database\",\"data\":{},\"timestamp\":" + System.currentTimeMillis() + "}\n");
                    fw.close();
                } catch (Exception e) {}
                // #endregion
            }
        } else {
            // #region agent log
            try {
                java.io.FileWriter fw = new java.io.FileWriter("c:\\Users\\ADMIN\\Downloads\\MobileApp_Project-main (2)\\MobileApp_Project-main\\.cursor\\debug.log", true);
                fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"A\",\"location\":\"SentenceActivity.loadSentences:120\",\"message\":\"planetIdInt invalid\",\"data\":{\"planetIdInt\":" + planetIdInt + "},\"timestamp\":" + System.currentTimeMillis() + "}\n");
                fw.close();
            } catch (Exception e) {}
            // #endregion
        }
        
        // Fallback: Try GameDataProvider if database doesn't have sentences
        if (sentences.isEmpty()) {
            // #region agent log
            try {
                java.io.FileWriter fw = new java.io.FileWriter("c:\\Users\\ADMIN\\Downloads\\MobileApp_Project-main (2)\\MobileApp_Project-main\\.cursor\\debug.log", true);
                fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"E\",\"location\":\"SentenceActivity.loadSentences:123\",\"message\":\"Trying GameDataProvider fallback\",\"data\":{\"planetId\":\"" + planetId + "\",\"zoneIndex\":" + zoneIndex + "},\"timestamp\":" + System.currentTimeMillis() + "}\n");
                fw.close();
            } catch (Exception e) {}
            // #endregion
            Planet planet = GameDataProvider.getPlanetById(planetId);
            // #region agent log
            try {
                java.io.FileWriter fw = new java.io.FileWriter("c:\\Users\\ADMIN\\Downloads\\MobileApp_Project-main (2)\\MobileApp_Project-main\\.cursor\\debug.log", true);
                fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"E\",\"location\":\"SentenceActivity.loadSentences:124\",\"message\":\"GameDataProvider planet result\",\"data\":{\"planetId\":\"" + planetId + "\",\"planetFound\":" + (planet != null) + "},\"timestamp\":" + System.currentTimeMillis() + "}\n");
                fw.close();
            } catch (Exception e) {}
            // #endregion
            if (planet != null && planet.getZones() != null && zoneIndex < planet.getZones().size()) {
                Zone zone = planet.getZones().get(zoneIndex);
                // #region agent log
                try {
                    java.io.FileWriter fw = new java.io.FileWriter("c:\\Users\\ADMIN\\Downloads\\MobileApp_Project-main (2)\\MobileApp_Project-main\\.cursor\\debug.log", true);
                    fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"E\",\"location\":\"SentenceActivity.loadSentences:126\",\"message\":\"Zone found\",\"data\":{\"zoneIndex\":" + zoneIndex + ",\"hasSentences\":" + (zone.getSentences() != null) + ",\"sentenceCount\":" + (zone.getSentences() != null ? zone.getSentences().size() : 0) + "},\"timestamp\":" + System.currentTimeMillis() + "}\n");
                    fw.close();
                } catch (Exception e) {}
                // #endregion
                if (zone.getSentences() != null) {
                    sentences = new ArrayList<>(zone.getSentences());
                }
            } else {
                // #region agent log
                try {
                    java.io.FileWriter fw = new java.io.FileWriter("c:\\Users\\ADMIN\\Downloads\\MobileApp_Project-main (2)\\MobileApp_Project-main\\.cursor\\debug.log", true);
                    fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"E\",\"location\":\"SentenceActivity.loadSentences:131\",\"message\":\"Zone not found or invalid\",\"data\":{\"planetFound\":" + (planet != null) + ",\"hasZones\":" + (planet != null && planet.getZones() != null) + ",\"zoneIndex\":" + zoneIndex + "},\"timestamp\":" + System.currentTimeMillis() + "}\n");
                    fw.close();
                } catch (Exception e) {}
                // #endregion
            }
        }
        
        // Ensure sentences is not null
        if (sentences == null) {
            sentences = new ArrayList<>();
        }
        // #region agent log
        try {
            java.io.FileWriter fw = new java.io.FileWriter("c:\\Users\\ADMIN\\Downloads\\MobileApp_Project-main (2)\\MobileApp_Project-main\\.cursor\\debug.log", true);
            fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"ALL\",\"location\":\"SentenceActivity.loadSentences:136\",\"message\":\"loadSentences exit\",\"data\":{\"finalCount\":" + sentences.size() + "},\"timestamp\":" + System.currentTimeMillis() + "}\n");
            fw.close();
        } catch (Exception e) {}
        // #endregion
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
                // Complete lesson when finished
                if (planetIdInt > 0 && sceneId > 0) {
                    int starsEarned = 3;
                    // Update scene progress in database
                    dbHelper.updateSceneProgress(sceneId, starsEarned);
                    dbHelper.addStars(starsEarned);
                    // Record lesson completion to unlock next lesson
                    progressionManager.recordLessonCompleted(planetIdInt, sceneId, starsEarned);
                }
                
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
