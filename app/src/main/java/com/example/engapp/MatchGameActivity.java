package com.example.engapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.engapp.data.GameDataProvider;
import com.example.engapp.database.GameDatabaseHelper;
import com.example.engapp.manager.ProgressionManager;
import com.example.engapp.model.Planet;
import com.example.engapp.model.Word;
import com.example.engapp.model.Zone;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MatchGameActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private TextView tvScore, tvMatches;
    private ImageView btnBack;
    private ProgressBar progressBar;

    private TextToSpeech tts;
    private GameDatabaseHelper dbHelper;
    private ProgressionManager progressionManager;
    private List<Word> words;
    private List<MatchCard> cards = new ArrayList<>();
    private MatchCard firstSelected = null;
    private MatchCard secondSelected = null;
    private int matches = 0;
    private int totalPairs = 0;
    private int score = 0;
    private int attempts = 0;
    private boolean isProcessing = false;
    private int planetIdInt = -1;
    private int sceneId = -1;

    private Handler handler = new Handler();

    private CardView[] cardViews = new CardView[16];
    private TextView[] cardTexts = new TextView[16];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_game);

        // Get planet_id as Integer (consistent with PlanetMapActivity)
        planetIdInt = getIntent().getIntExtra("planet_id", -1);
        sceneId = getIntent().getIntExtra("scene_id", -1);
        String planetId = planetIdInt > 0 ? String.valueOf(planetIdInt) : getIntent().getStringExtra("planet_id");
        int zoneIndex = getIntent().getIntExtra("zone_index", 0);

        if (planetId == null || planetIdInt <= 0) {
            finish();
            return;
        }

        dbHelper = GameDatabaseHelper.getInstance(this);
        progressionManager = ProgressionManager.getInstance(this);

        initViews();
        initTTS();
        loadWords(planetId, zoneIndex);
        
        // Check if words loaded successfully
        if (words == null || words.isEmpty()) {
            Toast.makeText(this, "Không thể tải dữ liệu từ vựng. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        setupGame();
    }

    private void initViews() {
        tvScore = findViewById(R.id.tvScore);
        tvMatches = findViewById(R.id.tvMatches);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);

        cardViews[0] = findViewById(R.id.card1);
        cardViews[1] = findViewById(R.id.card2);
        cardViews[2] = findViewById(R.id.card3);
        cardViews[3] = findViewById(R.id.card4);
        cardViews[4] = findViewById(R.id.card5);
        cardViews[5] = findViewById(R.id.card6);
        cardViews[6] = findViewById(R.id.card7);
        cardViews[7] = findViewById(R.id.card8);
        cardViews[8] = findViewById(R.id.card9);
        cardViews[9] = findViewById(R.id.card10);
        cardViews[10] = findViewById(R.id.card11);
        cardViews[11] = findViewById(R.id.card12);
        cardViews[12] = findViewById(R.id.card13);
        cardViews[13] = findViewById(R.id.card14);
        cardViews[14] = findViewById(R.id.card15);
        cardViews[15] = findViewById(R.id.card16);

        cardTexts[0] = findViewById(R.id.cardText1);
        cardTexts[1] = findViewById(R.id.cardText2);
        cardTexts[2] = findViewById(R.id.cardText3);
        cardTexts[3] = findViewById(R.id.cardText4);
        cardTexts[4] = findViewById(R.id.cardText5);
        cardTexts[5] = findViewById(R.id.cardText6);
        cardTexts[6] = findViewById(R.id.cardText7);
        cardTexts[7] = findViewById(R.id.cardText8);
        cardTexts[8] = findViewById(R.id.cardText9);
        cardTexts[9] = findViewById(R.id.cardText10);
        cardTexts[10] = findViewById(R.id.cardText11);
        cardTexts[11] = findViewById(R.id.cardText12);
        cardTexts[12] = findViewById(R.id.cardText13);
        cardTexts[13] = findViewById(R.id.cardText14);
        cardTexts[14] = findViewById(R.id.cardText15);
        cardTexts[15] = findViewById(R.id.cardText16);

        btnBack.setOnClickListener(v -> showExitConfirmation());
    }

    private void initTTS() {
        tts = new TextToSpeech(this, this);
    }

    private void loadWords(String planetId, int zoneIndex) {
        words = new ArrayList<>();
        
        // Try to load from database first (preferred method)
        if (planetIdInt > 0) {
            List<GameDatabaseHelper.WordData> wordDataList = dbHelper.getWordsForPlanet(planetIdInt);
            if (wordDataList != null && !wordDataList.isEmpty()) {
                // Convert WordData to Word model
                for (GameDatabaseHelper.WordData wordData : wordDataList) {
                    Word word = new Word(wordData.english, wordData.vietnamese, wordData.emoji);
                    if (wordData.exampleSentence != null) {
                        word.setExampleSentence(wordData.exampleSentence);
                    }
                    if (wordData.exampleTranslation != null) {
                        word.setExampleTranslation(wordData.exampleTranslation);
                    }
                    words.add(word);
                }
            }
        }
        
        // Fallback: Try GameDataProvider if database doesn't have words
        if (words.isEmpty()) {
            Planet planet = GameDataProvider.getPlanetById(planetId);
            if (planet != null && planet.getZones() != null && zoneIndex < planet.getZones().size()) {
                Zone zone = planet.getZones().get(zoneIndex);
                if (zone.getWords() != null) {
                    words = new ArrayList<>(zone.getWords());
                }
            }
        }

        // Final check
        if (words == null || words.size() < 8) {
            // Don't finish here, let onCreate handle it
            words = new ArrayList<>(); // Ensure it's not null
        }
    }

    private void setupGame() {
        cards.clear();
        matches = 0;
        score = 0;
        attempts = 0;
        firstSelected = null;
        secondSelected = null;

        Collections.shuffle(words);
        List<Word> selectedWords = words.subList(0, Math.min(8, words.size()));
        totalPairs = selectedWords.size();

        for (Word word : selectedWords) {
            // Card với hình (emoji)
            String emoji = word.getImageUrl() != null ? word.getImageUrl() : "❓";
            MatchCard emojiCard = new MatchCard(emoji, word.getEnglish(), true);
            cards.add(emojiCard);

            // Card với nghĩa (tiếng Việt)
            MatchCard meaningCard = new MatchCard(word.getVietnamese(), word.getEnglish(), false);
            cards.add(meaningCard);
        }

        Collections.shuffle(cards);

        for (int i = 0; i < 16 && i < cards.size(); i++) {
            final int index = i;
            MatchCard card = cards.get(i);
            card.viewIndex = i;

            cardTexts[i].setText("❓");
            cardViews[i].setCardBackgroundColor(0xFF3B82F6);
            cardViews[i].setVisibility(View.VISIBLE);

            cardViews[i].setOnClickListener(v -> onCardClick(index));
        }

        for (int i = cards.size(); i < 16; i++) {
            cardViews[i].setVisibility(View.GONE);
        }

        updateUI();
    }

    private void onCardClick(int index) {
        if (isProcessing || index >= cards.size()) return;

        MatchCard card = cards.get(index);
        if (card.isMatched || card.isFlipped) return;

        card.isFlipped = true;
        cardTexts[index].setText(card.displayText);
        
        // Điều chỉnh kích thước chữ: emoji lớn hơn, nghĩa nhỏ hơn
        if (card.isEmoji) {
            cardTexts[index].setTextSize(32);
        } else {
            cardTexts[index].setTextSize(14);
        }
        
        cardViews[index].setCardBackgroundColor(0x60FFFFFF);

        // Phát âm từ khi chọn card hình (emoji)
        if (card.isEmoji && tts != null) {
            tts.speak(card.matchKey, TextToSpeech.QUEUE_FLUSH, null, "word");
        }

        if (firstSelected == null) {
            firstSelected = card;
        } else if (secondSelected == null && card != firstSelected) {
            secondSelected = card;
            attempts++;
            isProcessing = true;

            handler.postDelayed(() -> checkMatch(), 800);
        }
    }

    private void checkMatch() {
        if (firstSelected == null || secondSelected == null) {
            isProcessing = false;
            return;
        }

        if (firstSelected.matchKey.equals(secondSelected.matchKey)) {
            firstSelected.isMatched = true;
            secondSelected.isMatched = true;

            cardViews[firstSelected.viewIndex].setCardBackgroundColor(getColor(R.color.correct_green));
            cardViews[secondSelected.viewIndex].setCardBackgroundColor(getColor(R.color.correct_green));

            matches++;
            score += 20;

            Toast.makeText(this, "🎉 Ghép đúng!", Toast.LENGTH_SHORT).show();

            if (matches >= totalPairs) {
                handler.postDelayed(this::endGame, 500);
            }
        } else {
            cardViews[firstSelected.viewIndex].setCardBackgroundColor(getColor(R.color.wrong_red));
            cardViews[secondSelected.viewIndex].setCardBackgroundColor(getColor(R.color.wrong_red));

            final MatchCard f = firstSelected;
            final MatchCard s = secondSelected;

            handler.postDelayed(() -> {
                f.isFlipped = false;
                s.isFlipped = false;
                cardTexts[f.viewIndex].setText("❓");
                cardTexts[f.viewIndex].setTextSize(24); // Reset về kích thước mặc định
                cardTexts[s.viewIndex].setText("❓");
                cardTexts[s.viewIndex].setTextSize(24); // Reset về kích thước mặc định
                cardViews[f.viewIndex].setCardBackgroundColor(0xFF3B82F6);
                cardViews[s.viewIndex].setCardBackgroundColor(0xFF3B82F6);
            }, 500);
        }

        firstSelected = null;
        secondSelected = null;
        isProcessing = false;
        updateUI();
    }

    private void endGame() {
        int maxScore = totalPairs * 20;
        int stars = attempts <= totalPairs + 2 ? 3 : attempts <= totalPairs + 4 ? 2 : 1;

        saveProgress(stars);
        
        // Record lesson completion to unlock next lesson
        if (planetIdInt > 0 && sceneId > 0 && stars > 0) {
            // Update scene progress in database
            dbHelper.updateSceneProgress(sceneId, stars);
            dbHelper.addStars(stars);
            // Record lesson completion
            progressionManager.recordLessonCompleted(planetIdInt, sceneId, stars);
        }

        String message = "Điểm: " + score + "/" + maxScore + "\n";
        message += "Số lần thử: " + attempts + "\n";
        for (int i = 0; i < 3; i++) {
            message += i < stars ? "⭐" : "☆";
        }

        new AlertDialog.Builder(this)
            .setTitle("🎉 Hoàn thành!")
            .setMessage(message)
            .setPositiveButton("Chơi lại", (d, w) -> setupGame())
            .setNegativeButton("Thoát", (d, w) -> finish())
            .setCancelable(false)
            .show();
    }

    private void saveProgress(int stars) {
        SharedPreferences prefs = getSharedPreferences("game_progress", MODE_PRIVATE);
        int totalStars = prefs.getInt("total_stars", 0);
        prefs.edit().putInt("total_stars", totalStars + stars).apply();
    }

    private void updateUI() {
        tvScore.setText("⭐ " + score);
        tvMatches.setText("Ghép: " + matches + "/" + totalPairs);
        int progress = totalPairs > 0 ? (matches * 100) / totalPairs : 0;
        progressBar.setProgress(progress);
    }

    private void showExitConfirmation() {
        new AlertDialog.Builder(this)
            .setTitle("Thoát game?")
            .setMessage("Bạn có chắc muốn thoát?")
            .setPositiveButton("Thoát", (d, w) -> finish())
            .setNegativeButton("Tiếp tục", null)
            .show();
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
        showExitConfirmation();
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    static class MatchCard {
        String displayText;
        String matchKey;
        boolean isEmoji;
        boolean isFlipped = false;
        boolean isMatched = false;
        int viewIndex;

        MatchCard(String displayText, String matchKey, boolean isEmoji) {
            this.displayText = displayText;
            this.matchKey = matchKey;
            this.isEmoji = isEmoji;
        }
    }
}
