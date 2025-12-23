package com.example.engapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.engapp.database.GameDatabaseHelper;
import com.example.engapp.database.GameDatabaseHelper.WordData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    private static final long IDLE_WARN_1_MS = 12000L;
    private static final long IDLE_WARN_2_MS = 18000L;
    private static final long IDLE_LOSE_MS = 25000L;
    private static final long NO_PROGRESS_LOSE_MS = 45000L;

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
    private TextView[] tvAiStatus = new TextView[AI_COUNT];
    private ProgressBar progressPlayer;
    private ProgressBar[] progressAi = new ProgressBar[AI_COUNT];

    // UI - Word panel
    private TextView tvCurrentWord;
    private GridLayout gridLetters;
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
    private final Set<String> learnedWordsSet = new HashSet<>();
    private final Set<String> dictionaryWordsSet = new HashSet<>();
    private final Set<String> usedWords = new HashSet<>();

    private long lastTickMs;
    private long raceStartMs;
    private long lastInputMs;
    private int idleWarnStage;
    private boolean raceRunning;

    private float finishDistance;
    private float playerDistance;
    private float[] aiDistance = new float[AI_COUNT];

    private float playerBaseSpeed;
    private float[] aiBaseSpeed = new float[AI_COUNT];
    private float playerBoostSpeed;
    private long playerBoostEndMs;

    private float[] aiBoostSpeed = new float[AI_COUNT];
    private float[] aiBoostBase = new float[AI_COUNT];
    private long[] aiBoostEndMs = new long[AI_COUNT];
    private long[] aiNextBoostMs = new long[AI_COUNT];
    private long[] aiBoostIntervalMs = new long[AI_COUNT];

    private int learnedCount;
    private int rackSize;
    private int maxWordLength;
    private int score;
    private int wrongAttempts;
    private int correctWords;
    private int maxWrongAttempts = 5;

    private final String[] idleMessages = new String[] {
        "Nhanh len! Chon chu nao!",
        "Ban oi, hay ghep tu de tang toc!",
        "Co len! Con ngua dang cham day!"
    };

    private final String[] wrongMessages = new String[] {
        "Gan dung roi! Thu lai nhe!",
        "Khong sao! Chon tu khac!",
        "Co len! Tu nay chua dung!"
    };

    private final String[] loseMessages = new String[] {
        "Lan sau se tot hon!",
        "Hay thu lai nhe!",
        "Dung bo cuoc, ban lam duoc!"
    };

    private final Runnable raceTick = new Runnable() {
        @Override
        public void run() {
            if (!raceRunning) {
                return;
            }
            long now = SystemClock.uptimeMillis();
            float deltaSec = (now - lastTickMs) / 1000f;
            lastTickMs = now;

            if (checkIdle(now) || checkPoorPlay(now)) {
                return;
            }

            float boost = (now < playerBoostEndMs) ? playerBoostSpeed : 0f;
            playerDistance += (playerBaseSpeed + boost) * deltaSec;

            for (int i = 0; i < AI_COUNT; i++) {
            if (now >= aiNextBoostMs[i]) {
                applyAiWordBoost(i, now);
            }
            float aiBoost = (now < aiBoostEndMs[i]) ? aiBoostSpeed[i] : 0f;
            float jitter = (random.nextFloat() - 0.45f) * 6f;
            aiDistance[i] += (aiBaseSpeed[i] + aiBoost + jitter) * deltaSec;
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
        tvAiStatus[0] = findViewById(R.id.tvAiStatus1);
        tvAiStatus[1] = findViewById(R.id.tvAiStatus2);
        tvAiStatus[2] = findViewById(R.id.tvAiStatus3);
        progressPlayer = findViewById(R.id.progressPlayer);
        progressAi[0] = findViewById(R.id.progressAi1);
        progressAi[1] = findViewById(R.id.progressAi2);
        progressAi[2] = findViewById(R.id.progressAi3);

        tvCurrentWord = findViewById(R.id.tvCurrentWord);
        gridLetters = findViewById(R.id.gridLetters);
        btnClear = findViewById(R.id.btnClear);
        btnShuffle = findViewById(R.id.btnShuffle);

        resultOverlay = findViewById(R.id.resultOverlay);
        tvResultTitle = findViewById(R.id.tvResultTitle);
        tvResultDetail = findViewById(R.id.tvResultDetail);
        btnRetry = findViewById(R.id.btnRetry);
        btnExit = findViewById(R.id.btnExit);

        btnBack.setOnClickListener(v -> finish());
        btnClear.setOnClickListener(v -> clearSelection());
        btnShuffle.setOnClickListener(v -> shuffleRack());
        btnRetry.setOnClickListener(v -> restartGame());
        btnExit.setOnClickListener(v -> finish());

        tvTitle.setText("Word Race");
        tvPlayerName.setText("You");
        tvAiName[0].setText("Comet");
        tvAiName[1].setText("Bolt");
        tvAiName[2].setText("Rocket");

        Animation bob = AnimationUtils.loadAnimation(this, R.anim.horse_bob);
        horsePlayer.startAnimation(bob);
        horseAi[0].startAnimation(bob);
        horseAi[1].startAnimation(bob);
        horseAi[2].startAnimation(bob);
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

        rackSize = 10;
        maxWordLength = rackSize;

        playerBaseSpeed = 40f + (tier * 3.5f);
        for (int i = 0; i < AI_COUNT; i++) {
            configureAiProfile(i, tier);
        }

        finishDistance = 1500f + (tier * 180f);

        tvLearned.setText("Learned: " + learnedCount);
        tvDifficulty.setText(tier == 1 ? "Easy" : tier == 2 ? "Medium" : tier == 3 ? "Hard" : "Master");
    }

    private void loadWordPool() {
        validWords.clear();
        learnedWordsSet.clear();
        dictionaryWordsSet.clear();

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
            if (cleaned.length() >= 1) {
                learnedWordsSet.add(cleaned);
                validWords.add(cleaned);
            }
        }

        loadDictionaryWords();
        validWords.addAll(dictionaryWordsSet);

        if (validWords.size() < 10) {
            Collections.addAll(validWords,
                "a", "i", "am", "an", "at", "be", "cat", "dog", "sun", "star",
                "moon", "ball", "fish", "car", "book", "tree", "rain", "bird",
                "frog", "cake", "milk", "shoe", "blue", "pink", "jump", "play"
            );
        }
    }

    private void startRace() {
        raceRunning = true;
        long now = SystemClock.uptimeMillis();
        lastTickMs = now;
        raceStartMs = now;
        lastInputMs = now;
        idleWarnStage = 0;
        wrongAttempts = 0;
        correctWords = 0;

        for (int i = 0; i < AI_COUNT; i++) {
            aiNextBoostMs[i] = now + 1400L + (i * 700L);
            aiBoostEndMs[i] = 0L;
        }

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

        startRace();
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

        gridLetters.setColumnCount(rackLetters.size() <= 8 ? 4 : 5);

        int tileSize = (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 52, getResources().getDisplayMetrics());
        int margin = (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 6, getResources().getDisplayMetrics());

        for (int i = 0; i < rackLetters.size(); i++) {
            char letter = rackLetters.get(i);
            Button btn = new Button(this);
            btn.setAllCaps(true);
            btn.setText(String.valueOf(letter).toUpperCase(Locale.US));
        btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        btn.setTextColor(ContextCompat.getColor(this, R.color.text_white));
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
        recordInput();
        selectedIndices.add(index);
        currentWord.append(rackLetters.get(index));

        Button btn = letterButtons.get(index);
        btn.setEnabled(false);
        btn.setBackgroundResource(R.drawable.bg_letter_tile_selected);
        btn.setTextColor(ContextCompat.getColor(this, R.color.text_primary));

        updateCurrentWord();
        autoSubmitIfReady();
    }

    private void clearSelection() {
        selectedIndices.clear();
        currentWord.setLength(0);
        updateCurrentWord();

        for (Button btn : letterButtons) {
            btn.setEnabled(true);
            btn.setBackgroundResource(R.drawable.bg_letter_tile);
            btn.setTextColor(ContextCompat.getColor(this, R.color.text_white));
        }
    }

    private void shuffleRack() {
        recordInput();
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
        recordInput();
        String word = currentWord.toString().toLowerCase(Locale.US);
        if (word.length() > rackSize) {
            registerWrong("Tu dai qua roi!");
            return;
        }
        if (!validWords.contains(word)) {
            registerWrong("Tu nay khong dung hoac chua co trong tu dien");
            return;
        }
        if (usedWords.contains(word)) {
            registerWrong("Tu nay da dung roi!");
            return;
        }

        usedWords.add(word);
        correctWords++;
        int length = word.length();
        long now = SystemClock.uptimeMillis();
        playerBoostSpeed = 12f + (length * 6f);
        playerBoostEndMs = now + (900L + length * 160L);
        playerDistance += length * 10f;

        score += length * 10;
        tvScore.setText("Score: " + score);

        showStatus("Boost x" + length + "!");
        playStatusPop();
        speakWord(word);
        clearSelection();

        if (usedWords.size() % 3 == 0) {
            generateLetterRack();
        }
    }

    private void autoSubmitIfReady() {
        String word = currentWord.toString().toLowerCase(Locale.US);
        if (word.length() < 2) {
            return;
        }
        if (!validWords.contains(word)) {
            return;
        }
        if (usedWords.contains(word)) {
            return;
        }
        submitWord();
    }

    private void registerWrong(String message) {
        wrongAttempts++;
        showStatus(message);
        playStatusPop();
        playerDistance = Math.max(0f, playerDistance - 20f);
        if (wrongAttempts >= maxWrongAttempts) {
            loseRace("Sai qua nhieu roi. " + randomMessage(loseMessages));
        }
    }

    private void recordInput() {
        lastInputMs = SystemClock.uptimeMillis();
        idleWarnStage = 0;
    }

    private boolean checkIdle(long now) {
        long idle = now - lastInputMs;
        if (idle >= IDLE_LOSE_MS) {
            loseRace("Dung lai lau qua. " + randomMessage(loseMessages));
            return true;
        }
        if (idle >= IDLE_WARN_2_MS && idleWarnStage < 2) {
            showStatus(randomMessage(idleMessages));
            idleWarnStage = 2;
            return false;
        }
        if (idle >= IDLE_WARN_1_MS && idleWarnStage < 1) {
            showStatus(randomMessage(idleMessages));
            idleWarnStage = 1;
        }
        return false;
    }

    private boolean checkPoorPlay(long now) {
        if (correctWords == 0 && now - raceStartMs >= NO_PROGRESS_LOSE_MS) {
            loseRace("Chua co tu dung. " + randomMessage(loseMessages));
            return true;
        }
        if (wrongAttempts >= 3 && correctWords == 0 && now - raceStartMs >= 25000L) {
            loseRace("Hay thu lai nhe. " + randomMessage(loseMessages));
            return true;
        }
        return false;
    }

    private void showStatus(String text) {
        tvRaceStatus.setText(text);
    }

    private void playStatusPop() {
        Animation pop = AnimationUtils.loadAnimation(this, R.anim.status_pop);
        tvRaceStatus.startAnimation(pop);
    }

    private String randomMessage(String[] messages) {
        return messages[random.nextInt(messages.length)];
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

    private void updateHorsePosition(FrameLayout lane, View horse, float progress) {
        int laneWidth = lane.getWidth();
        int horseWidth = horse.getWidth();
        if (laneWidth == 0 || horseWidth == 0) {
            return;
        }
        float maxX = laneWidth - horseWidth - dp(8);
        horse.setTranslationX(maxX * progress);
    }

    private void configureAiProfile(int index, int tier) {
        int learnedFactor = Math.min(learnedCount, 120);
        int baseInterval = Math.max(1400, 3600 - (learnedFactor * 16));

        switch (index) {
            case 0: // Comet - steady, slower
                aiBaseSpeed[index] = 36f + (tier * 3.2f);
                aiBoostBase[index] = 8f + (tier * 1.2f);
                aiBoostIntervalMs[index] = baseInterval + 600L;
                break;
            case 1: // Bolt - balanced
                aiBaseSpeed[index] = 38f + (tier * 3.4f);
                aiBoostBase[index] = 10f + (tier * 1.4f);
                aiBoostIntervalMs[index] = baseInterval + 200L;
                break;
            default: // Rocket - aggressive
                aiBaseSpeed[index] = 40f + (tier * 3.6f);
                aiBoostBase[index] = 12f + (tier * 1.6f);
                aiBoostIntervalMs[index] = Math.max(1200L, baseInterval - 200L);
                break;
        }
    }

    private void applyAiWordBoost(int index, long now) {
        int learnedFactor = Math.min(learnedCount, 120);
        int baseLen = Math.min(maxWordLength, 2 + (learnedFactor / 20));
        int length = Math.max(2, Math.min(maxWordLength, baseLen + index + random.nextInt(2)));
        String word = pickAiWord(length);

        aiBoostEndMs[index] = now + 700L + (length * 140L);
        aiBoostSpeed[index] = aiBoostBase[index] + (length * 1.2f);
        aiDistance[index] += length * 9f;
        aiNextBoostMs[index] = now + aiBoostIntervalMs[index] + random.nextInt(500);

        if (tvAiStatus[index] != null) {
            tvAiStatus[index].setText(tvAiName[index].getText() + ": " + word.toUpperCase(Locale.US) + " +" + length);
        }
    }

    private String pickAiWord(int length) {
        List<String> pool = new ArrayList<>();
        for (String word : validWords) {
            if (word.length() == length) {
                pool.add(word);
            }
        }
        if (!pool.isEmpty()) {
            return pool.get(random.nextInt(pool.size()));
        }
        String letters = "abcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(letters.charAt(random.nextInt(letters.length())));
        }
        return sb.toString();
    }

    private void loadDictionaryWords() {
        InputStream inputStream = getResources().openRawResource(R.raw.wordlist_en);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String cleaned = line.trim().toLowerCase(Locale.US).replaceAll("[^a-z]", "");
                if (cleaned.length() >= 1) {
                    dictionaryWordsSet.add(cleaned);
                }
            }
        } catch (IOException e) {
            // Ignore dictionary load issues; fallback list will be used.
        }
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

    private void loseRace(String reason) {
        tvResultTitle.setText("You lose!");
        tvResultDetail.setText(reason);
        endRace();
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
