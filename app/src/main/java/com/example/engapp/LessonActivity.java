package com.example.engapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.engapp.models.Lesson;
import com.example.engapp.models.Question;
import com.example.engapp.utils.DatabaseHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class LessonActivity extends AppCompatActivity {

    private TextView tvLessonContent;
    private Button btnNext;
    private RadioGroup rgOptions;
    private RadioButton rbOption1, rbOption2, rbOption3, rbOption4;

    private List<Lesson> lessons;
    private int currentLessonIndex = 0;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private Lesson currentLesson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);

        initViews();
        loadLessons();
    }

    private void initViews() {
        tvLessonContent = findViewById(R.id.tvLessonContent);
        btnNext = findViewById(R.id.btnNext);
        rgOptions = findViewById(R.id.rgOptions);
        rbOption1 = findViewById(R.id.rbOption1);
        rbOption2 = findViewById(R.id.rbOption2);
        rbOption3 = findViewById(R.id.rbOption3);
        rbOption4 = findViewById(R.id.rbOption4);

        // Hide quiz UI initially
        rgOptions.setVisibility(View.GONE);

        btnNext.setOnClickListener(v -> handleNextButton());
    }

    private void loadLessons() {
        tvLessonContent.setText("Đang tải bài học...");
        btnNext.setEnabled(false);

        DatabaseHelper.getInstance().getAllLessons(new DatabaseHelper.LessonsCallback() {
            @Override
            public void onLessonsReceived(List<Lesson> loadedLessons) {
                lessons = loadedLessons;
                if (lessons != null && !lessons.isEmpty()) {
                    currentLesson = lessons.get(currentLessonIndex);
                    showLessonIntro();
                } else {
                    tvLessonContent.setText("Không có bài học nào. Vui lòng thử lại sau.");
                }
            }

            @Override
            public void onError(Exception e) {
                tvLessonContent.setText("Lỗi tải bài học: " + e.getMessage());
                Toast.makeText(LessonActivity.this, "Không thể tải bài học", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLessonIntro() {
        rgOptions.setVisibility(View.GONE);
        tvLessonContent.setText("📚 " + currentLesson.getTitle() + "\n\n" +
                currentLesson.getDescription() + "\n\n" +
                "Độ khó: " + getDifficultyText(currentLesson.getDifficulty()) + "\n" +
                "Số câu hỏi: " + currentLesson.getQuestions().size() + "\n\n" +
                "Nhấn 'Bắt đầu' để làm bài!");
        btnNext.setText("Bắt đầu");
        btnNext.setEnabled(true);
    }

    private String getDifficultyText(String difficulty) {
        switch (difficulty.toLowerCase()) {
            case "beginner": return "Cơ bản ⭐";
            case "intermediate": return "Trung cấp ⭐⭐";
            case "advanced": return "Nâng cao ⭐⭐⭐";
            default: return difficulty;
        }
    }

    private void showQuestion() {
        if (currentQuestionIndex < currentLesson.getQuestions().size()) {
            Question question = currentLesson.getQuestions().get(currentQuestionIndex);

            // Show quiz UI
            rgOptions.setVisibility(View.VISIBLE);
            rgOptions.clearCheck();

            // Set question
            tvLessonContent.setText("Câu " + (currentQuestionIndex + 1) + "/" +
                    currentLesson.getQuestions().size() + "\n\n" + question.getQuestion());

            // Set options
            List<String> options = question.getOptions();
            rbOption1.setText(options.get(0));
            rbOption2.setText(options.get(1));
            rbOption3.setText(options.get(2));
            rbOption4.setText(options.get(3));

            btnNext.setText("Kiểm tra");
            btnNext.setEnabled(false);

            // Enable button when an option is selected
            rgOptions.setOnCheckedChangeListener((group, checkedId) -> {
                btnNext.setEnabled(true);
            });
        } else {
            showLessonComplete();
        }
    }

    private void handleNextButton() {
        String buttonText = btnNext.getText().toString();

        if (buttonText.equals("Bắt đầu")) {
            currentQuestionIndex = 0;
            score = 0;
            showQuestion();
        } else if (buttonText.equals("Kiểm tra")) {
            checkAnswer();
        } else if (buttonText.equals("Tiếp theo")) {
            currentQuestionIndex++;
            showQuestion();
        } else if (buttonText.equals("Hoàn thành")) {
            moveToNextLesson();
        } else if (buttonText.equals("Về trang chủ")) {
            finish();
        }
    }

    private void checkAnswer() {
        Question question = currentLesson.getQuestions().get(currentQuestionIndex);
        int selectedId = rgOptions.getCheckedRadioButtonId();

        int selectedIndex = -1;
        if (selectedId == R.id.rbOption1) selectedIndex = 0;
        else if (selectedId == R.id.rbOption2) selectedIndex = 1;
        else if (selectedId == R.id.rbOption3) selectedIndex = 2;
        else if (selectedId == R.id.rbOption4) selectedIndex = 3;

        if (selectedIndex == question.getCorrectAnswer()) {
            // Correct answer
            score += 10;
            showAnswerDialog(true, question.getExplanation());
        } else {
            // Wrong answer
            showAnswerDialog(false, question.getExplanation());
        }
    }

    private void showAnswerDialog(boolean isCorrect, String explanation) {
        String title = isCorrect ? "✅ Chính xác!" : "❌ Sai rồi!";
        String message = explanation + "\n\nĐiểm hiện tại: " + score;

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> {
                    if (currentQuestionIndex < currentLesson.getQuestions().size() - 1) {
                        btnNext.setText("Tiếp theo");
                    } else {
                        btnNext.setText("Hoàn thành");
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void showLessonComplete() {
        rgOptions.setVisibility(View.GONE);

        int totalQuestions = currentLesson.getQuestions().size();
        int correctAnswers = score / 10;
        double percentage = (double) correctAnswers / totalQuestions * 100;

        String resultEmoji = percentage >= 80 ? "🎉" : percentage >= 60 ? "👍" : "💪";

        tvLessonContent.setText(resultEmoji + " Hoàn thành bài học!\n\n" +
                "📊 Kết quả:\n" +
                "✓ Đúng: " + correctAnswers + "/" + totalQuestions + "\n" +
                "🏆 Điểm: " + score + "\n" +
                "📈 Tỷ lệ: " + String.format("%.0f", percentage) + "%\n\n" +
                (percentage >= 80 ? "Xuất sắc! 🌟" :
                 percentage >= 60 ? "Khá tốt! Cố gắng hơn nữa!" :
                 "Hãy ôn lại và thử lại nhé!"));

        // Save score to Firebase
        saveProgress();

        if (currentLessonIndex < lessons.size() - 1) {
            btnNext.setText("Bài tiếp theo →");
            btnNext.setEnabled(true);
            btnNext.setOnClickListener(v -> moveToNextLesson());
        } else {
            btnNext.setText("Về trang chủ");
            btnNext.setEnabled(true);
            btnNext.setOnClickListener(v -> finish());
        }
    }

    private void saveProgress() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseHelper.getInstance().updateUserScore(user.getUid(), score);
            DatabaseHelper.getInstance().incrementLessonsCompleted(user.getUid());
        }
    }

    private void moveToNextLesson() {
        currentLessonIndex++;
        if (currentLessonIndex < lessons.size()) {
            currentLesson = lessons.get(currentLessonIndex);
            currentQuestionIndex = 0;
            score = 0;
            showLessonIntro();
        } else {
            finish();
        }
    }
}