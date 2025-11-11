package com.example.engapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.example.engapp.utils.DatabaseHelper;
import com.example.engapp.models.User;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Khởi tạo lessons nếu chưa có
        DatabaseHelper.getInstance().initializeLessons();

        auth = FirebaseAuth.getInstance();

        // Hiển thị thông tin user
        displayUserInfo();

        // Kiểm tra email verification
        checkEmailVerification();


        // Load user stats from database
        loadUserStats();
        // Setup click listeners
        setupClickListeners();
    }

    private void displayUserInfo() {
        FirebaseUser user = auth.getCurrentUser();
        TextView tvWelcome = findViewById(R.id.tvWelcome);
        if (user != null) {
            String welcomeText = "Xin chào, " +
                    (user.getDisplayName() != null ? user.getDisplayName() : user.getEmail());
            tvWelcome.setText(welcomeText);
        }
    }

    private void loadUserStats() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            DatabaseHelper.getInstance().getUserData(user.getUid(), new DatabaseHelper.UserDataCallback() {
                @Override
                public void onUserDataReceived(User userData) {
                    if (userData != null) {
                        updateStatsUI(userData);
                    }
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(HomeActivity.this, "Không thể tải dữ liệu người dùng", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateStatsUI(User userData) {
        TextView tvWelcome = findViewById(R.id.tvWelcome);
        String statsText = "Xin chào, " + userData.getDisplayName() + "!\n" +
                "🏆 Điểm: " + userData.getTotalScore() + " | " +
                "📚 Bài học: " + userData.getLessonsCompleted();
        tvWelcome.setText(statsText);
    }


    private void checkEmailVerification() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null && !user.isEmailVerified()) {
            // Nếu user đăng nhập nhưng email chưa xác nhận, hiển thị thông báo
            showEmailVerificationReminder(user);
        }
    }

    private void showEmailVerificationReminder(FirebaseUser user) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Xác nhận email")
                .setMessage("Tài khoản của bạn chưa được xác nhận. " +
                        "Vui lòng kiểm tra email và click vào link xác nhận để sử dụng đầy đủ tính năng.")
                .setPositiveButton("Gửi lại email", (dialog, which) -> {
                    user.sendEmailVerification()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(this, "Đã gửi lại email xác nhận!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(this, "Gửi email thất bại!", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Để sau", null)
                .show();
    }

    private void setupClickListeners() {
        // Lesson button
        findViewById(R.id.btnLessons).setOnClickListener(v -> {
            startActivity(new Intent(this, LessonActivity.class));
        });

        // Practice button
        findViewById(R.id.btnPractice).setOnClickListener(v -> {
            startActivity(new Intent(this, PracticeActivity.class));
        });

        // Profile button
        findViewById(R.id.btnProfile).setOnClickListener(v -> {
            showProfileDialog();
        });

        // Logout button
        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            logout();
        });
    }

    private void showProfileDialog() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String profileInfo = "Email: " + user.getEmail() +
                    "\nTên: " + (user.getDisplayName() != null ? user.getDisplayName() : "Chưa đặt") +
                    "\nXác nhận email: " + (user.isEmailVerified() ? "Đã xác nhận" : "Chưa xác nhận");

            new android.app.AlertDialog.Builder(this)
                    .setTitle("Thông tin tài khoản")
                    .setMessage(profileInfo)
                    .setPositiveButton("OK", null)
                    .show();
        }
    }

    private void logout() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    auth.signOut();
                    Intent intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}