package com.example.engapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.engapp.utils.DatabaseHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Random;

public class PracticeActivity extends AppCompatActivity {

    private TextView tvPracticeQuestion;
    private Button btnAnswer1, btnAnswer2, btnAnswer3, btnAnswer4;
    private int score = 0;
    private int questionsAnswered = 0;
    private final int MAX_QUESTIONS = 5;

    // Sample practice questions
    private final String[][] questions = {
            {"What is 'Xin chào' in English?", "Goodbye", "Hello", "Thank you", "Sorry", "1"},
            {"How do you say 'Cảm ơn'?", "Welcome", "Sorry", "Please", "Thank you", "3"},
            {"What color is 'Xanh lá'?", "Blue", "Red", "Green", "Yellow", "2"},
            {"What is 'một' in English?", "Two", "One", "Three", "Zero", "1"},
            {"How do you say 'Tạm biệt'?", "Hello", "Goodbye", "Good night", "Good morning", "1"},
            {"What is 'hai' in English?", "One", "Two", "Three", "Four", "1"},
            {"What color is the sky?", "Red", "Blue", "Green", "Yellow", "1"},
            {"How do you greet in the morning?", "Good night", "Good evening", "Good morning", "Goodbye", "2"},
    };

    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practice);

        initViews();
        loadNextQuestion();
    }

    private void initViews() {
        tvPracticeQuestion = findViewById(R.id.tvPracticeQuestion);
        btnAnswer1 = findViewById(R.id.btnAnswer1);
        btnAnswer2 = findViewById(R.id.btnAnswer2);
        btnAnswer3 = findViewById(R.id.btnAnswer3);
        btnAnswer4 = findViewById(R.id.btnAnswer4);

        btnAnswer1.setOnClickListener(v -> checkAnswer(0));
        btnAnswer2.setOnClickListener(v -> checkAnswer(1));
        btnAnswer3.setOnClickListener(v -> checkAnswer(2));
        btnAnswer4.setOnClickListener(v -> checkAnswer(3));
    }

    private void loadNextQuestion() {
        if (questionsAnswered >= MAX_QUESTIONS) {
            showResults();
            return;
        }

        String[] question = questions[random.nextInt(questions.length)];

        tvPracticeQuestion.setText("Câu " + (questionsAnswered + 1) + "/" + MAX_QUESTIONS + "\n\n" + question[0]);
        btnAnswer1.setText(question[1]);
        btnAnswer2.setText(question[2]);
        btnAnswer3.setText(question[3]);
        btnAnswer4.setText(question[4]);

        // Store correct answer in tag
        btnAnswer1.setTag(question[5]);
    }

    private void checkAnswer(int selectedAnswer) {
        String correctAnswerStr = (String) btnAnswer1.getTag();
        int correctAnswer = Integer.parseInt(correctAnswerStr);

        questionsAnswered++;

        if (selectedAnswer == correctAnswer) {
            score += 10;
            Toast.makeText(this, "✅ Chính xác! +10 điểm", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "❌ Sai rồi!", Toast.LENGTH_SHORT).show();
        }

        // Delay before next question
        tvPracticeQuestion.postDelayed(this::loadNextQuestion, 800);
    }

    private void showResults() {
        double percentage = (double) score / (MAX_QUESTIONS * 10) * 100;
        String resultEmoji = percentage >= 80 ? "🎉" : percentage >= 60 ? "👍" : "💪";

        new AlertDialog.Builder(this)
                .setTitle(resultEmoji + " Hoàn thành!")
                .setMessage("Kết quả luyện tập:\n\n" +
                        "🏆 Điểm: " + score + "/" + (MAX_QUESTIONS * 10) + "\n" +
                        "📈 Tỷ lệ: " + String.format("%.0f", percentage) + "%")
                .setPositiveButton("Luyện lại", (dialog, which) -> {
                    score = 0;
                    questionsAnswered = 0;
                    loadNextQuestion();
                })
                .setNegativeButton("Về trang chủ", (dialog, which) -> finish())
                .setCancelable(false)
                .show();

        // Save score
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseHelper.getInstance().updateUserScore(user.getUid(), score);
        }
    }
}

