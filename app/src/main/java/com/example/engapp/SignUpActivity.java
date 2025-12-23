package com.example.engapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.*;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private TextInputEditText etEmail, etPassword, etConfirmPassword;
    private TextInputLayout emailLayout, passwordLayout, confirmPasswordLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        auth = FirebaseAuth.getInstance();
        initViews();
        setupSignUp();

        // Nhận email từ LoginActivity (nếu có)
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("email")) {
            String email = intent.getStringExtra("email");
            etEmail.setText(email);
        }
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout);

        // Back to login
        TextView tvBackToLogin = findViewById(R.id.tvBackToLogin);
        tvBackToLogin.setOnClickListener(v -> finish());
    }

    private void setupSignUp() {
        Button btnSignUp = findViewById(R.id.btnSignUp);
        btnSignUp.setOnClickListener(v -> signUpWithEmailPassword());
    }

    private void signUpWithEmailPassword() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (!validateInputs(email, password, confirmPassword)) {
            return;
        }

        showLoading(true);

        // KIỂM TRA EMAIL ĐÃ TỒN TẠI CHƯA
        auth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        java.util.List<String> signInMethods = task.getResult().getSignInMethods();

                        if (signInMethods != null && !signInMethods.isEmpty()) {
                            // EMAIL ĐÃ TỒN TẠI
                            showLoading(false);
                            if (signInMethods.contains("google.com")) {
                                showErrorDialog("Email đã được đăng ký với Google. Vui lòng sử dụng email khác hoặc đăng nhập bằng Google.");
                            } else {
                                showErrorDialog("Email đã được đăng ký. Vui lòng sử dụng email khác.");
                            }
                        } else {
                            // EMAIL CHƯA TỒN TẠI - TIẾN HÀNH ĐĂNG KÝ
                            createAccountWithEmailPassword(email, password);
                        }
                    } else {
                        showLoading(false);
                        Toast.makeText(this, "Lỗi kiểm tra email", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void createAccountWithEmailPassword(String email, String password) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // ✅ ĐĂNG KÝ THÀNH CÔNG - GỬI EMAIL XÁC NHẬN
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            sendEmailVerification(user, email);
                        }
                    } else {
                        // ❌ ĐĂNG KÝ THẤT BẠI
                        showLoading(false);
                        Exception error = task.getException();
                        Log.e("SIGNUP_ERROR", "Create account failed", error);

                        if (error instanceof FirebaseAuthUserCollisionException) {
                            showErrorDialog("Email đã được đăng ký. Vui lòng sử dụng email khác.");
                        } else if (error instanceof FirebaseAuthWeakPasswordException) {
                            showErrorDialog("Mật khẩu quá yếu. Vui lòng chọn mật khẩu mạnh hơn.");
                        } else {
                            showErrorDialog("Đăng ký thất bại: " + error.getMessage());
                        }
                    }
                });
    }

    private void sendEmailVerification(FirebaseUser user, String email) {
        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        showLoading(false);

                        if (task.isSuccessful()) {
                            // ✅ GỬI EMAIL XÁC NHẬN THÀNH CÔNG
                            showVerificationSuccessDialog(email);

                            // Cập nhật display name
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(email.split("@")[0])
                                    .build();

                            user.updateProfile(profileUpdates);

                        } else {
                            // ❌ GỬI EMAIL THẤT BẠI
                            Log.e("EMAIL_VERIFICATION", "Send email verification failed", task.getException());
                            showErrorDialog("Không thể gửi email xác nhận. Vui lòng kiểm tra email và thử lại.");
                        }
                    }
                });
    }

    private void showVerificationSuccessDialog(String email) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Đăng ký thành công!")
                .setMessage("Chúng tôi đã gửi email xác nhận đến:\n" + email +
                        "\n\nVui lòng kiểm tra hộp thư và click vào link xác nhận để kích hoạt tài khoản." +
                        "\n\nSau khi xác nhận, bạn có thể đăng nhập vào ứng dụng.")
                .setPositiveButton("Đã hiểu", (dialog, which) -> {
                    // Trả về email cho LoginActivity
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("email", email);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private boolean validateInputs(String email, String password, String confirmPassword) {
        boolean isValid = true;

        // Validate email
        if (email.isEmpty()) {
            emailLayout.setError("Email không được để trống");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Email không hợp lệ");
            isValid = false;
        } else {
            emailLayout.setError(null);
        }

        // Validate password
        if (password.isEmpty()) {
            passwordLayout.setError("Mật khẩu không được để trống");
            isValid = false;
        } else if (password.length() < 6) {
            passwordLayout.setError("Mật khẩu phải có ít nhất 6 ký tự");
            isValid = false;
        } else {
            passwordLayout.setError(null);
        }

        // Validate confirm password
        if (confirmPassword.isEmpty()) {
            confirmPasswordLayout.setError("Xác nhận mật khẩu không được để trống");
            isValid = false;
        } else if (!confirmPassword.equals(password)) {
            confirmPasswordLayout.setError("Mật khẩu xác nhận không khớp");
            isValid = false;
        } else {
            confirmPasswordLayout.setError(null);
        }

        return isValid;
    }

    private void showErrorDialog(String message) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Lỗi đăng ký")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showLoading(boolean show) {
        Button btnSignUp = findViewById(R.id.btnSignUp);
        btnSignUp.setText(show ? "Đang xử lý..." : "Đăng ký");
        btnSignUp.setEnabled(!show);
    }
}