package com.example.engapp;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.engapp.database.GameDatabaseHelper;
import com.example.engapp.database.GameDatabaseHelper.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WordLabActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private RecyclerView recyclerWords;
    private TextView tvTotalWords;
    private TextView tabAll, tabColors, tabShapes, tabAdjectives;
    private ImageView btnBack;

    private TextToSpeech tts;
    private GameDatabaseHelper dbHelper;
    private List<WordData> allWords = new ArrayList<>();
    private List<WordData> filteredWords = new ArrayList<>();
    private WordAdapter adapter;
    private String currentFilter = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_lab);

        dbHelper = GameDatabaseHelper.getInstance(this);
        tts = new TextToSpeech(this, this);

        initViews();
        loadWords();
        setupTabs();
    }

    private void initViews() {
        recyclerWords = findViewById(R.id.recyclerWords);
        tvTotalWords = findViewById(R.id.tvTotalWords);
        btnBack = findViewById(R.id.btnBack);
        tabAll = findViewById(R.id.tabAll);
        tabColors = findViewById(R.id.tabColors);
        tabShapes = findViewById(R.id.tabShapes);
        tabAdjectives = findViewById(R.id.tabAdjectives);

        btnBack.setOnClickListener(v -> finish());

        recyclerWords.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new WordAdapter();
        recyclerWords.setAdapter(adapter);
    }

    private void loadWords() {
        // Load words from all planets
        List<PlanetData> planets = dbHelper.getAllPlanets();
        allWords.clear();

        for (PlanetData planet : planets) {
            List<WordData> planetWords = dbHelper.getWordsForPlanet(planet.id);
            allWords.addAll(planetWords);
        }

        filterWords("all");

        // Count learned words
        int learnedCount = 0;
        for (WordData word : allWords) {
            if (word.isLearned) learnedCount++;
        }
        tvTotalWords.setText(String.valueOf(learnedCount));
    }

    private void setupTabs() {
        tabAll.setOnClickListener(v -> filterWords("all"));
        tabColors.setOnClickListener(v -> filterWords("color"));
        tabShapes.setOnClickListener(v -> filterWords("shape"));
        tabAdjectives.setOnClickListener(v -> filterWords("adjective"));
    }

    private void filterWords(String category) {
        currentFilter = category;
        filteredWords.clear();

        if (category.equals("all")) {
            filteredWords.addAll(allWords);
        } else {
            for (WordData word : allWords) {
                if (word.category != null && word.category.equals(category)) {
                    filteredWords.add(word);
                }
            }
        }

        // Update tab backgrounds
        tabAll.setBackgroundResource(category.equals("all") ? R.drawable.bg_tab_selected : R.drawable.bg_tab_normal);
        tabColors.setBackgroundResource(category.equals("color") ? R.drawable.bg_tab_selected : R.drawable.bg_tab_normal);
        tabShapes.setBackgroundResource(category.equals("shape") ? R.drawable.bg_tab_selected : R.drawable.bg_tab_normal);
        tabAdjectives.setBackgroundResource(category.equals("adjective") ? R.drawable.bg_tab_selected : R.drawable.bg_tab_normal);

        adapter.notifyDataSetChanged();
    }

    private void speakWord(String word) {
        if (tts != null) {
            tts.speak(word, TextToSpeech.QUEUE_FLUSH, null, "word");
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.US);
            tts.setSpeechRate(0.8f);
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    // ============ WORD ADAPTER ============

    class WordAdapter extends RecyclerView.Adapter<WordAdapter.WordViewHolder> {

        @NonNull
        @Override
        public WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_word_card, parent, false);
            return new WordViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
            WordData word = filteredWords.get(position);

            holder.tvWordEmoji.setText(word.emoji);
            holder.tvEnglish.setText(capitalize(word.english));
            holder.tvPronunciation.setText(word.pronunciation);
            holder.tvVietnamese.setText(word.vietnamese);

            if (word.isLearned) {
                holder.tvLearnedBadge.setVisibility(View.VISIBLE);
            } else {
                holder.tvLearnedBadge.setVisibility(View.GONE);
            }

            holder.btnListen.setOnClickListener(v -> speakWord(word.english));
            holder.itemView.setOnClickListener(v -> speakWord(word.english));
        }

        @Override
        public int getItemCount() {
            return filteredWords.size();
        }

        class WordViewHolder extends RecyclerView.ViewHolder {
            TextView tvWordEmoji, tvEnglish, tvPronunciation, tvVietnamese;
            TextView btnListen, tvLearnedBadge;

            WordViewHolder(@NonNull View itemView) {
                super(itemView);
                tvWordEmoji = itemView.findViewById(R.id.tvWordEmoji);
                tvEnglish = itemView.findViewById(R.id.tvEnglish);
                tvPronunciation = itemView.findViewById(R.id.tvPronunciation);
                tvVietnamese = itemView.findViewById(R.id.tvVietnamese);
                btnListen = itemView.findViewById(R.id.btnListen);
                tvLearnedBadge = itemView.findViewById(R.id.tvLearnedBadge);
            }
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}

