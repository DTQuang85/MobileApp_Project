package com.example.engapp;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import java.util.Locale;

public class VocabularyDetailActivity extends AppCompatActivity {
    private TextView tvTerm, tvType, tvPronunciation, tvDefinition, tvExample, tvCategory;
    private ImageButton btnSpeak;
    private ImageView ivVocabImage;
    private androidx.cardview.widget.CardView cardImageDetail;
    private TextToSpeech textToSpeech;
    private String term, pronunciation;
    private boolean ttsReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vocabulary_detail);

        // Khởi tạo views
        tvTerm = findViewById(R.id.tvTermDetail);
        tvType = findViewById(R.id.tvTypeDetail);
        tvPronunciation = findViewById(R.id.tvPronunciationDetail);
        tvDefinition = findViewById(R.id.tvDefinitionDetail);
        tvExample = findViewById(R.id.tvExampleDetail);
        tvCategory = findViewById(R.id.tvCategoryDetail);
        btnSpeak = findViewById(R.id.btnSpeak);
        ivVocabImage = findViewById(R.id.ivVocabImageDetail);
        cardImageDetail = findViewById(R.id.cardImageDetail);

        // Lấy dữ liệu từ Intent
        term = getIntent().getStringExtra("term");
        String type = getIntent().getStringExtra("type");
        pronunciation = getIntent().getStringExtra("pronunciation");
        String definition = getIntent().getStringExtra("definition");
        String example = getIntent().getStringExtra("example");
        String category = getIntent().getStringExtra("category");
        String image = getIntent().getStringExtra("image");

        // Hiển thị dữ liệu
        tvTerm.setText(term);
        tvType.setText("(" + type + ")");
        tvPronunciation.setText(pronunciation);
        tvDefinition.setText(definition);
        tvExample.setText("\"" + example + "\"");
        tvCategory.setText(category);

        // Load image từ URL nếu có
        if (image != null && !image.isEmpty() && (image.startsWith("http://") || image.startsWith("https://"))) {
            cardImageDetail.setVisibility(View.VISIBLE);
            Glide.with(this)
                .load(image)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .centerCrop()
                .into(ivVocabImage);
        } else {
            cardImageDetail.setVisibility(View.GONE);
        }

        // Disable nút Speak cho đến khi TTS sẵn sàng
        btnSpeak.setEnabled(false);
        btnSpeak.setAlpha(0.5f);

        // Khởi tạo Text-to-Speech
        initTextToSpeech();

        // Xử lý nút Speak
        btnSpeak.setOnClickListener(v -> speakWord());
    }

    private void initTextToSpeech() {
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.US);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this, "Language not supported", Toast.LENGTH_SHORT).show();
                    btnSpeak.setEnabled(false);
                    btnSpeak.setAlpha(0.5f);
                    ttsReady = false;
                } else {
                    // TTS sẵn sàng - enable nút và đặt alpha về bình thường
                    ttsReady = true;
                    btnSpeak.setEnabled(true);
                    btnSpeak.setAlpha(1.0f);
                }
            } else {
                Toast.makeText(this, "Text-to-Speech initialization failed", Toast.LENGTH_SHORT).show();
                btnSpeak.setEnabled(false);
                btnSpeak.setAlpha(0.5f);
                ttsReady = false;
            }
        });
    }

    private void speakWord() {
        if (textToSpeech != null && ttsReady && term != null && !term.isEmpty()) {
            // Dừng speech trước nếu đang chạy
            if (textToSpeech.isSpeaking()) {
                textToSpeech.stop();
            }
            textToSpeech.speak(term, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}
