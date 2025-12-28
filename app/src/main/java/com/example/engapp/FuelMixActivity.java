package com.example.engapp;

import android.content.ClipData;
import android.os.Build;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.engapp.database.GameDatabaseHelper;

public class FuelMixActivity extends AppCompatActivity {

    private FrameLayout slotRed;
    private FrameLayout slotBlue;
    private FrameLayout slotYellow;
    private TextView tokenRed;
    private TextView tokenBlue;
    private TextView tokenYellow;
    private TextView tvStatus;
    private LinearLayout tokensRow;
    private Button btnReset;
    private ImageButton btnBack;

    private int matchedCount = 0;
    private GameDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fuel_mix);

        dbHelper = GameDatabaseHelper.getInstance(this);
        initViews();
        setupDragAndDrop();
        setupListeners();
    }

    private void initViews() {
        slotRed = findViewById(R.id.slotRed);
        slotBlue = findViewById(R.id.slotBlue);
        slotYellow = findViewById(R.id.slotYellow);
        tokenRed = findViewById(R.id.tokenRed);
        tokenBlue = findViewById(R.id.tokenBlue);
        tokenYellow = findViewById(R.id.tokenYellow);
        tvStatus = findViewById(R.id.tvStatus);
        tokensRow = findViewById(R.id.tokensRow);
        btnReset = findViewById(R.id.btnReset);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupListeners() {
        btnReset.setOnClickListener(v -> resetTokens());
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupDragAndDrop() {
        setupTokenDrag(tokenRed);
        setupTokenDrag(tokenBlue);
        setupTokenDrag(tokenYellow);

        setupSlotDrop(slotRed);
        setupSlotDrop(slotBlue);
        setupSlotDrop(slotYellow);
    }

    private void setupTokenDrag(View token) {
        token.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                ClipData data = ClipData.newPlainText("fuel", String.valueOf(v.getTag()));
                View.DragShadowBuilder shadow = new View.DragShadowBuilder(v);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    v.startDragAndDrop(data, shadow, v, 0);
                } else {
                    v.startDrag(data, shadow, v, 0);
                }
                v.setAlpha(0.6f);
                return true;
            } else if (event.getAction() == MotionEvent.ACTION_UP
                || event.getAction() == MotionEvent.ACTION_CANCEL) {
                v.setAlpha(1f);
            }
            return false;
        });
    }

    private void setupSlotDrop(View slot) {
        slot.setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_ENTERED:
                    v.setAlpha(0.9f);
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    v.setAlpha(1f);
                    return true;
                case DragEvent.ACTION_DROP:
                    v.setAlpha(1f);
                    View dragged = (View) event.getLocalState();
                    if (dragged == null) {
                        return true;
                    }
                    String slotTag = String.valueOf(v.getTag());
                    String tokenTag = String.valueOf(dragged.getTag());
                    if (slotTag.equals(tokenTag) && v instanceof FrameLayout) {
                        attachTokenToSlot((FrameLayout) v, dragged);
                        matchedCount++;
                        tvStatus.setText("Đúng rồi!");
                        if (matchedCount >= 3) {
                            dbHelper.addExperience(5);
                            Toast.makeText(this, "Hoàn thành! +5 XP", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        tvStatus.setText("Sai rồi, thử lại nhé");
                        dragged.setAlpha(1f);
                    }
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    View ended = (View) event.getLocalState();
                    if (ended != null) {
                        ended.setAlpha(1f);
                    }
                    v.setAlpha(1f);
                    return true;
                default:
                    return true;
            }
        });
    }

    private void attachTokenToSlot(FrameLayout slot, View token) {
        ViewGroup parent = (ViewGroup) token.getParent();
        if (parent != null) {
            parent.removeView(token);
        }
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = android.view.Gravity.CENTER;
        token.setLayoutParams(params);
        slot.addView(token);
        token.setOnTouchListener(null);
    }

    private void resetTokens() {
        matchedCount = 0;
        tvStatus.setText("");
        resetToken(tokenRed);
        resetToken(tokenBlue);
        resetToken(tokenYellow);
    }

    private void resetToken(View token) {
        ViewGroup parent = (ViewGroup) token.getParent();
        if (parent != null) {
            parent.removeView(token);
        }
        tokensRow.addView(token);
        token.setOnTouchListener(null);
        setupTokenDrag(token);
    }
}
