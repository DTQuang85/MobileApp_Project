package com.example.engapp;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.engapp.database.GameDatabaseHelper;
import com.example.engapp.database.GameDatabaseHelper.*;
import com.example.engapp.manager.ProgressionManager;
import com.example.engapp.view.ConstellationView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ExploreActivity extends AppCompatActivity implements TextToSpeech.OnInitListener, ConstellationView.OnConstellationCompleteListener {

    private TextView tvProgress, tvCrystals, tvInstruction;
    private ProgressBar progressBar;
    private ConstellationView constellationView;
    private ImageView btnBack;

    private TextToSpeech tts;
    private GameDatabaseHelper dbHelper;
    private ProgressionManager progressionManager;
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
        progressionManager = ProgressionManager.getInstance(this);
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
        constellationView = findViewById(R.id.constellationView);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        tvInstruction.setText("⭐ Nối các điểm sao theo thứ tự để tạo thành chòm sao và học từ vựng!");
        
        if (constellationView != null) {
            constellationView.setOnConstellationCompleteListener(this);
        }
    }

    private void loadWords() {
        words = dbHelper.getWordsForPlanet(planetId);
        if (words == null || words.isEmpty()) {
            Toast.makeText(this, "Không có từ vựng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Collections.shuffle(words);
        // Take max 6 words for constellation (đơn giản hơn)
        if (words.size() > 6) {
            words = words.subList(0, 6);
        }
    }

    private void setupCrystals() {
        if (constellationView != null && words != null) {
            constellationView.setWords(words);
        }
        updateProgress();
    }

    @Override
    public void onStarConnected(WordData word, int order) {
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

        // Update progress
        updateProgress();
    }

    @Override
    public void onConstellationComplete() {
        // Tất cả các từ đã được thu thập trong quá trình nối
        completeScene();
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
        if (words == null || words.isEmpty()) {
            progressBar.setProgress(0);
            tvProgress.setText("0/0");
            return;
        }
        
        int totalStars = constellationView != null ? constellationView.getTotalCount() : words.size();
        int connectedStars = constellationView != null ? constellationView.getConnectedCount() : collectedWords.size();
        
        int progress = (connectedStars * 100) / totalStars;
        progressBar.setProgress(progress);
        tvProgress.setText(connectedStars + "/" + totalStars);
        tvCrystals.setText("⭐ " + totalCrystals);
    }

    private void completeScene() {
        // #region agent log
        try {
            java.io.FileWriter fw = new java.io.FileWriter("c:\\Users\\ADMIN\\Downloads\\MobileApp_Project-main (2)\\MobileApp_Project-main\\.cursor\\debug.log", true);
            fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"E\",\"location\":\"ExploreActivity.completeScene:174\",\"message\":\"completeScene entry\",\"data\":{\"planetId\":" + planetId + ",\"sceneId\":" + sceneId + ",\"totalCrystals\":" + totalCrystals + "},\"timestamp\":" + System.currentTimeMillis() + "}\n");
            fw.close();
        } catch (Exception e) {}
        // #endregion
        int starsEarned = 3;
        
        // Save progress
        dbHelper.updateSceneProgress(sceneId, starsEarned);
        dbHelper.addStars(starsEarned);
        
        // IMPORTANT: Record lesson completion to unlock next lesson
        // #region agent log
        try {
            java.io.FileWriter fw = new java.io.FileWriter("c:\\Users\\ADMIN\\Downloads\\MobileApp_Project-main (2)\\MobileApp_Project-main\\.cursor\\debug.log", true);
            fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"E\",\"location\":\"ExploreActivity.completeScene:182\",\"message\":\"Calling recordLessonCompleted\",\"data\":{\"planetId\":" + planetId + ",\"sceneId\":" + sceneId + ",\"starsEarned\":" + starsEarned + "},\"timestamp\":" + System.currentTimeMillis() + "}\n");
            fw.close();
        } catch (Exception e) {}
        // #endregion
        if (planetId > 0 && sceneId > 0) {
            progressionManager.recordLessonCompleted(planetId, sceneId, starsEarned);
        }

        String message = "Bạn đã hoàn thành chòm sao từ vựng!\n\n" +
                        "⭐ +" + totalCrystals + " Stars";
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
