package com.example.engapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class BehavioralListActivity extends AppCompatActivity {

    private RecyclerView recyclerViewQuestions;
    private BehavioralAdapter adapter;
    private List<BehavioralQuestion> allQuestions = new ArrayList<>();
    private List<BehavioralQuestion> filteredQuestions = new ArrayList<>();
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private FirebaseFirestore db;

    // Filter chips
    private TextView chipAll, chipEasy, chipMedium, chipHard;
    private String currentCategoryFilter = "All";
    private String currentDifficultyFilter = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_behavioral_list);

        db = FirebaseFirestore.getInstance();

        // Initialize views
        ImageView btnBack = findViewById(R.id.btnBack);
        recyclerViewQuestions = findViewById(R.id.recyclerViewQuestions);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);

        // Filter chips
        chipAll = findViewById(R.id.chipAll);
        chipEasy = findViewById(R.id.chipEasy);
        chipMedium = findViewById(R.id.chipMedium);
        chipHard = findViewById(R.id.chipHard);

        // Check if views are found
        if (chipAll == null || chipEasy == null || chipMedium == null || chipHard == null || recyclerViewQuestions == null) {
            android.widget.Toast.makeText(this, "Layout initialization error", android.widget.Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Setup RecyclerView
        recyclerViewQuestions.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BehavioralAdapter(this, filteredQuestions);
        recyclerViewQuestions.setAdapter(adapter);

        // Back button
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Setup filter chips
        setupFilterChips();

        // Load questions
        loadQuestions();
    }

    private void setupFilterChips() {
        chipAll.setOnClickListener(v -> {
            currentCategoryFilter = "All";
            currentDifficultyFilter = "All";
            updateChipStates();
            filterQuestions();
        });

        chipEasy.setOnClickListener(v -> {
            if (currentDifficultyFilter.equals("easy")) {
                currentDifficultyFilter = "All";
            } else {
                currentDifficultyFilter = "easy";
            }
            updateChipStates();
            filterQuestions();
        });

        chipMedium.setOnClickListener(v -> {
            if (currentDifficultyFilter.equals("medium")) {
                currentDifficultyFilter = "All";
            } else {
                currentDifficultyFilter = "medium";
            }
            updateChipStates();
            filterQuestions();
        });

        chipHard.setOnClickListener(v -> {
            if (currentDifficultyFilter.equals("hard")) {
                currentDifficultyFilter = "All";
            } else {
                currentDifficultyFilter = "hard";
            }
            updateChipStates();
            filterQuestions();
        });
    }

    private void updateChipStates() {
        // Reset all chips to default state
        chipAll.setBackgroundResource(R.drawable.bg_card);
        chipAll.setTextColor(getResources().getColor(R.color.text_secondary));
        
        chipEasy.setBackgroundResource(R.drawable.bg_card);
        chipEasy.setTextColor(getResources().getColor(R.color.text_secondary));
        
        chipMedium.setBackgroundResource(R.drawable.bg_card);
        chipMedium.setTextColor(getResources().getColor(R.color.text_secondary));
        
        chipHard.setBackgroundResource(R.drawable.bg_card);
        chipHard.setTextColor(getResources().getColor(R.color.text_secondary));

        // Highlight active chip
        if (currentDifficultyFilter.equals("All")) {
            chipAll.setBackgroundResource(R.drawable.bg_button_primary);
            chipAll.setTextColor(getResources().getColor(R.color.text_white));
        } else if (currentDifficultyFilter.equals("easy")) {
            chipEasy.setBackgroundResource(R.drawable.bg_button_primary);
            chipEasy.setTextColor(getResources().getColor(R.color.text_white));
        } else if (currentDifficultyFilter.equals("medium")) {
            chipMedium.setBackgroundResource(R.drawable.bg_button_primary);
            chipMedium.setTextColor(getResources().getColor(R.color.text_white));
        } else if (currentDifficultyFilter.equals("hard")) {
            chipHard.setBackgroundResource(R.drawable.bg_button_primary);
            chipHard.setTextColor(getResources().getColor(R.color.text_white));
        }
    }

    private void loadQuestions() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        db.collection("behavioral_questions")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allQuestions.clear();
                    android.util.Log.d("BehavioralList", "Total documents: " + queryDocumentSnapshots.size());
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            android.util.Log.d("BehavioralList", "Doc ID: " + document.getId());
                            
                            // Parse đúng structure Firebase của bạn
                            BehavioralQuestion question = new BehavioralQuestion();
                            
                            // id là Long trong Firebase, convert sang String
                            Long idNum = document.getLong("id");
                            question.setId(idNum != null ? String.valueOf(idNum) : document.getId());
                            
                            question.setQuestion(document.getString("question"));
                            question.setCategory(document.getString("category"));
                            question.setDifficulty(document.getString("difficulty"));
                            question.setSample_basic(document.getString("sample_basic"));
                            question.setSample_intermediate(document.getString("sample_intermediate"));
                            question.setSample_advanced(document.getString("sample_advanced"));
                            question.setExplanation(document.getString("explanation"));
                            question.setPractice_template(document.getString("practice_template"));
                            
                            // Parse keywords array
                            Object keywordsObj = document.get("keywords");
                            if (keywordsObj instanceof java.util.List) {
                                @SuppressWarnings("unchecked")
                                java.util.List<String> keywords = (java.util.List<String>) keywordsObj;
                                question.setKeywords(keywords);
                            }
                            
                            allQuestions.add(question);
                            
                            android.util.Log.d("BehavioralList", "✅ Loaded: " + question.getId() + " - " + question.getQuestion());
                        } catch (Exception e) {
                            android.util.Log.e("BehavioralList", "❌ Error doc " + document.getId() + ": " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    
                    android.util.Log.d("BehavioralList", "Total questions loaded: " + allQuestions.size());
                    filterQuestions();
                    progressBar.setVisibility(View.GONE);
                    
                    if (allQuestions.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        tvEmpty.setText("No behavioral questions found. Please add some to get started!");
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("Failed to load questions: " + e.getMessage());
                    android.util.Log.e("BehavioralList", "Firestore error: " + e.getMessage());
                    android.widget.Toast.makeText(this, "Error: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
                });
    }

    private void filterQuestions() {
        filteredQuestions.clear();

        for (BehavioralQuestion question : allQuestions) {
            if (question == null) continue;
            
            String category = question.getCategory() != null ? question.getCategory() : "";
            String difficulty = question.getDifficulty() != null ? question.getDifficulty() : "";
            
            boolean categoryMatch = currentCategoryFilter.equals("All") 
                    || category.equalsIgnoreCase(currentCategoryFilter);
            boolean difficultyMatch = currentDifficultyFilter.equals("All") 
                    || difficulty.equalsIgnoreCase(currentDifficultyFilter);

            if (categoryMatch && difficultyMatch) {
                filteredQuestions.add(question);
            }
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        
        if (filteredQuestions.isEmpty() && !allQuestions.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("No questions match your filters");
        } else if (filteredQuestions.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
        }
    }
}
