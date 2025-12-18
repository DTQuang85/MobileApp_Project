package com.example.engapp;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class BuddyRoomActivity extends AppCompatActivity {

    private TextView tvCurrentBuddy, tvBuddyName, tvBuddyLevel, tvBuddyMessage;
    private ImageView btnBack;
    private CardView cardBuddy1, cardBuddy2, cardBuddy3, cardBuddy4;

    private String[] buddyEmojis = {"🤖", "👽", "🐱", "🦊"};
    private String[] buddyNames = {"Robo-Buddy", "Alien-Friend", "Kitty-Pal", "Foxy-Guide"};
    private int currentBuddyIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buddy_room);

        initViews();
        setupBuddySelection();
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

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupBuddySelection() {
        cardBuddy1.setOnClickListener(v -> selectBuddy(0));
        cardBuddy2.setOnClickListener(v -> selectBuddy(1));
        cardBuddy3.setOnClickListener(v -> selectBuddy(2));
        cardBuddy4.setOnClickListener(v -> selectBuddy(3));

        // Locked buddies show toast
        findViewById(R.id.cardBuddy5).setOnClickListener(v ->
            Toast.makeText(this, "🔒 Hoàn thành 3 hành tinh để mở khóa!", Toast.LENGTH_SHORT).show());
        findViewById(R.id.cardBuddy6).setOnClickListener(v ->
            Toast.makeText(this, "🔒 Hoàn thành 5 hành tinh để mở khóa!", Toast.LENGTH_SHORT).show());
        findViewById(R.id.cardBuddy7).setOnClickListener(v ->
            Toast.makeText(this, "🔒 Hoàn thành 7 hành tinh để mở khóa!", Toast.LENGTH_SHORT).show());
        findViewById(R.id.cardBuddy8).setOnClickListener(v ->
            Toast.makeText(this, "🔒 Hoàn thành 9 hành tinh để mở khóa!", Toast.LENGTH_SHORT).show());
    }

    private void selectBuddy(int index) {
        currentBuddyIndex = index;
        tvCurrentBuddy.setText(buddyEmojis[index]);
        tvBuddyName.setText(buddyNames[index]);

        String[] messages = {
            "Beep boop! Mình là Robo-Buddy! Cùng học tiếng Anh nào! 🤖",
            "Xin chào từ hành tinh xa xôi! Mình là Alien-Friend! 👽",
            "Meo meo! Mình là Kitty-Pal, cùng chơi thôi! 🐱",
            "Yip yip! Mình là Foxy-Guide, sẵn sàng phiêu lưu! 🦊"
        };
        tvBuddyMessage.setText(messages[index]);

        Toast.makeText(this, "Đã chọn " + buddyNames[index] + "! ✨", Toast.LENGTH_SHORT).show();
    }
}

