package com.example.engapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class AdventureActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    // Views
    private TextView tvBuddyEmoji, tvBuddyName, tvBuddyMessage;
    private TextView tvLocation, tvSteps, tvWordsFound, tvEnergy;
    private ProgressBar progressEnergy;
    private LinearLayout sceneContainer;
    private CardView cardBuddy, cardEvent;
    private Button btnExplore, btnRest, btnUseItem;
    private ImageView btnBack;

    // Event views
    private TextView tvEventEmoji, tvEventTitle, tvEventDescription;
    private LinearLayout eventChoices;

    // Game state
    private TextToSpeech tts;
    private GameDatabaseHelper dbHelper;
    private SharedPreferences prefs;

    private int planetId;
    private String buddyEmoji = "🤖";
    private String buddyName = "Robo-Buddy";
    private int currentStep = 0;
    private int wordsFound = 0;
    private int energy = 100;
    private int maxEnergy = 100;

    private List<WordData> planetWords;
    private List<WordData> foundWords = new ArrayList<>();
    private Random random = new Random();

    // Adventure events
    private static final String[][] EVENTS = {
        // {emoji, title_vi, description_vi, type}
        {"🌟", "Ngôi sao rơi!", "Một ngôi sao rơi xuống gần đây! Có thứ gì đó lấp lánh...", "word_find"},
        {"🎁", "Rương kho báu!", "Buddy phát hiện một rương kho báu! Mở ra xem nào!", "treasure"},
        {"👽", "Người lạ thân thiện", "Một người bạn mới muốn dạy bạn từ mới!", "npc_teach"},
        {"🌀", "Cổng bí ẩn", "Một cổng ma thuật xuất hiện! Trả lời đúng để đi qua!", "quiz"},
        {"🎈", "Bóng bay từ vựng", "Nhiều bóng bay mang theo từ mới bay đến!", "balloon_pop"},
        {"🔮", "Quả cầu pha lê", "Quả cầu pha lê hiện lên một từ bí ẩn...", "crystal_ball"},
        {"🦋", "Bướm thần kỳ", "Một chú bướm đẹp bay đến mang theo từ mới!", "butterfly"},
        {"🌈", "Cầu vồng xuất hiện!", "Cầu vồng dẫn đến kho từ vựng bí mật!", "rainbow"},
        {"🎪", "Rạp xiếc vũ trụ", "Buddy muốn biểu diễn! Giúp buddy nói từ đúng!", "circus"},
        {"🏆", "Thử thách nhỏ", "Một thử thách nhỏ để kiểm tra kiến thức!", "mini_challenge"},
    };

    // Buddy reactions
    private String[][] buddyReactions = {
        // Robo-Buddy
        {"Beep boop! Phát hiện từ mới! 🤖", "Dữ liệu đã lưu thành công! ✨", "Cẩn thận! Có gì đó phía trước! ⚠️", "Tuyệt vời! Bạn thật giỏi! 🎉"},
        // Alien-Friend
        {"Zog zog! Từ mới từ hành tinh xa! 👽", "Trên sao của tôi cũng có từ này! 🛸", "Ooh! Đây là gì thế? 🌟", "Earth friend rất thông minh! 🎊"},
        // Kitty-Pal
        {"Meo meo! Tìm thấy rồi! 🐱", "Purrrr... Từ này hay quá! 😺", "Meow? Có gì đó lạ lạ... 🐾", "Meo yêu bạn! Bạn giỏi lắm! 💕"},
        // Foxy-Guide
        {"Yip! Mũi tôi ngửi thấy từ mới! 🦊", "Cáo thông minh biết từ này! 🧠", "Cẩn thận bước đi nhé! 🌲", "Bạn nhanh như cáo vậy! ⚡"},
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adventure);
        overridePendingTransition(R.anim.fade_scale_in, 0);

        planetId = getIntent().getIntExtra("planet_id", 1);

        dbHelper = GameDatabaseHelper.getInstance(this);
        prefs = getSharedPreferences("game_prefs", MODE_PRIVATE);
        tts = new TextToSpeech(this, this);

        loadBuddyInfo();
        initViews();
        loadPlanetWords();
        startAdventure();
    }

    private void loadBuddyInfo() {
        int buddyIndex = prefs.getInt("buddy_index", 0);
        String[] emojis = {"🤖", "👽", "🐱", "🦊"};
        String[] names = {"Robo-Buddy", "Alien-Friend", "Kitty-Pal", "Foxy-Guide"};

        if (buddyIndex >= 0 && buddyIndex < emojis.length) {
            buddyEmoji = emojis[buddyIndex];
            buddyName = names[buddyIndex];
        }
    }

    private void initViews() {
        tvBuddyEmoji = findViewById(R.id.tvBuddyEmoji);
        tvBuddyName = findViewById(R.id.tvBuddyName);
        tvBuddyMessage = findViewById(R.id.tvBuddyMessage);
        tvLocation = findViewById(R.id.tvLocation);
        tvSteps = findViewById(R.id.tvSteps);
        tvWordsFound = findViewById(R.id.tvWordsFound);
        tvEnergy = findViewById(R.id.tvEnergy);
        progressEnergy = findViewById(R.id.progressEnergy);
        sceneContainer = findViewById(R.id.sceneContainer);
        cardBuddy = findViewById(R.id.cardBuddy);
        cardEvent = findViewById(R.id.cardEvent);
        btnExplore = findViewById(R.id.btnExplore);
        btnRest = findViewById(R.id.btnRest);
        btnBack = findViewById(R.id.btnBack);

        tvEventEmoji = findViewById(R.id.tvEventEmoji);
        tvEventTitle = findViewById(R.id.tvEventTitle);
        tvEventDescription = findViewById(R.id.tvEventDescription);
        eventChoices = findViewById(R.id.eventChoices);

        // Set buddy info
        tvBuddyEmoji.setText(buddyEmoji);
        tvBuddyName.setText(buddyName);

        btnBack.setOnClickListener(v -> finish());
        btnExplore.setOnClickListener(v -> explore());
        btnRest.setOnClickListener(v -> rest());

        updateUI();
    }

    private void loadPlanetWords() {
        planetWords = dbHelper.getWordsForPlanet(planetId);
        if (planetWords == null || planetWords.isEmpty()) {
            // Load default words if none exist
            planetWords = dbHelper.getWordsForPlanet(1);
        }
        Collections.shuffle(planetWords);
    }

    private void startAdventure() {
        String welcomeMsg = "Chào mừng đến cuộc phiêu lưu! " + buddyName + " sẽ đồng hành cùng bạn! 🚀";
        showBuddyMessage(welcomeMsg);

        cardEvent.setVisibility(View.GONE);
        updateUI();
    }

    private void explore() {
        if (energy < 10) {
            showBuddyMessage("Hết năng lượng rồi! Nghỉ ngơi một chút nhé! 😴");
            return;
        }

        energy -= 10;
        currentStep++;
        updateUI();

        // Animate buddy walking
        Animation bounce = AnimationUtils.loadAnimation(this, R.anim.fade_scale_in);
        tvBuddyEmoji.startAnimation(bounce);

        // Random event
        new Handler().postDelayed(() -> {
            triggerRandomEvent();
        }, 500);
    }

    private void rest() {
        if (energy >= maxEnergy) {
            showBuddyMessage("Năng lượng đã đầy rồi! Đi khám phá thôi! 🌟");
            return;
        }

        energy = Math.min(energy + 30, maxEnergy);
        updateUI();

        String[] restMessages = {
            "Nghỉ ngơi một chút... Đã khỏe hơn rồi! 💪",
            "Zzzz... Tỉnh dậy nào! Năng lượng đã phục hồi! ⚡",
            "Uống nước, ăn bánh... Sẵn sàng tiếp tục! 🍪",
        };
        showBuddyMessage(restMessages[random.nextInt(restMessages.length)]);
    }

    private void triggerRandomEvent() {
        int eventIndex = random.nextInt(EVENTS.length);
        String[] event = EVENTS[eventIndex];

        showEvent(event[0], event[1], event[2], event[3]);
    }

    private void showEvent(String emoji, String title, String description, String type) {
        cardEvent.setVisibility(View.VISIBLE);
        cardEvent.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_right));

        tvEventEmoji.setText(emoji);
        tvEventTitle.setText(title);
        tvEventDescription.setText(description);

        eventChoices.removeAllViews();

        // Get buddy reaction
        int buddyIndex = prefs.getInt("buddy_index", 0);
        if (buddyIndex < buddyReactions.length) {
            String reaction = buddyReactions[buddyIndex][random.nextInt(4)];
            showBuddyMessage(reaction);
        }

        switch (type) {
            case "word_find":
                setupWordFindEvent();
                break;
            case "treasure":
                setupTreasureEvent();
                break;
            case "npc_teach":
                setupNpcTeachEvent();
                break;
            case "quiz":
                setupQuizEvent();
                break;
            case "balloon_pop":
                setupBalloonEvent();
                break;
            case "crystal_ball":
                setupCrystalBallEvent();
                break;
            case "butterfly":
                setupButterflyEvent();
                break;
            case "rainbow":
                setupRainbowEvent();
                break;
            case "circus":
                setupCircusEvent();
                break;
            case "mini_challenge":
                setupMiniChallengeEvent();
                break;
        }
    }

    private void setupWordFindEvent() {
        if (planetWords.isEmpty()) return;

        WordData word = getRandomUnfoundWord();
        if (word == null) {
            tvEventDescription.setText("Bạn đã tìm thấy tất cả từ ở đây rồi! 🎉");
            addEventButton("Tiếp tục khám phá", v -> hideEvent());
            return;
        }

        tvEventDescription.setText("Bạn tìm thấy: " + word.emoji + "\n\nĐây là từ gì?");

        // Create answer buttons
        List<String> options = new ArrayList<>();
        options.add(word.english);

        // Add wrong options
        for (WordData w : planetWords) {
            if (!w.english.equals(word.english) && options.size() < 4) {
                options.add(w.english);
            }
        }
        Collections.shuffle(options);

        for (String option : options) {
            addEventButton(option, v -> {
                if (option.equals(word.english)) {
                    correctAnswer(word);
                } else {
                    wrongAnswer(word);
                }
            });
        }
    }

    private void setupTreasureEvent() {
        WordData word = getRandomUnfoundWord();
        if (word == null) {
            tvEventDescription.setText("Rương trống! Nhưng có 10 năng lượng bên trong! ⚡");
            energy = Math.min(energy + 10, maxEnergy);
            updateUI();
            addEventButton("Lấy năng lượng", v -> hideEvent());
            return;
        }

        tvEventDescription.setText("Trong rương có: " + word.emoji + " " + word.english +
            "\n\nPhiên âm: " + word.pronunciation +
            "\nNghĩa: " + word.vietnamese);

        addEventButton("🔊 Nghe phát âm", v -> speakWord(word.english));
        addEventButton("📚 Thu thập từ này!", v -> {
            collectWord(word);
            hideEvent();
        });
    }

    private void setupNpcTeachEvent() {
        WordData word = getRandomUnfoundWord();
        if (word == null) {
            tvEventDescription.setText("\"Bạn đã học hết từ của tôi rồi! Giỏi lắm!\" 🎓");
            addEventButton("Cảm ơn!", v -> hideEvent());
            return;
        }

        String[] npcEmojis = {"👨‍🏫", "👩‍🔬", "🧙‍♂️", "🧚", "👸"};
        String npc = npcEmojis[random.nextInt(npcEmojis.length)];

        tvEventEmoji.setText(npc);
        tvEventDescription.setText("\"Xin chào! Để tôi dạy bạn từ mới nhé!\"\n\n" +
            word.emoji + " " + word.english + " = " + word.vietnamese + "\n\n" +
            "Ví dụ: " + word.exampleSentence);

        addEventButton("🔊 Nghe", v -> speakWord(word.english));
        addEventButton("Học từ này!", v -> {
            collectWord(word);
            showBuddyMessage("Wow! Bạn học được từ mới từ " + npc + "! 🌟");
            hideEvent();
        });
    }

    private void setupQuizEvent() {
        if (foundWords.isEmpty()) {
            tvEventDescription.setText("Cổng mở tự động vì bạn là khách mới! 🚪✨");
            addEventButton("Đi qua cổng", v -> {
                energy = Math.min(energy + 20, maxEnergy);
                updateUI();
                hideEvent();
            });
            return;
        }

        WordData word = foundWords.get(random.nextInt(foundWords.size()));
        tvEventDescription.setText("Để đi qua cổng, hãy chọn nghĩa của:\n\n" +
            word.emoji + " " + word.english.toUpperCase());

        List<String> options = new ArrayList<>();
        options.add(word.vietnamese);

        for (WordData w : planetWords) {
            if (!w.vietnamese.equals(word.vietnamese) && options.size() < 4) {
                options.add(w.vietnamese);
            }
        }
        Collections.shuffle(options);

        for (String option : options) {
            addEventButton(option, v -> {
                if (option.equals(word.vietnamese)) {
                    showBuddyMessage("Đúng rồi! Cổng mở ra! 🎊");
                    energy = Math.min(energy + 15, maxEnergy);
                    updateUI();
                    hideEvent();
                } else {
                    showBuddyMessage("Sai rồi! Đáp án là: " + word.vietnamese + " 💪");
                    hideEvent();
                }
            });
        }
    }

    private void setupBalloonEvent() {
        List<WordData> balloonWords = new ArrayList<>();
        for (int i = 0; i < 3 && i < planetWords.size(); i++) {
            WordData w = getRandomUnfoundWord();
            if (w != null) balloonWords.add(w);
        }

        if (balloonWords.isEmpty()) {
            tvEventDescription.setText("Bóng bay đã bay hết rồi! 🎈💨");
            addEventButton("OK", v -> hideEvent());
            return;
        }

        StringBuilder sb = new StringBuilder("Chọn bóng bay để học từ mới!\n\n");
        for (WordData w : balloonWords) {
            sb.append("🎈 ").append(w.emoji).append(" ");
        }
        tvEventDescription.setText(sb.toString());

        for (WordData w : balloonWords) {
            addEventButton(w.emoji + " " + w.english, v -> {
                collectWord(w);
                speakWord(w.english);
                showBuddyMessage("Bắt được bóng " + w.emoji + "! Từ mới: " + w.english + "! 🎈");
            });
        }

        addEventButton("Xong!", v -> hideEvent());
    }

    private void setupCrystalBallEvent() {
        WordData word = getRandomUnfoundWord();
        if (word == null) {
            tvEventDescription.setText("Quả cầu pha lê mờ dần... Không còn từ mới! 🔮");
            addEventButton("OK", v -> hideEvent());
            return;
        }

        tvEventDescription.setText("Quả cầu pha lê hiện lên...\n\n" +
            "✨ " + word.english.toUpperCase() + " ✨\n\n" +
            "Phiên âm: " + word.pronunciation);

        addEventButton("🔊 Nghe bí ẩn", v -> speakWord(word.english));
        addEventButton("Hỏi nghĩa", v -> {
            tvEventDescription.setText("Quả cầu trả lời:\n\n" +
                word.emoji + " " + word.english + "\n= " + word.vietnamese + "\n\n" +
                "\"" + word.exampleSentence + "\"");
            collectWord(word);
        });
        addEventButton("Tiếp tục", v -> hideEvent());
    }

    private void setupButterflyEvent() {
        WordData word = getRandomUnfoundWord();
        if (word == null) {
            tvEventDescription.setText("Bướm bay đi mất rồi! 🦋💨");
            addEventButton("Tạm biệt bướm!", v -> hideEvent());
            return;
        }

        tvEventDescription.setText("Bướm thần kỳ mang đến từ:\n\n" +
            word.emoji + " " + word.english + "\n\n" +
            "Nói theo bướm để bắt từ này!");

        addEventButton("🔊 Nghe bướm nói", v -> speakWord(word.english));
        addEventButton("🦋 Bắt bướm (học từ)", v -> {
            collectWord(word);
            showBuddyMessage("Bắt được bướm! Học từ: " + word.english + " = " + word.vietnamese + "! 🦋✨");
            hideEvent();
        });
    }

    private void setupRainbowEvent() {
        List<WordData> rainbowWords = new ArrayList<>();
        for (int i = 0; i < 5 && i < planetWords.size(); i++) {
            WordData w = getRandomUnfoundWord();
            if (w != null && !rainbowWords.contains(w)) {
                rainbowWords.add(w);
            }
        }

        if (rainbowWords.isEmpty()) {
            tvEventDescription.setText("Cầu vồng dẫn đến... năng lượng! +30⚡");
            energy = Math.min(energy + 30, maxEnergy);
            updateUI();
            addEventButton("Tuyệt vời!", v -> hideEvent());
            return;
        }

        StringBuilder sb = new StringBuilder("Cầu vồng mang đến kho từ vựng:\n\n");
        for (WordData w : rainbowWords) {
            sb.append(w.emoji).append(" ").append(w.english).append(" = ").append(w.vietnamese).append("\n");
        }
        tvEventDescription.setText(sb.toString());

        addEventButton("🌈 Thu thập tất cả!", v -> {
            for (WordData w : rainbowWords) {
                collectWord(w);
            }
            showBuddyMessage("WOW! Thu được " + rainbowWords.size() + " từ mới từ cầu vồng! 🌈🎉");
            hideEvent();
        });
    }

    private void setupCircusEvent() {
        WordData word = getRandomUnfoundWord();
        if (word == null) {
            tvEventDescription.setText("Rạp xiếc đã đóng cửa! 🎪");
            addEventButton("Tạm biệt!", v -> hideEvent());
            return;
        }

        tvEventDescription.setText(buddyName + " muốn biểu diễn!\n\n" +
            "\"Tôi sẽ nói từ này: " + word.emoji + "\"\n\n" +
            "Hãy nghe và chọn từ đúng!");

        List<String> options = new ArrayList<>();
        options.add(word.english);
        for (WordData w : planetWords) {
            if (!w.english.equals(word.english) && options.size() < 4) {
                options.add(w.english);
            }
        }
        Collections.shuffle(options);

        addEventButton("🔊 Nghe " + buddyName + " nói", v -> speakWord(word.english));

        for (String option : options) {
            addEventButton(option, v -> {
                if (option.equals(word.english)) {
                    collectWord(word);
                    showBuddyMessage("Đúng rồi! " + buddyName + " biểu diễn thành công! 🎪👏");
                    hideEvent();
                } else {
                    showBuddyMessage("Ôi không! Sai rồi! Đáp án là: " + word.english + " 😅");
                }
            });
        }
    }

    private void setupMiniChallengeEvent() {
        if (foundWords.size() < 3) {
            tvEventDescription.setText("Bạn cần học thêm từ để tham gia thử thách! 📚");
            addEventButton("OK", v -> hideEvent());
            return;
        }

        // Pick 3 random words from found words
        List<WordData> challengeWords = new ArrayList<>(foundWords);
        Collections.shuffle(challengeWords);
        challengeWords = challengeWords.subList(0, Math.min(3, challengeWords.size()));

        final int[] correct = {0};
        final int[] current = {0};

        showChallengeQuestion(challengeWords, current, correct);
    }

    private void showChallengeQuestion(List<WordData> words, int[] current, int[] correct) {
        if (current[0] >= words.size()) {
            // Challenge complete
            String result = correct[0] + "/" + words.size() + " câu đúng!";
            if (correct[0] == words.size()) {
                result += "\n\n🏆 Hoàn hảo! +50 năng lượng!";
                energy = Math.min(energy + 50, maxEnergy);
            } else if (correct[0] > 0) {
                result += "\n\n⭐ Tốt lắm! +20 năng lượng!";
                energy = Math.min(energy + 20, maxEnergy);
            }
            updateUI();

            tvEventDescription.setText("Kết quả thử thách:\n\n" + result);
            eventChoices.removeAllViews();
            addEventButton("Tiếp tục phiêu lưu!", v -> hideEvent());
            return;
        }

        WordData word = words.get(current[0]);
        tvEventDescription.setText("Câu " + (current[0] + 1) + "/" + words.size() + "\n\n" +
            word.emoji + " " + word.english + " nghĩa là gì?");

        eventChoices.removeAllViews();

        List<String> options = new ArrayList<>();
        options.add(word.vietnamese);
        for (WordData w : planetWords) {
            if (!w.vietnamese.equals(word.vietnamese) && options.size() < 4) {
                options.add(w.vietnamese);
            }
        }
        Collections.shuffle(options);

        for (String option : options) {
            addEventButton(option, v -> {
                if (option.equals(word.vietnamese)) {
                    correct[0]++;
                    showBuddyMessage("Đúng! ✅");
                } else {
                    showBuddyMessage("Sai! Đáp án: " + word.vietnamese + " ❌");
                }
                current[0]++;
                new Handler().postDelayed(() -> showChallengeQuestion(words, current, correct), 1000);
            });
        }
    }

    private WordData getRandomUnfoundWord() {
        List<WordData> unfound = new ArrayList<>();
        for (WordData w : planetWords) {
            if (!foundWords.contains(w)) {
                unfound.add(w);
            }
        }
        if (unfound.isEmpty()) return null;
        return unfound.get(random.nextInt(unfound.size()));
    }

    private void correctAnswer(WordData word) {
        collectWord(word);
        showBuddyMessage("Đúng rồi! 🎉 " + word.english + " = " + word.vietnamese);
        speakWord(word.english);

        new Handler().postDelayed(this::hideEvent, 1500);
    }

    private void wrongAnswer(WordData word) {
        showBuddyMessage("Ôi không! Đáp án đúng là: " + word.english + " 💪");
        speakWord(word.english);

        new Handler().postDelayed(this::hideEvent, 2000);
    }

    private void collectWord(WordData word) {
        if (!foundWords.contains(word)) {
            foundWords.add(word);
            wordsFound++;
            dbHelper.markWordAsLearned(word.id);
            updateUI();
        }
    }

    private void addEventButton(String text, View.OnClickListener listener) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextColor(getColor(R.color.text_white));
        btn.setBackgroundResource(R.drawable.bg_event_button);
        btn.setPadding(32, 24, 32, 24);
        btn.setTextSize(14);
        btn.setAllCaps(false);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 8, 0, 8);
        btn.setLayoutParams(params);

        btn.setOnClickListener(listener);
        eventChoices.addView(btn);
    }

    private void hideEvent() {
        cardEvent.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out_right));
        new Handler().postDelayed(() -> cardEvent.setVisibility(View.GONE), 300);
    }

    private void showBuddyMessage(String message) {
        tvBuddyMessage.setText(message);
        cardBuddy.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_scale_in));
    }

    private void speakWord(String text) {
        if (tts != null) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "word");
        }
    }

    private void updateUI() {
        tvSteps.setText("👣 " + currentStep);
        tvWordsFound.setText("📚 " + wordsFound);
        tvEnergy.setText("⚡ " + energy);
        progressEnergy.setProgress(energy);

        PlanetData planet = dbHelper.getPlanetById(planetId);
        if (planet != null) {
            tvLocation.setText(planet.emoji + " " + planet.name);
        }
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

