package com.example.engapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import java.util.Locale;
import java.util.Random;

public class BuddyRoomActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private static final String PREFS_NAME = "game_prefs";
    private static final String KEY_BUDDY_INDEX = "buddy_index";

    private TextView tvCurrentBuddy, tvBuddyName, tvBuddyLevel, tvBuddyMessage;
    private ImageView btnBack;
    private CardView cardBuddy1, cardBuddy2, cardBuddy3, cardBuddy4;
    private LinearLayout btnTalk, btnPlay, btnLearn;

    private TextToSpeech tts;
    private SharedPreferences prefs;
    private String[] buddyEmojis = {"🤖", "👽", "🐱", "🦊"};
    private String[] buddyNames = {"Robo-Buddy", "Alien-Friend", "Kitty-Pal", "Foxy-Guide"};
    private int currentBuddyIndex = 0;

    private String[][] buddyResponses = {
        // Robo-Buddy responses
        {"Beep boop! Bạn muốn học gì nào? 🤖",
         "Tôi là robot siêu thông minh! Let's learn English! 🔧",
         "Whirr... Processing... Bạn thật giỏi! ⚡",
         "01001000 01101001! Đó là 'Hi' trong ngôn ngữ robot! 😄"},
        // Alien-Friend responses
        {"Greetings from Planet Zog! 👽",
         "Trên hành tinh tôi, chúng tôi nói 'Zog zog' nghĩa là 'Hello'! 🛸",
         "Earth language is so fun! Bạn dạy tôi thêm nhé! 🌍",
         "My spaceship runs on English words! Học nhiều để bay xa! 🚀"},
        // Kitty-Pal responses
        {"Meo meo! Hôm nay học gì nào? 🐱",
         "Purrrr... Tôi thích từ 'fish'! Bạn thích từ nào? 🐟",
         "Meow means 'I love you' in cat language! 💕",
         "Let's play and learn! Meo meo! 🎮"},
        // Foxy-Guide responses
        {"Yip yip! Tôi là cáo thông minh nhất rừng! 🦊",
         "Trong rừng, tôi dạy các bạn thú học tiếng Anh! 🌲",
         "Quick like a fox! Học nhanh như cáo nào! ⚡",
         "Adventure awaits! Cùng khám phá từ mới! 🗺️"}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buddy_room);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        tts = new TextToSpeech(this, this);
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        initViews();
        loadSavedBuddy();
        setupBuddySelection();
        setupInteractionButtons();
    }

    private void loadSavedBuddy() {
        currentBuddyIndex = prefs.getInt(KEY_BUDDY_INDEX, 0);
        tvCurrentBuddy.setText(buddyEmojis[currentBuddyIndex]);
        tvBuddyName.setText(buddyNames[currentBuddyIndex]);
    }

    private void initViews() {
        tvCurrentBuddy = findViewById(R.id.tvCurrentBuddy);
        tvBuddyName = findViewById(R.id.tvBuddyName);
        tvBuddyLevel = findViewById(R.id.tvBuddyLevel);
        tvBuddyMessage = findViewById(R.id.tvBuddyMessage);
        btnBack = findViewById(R.id.btnBack);

        cardBuddy1 = findViewById(R.id.cardBuddy1);
        cardBuddy2 = findViewById(R.id.cardBuddy2);
        cardBuddy3 = findViewById(R.id.cardBuddy3);
        cardBuddy4 = findViewById(R.id.cardBuddy4);

        btnTalk = findViewById(R.id.btnTalk);
        btnPlay = findViewById(R.id.btnPlay);
        btnLearn = findViewById(R.id.btnLearn);

        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });
    }

    private void setupBuddySelection() {
        cardBuddy1.setOnClickListener(v -> selectBuddy(0));
        cardBuddy2.setOnClickListener(v -> selectBuddy(1));
        cardBuddy3.setOnClickListener(v -> selectBuddy(2));
        cardBuddy4.setOnClickListener(v -> selectBuddy(3));

        findViewById(R.id.cardBuddy5).setOnClickListener(v ->
            showLockedMessage("Hoàn thành 3 hành tinh để mở khóa Dragon! 🐲"));
        findViewById(R.id.cardBuddy6).setOnClickListener(v ->
            showLockedMessage("Hoàn thành 5 hành tinh để mở khóa Unicorn! 🦄"));
        findViewById(R.id.cardBuddy7).setOnClickListener(v ->
            showLockedMessage("Hoàn thành 7 hành tinh để mở khóa Panda! 🐼"));
        findViewById(R.id.cardBuddy8).setOnClickListener(v ->
            showLockedMessage("Hoàn thành 9 hành tinh để mở khóa Lion! 🦁"));
    }

    private void setupInteractionButtons() {
        if (btnTalk != null) {
            btnTalk.setOnClickListener(v -> talkToBuddy());
        }
        if (btnPlay != null) {
            btnPlay.setOnClickListener(v -> playWithBuddy());
        }
        if (btnLearn != null) {
            btnLearn.setOnClickListener(v -> learnWithBuddy());
        }
    }

    private void selectBuddy(int index) {
        currentBuddyIndex = index;
        tvCurrentBuddy.setText(buddyEmojis[index]);
        tvBuddyName.setText(buddyNames[index]);

        // Save to preferences
        prefs.edit().putInt(KEY_BUDDY_INDEX, index).apply();

        // Animate buddy
        tvCurrentBuddy.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_scale_in));

        // Show greeting
        String greeting = buddyResponses[index][0];
        showBuddyMessage(greeting);

        Toast.makeText(this, "Đã chọn " + buddyNames[index] + "! ✨", Toast.LENGTH_SHORT).show();
    }

    private void showLockedMessage(String message) {
        Toast.makeText(this, "🔒 " + message, Toast.LENGTH_LONG).show();
    }

    private void talkToBuddy() {
        // Show random response from current buddy
        Random random = new Random();
        String[] responses = buddyResponses[currentBuddyIndex];
        String response = responses[random.nextInt(responses.length)];

        showBuddyMessage(response);

        // Animate buddy
        tvCurrentBuddy.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_scale_in));
    }

    private void playWithBuddy() {
        String[] games = {
            "Chơi đoán từ nhé! Tôi nghĩ đến một con vật... 🤔",
            "Hãy đếm từ 1 đến 10 bằng tiếng Anh! One, two... 🔢",
            "Nói 'Hello' thật to nào! 📢",
            "Tìm 3 đồ vật màu đỏ xung quanh bạn! 🔴"
        };

        Random random = new Random();
        String game = games[random.nextInt(games.length)];
        showBuddyMessage("🎮 " + game);

        tvCurrentBuddy.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_scale_in));
    }

    private void learnWithBuddy() {
        String[][] lessons = {
            {"cat", "Con mèo", "🐱"},
            {"dog", "Con chó", "🐕"},
            {"apple", "Quả táo", "🍎"},
            {"star", "Ngôi sao", "⭐"},
            {"sun", "Mặt trời", "☀️"},
            {"moon", "Mặt trăng", "🌙"}
        };

        Random random = new Random();
        String[] lesson = lessons[random.nextInt(lessons.length)];

        String message = "📚 Từ mới: " + lesson[2] + "\n\n" +
                        "Tiếng Anh: " + lesson[0].toUpperCase() + "\n" +
                        "Tiếng Việt: " + lesson[1] + "\n\n" +
                        "Nhấn 🔊 để nghe!";

        showBuddyMessage(message);

        // Speak the word
        if (tts != null) {
            new Handler().postDelayed(() -> {
                tts.speak(lesson[0], TextToSpeech.QUEUE_FLUSH, null, "word");
            }, 500);
        }
    }

    private void showBuddyMessage(String message) {
        tvBuddyMessage.setText(message);
        tvBuddyMessage.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_right));
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
