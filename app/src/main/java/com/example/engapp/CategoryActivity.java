package com.example.engapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.List;

public class CategoryActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private CategoryAdapter adapter;
    private List<Category> categoryList;
    private TextView tvWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        tvWelcome = findViewById(R.id.tvWelcome);
        recyclerView = findViewById(R.id.recyclerViewCategories);

        // Lấy username từ Intent hoặc Firebase
        String username = getIntent().getStringExtra("username");
        if (username == null && FirebaseAuth.getInstance().getCurrentUser() != null) {
            username = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        }
        tvWelcome.setText("Welcome, " + (username != null ? username : "User") + "!");

        // Setup RecyclerView với GridLayout (2 cột)
        setupCategories();
        adapter = new CategoryAdapter(this, categoryList, category -> {
            // Khi click vào category, mở VocabularyActivity
            Intent intent = new Intent(CategoryActivity.this, VocabularyActivity.class);
            intent.putExtra("category", category.getName());
            startActivity(intent);
        });
        
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);
    }

    private void setupCategories() {
        categoryList = new ArrayList<>();
        categoryList.add(new Category("Software Development", "💻", 0xFF6C63FF));
        categoryList.add(new Category("Backend Engineering", "⚙️", 0xFF00BFA5));
        categoryList.add(new Category("Frontend Engineering", "🎨", 0xFFFF6F00));
        categoryList.add(new Category("Mobile Development", "📱", 0xFF00C853));
        categoryList.add(new Category("Database", "🗄️", 0xFF2196F3));
        categoryList.add(new Category("Cloud & DevOps", "☁️", 0xFF9C27B0));
        categoryList.add(new Category("Cybersecurity", "🔒", 0xFFD32F2F));
        categoryList.add(new Category("AI & Machine Learning", "🤖", 0xFFFF9800));
        categoryList.add(new Category("QA / Testing", "✅", 0xFF4CAF50));
        categoryList.add(new Category("System Design / Architecture", "🏗️", 0xFF607D8B));
    }
}
