package com.example.engapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.example.engapp.database.GameDatabaseHelper;
import com.example.engapp.database.GameDatabaseHelper.WordData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

/**
 * WordBattle - Horse race word game for kids.
 * Build words from letters to boost the player's horse.
 */
public class WordBattleActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private static final int AI_COUNT = 3;
    private static final long TICK_MS = 50L;

    // UI - Top
    private ImageView btnBack;
    private TextView tvTitle;
    private TextView tvLearned;
    private TextView tvDifficulty;
    private TextView tvScore;

    // UI - Race
    private TextView tvRaceStatus;
    private FrameLayout lanePlayer;
    private FrameLayout[] laneAi = new FrameLayout[AI_COUNT];
    private TextView horsePlayer;
    private TextView[] horseAi = new TextView[AI_COUNT];
    private TextView tvPlayerName;
    private TextView[] tvAiName = new TextView[AI_COUNT];
    private ProgressBar progressPlayer;
    private ProgressBar[] progressAi = new ProgressBar[AI_COUNT];

    // UI - Word panel
    private TextView tvCurrentWord;
    private GridLayout gridLetters;
    private Button btnBoost;
    private Button btnClear;
    private Button btnShuffle;

    // UI - Result
    private View resultOverlay;
    private TextView tvResultTitle;
    private TextView tvResultDetail;
    private Button btnRetry;
    private Button btnExit;

    // Game state
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();
    private TextToSpeech tts;
    private GameDatabaseHelper dbHelper;

    private final List<Character> rackLetters = new ArrayList<>();
    private final List<Button> letterButtons = new ArrayList<>();
    private final List<Integer> selectedIndices = new ArrayList<>();
    private final StringBuilder currentWord = new StringBuilder();
    private final Set<String> validWords = new HashSet<>();
    private final Set<String> usedWords = new HashSet<>();

    private long lastTickMs;
    private boolean raceRunning;

    private float finishDistance;
    private float playerDistance;
    private float[] aiDistance = new float[AI_COUNT];

    private float playerBaseSpeed;
    private float[] aiBaseSpeed = new float[AI_COUNT];
    private float playerBoostSpeed;
    private long playerBoostEndMs;

    private int learnedCount;
    private int rackSize;
    private int maxWordLength;
    private int score;

    private final Runnable raceTick = new Runnable() {
        @Override
        public void run() {
            if (!raceRunning) {
                return;
            }
            long now = SystemClock.uptimeMillis();
            float deltaSec = (now - lastTickMs) / 1000f;
            lastTickMs = now;

            float boost = (now < playerBoostEndMs) ? playerBoostSpeed : 0f;
            playerDistance += (playerBaseSpeed + boost) * deltaSec;

            for (int i = 0; i < AI_COUNT; i++) {
                float jitter = (random.nextFloat() - 0.4f) * 6f;
                aiDistance[i] += (aiBaseSpeed[i] + jitter) * deltaSec;
            }

            updateRaceUI();

            if (checkRaceFinish()) {
                endRace();
                return;
            }

            handler.postDelayed(this, TICK_MS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_battle);

        dbHelper = GameDatabaseHelper.getInstance(this);
        tts = new TextToSpeech(this, this);

        initViews();
        setupDifficulty();
        loadWordPool();
        generateLetterRack();
        startRace();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvTitle = findViewById(R.id.tvTitle);
        tvLearned = findViewById(R.id.tvLearned);
        tvDifficulty = findViewById(R.id.tvDifficulty);
        tvScore = findViewById(R.id.tvScore);

        tvRaceStatus = findViewById(R.id.tvRaceStatus);
        lanePlayer = findViewById(R.id.lanePlayer);
        laneAi[0] = findViewById(R.id.laneAi1);
        laneAi[1] = findViewById(R.id.laneAi2);
        laneAi[2] = findViewById(R.id.laneAi3);
        horsePlayer = findViewById(R.id.horsePlayer);
        horseAi[0] = findViewById(R.id.horseAi1);
        horseAi[1] = findViewById(R.id.horseAi2);
        horseAi[2] = findViewById(R.id.horseAi3);
        tvPlayerName = findViewById(R.id.tvPlayerName);
        tvAiName[0] = findViewById(R.id.tvAiName1);
        tvAiName[1] = findViewById(R.id.tvAiName2);
        tvAiName[2] = findViewById(R.id.tvAiName3);
        progressPlayer = findViewById(R.id.progressPlayer);
        progressAi[0] = findViewById(R.id.progressAi1);
        progressAi[1] = findViewById(R.id.progressAi2);
        progressAi[2] = findViewById(R.id.progressAi3);

        tvCurrentWord = findViewById(R.id.tvCurrentWord);
        gridLetters = findViewById(R.id.gridLetters);
        btnBoost = findViewById(R.id.btnBoost);
        btnClear = findViewById(R.id.btnClear);
        btnShuffle = findViewById(R.id.btnShuffle);

        resultOverlay = findViewById(R.id.resultOverlay);
        tvResultTitle = findViewById(R.id.tvResultTitle);
        tvResultDetail = findViewById(R.id.tvResultDetail);
        btnRetry = findViewById(R.id.btnRetry);
        btnExit = findViewById(R.id.btnExit);

        btnBack.setOnClickListener(v -> finish());
        btnBoost.setOnClickListener(v -> submitWord());
        btnClear.setOnClickListener(v -> clearSelection());
        btnShuffle.setOnClickListener(v -> shuffleRack());
        btnRetry.setOnClickListener(v -> restartGame());
        btnExit.setOnClickListener(v -> finish());

        tvTitle.setText("Word Race");
        tvPlayerName.setText("You");
        tvAiName[0].setText("Comet");
        tvAiName[1].setText("Bolt");
        tvAiName[2].setText("Rocket");
    }

    private void setupDifficulty() {
        learnedCount = 0;
        GameDatabaseHelper.UserProgressData progress = dbHelper.getUserProgress();
        if (progress != null) {
            learnedCount = progress.wordsLearned;
        }

        List<WordData> learnedWords = dbHelper.getLearnedWords();
        learnedCount = Math.max(learnedCount, learnedWords.size());

        int tier;
        if (learnedCount < 10) {
            tier = 1;
        } else if (learnedCount < 30) {
            tier = 2;
        } else if (learnedCount < 60) {
            tier = 3;
        } else {
            tier = 4;
        }

        rackSize = 5 + tier;
        maxWordLength = 2 + tier;

        playerBaseSpeed = 46f + (tier * 4f);
        for (int i = 0; i < AI_COUNT; i++) {
            aiBaseSpeed[i] = 44f + (tier * 4f) + (i * 1.2f);
        }

        finishDistance = 900f + (tier * 120f);

        tvLearned.setText("Learned: " + learnedCount);
        tvDifficulty.setText(tier == 1 ? "Easy" : tier == 2 ? "Medium" : tier == 3 ? "Hard" : "Master");
    }

    private void loadWordPool() {
        validWords.clear();

        List<WordData> learnedWords = dbHelper.getLearnedWords();
        if (learnedWords.isEmpty()) {
            for (int i = 1; i <= 9; i++) {
                learnedWords.addAll(dbHelper.getWordsForPlanet(i));
            }
        }

        for (WordData word : learnedWords) {
            if (word == null || word.english == null) {
                continue;
            }
            String cleaned = word.english.toLowerCase(Locale.US).replaceAll("[^a-z]", "");
            if (cleaned.length() >= 2 && cleaned.length() <= Math.max(rackSize, maxWordLength)) {
                validWords.add(cleaned);
            }
        }

        if (validWords.isEmpty()) {
            Collections.addAll(validWords,
                "cat", "dog", "sun", "star", "moon", "ball", "fish", "car", "book", "tree",
                "rain", "bird", "frog", "cake", "milk", "shoe", "blue", "pink", "jump", "play"
            );
        }
    }

    private void startRace() {
        raceRunning = true;
        lastTickMs = SystemClock.uptimeMillis();
        tvRaceStatus.setText("Build words to boost your horse!");
        handler.post(raceTick);
    }

    private void restartGame() {
        resultOverlay.setVisibility(View.GONE);
        score = 0;
        tvScore.setText("Score: 0");
        usedWords.clear();
        clearSelection();
        generateLetterRack();

        playerDistance = 0f;
        for (int i = 0; i < AI_COUNT; i++) {
            aiDistance[i] = 0f;
        }
        playerBoostSpeed = 0f;
        playerBoostEndMs = 0L;

        raceRunning = true;
        lastTickMs = SystemClock.uptimeMillis();
        handler.post(raceTick);
    }

    private void generateLetterRack() {
        rackLetters.clear();

        String seedWord = pickSeedWord();
        for (int i = 0; i < seedWord.length(); i++) {
            rackLetters.add(seedWord.charAt(i));
        }

        while (rackLetters.size() < rackSize) {
            rackLetters.add(randomLetter());
        }

        Collections.shuffle(rackLetters);
        renderLetters();
    }

    private String pickSeedWord() {
        List<String> pool = new ArrayList<>(validWords);
        Collections.shuffle(pool);
        for (String word : pool) {
            if (word.length() <= rackSize && word.length() <= maxWordLength) {
                return word;
            }
        }
        return "cat";
    }

    private char randomLetter() {
        String vowels = "aeiou";
        String consonants = "bcdfghjklmnpqrstvwxyz";
        boolean pickVowel = random.nextFloat() < 0.35f;
        String source = pickVowel ? vowels : consonants;
        return source.charAt(random.nextInt(source.length()));
    }

    private void renderLetters() {
        gridLetters.removeAllViews();
        letterButtons.clear();
        selectedIndices.clear();
        currentWord.setLength(0);
        updateCurrentWord();

        int tileSize = (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 52, getResources().getDisplayMetrics());
        int margin = (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 6, getResources().getDisplayMetrics());

        for (int i = 0; i < rackLetters.size(); i++) {
            char letter = rackLetters.get(i);
            Button btn = new Button(this);
            btn.setAllCaps(true);
            btn.setText(String.valueOf(letter).toUpperCase(Locale.US));
            btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            btn.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            btn.setBackgroundResource(R.drawable.bg_letter_tile);
            btn.setPadding(0, 0, 0, 0);

            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = tileSize;
            lp.height = tileSize;
            lp.setMargins(margin, margin, margin, margin);
            btn.setLayoutParams(lp);

            int index = i;
            btn.setOnClickListener(v -> onLetterSelected(index));

            letterButtons.add(btn);
            gridLetters.addView(btn);
        }
    }

    private void onLetterSelected(int index) {
        if (selectedIndices.contains(index)) {
            return;
        }
        selectedIndices.add(index);
        currentWord.append(rackLetters.get(index));

        Button btn = letterButtons.get(index);
        btn.setEnabled(false);
        btn.setBackgroundResource(R.drawable.bg_letter_tile_selected);

        updateCurrentWord();
    }

    private void clearSelection() {
        selectedIndices.clear();
        currentWord.setLength(0);
        updateCurrentWord();

        for (Button btn : letterButtons) {
            btn.setEnabled(true);
            btn.setBackgroundResource(R.drawable.bg_letter_tile);
        }
    }

    private void shuffleRack() {
        Collections.shuffle(rackLetters);
        renderLetters();
    }

    private void updateCurrentWord() {
        if (currentWord.length() == 0) {
            tvCurrentWord.setText("Word: ...");
        } else {
            tvCurrentWord.setText("Word: " + currentWord.toString().toUpperCase(Locale.US));
        }
    }

    private void submitWord() {
        String word = currentWord.toString().toLowerCase(Locale.US);
        if (word.length() < 2) {
            showStatus("Try a longer word!");
            return;
        }
        if (word.length() > rackSize) {
            showStatus("Too long for this round");
            return;
        }
        if (!validWords.contains(word)) {
            showStatus("Not in the word list");
            shakeStatus();
            return;
        }
        if (usedWords.contains(word)) {
            showStatus("You already used that word");
            return;
        }

        usedWords.add(word);
        int length = word.length();
        long now = SystemClock.uptimeMillis();
        playerBoostSpeed = 12f + (length * 6f);
        playerBoostEndMs = now + (900L + length * 160L);
        playerDistance += length * 10f;

        score += length * 10;
        tvScore.setText("Score: " + score);

        showStatus("Boost! +" + length);
        speakWord(word);
        clearSelection();

        if (usedWords.size() % 3 == 0) {
            generateLetterRack();
        }
    }

    private void showStatus(String text) {
        tvRaceStatus.setText(text);
    }

    private void shakeStatus() {
        tvRaceStatus.animate().translationX(8f).setDuration(60).withEndAction(() ->
            tvRaceStatus.animate().translationX(0f).setDuration(60).start()
        ).start();
    }

    private void updateRaceUI() {
        float playerProgress = Math.min(1f, playerDistance / finishDistance);
        progressPlayer.setProgress((int) (playerProgress * 100));
        updateHorsePosition(lanePlayer, horsePlayer, playerProgress);

        for (int i = 0; i < AI_COUNT; i++) {
            float aiProgress = Math.min(1f, aiDistance[i] / finishDistance);
            progressAi[i].setProgress((int) (aiProgress * 100));
            updateHorsePosition(laneAi[i], horseAi[i], aiProgress);
        }
    }

    private void updateHorsePosition(FrameLayout lane, TextView horse, float progress) {
        int laneWidth = lane.getWidth();
        int horseWidth = horse.getWidth();
        if (laneWidth == 0 || horseWidth == 0) {
            return;
        }
        float maxX = laneWidth - horseWidth - dp(8);
        horse.setTranslationX(maxX * progress);
    }

    private boolean checkRaceFinish() {
        if (playerDistance >= finishDistance) {
            tvResultTitle.setText("You win!");
            tvResultDetail.setText("Great job! Score: " + score);
            return true;
        }
        for (int i = 0; i < AI_COUNT; i++) {
            if (aiDistance[i] >= finishDistance) {
                tvResultTitle.setText("Try again!");
                tvResultDetail.setText(tvAiName[i].getText() + " wins this race.");
                return true;
            }
        }
        return false;
    }

    private void endRace() {
        raceRunning = false;
        resultOverlay.setVisibility(View.VISIBLE);
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }

    private void speakWord(String word) {
        if (tts == null) {
            return;
        }
        tts.speak(word, TextToSpeech.QUEUE_FLUSH, null, "word");
    }

    @Override
    protected void onPause() {
        super.onPause();
        raceRunning = false;
        handler.removeCallbacks(raceTick);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (resultOverlay.getVisibility() != View.VISIBLE) {
            raceRunning = true;
            lastTickMs = SystemClock.uptimeMillis();
            handler.post(raceTick);
        }
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(raceTick);
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.US);
            tts.setSpeechRate(0.9f);
        }
    }
}
