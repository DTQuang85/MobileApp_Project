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

    private TextView tvPracticeQuestion, tvCategory;
    private Button btnAnswer1, btnAnswer2, btnAnswer3, btnAnswer4, btnChangeCategory;
    private int score = 0;
    private int questionsAnswered = 0;
    private int MAX_QUESTIONS = 10; // Tăng số câu hỏi
    private String currentCategory = "all";

    // Câu hỏi theo chủ đề - [Câu hỏi, Đáp án 1, Đáp án 2, Đáp án 3, Đáp án 4, Index đúng, Chủ đề]
    private final String[][] allQuestions = {
            // GREETINGS & BASIC (Chào hỏi cơ bản)
            {"How do you say 'Xin chào'?", "Goodbye", "Hello", "Sorry", "Thanks", "1", "greetings"},
            {"What means 'Good morning'?", "Buổi tối", "Chào buổi sáng", "Tạm biệt", "Cảm ơn", "1", "greetings"},
            {"'Tạm biệt' in English?", "Hello", "Goodbye", "Good night", "Welcome", "1", "greetings"},
            {"'Cảm ơn' means?", "Sorry", "Please", "Thank you", "Welcome", "2", "greetings"},
            {"How to say 'Xin lỗi'?", "Thank you", "Excuse me", "Sorry", "Please", "2", "greetings"},
            {"'Chúc ngủ ngon' in English?", "Good day", "Good night", "Good bye", "Good luck", "1", "greetings"},

            // NUMBERS (Số đếm)
            {"'Một' in English?", "Two", "One", "Three", "Four", "1", "numbers"},
            {"What is 'Five'?", "Bốn", "Năm", "Sáu", "Ba", "1", "numbers"},
            {"'Mười' means?", "Nine", "Ten", "Eleven", "Eight", "1", "numbers"},
            {"'Ba' in English?", "Two", "Four", "Three", "Five", "2", "numbers"},
            {"What is 'Seven'?", "Sáu", "Bảy", "Tám", "Chín", "1", "numbers"},
            {"'Hai' means?", "One", "Two", "Three", "Zero", "1", "numbers"},
            {"'Bốn' in English?", "Three", "Four", "Five", "Six", "1", "numbers"},
            {"What is 'Nine'?", "Bảy", "Tám", "Chín", "Mười", "2", "numbers"},

            // COLORS (Màu sắc)
            {"What color is 'Đỏ'?", "Blue", "Red", "Green", "Yellow", "1", "colors"},
            {"'Xanh dương' means?", "Red", "Blue", "Green", "Yellow", "1", "colors"},
            {"What is 'Green'?", "Đỏ", "Xanh lá", "Vàng", "Trắng", "1", "colors"},
            {"'Vàng' in English?", "White", "Black", "Yellow", "Orange", "2", "colors"},
            {"Color of the sun?", "Blue", "Green", "Yellow", "Red", "2", "colors"},
            {"'Trắng' means?", "Black", "White", "Gray", "Brown", "1", "colors"},
            {"What is 'Black'?", "Trắng", "Đen", "Xám", "Nâu", "1", "colors"},
            {"'Hồng' in English?", "Purple", "Pink", "Orange", "Brown", "1", "colors"},

            // ANIMALS (Động vật)
            {"'Con mèo' in English?", "Dog", "Cat", "Bird", "Fish", "1", "animals"},
            {"What is 'Dog'?", "Mèo", "Chó", "Gà", "Vịt", "1", "animals"},
            {"'Con voi' means?", "Tiger", "Elephant", "Lion", "Bear", "1", "animals"},
            {"What animal says 'Meow'?", "Dog", "Cat", "Cow", "Duck", "1", "animals"},
            {"'Con cá' in English?", "Bird", "Cat", "Fish", "Frog", "2", "animals"},
            {"What is 'Rabbit'?", "Chuột", "Thỏ", "Gấu", "Hổ", "1", "animals"},
            {"'Con gà' means?", "Duck", "Chicken", "Bird", "Goose", "1", "animals"},
            {"King of jungle?", "Tiger", "Lion", "Bear", "Wolf", "1", "animals"},

            // FAMILY (Gia đình)
            {"'Mẹ' in English?", "Father", "Mother", "Sister", "Brother", "1", "family"},
            {"What is 'Father'?", "Mẹ", "Bố", "Anh", "Em", "1", "family"},
            {"'Anh trai' means?", "Sister", "Brother", "Uncle", "Cousin", "1", "family"},
            {"What is 'Sister'?", "Anh trai", "Chị/Em gái", "Mẹ", "Bố", "1", "family"},
            {"'Ông nội/ngoại' in English?", "Uncle", "Grandfather", "Father", "Brother", "1", "family"},
            {"What is 'Grandmother'?", "Cô", "Bà", "Mẹ", "Dì", "1", "family"},

            // FOOD (Đồ ăn)
            {"'Táo' in English?", "Orange", "Apple", "Banana", "Grape", "1", "food"},
            {"What is 'Banana'?", "Cam", "Chuối", "Dưa", "Nho", "1", "food"},
            {"'Sữa' means?", "Water", "Milk", "Juice", "Tea", "1", "food"},
            {"What is 'Bread'?", "Cơm", "Bánh mì", "Phở", "Bún", "1", "food"},
            {"'Nước' in English?", "Milk", "Water", "Juice", "Coffee", "1", "food"},
            {"What is 'Rice'?", "Phở", "Cơm", "Bún", "Mì", "1", "food"},
            {"'Trứng' means?", "Milk", "Egg", "Bread", "Meat", "1", "food"},
            {"What is 'Fish'?", "Thịt", "Cá", "Gà", "Tôm", "1", "food"},

            // BODY PARTS (Bộ phận cơ thể)
            {"'Đầu' in English?", "Hand", "Head", "Leg", "Arm", "1", "body"},
            {"What is 'Hand'?", "Chân", "Tay", "Mắt", "Tai", "1", "body"},
            {"'Mắt' means?", "Nose", "Eye", "Ear", "Mouth", "1", "body"},
            {"What is 'Mouth'?", "Mũi", "Miệng", "Tai", "Răng", "1", "body"},
            {"'Chân' in English?", "Hand", "Arm", "Leg", "Foot", "2", "body"},
            {"What is 'Nose'?", "Mắt", "Mũi", "Tai", "Miệng", "1", "body"},

            // SCHOOL (Trường học)
            {"'Sách' in English?", "Pen", "Book", "Pencil", "Paper", "1", "school"},
            {"What is 'Teacher'?", "Học sinh", "Giáo viên", "Bác sĩ", "Cô giáo", "1", "school"},
            {"'Bút chì' means?", "Pen", "Pencil", "Eraser", "Ruler", "1", "school"},
            {"What is 'School'?", "Nhà", "Trường học", "Công viên", "Bệnh viện", "1", "school"},
            {"'Bảng đen' in English?", "Whiteboard", "Blackboard", "Table", "Chair", "1", "school"},

            // WEATHER (Thời tiết)
            {"'Mưa' in English?", "Sun", "Rain", "Cloud", "Wind", "1", "weather"},
            {"What is 'Sunny'?", "Mưa", "Nắng", "Gió", "Mây", "1", "weather"},
            {"'Gió' means?", "Rain", "Cloud", "Wind", "Storm", "2", "weather"},
            {"What is 'Cold'?", "Nóng", "Lạnh", "Ấm", "Mát", "1", "weather"},
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
        tvCategory = findViewById(R.id.tvCategory);
        btnAnswer1 = findViewById(R.id.btnAnswer1);
        btnAnswer2 = findViewById(R.id.btnAnswer2);
        btnAnswer3 = findViewById(R.id.btnAnswer3);
        btnAnswer4 = findViewById(R.id.btnAnswer4);
        btnChangeCategory = findViewById(R.id.btnChangeCategory);

        btnAnswer1.setOnClickListener(v -> checkAnswer(0));
        btnAnswer2.setOnClickListener(v -> checkAnswer(1));
        btnAnswer3.setOnClickListener(v -> checkAnswer(2));
        btnAnswer4.setOnClickListener(v -> checkAnswer(3));

        if (btnChangeCategory != null) {
            btnChangeCategory.setOnClickListener(v -> showCategoryDialog());
        }

        updateCategoryDisplay();
    }

    private void showCategoryDialog() {
        String[] categories = {
            "📚 Tất cả chủ đề",
            "👋 Chào hỏi (Greetings)",
            "🔢 Số đếm (Numbers)",
            "🎨 Màu sắc (Colors)",
            "🐾 Động vật (Animals)",
            "👨‍👩‍👧 Gia đình (Family)",
            "🍎 Đồ ăn (Food)",
            "👤 Cơ thể (Body)",
            "📖 Trường học (School)",
            "🌤️ Thời tiết (Weather)"
        };

        String[] categoryKeys = {
            "all", "greetings", "numbers", "colors", "animals",
            "family", "food", "body", "school", "weather"
        };

        new AlertDialog.Builder(this)
            .setTitle("Chọn chủ đề luyện tập")
            .setItems(categories, (dialog, which) -> {
                currentCategory = categoryKeys[which];
                // Reset game
                score = 0;
                questionsAnswered = 0;
                updateCategoryDisplay();
                loadNextQuestion();
            })
            .show();
    }

    private void updateCategoryDisplay() {
        if (tvCategory != null) {
            String categoryName = getCategoryName(currentCategory);
            tvCategory.setText("📂 Chủ đề: " + categoryName);
        }
    }

    private String getCategoryName(String category) {
        switch (category) {
            case "greetings": return "Chào hỏi";
            case "numbers": return "Số đếm";
            case "colors": return "Màu sắc";
            case "animals": return "Động vật";
            case "family": return "Gia đình";
            case "food": return "Đồ ăn";
            case "body": return "Cơ thể";
            case "school": return "Trường học";
            case "weather": return "Thời tiết";
            default: return "Tất cả";
        }
    }

    private void loadNextQuestion() {
        if (questionsAnswered >= MAX_QUESTIONS) {
            showResults();
            return;
        }

        // Lọc câu hỏi theo chủ đề
        java.util.ArrayList<String[]> filteredQuestions = new java.util.ArrayList<>();
        for (String[] q : allQuestions) {
            if (currentCategory.equals("all") || q[6].equals(currentCategory)) {
                filteredQuestions.add(q);
            }
        }

        if (filteredQuestions.isEmpty()) {
            Toast.makeText(this, "Không có câu hỏi cho chủ đề này", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] question = filteredQuestions.get(random.nextInt(filteredQuestions.size()));

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

