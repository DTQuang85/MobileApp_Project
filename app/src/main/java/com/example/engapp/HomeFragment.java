package com.example.engapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private RecyclerView recyclerView;
    private CategoryAdapter adapter;
    private List<Category> categoryList;
    private TextView tvWelcome;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvWelcome = view.findViewById(R.id.tvWelcome);
        recyclerView = view.findViewById(R.id.recyclerViewCategories);

        // Lấy username
        String username = "User";
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String displayName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
            username = displayName != null ? displayName : "User";
        }
        tvWelcome.setText("Welcome, " + username + "!");

        // Game button
        view.findViewById(R.id.btnPlayGame).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), VocabularyGameActivity.class);
            startActivity(intent);
        });

        // Behavioral Interview button
        view.findViewById(R.id.btnBehavioral).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), BehavioralListActivity.class);
            startActivity(intent);
        });

        // Setup RecyclerView
        setupCategories();
        adapter = new CategoryAdapter(getContext(), categoryList, category -> {
            Intent intent = new Intent(getActivity(), CategoryDetailActivity.class);
            intent.putExtra("category", category.getName());
            startActivity(intent);
        });
        
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setAdapter(adapter);

        return view;
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
