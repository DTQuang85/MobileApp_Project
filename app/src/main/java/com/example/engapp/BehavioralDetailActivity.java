package com.example.engapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class BehavioralDetailActivity extends AppCompatActivity {

    private TextView tvQuestion, tvCategory, tvDifficulty, tvExplanation, tvKeywords;
    private TextView tvSampleBasic, tvSampleIntermediate, tvSampleAdvanced;
    private Button btnPractice;
    private ImageView btnBack;

    private String questionId, question, category, difficulty, sampleBasic, sampleIntermediate, 
                   sampleAdvanced, keywords, explanation, practiceTemplate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_behavioral_detail);

        // Initialize views
        btnBack = findViewById(R.id.btnBack);
        tvQuestion = findViewById(R.id.tvQuestion);
        tvCategory = findViewById(R.id.tvCategory);
        tvDifficulty = findViewById(R.id.tvDifficulty);
        tvExplanation = findViewById(R.id.tvExplanation);
        tvKeywords = findViewById(R.id.tvKeywords);
        tvSampleBasic = findViewById(R.id.tvSampleBasic);
        tvSampleIntermediate = findViewById(R.id.tvSampleIntermediate);
        tvSampleAdvanced = findViewById(R.id.tvSampleAdvanced);
        btnPractice = findViewById(R.id.btnPractice);

        // Get data from intent
        Intent intent = getIntent();
        questionId = intent.getStringExtra("questionId");
        question = intent.getStringExtra("question");
        category = intent.getStringExtra("category");
        difficulty = intent.getStringExtra("difficulty");
        sampleBasic = intent.getStringExtra("sample_basic");
        sampleIntermediate = intent.getStringExtra("sample_intermediate");
        sampleAdvanced = intent.getStringExtra("sample_advanced");
        keywords = intent.getStringExtra("keywords");
        explanation = intent.getStringExtra("explanation");
        practiceTemplate = intent.getStringExtra("practice_template");

        // Display data
        tvQuestion.setText(question);
        tvCategory.setText(category);
        tvDifficulty.setText(difficulty.toUpperCase());
        tvExplanation.setText(explanation != null ? explanation : "Use STAR method: Situation, Task, Action, Result");
        tvKeywords.setText(keywords != null ? keywords.replace(",", ", ") : "");
        tvSampleBasic.setText(sampleBasic != null ? sampleBasic : "No sample available");
        tvSampleIntermediate.setText(sampleIntermediate != null ? sampleIntermediate : "No sample available");
        tvSampleAdvanced.setText(sampleAdvanced != null ? sampleAdvanced : "No sample available");

        // Set difficulty color
        int difficultyColor;
        switch (difficulty.toLowerCase()) {
            case "easy":
                difficultyColor = getResources().getColor(R.color.difficulty_easy);
                break;
            case "medium":
                difficultyColor = getResources().getColor(R.color.difficulty_medium);
                break;
            case "hard":
                difficultyColor = getResources().getColor(R.color.difficulty_hard);
                break;
            default:
                difficultyColor = getResources().getColor(R.color.text_primary);
        }
        tvDifficulty.setTextColor(difficultyColor);

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Practice button
        btnPractice.setOnClickListener(v -> {
            Intent practiceIntent = new Intent(BehavioralDetailActivity.this, BehavioralPracticeActivity.class);
            practiceIntent.putExtra("questionId", questionId);
            practiceIntent.putExtra("question", question);
            practiceIntent.putExtra("keywords", keywords);
            practiceIntent.putExtra("practice_template", practiceTemplate);
            startActivity(practiceIntent);
        });
    }
}
