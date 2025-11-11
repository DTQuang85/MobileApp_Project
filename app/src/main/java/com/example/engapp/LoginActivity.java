package com.example.engapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.*;
import java.util.List;
import android.widget.LinearLayout;
import com.example.engapp.utils.DatabaseHelper;

public class LoginActivity extends AppCompatActivity {

    private GoogleSignInClient googleClient;
    private FirebaseAuth auth;

    private TextInputEditText etEmail, etPassword;
    private TextInputLayout emailLayout, passwordLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        // Nếu đã đăng nhập rồi -> vào thẳng Home
        if (auth.getCurrentUser() != null) {
            goHome(auth.getCurrentUser().getDisplayName());
            return;
        }

        initViews();
        setupGoogleSignIn();
        setupEmailPasswordLogin();
        setupForgotPassword();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);

        // Sign up text
        TextView tvSignUp = findViewById(R.id.tvSignUp);
        tvSignUp.setOnClickListener(v -> showSignUpDialog());

        // Forgot password text
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
    }

    private void setupForgotPassword() {
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
    }

    private void showForgotPasswordDialog() {
        // Tạo dialog nhập email để reset mật khẩu
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Quên mật khẩu");
        builder.setMessage("Nhập email của bạn để nhận link đặt lại mật khẩu");

        // Tạo input field
        final TextInputEditText input = new TextInputEditText(this);
        input.setHint("Email của bạn");
        input.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(50, 0, 50, 0);
        container.addView(input);

        builder.setView(container);

        builder.setPositiveButton("Gửi link", (dialog, which) -> {
            String email = input.getText().toString().trim();
            if (!email.isEmpty()) {
                sendPasswordResetEmail(email);
            } else {
                Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void sendPasswordResetEmail(String email) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hiển thị loading
        showForgotPasswordLoading(true);

        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        showForgotPasswordLoading(false);

                        if (task.isSuccessful()) {
                            // Gửi email thành công
                            showResetEmailSentDialog(email);
                        } else {
                            // Xử lý lỗi
                            handleResetPasswordError(task.getException(), email);
                        }
                    }
                });
    }

    private void showResetEmailSentDialog(String email) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Đã gửi email đặt lại mật khẩu")
                .setMessage("Chúng tôi đã gửi link đặt lại mật khẩu đến:\n" + email +
                        "\n\nVui lòng kiểm tra hộp thư và làm theo hướng dẫn.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void handleResetPasswordError(Exception exception, String email) {
        Log.e("RESET_PASSWORD", "Error sending reset email", exception);

        if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_LONG).show();
        } else if (exception instanceof FirebaseAuthInvalidUserException) {
            // Kiểm tra xem email có tồn tại không
            auth.fetchSignInMethodsForEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            List<String> signInMethods = task.getResult().getSignInMethods();
                            if (signInMethods == null || signInMethods.isEmpty()) {
                                Toast.makeText(this, "Email chưa được đăng ký", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(this, "Không thể gửi email đặt lại mật khẩu", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(this, "Lỗi hệ thống, vui lòng thử lại", Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Lỗi: " + exception.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showForgotPasswordLoading(boolean show) {
        // Có thể thêm progress bar nếu muốn
        if (show) {
            Toast.makeText(this, "Đang gửi email...", Toast.LENGTH_SHORT).show();
        }
    }

    // CẬP NHẬT method handleLoginError - thêm option quên mật khẩu
    private void handleLoginError(String email, Exception exception) {
        // Kiểm tra xem email đã được đăng ký với provider nào
        auth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(task -> {
                    showLoading(false);

                    if (task.isSuccessful()) {
                        List<String> signInMethods = task.getResult().getSignInMethods();

                        if (signInMethods != null && signInMethods.contains("google.com")) {
                            // 📧 Email đã được dùng với Google Sign-in
                            showProviderConflictDialog(email, "Google");
                        } else if (signInMethods != null && !signInMethods.isEmpty()) {
                            // 📧 Email đã được dùng với email/password - THÊM OPTION QUÊN MẬT KHẨU
                            showLoginErrorWithResetOption(email);
                        } else {
                            // 📧 Email chưa đăng ký
                            showEmailNotRegisteredDialog(email);
                        }
                    } else {
                        // 📧 Lỗi kiểm tra
                        showLoginErrorDialog("Email chưa được đăng ký. Vui lòng đăng ký tài khoản mới.");
                    }
                });
    }

    // THÊM METHOD MỚI - Hiển thị lỗi với option reset mật khẩu
    private void showLoginErrorWithResetOption(String email) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Đăng nhập thất bại")
                .setMessage("Sai mật khẩu. Bạn có muốn đặt lại mật khẩu không?")
                .setPositiveButton("Đặt lại mật khẩu", (dialog, which) -> {
                    sendPasswordResetEmail(email);
                })
                .setNegativeButton("Thử lại", null)
                .show();
    }

    // Các method khác giữ nguyên...
    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleClient = GoogleSignIn.getClient(this, gso);

        findViewById(R.id.btnGoogleSignIn).setOnClickListener(v -> {
            // LUÔN HIỆN DIALOG CHỌN TÀI KHOẢN BẰNG CÁCH ĐĂNG XUẤT TRƯỚC
            googleClient.signOut().addOnCompleteListener(this, task -> {
                // SAU KHI ĐĂNG XUẤT, MỞ DIALOG CHỌN TÀI KHOẢN
                Intent signInIntent = googleClient.getSignInIntent();
                signInLauncher.launch(signInIntent);
            });
        });
    }

    private void setupEmailPasswordLogin() {
        Button btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(v -> loginWithEmailPassword());
    }

    private void loginWithEmailPassword() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!validateInputs(email, password)) {
            return;
        }

        showLoading(true);

        // CHỈ ĐĂNG NHẬP, KHÔNG TỰ ĐỘNG ĐĂNG KÝ
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // ✅ ĐĂNG NHẬP THÀNH CÔNG - KIỂM TRA EMAIL ĐÃ XÁC NHẬN CHƯA
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            // EMAIL ĐÃ XÁC NHẬN - Lưu vào Database
                            String displayName = user.getDisplayName() != null ? user.getDisplayName() : user.getEmail().split("@")[0];
                            DatabaseHelper.getInstance().createOrUpdateUser(user.getUid(), user.getEmail(), displayName);
                            // EMAIL ĐÃ XÁC NHẬN
                            goHome(displayName);
                        }
                    } else {
                        // ❌ ĐĂNG NHẬP THẤT BẠI
                        handleLoginError(email, task.getException());
                    }
                });
    }

    private void showEmailNotVerifiedDialog(FirebaseUser user) {
        showLoading(false);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Email chưa được xác nhận")
                .setMessage("Tài khoản của bạn chưa được xác nhận. " +
                        "Vui lòng kiểm tra email và click vào link xác nhận chúng tôi đã gửi." +
                        "\n\nBạn có muốn chúng tôi gửi lại email xác nhận không?")
                .setPositiveButton("Gửi lại email", (dialog, which) -> {
                    sendVerificationEmail(user);
                })
                .setNegativeButton("Để sau", (dialog, which) -> {
                    // Đăng xuất user vì email chưa xác nhận
                    auth.signOut();
                })
                .setCancelable(false)
                .show();
    }

    private void sendVerificationEmail(FirebaseUser user) {
        showLoading(true);

        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    showLoading(false);

                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Đã gửi lại email xác nhận!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Gửi email thất bại: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }

                    // Đăng xuất user
                    auth.signOut();
                });
    }

    private void showProviderConflictDialog(String email, String provider) {
        String message = "Email " + email + " đã được đăng ký với " + provider + ".\n\n";

        if (provider.equals("Google")) {
            message += "Vui lòng sử dụng nút \"Đăng nhập với Google\" bên dưới.";
        } else {
            message += "Vui lòng sử dụng email khác để đăng ký tài khoản mới.";
        }

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Email đã tồn tại")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showEmailNotRegisteredDialog(String email) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Email chưa đăng ký")
                .setMessage("Email " + email + " chưa được đăng ký. Bạn có muốn đăng ký tài khoản mới không?")
                .setPositiveButton("Đăng ký ngay", (dialog, which) -> {
                    // Chuyển đến màn hình đăng ký
                    Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                    intent.putExtra("email", email);
                    startActivityForResult(intent, 100);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showLoginErrorDialog(String message) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Đăng nhập thất bại")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private boolean validateInputs(String email, String password) {
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

        return isValid;
    }

    private void showSignUpDialog() {
        // Chuyển đến màn hình đăng ký
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivityForResult(intent, 100);
    }

    // Nhận kết quả từ SignUpActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            String email = data.getStringExtra("email");
            if (email != null) {
                etEmail.setText(email);
                etPassword.requestFocus();
                Toast.makeText(this, "Đăng ký thành công! Vui lòng đăng nhập.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showLoading(boolean show) {
        Button btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setText(show ? "Đang xử lý..." : "Đăng nhập");
        btnLogin.setEnabled(!show);
    }

    private final ActivityResultLauncher<Intent> signInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_CANCELED) {
                    Toast.makeText(this, "Đã hủy đăng nhập Google", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent data = result.getData();
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                handleSignInResult(task);
            });

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                // HIỆN THÔNG BÁO ĐANG XỬ LÝ
                Toast.makeText(this, "Đang đăng nhập với Google...", Toast.LENGTH_SHORT).show();

                // ĐĂNG NHẬP VỚI FIREBASE
                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                auth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            String name = user.getDisplayName() != null ? user.getDisplayName() : account.getDisplayName();
                            // Lưu user vào Database
                            DatabaseHelper.getInstance().createOrUpdateUser(user.getUid(), user.getEmail(), name);
                            Toast.makeText(this, "Đăng nhập Google thành công!", Toast.LENGTH_SHORT).show();
                            goHome(name);
                        }
                    } else {
                        Log.e("GSI", "Firebase signInWithCredential failed", task.getException());
                        Toast.makeText(this, "Đăng nhập Google thất bại", Toast.LENGTH_LONG).show();
                    }
                });
            }
        } catch (ApiException e) {
            int code = e.getStatusCode();
            Log.e("GSI", "Google sign-in failed, code=" + code, e);

            if (code == 12501) { // SIGN_IN_CANCELLED
                Toast.makeText(this, "Đã hủy đăng nhập Google", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Đăng nhập Google thất bại", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void goHome(String name) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra("username", name != null ? name : "User");
        startActivity(intent);
        finish();
    }
}