package com.example.engapp;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class BehavioralPracticeActivity extends AppCompatActivity {

    private static final int SPEECH_REQUEST_CODE = 100;

    private TextView tvQuestion, tvRecordStatus, tvRecordedText;
    private EditText etAnswer;
    private Button btnModeWrite, btnModeRecord, btnSubmit;
    private ImageView btnBack, btnRecord;
    private LinearLayout layoutWriteMode, layoutRecordMode;
    private CardView cardScore;
    private TextView tvOverallScore, tvKeywordScore, tvGrammar, tvStructure;

    private String questionId, question, keywords, practiceTemplate;
    private String answerText = "";
    private boolean isWriteMode = true;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_behavioral_practice);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize views
        btnBack = findViewById(R.id.btnBack);
        tvQuestion = findViewById(R.id.tvQuestion);
        btnModeWrite = findViewById(R.id.btnModeWrite);
        btnModeRecord = findViewById(R.id.btnModeRecord);
        layoutWriteMode = findViewById(R.id.layoutWriteMode);
        layoutRecordMode = findViewById(R.id.layoutRecordMode);
        etAnswer = findViewById(R.id.etAnswer);
        btnRecord = findViewById(R.id.btnRecord);
        tvRecordStatus = findViewById(R.id.tvRecordStatus);
        tvRecordedText = findViewById(R.id.tvRecordedText);
        btnSubmit = findViewById(R.id.btnSubmit);
        cardScore = findViewById(R.id.cardScore);
        tvOverallScore = findViewById(R.id.tvOverallScore);
        tvKeywordScore = findViewById(R.id.tvKeywordScore);
        tvGrammar = findViewById(R.id.tvGrammar);
        tvStructure = findViewById(R.id.tvStructure);

        // Get data from intent
        Intent intent = getIntent();
        questionId = intent.getStringExtra("questionId");
        question = intent.getStringExtra("question");
        keywords = intent.getStringExtra("keywords");
        practiceTemplate = intent.getStringExtra("practice_template");

        tvQuestion.setText(question);
        
        if (practiceTemplate != null && !practiceTemplate.isEmpty()) {
            etAnswer.setHint(practiceTemplate);
        }

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Mode selection
        btnModeWrite.setOnClickListener(v -> switchToWriteMode());
        btnModeRecord.setOnClickListener(v -> switchToRecordMode());

        // Record button
        btnRecord.setOnClickListener(v -> startSpeechRecognition());

        // Submit button
        btnSubmit.setOnClickListener(v -> submitAnswer());
    }

    private void switchToWriteMode() {
        isWriteMode = true;
        layoutWriteMode.setVisibility(View.VISIBLE);
        layoutRecordMode.setVisibility(View.GONE);
        btnModeWrite.setBackgroundResource(R.drawable.bg_button_primary_selector);
        btnModeWrite.setTextColor(getResources().getColor(android.R.color.white));
        btnModeRecord.setBackgroundResource(R.drawable.bg_card);
        btnModeRecord.setTextColor(getResources().getColor(R.color.primary_blue));
    }

    private void switchToRecordMode() {
        isWriteMode = false;
        layoutWriteMode.setVisibility(View.GONE);
        layoutRecordMode.setVisibility(View.VISIBLE);
        btnModeWrite.setBackgroundResource(R.drawable.bg_card);
        btnModeWrite.setTextColor(getResources().getColor(R.color.primary_blue));
        btnModeRecord.setBackgroundResource(R.drawable.bg_button_primary_selector);
        btnModeRecord.setTextColor(getResources().getColor(android.R.color.white));
    }

    private void startSpeechRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Answer the question clearly...");

        try {
            tvRecordStatus.setText("Listening...");
            startActivityForResult(intent, SPEECH_REQUEST_CODE);
        } catch (Exception e) {
            Toast.makeText(this, "Speech recognition not supported", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                String spokenText = results.get(0);
                
                // Append to existing text
                if (!answerText.isEmpty()) {
                    answerText += " " + spokenText;
                } else {
                    answerText = spokenText;
                }

                tvRecordedText.setText(answerText);
                tvRecordedText.setVisibility(View.VISIBLE);
                tvRecordStatus.setText("Tap to continue recording");
            }
        } else {
            tvRecordStatus.setText("Tap to start recording");
        }
    }

    private void submitAnswer() {
        // Get answer text based on mode
        String finalAnswer;
        if (isWriteMode) {
            finalAnswer = etAnswer.getText().toString().trim();
        } else {
            finalAnswer = answerText.trim();
        }

        if (finalAnswer.isEmpty()) {
            Toast.makeText(this, "Please provide an answer first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Calculate scores
        Map<String, Object> scoreResult = calculateScore(finalAnswer);
        int overallScore = (int) scoreResult.get("overall");
        int keywordMatches = (int) scoreResult.get("keywordMatches");
        int totalKeywords = (int) scoreResult.get("totalKeywords");
        boolean grammarGood = (boolean) scoreResult.get("grammarGood");
        boolean structureGood = (boolean) scoreResult.get("structureGood");

        // Display scores
        displayScore(overallScore, keywordMatches, totalKeywords, grammarGood, structureGood);

        // Save to Firestore
        saveAnswerToFirestore(finalAnswer, overallScore, keywordMatches, grammarGood, structureGood);
    }

    private Map<String, Object> calculateScore(String answer) {
        Map<String, Object> result = new HashMap<>();
        
        String answerLower = answer.toLowerCase();
        
        // Keyword scoring (40%)
        int keywordMatches = 0;
        int totalKeywords = 0;
        if (keywords != null && !keywords.isEmpty()) {
            String[] keywordArray = keywords.split(",");
            totalKeywords = keywordArray.length;
            for (String keyword : keywordArray) {
                if (answerLower.contains(keyword.trim().toLowerCase())) {
                    keywordMatches++;
                }
            }
        }
        int keywordScore = totalKeywords > 0 ? (int) ((keywordMatches / (double) totalKeywords) * 40) : 0;

        // Grammar scoring (30%)
        boolean hasCapitalization = Character.isUpperCase(answer.charAt(0));
        boolean hasProperLength = answer.split("\\s+").length >= 8;
        boolean hasPunctuation = answer.contains(".") || answer.contains("!") || answer.contains("?");
        boolean grammarGood = hasCapitalization && hasProperLength && hasPunctuation;
        int grammarScore = grammarGood ? 30 : 15;

        // Structure scoring (30%) - Check for STAR elements
        boolean hasSituation = answerLower.contains("situation") || answerLower.contains("when") || answerLower.contains("at");
        boolean hasTask = answerLower.contains("task") || answerLower.contains("needed") || answerLower.contains("goal");
        boolean hasAction = answerLower.contains("action") || answerLower.contains("did") || answerLower.contains("implemented");
        boolean hasResult = answerLower.contains("result") || answerLower.contains("achieved") || answerLower.contains("success");
        
        int structureElements = 0;
        if (hasSituation) structureElements++;
        if (hasTask) structureElements++;
        if (hasAction) structureElements++;
        if (hasResult) structureElements++;
        
        boolean structureGood = structureElements >= 3;
        int structureScore = (int) ((structureElements / 4.0) * 30);

        // Overall score
        int overallScore = keywordScore + grammarScore + structureScore;

        result.put("overall", overallScore);
        result.put("keywordMatches", keywordMatches);
        result.put("totalKeywords", totalKeywords);
        result.put("grammarGood", grammarGood);
        result.put("structureGood", structureGood);

        return result;
    }

    private void displayScore(int overall, int keywordMatches, int totalKeywords, boolean grammarGood, boolean structureGood) {
        cardScore.setVisibility(View.VISIBLE);
        tvOverallScore.setText(overall + "/100");
        tvKeywordScore.setText(keywordMatches + "/" + totalKeywords + " keywords");
        
        tvGrammar.setText(grammarGood ? "Good" : "Needs improvement");
        tvGrammar.setTextColor(getResources().getColor(grammarGood ? R.color.difficulty_easy : R.color.difficulty_medium));
        
        tvStructure.setText(structureGood ? "Complete" : "Missing elements");
        tvStructure.setTextColor(getResources().getColor(structureGood ? R.color.difficulty_easy : R.color.difficulty_medium));

        // Change overall score color based on value
        int scoreColor;
        if (overall >= 80) {
            scoreColor = R.color.difficulty_easy;
        } else if (overall >= 60) {
            scoreColor = R.color.difficulty_medium;
        } else {
            scoreColor = R.color.difficulty_hard;
        }
        tvOverallScore.setTextColor(getResources().getColor(scoreColor));
    }

    private void saveAnswerToFirestore(String answer, int score, int keywordScore, boolean grammarGood, boolean structureGood) {
        if (auth.getCurrentUser() == null) {
            return; // Don't save if not logged in
        }

        Map<String, Object> answerData = new HashMap<>();
        answerData.put("userId", auth.getCurrentUser().getUid());
        answerData.put("questionId", questionId);
        answerData.put("answer_text", answer);
        answerData.put("score", score);
        answerData.put("keywordScore", keywordScore);
        answerData.put("grammarGood", grammarGood);
        answerData.put("structureGood", structureGood);
        answerData.put("timestamp", System.currentTimeMillis());

        db.collection("user_behavioral_answers")
                .add(answerData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Answer saved!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
