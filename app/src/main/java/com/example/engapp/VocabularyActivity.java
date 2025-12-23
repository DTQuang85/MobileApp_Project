package com.example.engapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class VocabularyActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private VocabularyAdapter adapter;
    private List<Vocabulary> vocabularyList;
    private FirebaseFirestore db;
    private ProgressBar progressBar;
    private TextView tvCategoryTitle;
    private String selectedCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vocabulary);

        // Lấy category từ Intent
        selectedCategory = getIntent().getStringExtra("category");
        if (selectedCategory == null) {
            selectedCategory = "Software Development"; // Default
        }

        // Khởi tạo views
        recyclerView = findViewById(R.id.recyclerViewVocabulary);
        progressBar = findViewById(R.id.progressBar);
        tvCategoryTitle = findViewById(R.id.tvCategoryTitle);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        tvCategoryTitle.setText(selectedCategory);

        // Khởi tạo Firestore
        db = FirebaseFirestore.getInstance();

        // Setup RecyclerView
        vocabularyList = new ArrayList<>();
        adapter = new VocabularyAdapter(this, vocabularyList, vocabulary -> {
            // Khi click vào từ vựng, mở VocabularyDetailActivity
            Intent intent = new Intent(VocabularyActivity.this, VocabularyDetailActivity.class);
            intent.putExtra("vocabularyId", vocabulary.getId());
            intent.putExtra("term", vocabulary.getTerm());
            intent.putExtra("type", vocabulary.getType());
            intent.putExtra("pronunciation", vocabulary.getPronunciation());
            intent.putExtra("definition", vocabulary.getDefinition());
            intent.putExtra("example", vocabulary.getExample());
            intent.putExtra("category", vocabulary.getCategory());
            intent.putExtra("image", vocabulary.getImage());
            startActivity(intent);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Tải dữ liệu từ vựng theo category
        loadVocabulary();
    }

    private void loadVocabulary() {
        progressBar.setVisibility(View.VISIBLE);
        
        db.collection("vocabulary")
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        vocabularyList.clear();
                        // Filter theo category ở client-side
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Vocabulary vocab = document.toObject(Vocabulary.class);
                            if (vocab.getCategory() != null && 
                                vocab.getCategory().equals(selectedCategory)) {
                                vocabularyList.add(vocab);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        
                        if (vocabularyList.isEmpty()) {
                            Toast.makeText(VocabularyActivity.this, 
                                    "No vocabulary found for " + selectedCategory, 
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(VocabularyActivity.this, 
                                "Error loading data: " + task.getException().getMessage(), 
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
