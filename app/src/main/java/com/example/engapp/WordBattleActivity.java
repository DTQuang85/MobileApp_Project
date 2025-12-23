package com.example.engapp;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.engapp.database.GameDatabaseHelper;
import com.example.engapp.database.GameDatabaseHelper.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * WordBattle - Game chiến đấu bằng từ vựng kiểu Bookworm Adventures
 * Trẻ ghép chữ cái thành từ tiếng Anh để tấn công quái vật
 * Từ càng dài/khó = sát thương càng cao
 */
public class WordBattleActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    // UI Views
    private TextView tvBuddyEmoji, tvBuddyName, tvBuddyMessage;
    private TextView tvPlayerHealth, tvPlayerName;
    private TextView tvEnemyEmoji, tvEnemyName, tvEnemyHealth, tvDamagePopup;
    private ProgressBar progressPlayerHealth, progressEnemyHealth;
    private TextView tvCurrentWord, tvWordMeaning, tvDamagePreview;
    private GridLayout letterGrid;
    private Button btnAttack, btnClear, btnHint;
    private LinearLayout wordDisplay;
    private ImageView btnBack;
    private TextView tvLevel, tvScore, tvCombo;

    // Game State
    private TextToSpeech tts;
    private GameDatabaseHelper dbHelper;
    private SharedPreferences prefs;
    private Random random = new Random();

    // Player stats
    private int playerHealth = 100;
    private int maxPlayerHealth = 100;
    private int playerLevel = 1;
    private int totalScore = 0;
    private int combo = 0;
    private int currentStage = 1;
    private int initialStage = 1;

    // Enemy stats
    private int enemyHealth = 50;
    private int maxEnemyHealth = 50;
    private String enemyEmoji = "👾";
    private String enemyName = "Slime";
    private int enemyDamage = 10;

    // Letters & Words
    private char[] currentLetters = new char[16];
    private List<Integer> selectedIndices = new ArrayList<>();
    private StringBuilder currentWord = new StringBuilder();
    private Set<String> validWords = new HashSet<>();
    private Set<String> usedWords = new HashSet<>();
    private List<WordData> allWords;
    private List<WordData> learnedWords = new ArrayList<>();
    private List<String> currentBoardWords = new ArrayList<>();
    private int wordsLearnedCount = 0;
    private float difficultyScale = 1f;

    // Buddy
    private String buddyEmoji = "🤖";
    private String buddyName = "Robo-Buddy";
    private int buddyIndex = 0;

    // Enemy data for different stages - linked to planets
    private static final String[][] ENEMIES = {
        // {emoji, name, health, damage, planet_theme}
        {"🎨", "Color Blob", "40", "8"},       // Planet 1: Coloria
        {"🧸", "Evil Teddy", "50", "10"},      // Planet 2: Toytopia
        {"🦁", "Wild Lion", "60", "12"},       // Planet 3: Animania
        {"🚗", "Traffic Bot", "65", "13"},     // Planet 4: Citytron
        {"🍕", "Food Monster", "70", "14"},    // Planet 5: Foodora
        {"🌪️", "Storm Spirit", "80", "16"},   // Planet 6: Weatheron
        {"🤖", "Rogue Robot", "90", "17"},     // Planet 7: RoboLab
        {"⏰", "Time Phantom", "100", "18"},   // Planet 8: TimeLapse
        {"🐲", "Story Dragon", "120", "20"},   // Planet 9: Storyverse
        {"👾", "Galaxy Boss", "150", "25"},    // Final Boss
    };

    // Buddy messages
    private String[][] buddyMessages = {
        // Robo-Buddy
        {"Tấn công! Beep boop! 🤖", "Từ hay lắm! Dữ liệu ghi nhận! 💾", "Cẩn thận! HP thấp! ⚠️", "COMBO! Hiệu suất tối ưu! ⚡"},
        // Alien-Friend
        {"Zog! Tấn công đi! 👽", "Từ này hay! Trên sao tôi không có! 🛸", "Ouch! Cần hồi máu! 💫", "WOW! Earth friend giỏi quá! 🌟"},
        // Kitty-Pal
        {"Meo! Đánh nó đi! 🐱", "Purrrr... Từ đẹp lắm! 😺", "Meo đau! Cần nghỉ ngơi! 😿", "COMBO! Meo tự hào lắm! 🎉"},
        // Foxy-Guide
        {"Yip! Tấn công thôi! 🦊", "Từ hay! Cáo thông minh khen! 🧠", "Cẩn thận! Cáo lo lắng! 😰", "Tuyệt vời! Nhanh như cáo! ⚡"},
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_battle);
        overridePendingTransition(R.anim.fade_scale_in, 0);

        dbHelper = GameDatabaseHelper.getInstance(this);
        prefs = getSharedPreferences("game_prefs", MODE_PRIVATE);
        tts = new TextToSpeech(this, this);

        configureDifficulty();
        loadBuddyInfo();
        loadValidWords();
        initViews();
        setupGame();
        generateLetters();
        spawnEnemy();
    }

    private void configureDifficulty() {
        GameDatabaseHelper.UserProgressData progress = dbHelper.getUserProgress();
        if (progress != null) {
            wordsLearnedCount = progress.wordsLearned;
        }

        int requestedStage = getIntent().getIntExtra("start_stage", -1);
        int startingPlanet = getIntent().getIntExtra("planet_id", 1);
        int derivedStage = Math.max(1, Math.min(ENEMIES.length, 1 + wordsLearnedCount / 8));

        int baseStage = derivedStage;
        if (requestedStage > 0) {
            baseStage = Math.max(derivedStage, requestedStage);
        } else if (startingPlanet > 0) {
            baseStage = Math.max(derivedStage, startingPlanet);
        }

        initialStage = Math.max(1, Math.min(baseStage, ENEMIES.length));
        difficultyScale = calculateDifficultyScale(wordsLearnedCount);
        setStageAndLevel(initialStage);
    }

    private void setStageAndLevel(int stage) {
        currentStage = Math.max(1, Math.min(stage, ENEMIES.length));
        playerLevel = Math.max(1, ((currentStage - 1) / 2) + 1);
        maxPlayerHealth = 80 + playerLevel * 20;
        playerHealth = maxPlayerHealth;
    }

    private void loadBuddyInfo() {
        buddyIndex = prefs.getInt("buddy_index", 0);
        String[] emojis = {"🤖", "👽", "🐱", "🦊"};
        String[] names = {"Robo-Buddy", "Alien-Friend", "Kitty-Pal", "Foxy-Guide"};

        if (buddyIndex >= 0 && buddyIndex < emojis.length) {
            buddyEmoji = emojis[buddyIndex];
            buddyName = names[buddyIndex];
        }
    }

    private void loadValidWords() {
        validWords.clear();
        learnedWords = dbHelper.getLearnedWords();
        if (learnedWords == null) {
            learnedWords = new ArrayList<>();
        }
        allWords = new ArrayList<>();

        if (!learnedWords.isEmpty()) {
            allWords.addAll(learnedWords);
            for (WordData word : learnedWords) {
                if (word != null && word.english != null) {
                    validWords.add(word.english.toLowerCase());
                }
            }
        } else {
            // Fallback to general word list if child hasn't learned any yet
            List<WordData> starterWords = dbHelper.getWordsForPlanet(1);
            if (starterWords != null) {
                allWords.addAll(starterWords);
            }
            for (int i = 2; i <= 9; i++) {
                List<WordData> planetWords = dbHelper.getWordsForPlanet(i);
                if (planetWords != null) {
                    allWords.addAll(planetWords);
                }
            }
            for (WordData word : allWords) {
                if (word != null && word.english != null) {
                    validWords.add(word.english.toLowerCase());
                }
            }
        }

        wordsLearnedCount = Math.max(wordsLearnedCount, learnedWords.size());

        // Add simple English words to keep gameplay possible when vocabulary is still tiny
        if (validWords.size() < 25) {
            String[] commonWords = {
            "a", "i", "an", "at", "be", "by", "do", "go", "he", "if", "in", "is", "it", "me", "my", "no", "of", "on", "or", "so", "to", "up", "us", "we",
            "ace", "act", "add", "age", "ago", "aid", "aim", "air", "all", "and", "ant", "any", "ape", "arc", "are", "ark", "arm", "art", "ask", "ate",
            "bad", "bag", "ban", "bar", "bat", "bay", "bed", "bee", "bet", "big", "bit", "bow", "box", "boy", "bud", "bug", "bus", "but", "buy",
            "cab", "can", "cap", "car", "cat", "cop", "cot", "cow", "cry", "cub", "cup", "cut",
            "dad", "dam", "day", "den", "dew", "did", "die", "dig", "dim", "dip", "dog", "dot", "dry", "dub", "dud", "due", "dug", "dye",
            "ear", "eat", "eel", "egg", "ego", "elf", "elk", "elm", "emu", "end", "era", "eve", "ewe", "eye",
            "fan", "far", "fat", "fax", "fed", "fee", "few", "fig", "fin", "fir", "fit", "fix", "fly", "foe", "fog", "for", "fox", "fry", "fun", "fur",
            "gab", "gag", "gap", "gas", "gay", "gel", "gem", "get", "gig", "gin", "god", "got", "gum", "gun", "gut", "guy", "gym",
            "had", "ham", "has", "hat", "hay", "hem", "hen", "her", "hid", "him", "hip", "his", "hit", "hob", "hog", "hop", "hot", "how", "hub", "hue", "hug", "hum", "hut",
            "ice", "icy", "ill", "imp", "ink", "inn", "ion", "ire", "irk", "its", "ivy",
            "jab", "jag", "jam", "jar", "jaw", "jay", "jet", "jig", "job", "jog", "jot", "joy", "jug", "jut",
            "keg", "ken", "key", "kid", "kin", "kit",
            "lab", "lac", "lad", "lag", "lap", "law", "lay", "lea", "led", "leg", "let", "lid", "lie", "lip", "lit", "log", "lot", "low", "lug",
            "mad", "man", "map", "mar", "mat", "maw", "may", "men", "met", "mid", "mix", "mob", "mod", "mom", "mop", "mow", "mud", "mug", "mum",
            "nab", "nag", "nap", "nay", "net", "new", "nib", "nil", "nip", "nit", "nob", "nod", "nor", "not", "now", "nub", "nun", "nut",
            "oak", "oar", "oat", "odd", "ode", "off", "oft", "ohm", "oil", "old", "one", "opt", "orb", "ore", "our", "out", "owe", "owl", "own",
            "ace", "pad", "pal", "pan", "pap", "par", "pat", "paw", "pay", "pea", "peg", "pen", "pep", "per", "pet", "pew", "pie", "pig", "pin", "pit", "ply", "pod", "pop", "pot", "pow", "pro", "pry", "pub", "pug", "pun", "pup", "pus", "put",
            "rad", "rag", "ram", "ran", "rap", "rat", "raw", "ray", "red", "ref", "rep", "rib", "rid", "rig", "rim", "rip", "rob", "rod", "roe", "rot", "row", "rub", "rue", "rug", "rum", "run", "rut", "rye",
            "sac", "sad", "sag", "sap", "sat", "saw", "say", "sea", "set", "sew", "she", "shy", "sin", "sip", "sir", "sis", "sit", "six", "ski", "sky", "sly", "sob", "sod", "son", "sop", "sot", "sow", "soy", "spa", "spy", "sty", "sub", "sue", "sum", "sun", "sup",
            "tab", "tad", "tag", "tan", "tap", "tar", "tat", "tax", "tea", "ten", "the", "thy", "tic", "tie", "tin", "tip", "tit", "toe", "tog", "tom", "ton", "too", "top", "tot", "tow", "toy", "try", "tub", "tug", "tun", "tut", "two",
            "ugh", "ump", "uns", "ups", "urn", "use",
            "van", "vat", "vet", "via", "vie", "vim", "vow",
            "wad", "wag", "war", "was", "wax", "way", "web", "wed", "wee", "wet", "who", "why", "wig", "win", "wit", "woe", "wok", "won", "woo", "wow",
            "yak", "yam", "yap", "yaw", "yea", "yen", "yep", "yes", "yet", "yew", "yip", "you", "yow", "yuk", "yum", "yup",
            "zag", "zap", "zed", "zee", "zen", "zig", "zip", "zit", "zoo",
            // 4+ letter words
            "able", "also", "back", "ball", "bear", "best", "bird", "blue", "boat", "book", "both", "cake", "call", "came", "card", "care", "city", "come", "cool",
            "dark", "dear", "does", "done", "door", "down", "draw", "each", "east", "easy", "even", "ever", "face", "fact", "fall", "fast", "feel", "fire", "fish",
            "food", "from", "game", "gave", "girl", "give", "glad", "gold", "good", "grow", "hand", "hard", "have", "head", "hear", "help", "here", "high", "hold",
            "home", "hope", "hour", "idea", "into", "just", "keep", "kind", "king", "know", "lake", "land", "last", "left", "life", "like", "line", "live", "long",
            "look", "lost", "love", "made", "main", "make", "many", "mind", "more", "most", "move", "much", "must", "name", "near", "need", "next", "nice", "open",
            "over", "page", "part", "pick", "place", "plan", "play", "rain", "read", "rest", "ride", "road", "rock", "room", "same", "sand", "save", "show", "side",
            "snow", "some", "soon", "star", "stay", "stop", "such", "sure", "take", "talk", "tell", "than", "that", "them", "then", "they", "this", "time", "tree",
            "turn", "upon", "very", "wait", "walk", "want", "warm", "week", "well", "west", "what", "when", "will", "wind", "wish", "with", "wood", "word", "work",
            "year", "your", "zero", "zoom"
            };

            for (String w : commonWords) {
                validWords.add(w.toLowerCase());
            }
        }

        difficultyScale = calculateDifficultyScale(wordsLearnedCount);
    }

    private List<WordData> getActiveWordPool() {
        if (learnedWords != null && !learnedWords.isEmpty()) {
            return learnedWords;
        }
        if (allWords == null) {
            allWords = new ArrayList<>();
        }
        return allWords;
    }

    private void initViews() {
        // Buddy
        tvBuddyEmoji = findViewById(R.id.tvBuddyEmoji);
        tvBuddyName = findViewById(R.id.tvBuddyName);
        tvBuddyMessage = findViewById(R.id.tvBuddyMessage);

        // Player
        tvPlayerHealth = findViewById(R.id.tvPlayerHealth);
        tvPlayerName = findViewById(R.id.tvPlayerName);
        progressPlayerHealth = findViewById(R.id.progressPlayerHealth);

        // Enemy
        tvEnemyEmoji = findViewById(R.id.tvEnemyEmoji);
        tvEnemyName = findViewById(R.id.tvEnemyName);
        tvEnemyHealth = findViewById(R.id.tvEnemyHealth);
        tvDamagePopup = findViewById(R.id.tvDamagePopup);
        progressEnemyHealth = findViewById(R.id.progressEnemyHealth);

        // Word building
        tvCurrentWord = findViewById(R.id.tvCurrentWord);
        tvWordMeaning = findViewById(R.id.tvWordMeaning);
        tvDamagePreview = findViewById(R.id.tvDamagePreview);
        letterGrid = findViewById(R.id.letterGrid);
        wordDisplay = findViewById(R.id.wordDisplay);

        // Buttons
        btnAttack = findViewById(R.id.btnAttack);
        btnClear = findViewById(R.id.btnClear);
        btnHint = findViewById(R.id.btnHint);
        btnBack = findViewById(R.id.btnBack);

        // Stats
        tvLevel = findViewById(R.id.tvLevel);
        tvScore = findViewById(R.id.tvScore);
        tvCombo = findViewById(R.id.tvCombo);

        // Set buddy
        tvBuddyEmoji.setText(buddyEmoji);
        tvBuddyName.setText(buddyName);

        // Button listeners
        btnBack.setOnClickListener(v -> finish());
        btnAttack.setOnClickListener(v -> attack());
        btnClear.setOnClickListener(v -> clearWord());
        btnHint.setOnClickListener(v -> showHint());
    }

    private void setupGame() {
        playerHealth = maxPlayerHealth;
        updateUI();
        showBuddyMessage("Sẵn sàng chiến đấu! Ghép chữ thành từ tiếng Anh để tấn công! 💪");
    }

    private void generateLetters() {
        letterGrid.removeAllViews();
        currentBoardWords.clear();

        List<WordData> pool = getActiveWordPool();
        if (pool != null && !pool.isEmpty()) {
            List<WordData> shuffled = new ArrayList<>(pool);
            Collections.shuffle(shuffled);
            int letterBudget = 12;
            int totalLetters = 0;

            for (WordData word : shuffled) {
                if (word == null || word.english == null) continue;
                String clean = word.english.replaceAll("[^A-Za-z]", "");
                if (clean.length() < 2) continue;
                if (currentBoardWords.isEmpty() || totalLetters + clean.length() <= letterBudget) {
                    currentBoardWords.add(clean.toLowerCase());
                    totalLetters += clean.length();
                }
                if (currentBoardWords.size() >= 3 || totalLetters >= letterBudget) {
                    break;
                }
            }
        }

        if (currentBoardWords.isEmpty()) {
            currentBoardWords.add("cat");
            currentBoardWords.add("sun");
        }

        List<Character> letterPool = new ArrayList<>();
        for (String word : currentBoardWords) {
            for (char c : word.toUpperCase().toCharArray()) {
                letterPool.add(c);
            }
        }

        while (letterPool.size() < 16) {
            letterPool.add(pickFillerLetter());
        }

        Collections.shuffle(letterPool);

        for (int i = 0; i < 16; i++) {
            currentLetters[i] = letterPool.get(i);
            addLetterButton(i, currentLetters[i]);
        }
    }

    private void addLetterButton(int index, char letter) {
        CardView card = new CardView(this);
        card.setRadius(24);
        card.setCardElevation(0);
        card.setUseCompatPadding(false);
        card.setPreventCornerOverlap(false);
        card.setCardBackgroundColor(Color.TRANSPARENT);

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(index % 4, 1f);
        params.rowSpec = GridLayout.spec(index / 4);
        params.setMargins(8, 8, 8, 8);
        card.setLayoutParams(params);

        TextView tv = new TextView(this);
        tv.setText(String.valueOf(letter));
        tv.setTextColor(getColor(R.color.text_white));
        tv.setTextSize(24);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(0, 24, 0, 24);
        tv.setTypeface(null, android.graphics.Typeface.BOLD);

        card.addView(tv);
        card.setTag(index);
        applyLetterDefaultStyle(card);

        card.setOnClickListener(v -> selectLetter(index, card));

        letterGrid.addView(card);
    }

    private char pickFillerLetter() {
        float roll = random.nextFloat();
        if (roll < 0.4f) return randomCharFrom("AEIOU");
        if (roll < 0.9f) return randomCharFrom("BCDFGHLMNPRST");
        return randomCharFrom("JKQVWXYZ");
    }

    private char randomCharFrom(String source) {
        return source.charAt(random.nextInt(source.length()));
    }

    private void selectLetter(int index, CardView card) {
        if (selectedIndices.contains(index)) {
            // Deselect
            selectedIndices.remove(Integer.valueOf(index));
            applyLetterDefaultStyle(card);

            // Rebuild word
            rebuildWord();
        } else {
            // Select
            selectedIndices.add(index);
            applyLetterSelectedStyle(card);
            currentWord.append(currentLetters[index]);
        }

        updateWordDisplay();
    }

    private void rebuildWord() {
        currentWord = new StringBuilder();
        for (int i : selectedIndices) {
            currentWord.append(currentLetters[i]);
        }
    }

    private void updateWordDisplay() {
        String word = currentWord.toString().toLowerCase();
        tvCurrentWord.setText(word.isEmpty() ? "..." : word.toUpperCase());

        // Check if valid word
        boolean isValid = isValidWord(word);

        if (isValid && !usedWords.contains(word)) {
            int damage = calculateDamage(word);
            tvDamagePreview.setText("⚔️ " + damage + " sát thương");
            tvDamagePreview.setTextColor(getColor(R.color.correct_green));
            btnAttack.setEnabled(true);
            btnAttack.setAlpha(1f);

            // Find meaning
            String meaning = findWordMeaning(word);
            tvWordMeaning.setText(meaning);
            tvWordMeaning.setVisibility(View.VISIBLE);
        } else if (usedWords.contains(word)) {
            tvDamagePreview.setText("❌ Đã dùng từ này rồi!");
            tvDamagePreview.setTextColor(getColor(R.color.wrong_red));
            btnAttack.setEnabled(false);
            btnAttack.setAlpha(0.5f);
            tvWordMeaning.setVisibility(View.GONE);
        } else if (word.length() >= 2) {
            tvDamagePreview.setText("❓ Chưa phải từ hợp lệ");
            tvDamagePreview.setTextColor(getColor(R.color.text_hint));
            btnAttack.setEnabled(false);
            btnAttack.setAlpha(0.5f);
            tvWordMeaning.setVisibility(View.GONE);
        } else {
            tvDamagePreview.setText("Ghép ít nhất 2 chữ cái");
            tvDamagePreview.setTextColor(getColor(R.color.text_hint));
            btnAttack.setEnabled(false);
            btnAttack.setAlpha(0.5f);
            tvWordMeaning.setVisibility(View.GONE);
        }
    }

    private boolean isValidWord(String word) {
        return word.length() >= 2 && validWords.contains(word.toLowerCase());
    }

    private int calculateDamage(String word) {
        int baseDamage = word.length() * 5;

        // Bonus for longer words
        if (word.length() >= 5) baseDamage += 10;
        if (word.length() >= 6) baseDamage += 15;
        if (word.length() >= 7) baseDamage += 25;
        if (word.length() >= 8) baseDamage += 40;

        // Bonus for rare letters
        for (char c : word.toUpperCase().toCharArray()) {
            if ("JKQXZ".indexOf(c) >= 0) baseDamage += 5;
            if ("VWXY".indexOf(c) >= 0) baseDamage += 3;
        }

        // Combo bonus
        if (combo > 0) {
            baseDamage += combo * 3;
        }

        return baseDamage;
    }

    private String findWordMeaning(String word) {
        if (allWords != null) {
            for (WordData w : allWords) {
                if (w.english.equalsIgnoreCase(word)) {
                    return w.emoji + " " + w.vietnamese;
                }
            }
        }
        return "📖 Từ tiếng Anh hợp lệ";
    }

    private void attack() {
        String word = currentWord.toString().toLowerCase();

        if (!isValidWord(word) || usedWords.contains(word)) {
            Toast.makeText(this, "Từ không hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }

        usedWords.add(word);

        int damage = calculateDamage(word);

        // Attack animation - buddy attacks
        tvBuddyEmoji.startAnimation(AnimationUtils.loadAnimation(this, R.anim.attack_hit));

        // Enemy gets hit with shake
        new Handler().postDelayed(() -> {
            tvEnemyEmoji.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake));
        }, 200);

        // Deal damage
        enemyHealth -= damage;
        if (enemyHealth < 0) enemyHealth = 0;

        // Increase combo
        combo++;

        // Add score
        int scoreGain = damage * 10 + word.length() * 5;
        totalScore += scoreGain;

        // Play attack Lottie animation
        playAttackAnimation();

        // Show damage popup with animation
        showDamagePopup(damage, word);

        // Speak the word
        if (tts != null) {
            tts.speak(word, TextToSpeech.QUEUE_FLUSH, null, "attack");
        }

        // Buddy reaction
        if (buddyIndex < buddyMessages.length) {
            if (combo >= 3) {
                showBuddyMessage(buddyMessages[buddyIndex][3]); // Combo message
            } else {
                showBuddyMessage(buddyMessages[buddyIndex][1]); // Nice word message
            }
        }

        // Clear word
        clearWord();

        // Check enemy death
        if (enemyHealth <= 0) {
            new Handler().postDelayed(this::enemyDefeated, 500);
        } else {
            // Enemy counter-attack after delay
            new Handler().postDelayed(this::enemyAttack, 1200);
        }

        updateUI();
    }

    private void showDamagePopup(int damage, String word) {
        // Show floating damage text
        tvDamagePopup.setText("-" + damage);
        tvDamagePopup.setVisibility(View.VISIBLE);

        Animation popupAnim = AnimationUtils.loadAnimation(this, R.anim.damage_popup);
        popupAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                tvDamagePopup.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        tvDamagePopup.startAnimation(popupAnim);
    }

    private void playAttackAnimation() {
        // Use simple XML animation instead of Lottie for now
        if (tvEnemyEmoji != null) {
            tvEnemyEmoji.startAnimation(AnimationUtils.loadAnimation(this, R.anim.attack_hit));
        }
    }

    private void playVictoryAnimation() {
        // Victory animation using XML
        if (tvBuddyEmoji != null) {
            Animation victoryAnim = AnimationUtils.loadAnimation(this, R.anim.fade_scale_in);
            victoryAnim.setRepeatCount(3);
            tvBuddyEmoji.startAnimation(victoryAnim);
        }
    }

    private void enemyAttack() {
        if (enemyHealth <= 0) return;

        // Enemy attack animation
        tvEnemyEmoji.startAnimation(AnimationUtils.loadAnimation(this, R.anim.attack_hit));

        // Player gets hit
        new Handler().postDelayed(() -> {
            tvBuddyEmoji.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake));
        }, 200);

        // Enemy attacks player
        playerHealth -= enemyDamage;
        if (playerHealth < 0) playerHealth = 0;

        // Reset combo when hit
        combo = 0;

        // Animation
        progressPlayerHealth.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_scale_in));

        String enemyWord = getEnemyMoveWord();
        if (enemyWord != null) {
            showBuddyMessage(enemyEmoji + " phản đòn bằng từ \"" + enemyWord + "\"! Né cẩn thận! ⚠️");
        } else {
            showBuddyMessage(buddyMessages[buddyIndex][2]); // Take damage message
        }

        updateUI();

        // Check player death
        if (playerHealth <= 0) {
            gameOver();
        }
    }

    private void clearWord() {
        currentWord = new StringBuilder();
        selectedIndices.clear();

        // Reset all letter cards
        for (int i = 0; i < letterGrid.getChildCount(); i++) {
            View child = letterGrid.getChildAt(i);
            if (child instanceof CardView) {
                applyLetterDefaultStyle((CardView) child);
            }
        }

        updateWordDisplay();
    }

    private void applyLetterDefaultStyle(CardView card) {
        card.setCardBackgroundColor(Color.TRANSPARENT);
        card.setBackgroundResource(R.drawable.bg_letter_tile);
        View child = card.getChildAt(0);
        if (child instanceof TextView) {
            ((TextView) child).setTextColor(getColor(R.color.text_white));
        }
    }

    private void applyLetterSelectedStyle(CardView card) {
        card.setCardBackgroundColor(Color.TRANSPARENT);
        card.setBackgroundResource(R.drawable.bg_letter_tile_selected);
        View child = card.getChildAt(0);
        if (child instanceof TextView) {
            ((TextView) child).setTextColor(getColor(R.color.space_purple));
        }
    }

    private String getEnemyMoveWord() {
        if (!currentBoardWords.isEmpty()) {
            return currentBoardWords.get(random.nextInt(currentBoardWords.size())).toUpperCase();
        }
        if (!validWords.isEmpty()) {
            List<String> pool = new ArrayList<>(validWords);
            return pool.get(random.nextInt(pool.size())).toUpperCase();
        }
        return null;
    }

    private float calculateDifficultyScale(int learned) {
        if (learned < 5) return 0.9f;
        if (learned < 15) return 1.0f;
        if (learned < 30) return 1.15f;
        if (learned < 50) return 1.3f;
        return 1.5f;
    }

    private void showHint() {
        // Find a valid word that can be made
        String hint = findPossibleWord();
        if (hint != null) {
            showBuddyMessage("💡 Gợi ý: Thử ghép lại từ '" + hint.toUpperCase() + "' mà con đã học!");
        } else {
            showBuddyMessage("💡 Thử ghép các chữ cái thành từ tiếng Anh nhé!");
        }
    }

    private String findPossibleWord() {
        String letters = new String(currentLetters).toLowerCase();

        for (String boardWord : currentBoardWords) {
            String lower = boardWord.toLowerCase();
            if (!usedWords.contains(lower) && canMakeWord(lower, letters)) {
                return lower;
            }
        }

        // Fallback: search general dictionary words if needed
        for (String word : validWords) {
            if (word.length() >= 3 && word.length() <= 4 && !usedWords.contains(word)) {
                if (canMakeWord(word, letters)) {
                    return word;
                }
            }
        }
        return null;
    }

    private boolean canMakeWord(String word, String availableLetters) {
        StringBuilder available = new StringBuilder(availableLetters);
        for (char c : word.toCharArray()) {
            int index = available.indexOf(String.valueOf(c));
            if (index < 0) return false;
            available.deleteCharAt(index);
        }
        return true;
    }

    private void spawnEnemy() {
        int enemyIndex = Math.min(currentStage - 1, ENEMIES.length - 1);
        String[] enemy = ENEMIES[enemyIndex];

        enemyEmoji = enemy[0];
        enemyName = enemy[1];
        maxEnemyHealth = Integer.parseInt(enemy[2]) + (currentStage - 1) * 10;
        enemyDamage = Integer.parseInt(enemy[3]) + (currentStage - 1) * 2;

        maxEnemyHealth = Math.max(30, Math.round(maxEnemyHealth * difficultyScale));
        enemyHealth = maxEnemyHealth;
        enemyDamage = Math.max(4, Math.round(enemyDamage * difficultyScale));

        tvEnemyEmoji.setText(enemyEmoji);
        tvEnemyName.setText(enemyName + " Lv." + currentStage);

        // Spawn animation with rotation
        tvEnemyEmoji.startAnimation(AnimationUtils.loadAnimation(this, R.anim.enemy_spawn));

        showBuddyMessage(enemyEmoji + " " + enemyName + " xuất hiện! Chiến thôi! ⚔️");

        updateUI();
    }

    private void enemyDefeated() {
        // Death animation
        Animation deathAnim = AnimationUtils.loadAnimation(this, R.anim.enemy_death);
        deathAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                // Continue after death animation
                continueAfterVictory();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        tvEnemyEmoji.startAnimation(deathAnim);

        // Victory for this stage
        int bonus = currentStage * 100;
        totalScore += bonus;

        showBuddyMessage("🎉 Đánh bại " + enemyName + "! +" + bonus + " điểm!");
    }

    private void continueAfterVictory() {
        // Heal player slightly
        playerHealth = Math.min(playerHealth + 20, maxPlayerHealth);

        // Level up
        if (currentStage % 3 == 0) {
            playerLevel++;
            maxPlayerHealth += 20;
            playerHealth = maxPlayerHealth;
            showBuddyMessage("⬆️ Lên Level " + playerLevel + "! HP tối đa tăng!");
        }

        // Next stage
        currentStage++;
        usedWords.clear(); // Reset used words for new enemy

        // Spawn new enemy after delay
        new Handler().postDelayed(() -> {
            generateLetters(); // New letters
            spawnEnemy();
        }, 1500);

        updateUI();
    }

    private void gameOver() {
        // Save score
        prefs.edit().putInt("word_battle_high_score",
            Math.max(totalScore, prefs.getInt("word_battle_high_score", 0))).apply();

        String message = "Game Over!\n\n" +
            "📊 Điểm: " + totalScore + "\n" +
            "🏆 Stage: " + currentStage + "\n" +
            "📚 Từ đã dùng: " + usedWords.size();

        SpaceDialog.showResult(this, "💀", "Game Over!", message,
            currentStage >= 5 ? 3 : currentStage >= 3 ? 2 : 1,
            "Chơi lại", () -> restartGame());
    }

    private void restartGame() {
        setStageAndLevel(initialStage);
        totalScore = 0;
        combo = 0;
        usedWords.clear();

        generateLetters();
        spawnEnemy();
        updateUI();
    }

    private void updateUI() {
        // Player
        tvPlayerHealth.setText("❤️ " + playerHealth + "/" + maxPlayerHealth);
        progressPlayerHealth.setMax(maxPlayerHealth);
        progressPlayerHealth.setProgress(playerHealth);

        // Enemy
        tvEnemyHealth.setText("💀 " + enemyHealth + "/" + maxEnemyHealth);
        progressEnemyHealth.setMax(maxEnemyHealth);
        progressEnemyHealth.setProgress(enemyHealth);

        // Stats
        tvLevel.setText("Lv." + playerLevel);
        tvScore.setText("💰 " + totalScore);
        tvCombo.setText(combo > 0 ? "🔥 x" + combo : "");
    }

    private void showBuddyMessage(String message) {
        tvBuddyMessage.setText(message);
        if (tvBuddyMessage.getParent() != null) {
            ((View) tvBuddyMessage.getParent()).startAnimation(
                AnimationUtils.loadAnimation(this, R.anim.fade_scale_in));
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.US);
            tts.setSpeechRate(0.9f);
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
